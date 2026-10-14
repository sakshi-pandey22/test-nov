package com.novara.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.novara.app.data.ProfileStore
import com.novara.app.model.ScenarioResponse
import com.novara.app.network.NovaraRepository
import com.novara.app.ui.*
import com.novara.app.ui.theme.BgIvory
import com.novara.app.ui.theme.NovaraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profileStore = ProfileStore(applicationContext)
        val repository = NovaraRepository(profileStore)

        setContent {
            NovaraTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgIvory)
                ) {
                    val navController = rememberNavController()

                    var activeScenario by remember { mutableStateOf<ScenarioResponse?>(null) }
                    var currentProfile by remember { mutableStateOf(profileStore.loadProfile()) }

                    NavHost(navController = navController, startDestination = "splash") {
                        // 0. Splash Loading Screen (1.2s loader)
                        composable("splash") {
                            SplashScreen(
                                onSplashFinished = {
                                    val destination = if (profileStore.hasValidProfile()) "scenario" else "intake"
                                    navController.navigate(destination) {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 1. Intake / Registration Screen (Task 2)
                        composable("intake") {
                            IntakeScreen(
                                onProfileCreated = { newProfile ->
                                    profileStore.saveProfile(newProfile)
                                    currentProfile = newProfile

                                    val result = repository.createProfile(newProfile)
                                    if (result.isSuccess) {
                                        navController.navigate("scenario") {
                                            popUpTo("intake") { inclusive = true }
                                        }
                                    }
                                    result.isSuccess
                                },
                                onNavigateBack = {
                                    if (profileStore.hasValidProfile()) {
                                        navController.navigate("scenario")
                                    }
                                }
                            )
                        }

                        // 2. Scenario Screen (Task 4)
                        composable("scenario") {
                            val profile = currentProfile ?: profileStore.loadProfile()
                            val learnerId = profile?.learnerId ?: "sakshi_test_1"
                            val purpose = profile?.purpose ?: "trip"

                            ScenarioScreen(
                                learnerId = learnerId,
                                initialPurpose = purpose,
                                repository = repository,
                                onBeginConversation = { sc ->
                                    activeScenario = sc
                                    navController.navigate("conversation")
                                },
                                onEditProfile = {
                                    navController.navigate("intake")
                                }
                            )
                        }

                        // 3. Conversation Screen (Task 5)
                        composable("conversation") {
                            val profile = currentProfile ?: profileStore.loadProfile()
                            val learnerId = profile?.learnerId ?: "sakshi_test_1"
                            val sc = activeScenario ?: com.novara.app.data.MockDataProvider.getScenarioForPurpose(profile?.purpose ?: "trip")

                            ConversationScreen(
                                learnerId = learnerId,
                                scenario = sc,
                                repository = repository,
                                onNavigateToReadiness = {
                                    navController.navigate("readiness")
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 4. Readiness Screen (Task 6)
                        composable("readiness") {
                            val profile = currentProfile ?: profileStore.loadProfile()
                            val learnerId = profile?.learnerId ?: "sakshi_test_1"

                            ReadinessScreen(
                                learnerId = learnerId,
                                repository = repository,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onStartNewScenario = {
                                    navController.navigate("scenario") {
                                        popUpTo("scenario") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
