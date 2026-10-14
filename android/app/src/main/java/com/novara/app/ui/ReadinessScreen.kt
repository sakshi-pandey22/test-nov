package com.novara.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novara.app.data.MockDataProvider
import com.novara.app.model.ReadinessResponse
import com.novara.app.network.NovaraRepository
import com.novara.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ReadinessScreen(
    learnerId: String,
    repository: NovaraRepository,
    onNavigateBack: () -> Unit,
    onStartNewScenario: () -> Unit
) {
    var selectedSample by remember { mutableStateOf("live") }
    var readinessData by remember { mutableStateOf<ReadinessResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    fun loadData(sampleKey: String) {
        isLoading = true
        errorMessage = null
        scope.launch {
            if (sampleKey == "live") {
                val result = repository.getReadiness(learnerId)
                result.onSuccess {
                    readinessData = it
                }.onFailure {
                    errorMessage = it.message ?: "Failed to fetch readiness from backend."
                }
            } else {
                readinessData = MockDataProvider.readinessPayloads[sampleKey]
            }
            isLoading = false
        }
    }

    LaunchedEffect(selectedSample) {
        loadData(selectedSample)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgIvory)
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .verticalScroll(scrollState)
    ) {
        // Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onNavigateBack,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("← Back to Dialogue", color = AcademicNavy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Box(
                modifier = Modifier
                    .border(1.dp, AcademicNavy, RoundedCornerShape(4.dp))
                    .background(BgSurface)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("LEARNER: $learnerId", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AcademicNavy)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "NOVARA / COMPETENCY METRICS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AcademicNavy,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Country Readiness Assessment",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = "GET /readiness?learner_id=$learnerId · Weighted competency scoring",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Evaluator Switcher Bar (Task 6 Presets)
        Text(
            text = "DATA SOURCE EVALUATOR",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AcademicNavy,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                "live" to "🟢 Live ($learnerId)",
                "high" to "High (88%)",
                "mid" to "Mid (62%)",
                "low" to "Low (34%)"
            ).forEach { (key, label) ->
                val isSelected = selectedSample == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) AcademicNavy else BgCardBorder,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(if (isSelected) BgSubtle else BgSurface)
                        .clickable { selectedSample = key }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) AcademicNavy else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BgCardBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AcademicNavy, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Computing readiness scores...", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        } else if (readinessData != null) {
            val data = readinessData!!
            val scorePct = (data.aggregateScore * 100).toInt()

            // Aggregate Score Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BgCardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "AGGREGATE READINESS INDEX",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcademicNavy,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$scorePct%",
                                fontSize = 42.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = AcademicNavy
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${data.purpose.uppercase()} READINESS)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcademicSage,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Text(
                            text = "Score: ${String.format("%.2f", data.aggregateScore)} / 1.00",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { data.aggregateScore.toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = AcademicNavy,
                        trackColor = BgSubtle
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Formula: Readiness = Σ (weight_i × competency_score_i). Evaluates real communication beyond memorized vocabulary.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Competency Dimensions Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BgCardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "FOUR-DIMENSIONAL BREAKDOWN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcademicNavy,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val dimensions = listOf(
                        Triple("language_accuracy", "1. Language Accuracy", "Phonological & grammatical precision"),
                        Triple("repair_success_rate", "2. Communication & Repair Success", "Resilience following conversational breakdown"),
                        Triple("register_appropriateness", "3. Register Appropriateness", "Formal/informal (tú vs. usted) distinction"),
                        Triple("transfer_success", "4. Situational Transfer", "Breadth of situations navigated successfully")
                    )

                    dimensions.forEachIndexed { index, (key, title, subtitle) ->
                        val dimScore = data.breakdown[key] ?: 0.5
                        val pct = (dimScore * 100).toInt()

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(subtitle, fontSize = 10.sp, color = TextMuted)
                                }
                                Text("$pct%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AcademicNavy)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { dimScore.toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = AcademicSage,
                                trackColor = BgSubtle
                            )
                        }

                        if (index < dimensions.size - 1) {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = BgCardBorder, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onStartNewScenario,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AcademicNavy)
            ) {
                Text("Practice Another Scenario (Loop-back) →", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
