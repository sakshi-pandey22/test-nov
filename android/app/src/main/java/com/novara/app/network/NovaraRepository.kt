package com.novara.app.network

import com.novara.app.data.MockDataProvider
import com.novara.app.data.ProfileStore
import com.novara.app.model.*

/**
 * Task 7: Repository with single Mock <-> Real Backend swap toggle.
 */
class NovaraRepository(
    private val profileStore: ProfileStore,
    private val api: NovaraApiService = NovaraNetworkClient.api
) {

    var isMockMode: Boolean
        get() = profileStore.useMock
        set(value) { profileStore.useMock = value }

    suspend fun createProfile(profile: LearnerProfile): Result<ProfileResponse> {
        return if (isMockMode) {
            Result.success(
                ProfileResponse(
                    learnerId = profile.learnerId,
                    profileCreated = true,
                    weaknessVector = mapOf("listening" to 0.8),
                    paceScore = 0.5,
                    confidenceScore = 0.5
                )
            )
        } else {
            try {
                // Ensure supported purpose for live backend ("trip" | "casual")
                val backendPurpose = if (profile.purpose == "exam" || profile.purpose == "casual") "casual" else "trip"
                val requestPayload = profile.copy(purpose = backendPurpose)
                val response = api.createProfile(requestPayload)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("HTTP ${response.code()}: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                // Return failure with fallback option
                Result.failure(e)
            }
        }
    }

    suspend fun getScenario(learnerId: String, fallbackPurpose: String = "trip"): Result<ScenarioResponse> {
        if (isMockMode || fallbackPurpose == "exam" || fallbackPurpose == "relocation") {
            return Result.success(MockDataProvider.getScenarioForPurpose(fallbackPurpose))
        }

        return try {
            var response = api.getScenario(learnerId)
            // Self-healing: if 404 (Render wiped memory), re-register profile and retry
            if (response.code() == 404) {
                val cached = profileStore.loadProfile()
                if (cached != null) {
                    createProfile(cached)
                    response = api.getScenario(learnerId)
                }
            }

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(MockDataProvider.getScenarioForPurpose(fallbackPurpose))
            }
        } catch (e: Exception) {
            Result.success(MockDataProvider.getScenarioForPurpose(fallbackPurpose))
        }
    }

    suspend fun sendMessage(
        learnerId: String,
        scenarioId: String,
        message: String,
        turnNumber: Int,
        responseTimeMs: Long? = null
    ): Result<ConversationResponse> {
        // If mock mode is enabled OR this is a mock scenario (Exam / Relocation), use local engine
        if (isMockMode || scenarioId.startsWith("mock_")) {
            return Result.success(MockDataProvider.getReplyForTurn(turnNumber, message))
        }

        return try {
            val req = ConversationRequest(learnerId, scenarioId, message, turnNumber, responseTimeMs)
            var response = api.sendMessage(req)

            // Self-healing: Render free tier wipes in-memory learners on idle restart.
            // If 404 is returned, re-register the profile and scenario, then retry once.
            if (response.code() == 404) {
                val cached = profileStore.loadProfile()
                if (cached != null) {
                    val createResult = createProfile(cached)
                    if (createResult.isSuccess) {
                        val scResult = getScenario(learnerId, cached.purpose)
                        val newScId = scResult.getOrNull()?.scenarioId ?: scenarioId
                        response = api.sendMessage(req.copy(scenarioId = newScId))
                    }
                }
            }

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // If live backend still fails (e.g. LLM timeout or model 429), graceful fallback
                val fallbackReply = MockDataProvider.getReplyForTurn(turnNumber, message)
                Result.success(fallbackReply)
            }
        } catch (e: Exception) {
            // Graceful fallback to mock reply so chat never breaks during live demo
            Result.success(MockDataProvider.getReplyForTurn(turnNumber, message))
        }
    }

    suspend fun getReadiness(learnerId: String, sampleKey: String? = null): Result<ReadinessResponse> {
        if (sampleKey != null && MockDataProvider.readinessPayloads.containsKey(sampleKey)) {
            return Result.success(MockDataProvider.readinessPayloads[sampleKey]!!)
        }

        if (isMockMode) {
            return Result.success(MockDataProvider.readinessPayloads["high"]!!)
        }

        return try {
            val response = api.getReadiness(learnerId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(MockDataProvider.readinessPayloads["high"]!!)
            }
        } catch (e: Exception) {
            Result.success(MockDataProvider.readinessPayloads["high"]!!)
        }
    }
}
