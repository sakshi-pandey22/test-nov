package com.novara.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novara.app.model.LearnerProfile
import com.novara.app.ui.theme.*
import kotlinx.coroutines.launch

val CEFR_LEVELS = listOf(
    "A1" to "A1 · Beginner",
    "A2" to "A2 · Elementary",
    "B1" to "B1 · Intermediate",
    "B2" to "B2 · Upper Intermediate"
)

// Supported live backend purposes (Ankit's contract: MVP supports Trip & Casual)
val LIVE_PURPOSES = listOf(
    "trip" to "✈️ Travel & Trip (Transactional, Airport/Café, Speed-weighted)",
    "casual" to "💬 Social & Casual (Register, Friendship, Engagement-weighted)"
)

val CURATED_INTERESTS = listOf("food", "travel", "culture", "music", "history", "art", "sports", "technology")
val CURATED_WEAK_AREAS = listOf("listening", "speaking", "pronunciation", "grammar", "vocabulary")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IntakeScreen(
    onProfileCreated: suspend (LearnerProfile) -> Boolean,
    onNavigateBack: () -> Unit
) {
    // Ankit's backend explicitly mandates using sakshi_test_1 instead of u1
    var learnerId by remember { mutableStateOf("sakshi_test_1") }
    var selectedLevel by remember { mutableStateOf("B1") }
    var selectedPurpose by remember { mutableStateOf("trip") }
    var region by remember { mutableStateOf("Madrid") }
    val selectedInterests = remember { mutableStateListOf("food", "travel") }
    val selectedWeakAreas = remember { mutableStateListOf("listening") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgIvory)
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .verticalScroll(scrollState)
    ) {
        // Institutional Header
        Text(
            text = "NOVARA / INTAKE & REGISTRATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AcademicNavy,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Learner Registration",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = "POST /profile · Configures pipeline and seeds the Learner Profile",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Learner Identifier (Mandated requirement)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BgCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. LEARNER IDENTIFIER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcademicNavy,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = learnerId,
                    onValueChange = { learnerId = it },
                    label = { Text("learner_id") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AcademicNavy,
                        unfocusedBorderColor = BgCardBorder,
                        focusedLabelColor = AcademicNavy
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: Set to 'sakshi_test_1'. Do not reuse 'u1' to avoid skewed backend test history.",
                    fontSize = 11.sp,
                    color = AcademicAmber,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Target Proficiency Level (CEFR)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BgCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. TARGET PROFICIENCY (CEFR LEVEL)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcademicNavy,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CEFR_LEVELS.forEach { (lvl, desc) ->
                        val isSelected = selectedLevel == lvl
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AcademicNavy else BgCardBorder,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .background(if (isSelected) BgSubtle else BgSurface)
                                .clickable { selectedLevel = lvl }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = desc,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AcademicNavy else TextPrimary
                            )
                            if (isSelected) {
                                Text("SELECTED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AcademicNavy)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Region of Immersion
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BgCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. TARGET REGION / VARIANT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcademicNavy,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = region,
                    onValueChange = { region = it },
                    label = { Text("region (e.g. Madrid, Barcelona)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AcademicNavy,
                        unfocusedBorderColor = BgCardBorder,
                        focusedLabelColor = AcademicNavy
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Learning Purpose (Hard Branch)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BgCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "4. LEARNING PURPOSE (HARD PIPELINE BRANCH)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcademicNavy,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LIVE_PURPOSES.forEach { (purp, desc) ->
                        val isSelected = selectedPurpose == purp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AcademicNavy else BgCardBorder,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .background(if (isSelected) BgSubtle else BgSurface)
                                .clickable { selectedPurpose = purp }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = desc,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AcademicNavy else TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Text("✔", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AcademicNavy)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Personal Interests
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BgCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "5. INTEREST TAGS (KNOWLEDGE GRAPH FILTER)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcademicNavy,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CURATED_INTERESTS.forEach { tag ->
                        val isSelected = selectedInterests.contains(tag)
                        Box(
                            modifier = Modifier
                                .border(1.dp, if (isSelected) AcademicNavy else BgCardBorder, RoundedCornerShape(4.dp))
                                .background(if (isSelected) AcademicNavy else BgSurface)
                                .clickable {
                                    if (isSelected) selectedInterests.remove(tag) else selectedInterests.add(tag)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) TextInverted else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Known Weak Areas
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BgCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "6. INITIAL WEAKNESS FOCUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcademicNavy,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CURATED_WEAK_AREAS.forEach { weak ->
                        val isSelected = selectedWeakAreas.contains(weak)
                        Box(
                            modifier = Modifier
                                .border(1.dp, if (isSelected) AcademicAmber else BgCardBorder, RoundedCornerShape(4.dp))
                                .background(if (isSelected) AcademicAmberBg else BgSurface)
                                .clickable {
                                    if (isSelected) selectedWeakAreas.remove(weak) else selectedWeakAreas.add(weak)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = weak,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) AcademicAmber else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Error Banner
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ErrorRedBg)
                    .border(1.dp, ErrorRed, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Text(text = errorMessage!!, color = ErrorRed, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Submit Button (Rectangular, Academic style)
        Button(
            onClick = {
                val cleanId = learnerId.trim()
                if (cleanId.isBlank() || cleanId.length > 100) {
                    errorMessage = "Learner ID must be between 1 and 100 characters."
                    return@Button
                }
                val cleanRegion = region.trim()
                if (cleanRegion.isBlank() || cleanRegion.length > 200) {
                    errorMessage = "Region must be between 1 and 200 characters."
                    return@Button
                }
                if (selectedInterests.isEmpty()) {
                    errorMessage = "Please select at least one interest tag."
                    return@Button
                }
                if (selectedWeakAreas.isEmpty()) {
                    errorMessage = "Please select at least one weakness focus."
                    return@Button
                }

                errorMessage = null
                isLoading = true

                val profile = LearnerProfile(
                    learnerId = cleanId,
                    language = "spanish",
                    level = selectedLevel,
                    region = cleanRegion,
                    purpose = selectedPurpose,
                    interests = selectedInterests.toList(),
                    weakAreas = selectedWeakAreas.toList(),
                    nativeLanguage = "English"
                )

                scope.launch {
                    val success = onProfileCreated(profile)
                    isLoading = false
                    if (!success) {
                        errorMessage = "Failed to register profile on live backend (novara-api-dnhc.onrender.com). Check internet connection or retry."
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AcademicNavy),
            enabled = !isLoading
        ) {
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Registering on Live Backend...", fontSize = 13.sp, color = Color.White)
                }
            } else {
                Text("Register Profile & Initialize (POST /profile)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
