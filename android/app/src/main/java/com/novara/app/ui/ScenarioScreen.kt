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
import com.novara.app.model.ScenarioResponse
import com.novara.app.network.NovaraRepository
import com.novara.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScenarioScreen(
    learnerId: String,
    initialPurpose: String,
    repository: NovaraRepository,
    onBeginConversation: (ScenarioResponse) -> Unit,
    onEditProfile: () -> Unit
) {
    var selectedPurpose by remember { mutableStateOf(if (initialPurpose == "casual") "casual" else "trip") }
    var scenario by remember { mutableStateOf<ScenarioResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Standalone /repair diagnostic state
    var showRepairTester by remember { mutableStateOf(false) }
    var testUtterance by remember { mutableStateOf("quiero para llevo") }
    var expectedPattern by remember { mutableStateOf("quiero para llevar") }
    var repairTestResult by remember { mutableStateOf<String?>(null) }
    var isTestingRepair by remember { mutableStateOf(false) }

    fun loadScenario(purpose: String) {
        isLoading = true
        errorMessage = null
        scope.launch {
            val result = repository.getScenario(learnerId, purpose)
            result.onSuccess {
                scenario = it
            }.onFailure {
                errorMessage = it.message ?: "Failed to fetch scenario from backend."
            }
            isLoading = false
        }
    }

    LaunchedEffect(selectedPurpose) {
        loadScenario(selectedPurpose)
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
                onClick = onEditProfile,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("← Edit Registration", color = AcademicNavy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Box(
                modifier = Modifier
                    .border(1.dp, AcademicSage, RoundedCornerShape(4.dp))
                    .background(BgSurface)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("🟢 LIVE BACKEND ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AcademicSage)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "NOVARA / ADAPTIVE SCENARIOS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AcademicNavy,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Situational Scenario",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = "GET /scenario?learner_id=$learnerId · Scored by Adaptive Engine",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Purpose Selector (Live Backend Scope: Trip & Casual)
        Text(
            text = "SELECT IMMERSION PURPOSE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AcademicNavy,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("trip" to "✈️ Trip Immersion", "casual" to "💬 Casual Immersion").forEach { (purp, label) ->
                val isSelected = selectedPurpose == purp
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) AcademicNavy else BgCardBorder,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(if (isSelected) BgSubtle else BgSurface)
                        .clickable { selectedPurpose = purp }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) AcademicNavy else TextSecondary
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
                        Text("Querying GET /scenario...", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        } else if (scenario != null) {
            val sc = scenario!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BgCardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SCENARIO ID: ${sc.scenarioId}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcademicNavy,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sc.title,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Setting: ${sc.setting}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Situation Tags
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        sc.situationTags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(BgTag, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("#$tag", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BgCardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "NATIVE SPEAKER OPENING DIALOGUE:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcademicNavy,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgSubtle)
                            .border(1.dp, BgCardBorder, RoundedCornerShape(4.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "🇪🇸 \"${sc.openingLine}\"",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { onBeginConversation(sc) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AcademicNavy)
                    ) {
                        Text("Begin Practice Conversation (POST /conversation) →", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Diagnostic Card for Endpoint #4: POST /repair
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BgCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ENDPOINT 4: POST /repair TESTER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcademicAmber,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Verify rule-based Repair Engine error classification",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    TextButton(onClick = { showRepairTester = !showRepairTester }) {
                        Text(if (showRepairTester) "Hide" else "Open", fontSize = 12.sp, color = AcademicNavy)
                    }
                }

                if (showRepairTester) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = testUtterance,
                        onValueChange = { testUtterance = it },
                        label = { Text("learner_utterance (e.g. 'quiero para llevo')") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expectedPattern,
                        onValueChange = { expectedPattern = it },
                        label = { Text("expected_pattern (e.g. 'quiero para llevar')") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isTestingRepair = true
                            repairTestResult = null
                            scope.launch {
                                val res = repository.testRepair(testUtterance, expectedPattern)
                                isTestingRepair = false
                                repairTestResult = res.fold(
                                    onSuccess = { "CLASSIFIED: Error='${it.errorType}' | Strategy='${it.strategy}' | Repair='${it.repairText}'" },
                                    onFailure = { "Error: ${it.message}" }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AcademicAmber),
                        enabled = !isTestingRepair
                    ) {
                        Text(if (isTestingRepair) "Evaluating..." else "Send POST /repair Test", fontSize = 12.sp)
                    }
                    if (repairTestResult != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgSubtle)
                                .border(1.dp, BgCardBorder, RoundedCornerShape(4.dp))
                                .padding(10.dp)
                        ) {
                            Text(repairTestResult!!, fontSize = 12.sp, color = AcademicNavy, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
