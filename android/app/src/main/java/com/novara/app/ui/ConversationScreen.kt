package com.novara.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novara.app.model.ChatMessage
import com.novara.app.model.MessageRole
import com.novara.app.model.ScenarioResponse
import com.novara.app.network.NovaraRepository
import com.novara.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ConversationScreen(
    learnerId: String,
    scenario: ScenarioResponse,
    repository: NovaraRepository,
    onNavigateToReadiness: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "open",
                role = MessageRole.ASSISTANT,
                text = scenario.openingLine
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var turnNumber by remember { mutableIntStateOf(0) }
    var isSending by remember { mutableStateOf(false) }
    var failedSend by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var sendStartTime by remember { mutableLongStateOf(0L) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun performSend(text: String, turn: Int) {
        isSending = true
        failedSend = null
        val start = System.currentTimeMillis()

        scope.launch {
            val responseResult = repository.sendMessage(
                learnerId = learnerId,
                scenarioId = scenario.scenarioId,
                message = text,
                turnNumber = turn,
                responseTimeMs = if (sendStartTime > 0) System.currentTimeMillis() - sendStartTime else null
            )

            isSending = false

            responseResult.onSuccess { resp ->
                messages.add(
                    ChatMessage(
                        id = "a_$turn",
                        role = MessageRole.ASSISTANT,
                        text = resp.reply
                    )
                )

                // Inline Repair Trigger (Task 5 requirement)
                if (resp.repairTriggered && resp.repair != null) {
                    messages.add(
                        ChatMessage(
                            id = "r_$turn",
                            role = MessageRole.REPAIR,
                            text = resp.repair.repairText,
                            errorType = resp.repair.errorType,
                            strategy = resp.repair.strategy
                        )
                    )
                }
            }.onFailure {
                failedSend = text to turn
            }

            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isBlank() || isSending) return

        inputText = ""
        val newTurn = turnNumber + 1
        turnNumber = newTurn

        messages.add(
            ChatMessage(
                id = "u_$newTurn",
                role = MessageRole.USER,
                text = text
            )
        )

        performSend(text, newTurn)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgIvory)
    ) {
        // Institutional Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BgSurface,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onNavigateBack, contentPadding = PaddingValues(0.dp)) {
                        Text("← Scenarios", color = AcademicNavy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = scenario.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Turn $turnNumber · POST /conversation",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Button(
                    onClick = onNavigateToReadiness,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AcademicSage),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Readiness 📊", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Messages Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                when (msg.role) {
                    MessageRole.USER -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("YOU (LEARNER)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(AcademicNavy, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(msg.text, color = TextInverted, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                    }

                    MessageRole.ASSISTANT -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text("AI CONVERSATION PARTNER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AcademicNavy)
                            Spacer(modifier = Modifier.height(2.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(0.92f),
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(containerColor = BgSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BgCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = msg.text,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    MessageRole.REPAIR -> {
                        // Task 5 Core Deliverable: Academic Inline Repair Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = AcademicAmberBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AcademicAmber)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ REPAIR ENGINE: ${(msg.errorType ?: "DIAGNOSTIC").uppercase()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AcademicAmber,
                                        letterSpacing = 0.5.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, AcademicAmber, RoundedCornerShape(2.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "STRATEGY: ${(msg.strategy ?: "HINT").uppercase()}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AcademicAmber
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = AcademicAmber.copy(alpha = 0.3f), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }

            if (isSending) {
                item {
                    Text("AI Partner is responding…", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                }
            }
        }

        // Retry notification if network failed
        if (failedSend != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ErrorRedBg)
                    .border(1.dp, ErrorRed, RoundedCornerShape(0.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Connection error sending message", fontSize = 11.sp, color = ErrorRed)
                TextButton(
                    onClick = {
                        val (text, turn) = failedSend!!
                        performSend(text, turn)
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Retry", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // Bottom Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BgSurface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        if (sendStartTime == 0L && it.isNotBlank()) {
                            sendStartTime = System.currentTimeMillis()
                        }
                        inputText = it
                    },
                    placeholder = { Text("Escribe en español...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AcademicNavy,
                        unfocusedBorderColor = BgCardBorder
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { sendMessage() },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AcademicNavy),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    enabled = inputText.isNotBlank() && !isSending
                ) {
                    Text("Send", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
