package com.example.minlish.ui.screen

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minlish.R
import com.example.minlish.ui.component.AppLogo
import com.example.minlish.ui.component.AuthTextField
import com.example.minlish.ui.component.PrimaryButton
import com.example.minlish.ui.theme.*
import com.example.minlish.ui.viewmodel.AuthViewModel
import com.example.minlish.util.LocaleHelper
import com.example.minlish.util.SessionManager

data class ChipItem(val label: String, val value: String)

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val currentLang = SessionManager.getLanguage(context)

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val currentUser by authViewModel.currentUser.observeAsState()
    val error by authViewModel.error.observeAsState()
    val isLoading by authViewModel.isLoading.observeAsState(false)

    LaunchedEffect(currentUser) {
        if (currentUser != null) onRegisterSuccess()
    }

    val learningGoals = listOf(
        ChipItem(stringResource(R.string.goal_ielts), "ielts"),
        ChipItem(stringResource(R.string.goal_toeic), "toeic"),
        ChipItem(stringResource(R.string.goal_business), "business"),
        ChipItem(stringResource(R.string.goal_travel), "travel")
    )
    var selectedGoal by remember { mutableStateOf("ielts") }

    val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
    var selectedLevel by remember { mutableStateOf("B2") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            AppLogo()

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.create_your_account),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.start_learning_smart),
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            AuthTextField(
                value = fullName,
                onValueChange = { fullName = it; authViewModel.clearError() },
                placeholder = stringResource(R.string.full_name_placeholder),
                leadingIcon = Icons.Outlined.Person,
                keyboardType = KeyboardType.Text
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthTextField(
                value = email,
                onValueChange = { email = it; authViewModel.clearError() },
                placeholder = "alex@minlish.app",
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthTextField(
                value = password,
                onValueChange = { password = it; authViewModel.clearError() },
                placeholder = "••••••••",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.learning_goal),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    learningGoals.forEach { goal ->
                        SelectableChip(
                            label = goal.label,
                            isSelected = selectedGoal == goal.value,
                            onClick = { selectedGoal = goal.value }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.current_level),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    levels.forEach { level ->
                        LevelChip(
                            label = level,
                            isSelected = selectedLevel == level,
                            onClick = { selectedLevel = level }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = stringResource(R.string.create_account_btn),
                isLoading = isLoading,
                onClick = {
                    authViewModel.register(
                        name = fullName,
                        email = email,
                        password = password,
                        goal = selectedGoal,
                        level = selectedLevel
                    )
                }
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            val signInText = buildAnnotatedString {
                append(stringResource(R.string.already_have_account))
                withStyle(SpanStyle(color = LinkColor, fontWeight = FontWeight.SemiBold)) {
                    append(stringResource(R.string.sign_in))
                }
            }
            Text(
                text = signInText,
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Language Switcher (Defined LAST to be on top layer)
        Surface(
            onClick = {
                val newLang = if (currentLang == "en") "vi" else "en"
                SessionManager.saveLanguage(context, newLang)
                LocaleHelper.updateResources(context, newLang)
                LocaleHelper.findActivity(context)?.recreate()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            color = Color.White.copy(alpha = 0.9f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE8EAF6)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (currentLang == "vi") "Tiếng Việt" else "English",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
            }
        }
    }
}

@Composable
fun SelectableChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) ChipSelected else SurfaceWhite,
        shadowElevation = if (isSelected) 0.dp else 1.dp,
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else TextPrimary
            )
        }
    }
}

@Composable
fun LevelChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) ChipSelected else SurfaceWhite,
        shadowElevation = if (isSelected) 0.dp else 1.dp,
        modifier = Modifier.size(width = 44.dp, height = 36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else TextPrimary
            )
        }
    }
}
