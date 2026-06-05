package com.example.minlish.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minlish.R
import com.example.minlish.ui.component.AuthTextField
import com.example.minlish.ui.component.PrimaryButton
import com.example.minlish.ui.theme.*
import com.example.minlish.ui.viewmodel.AuthViewModel

data class PasswordRule(val label: String, val check: (String) -> Boolean)

@Composable
fun NewPasswordScreen(
    email: String,
    authViewModel: AuthViewModel,
    onNavigateToResetSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isLoading by authViewModel.isLoading.observeAsState(false)
    val error by authViewModel.error.observeAsState()

    var attemptedSubmit by remember { mutableStateOf(false) }

    LaunchedEffect(attemptedSubmit, error, isLoading) {
        if (attemptedSubmit && error == null && !isLoading) {
            onNavigateToResetSuccess()
        }
    }

    val rules = listOf(
        PasswordRule(stringResource(R.string.rule_8_chars)) { it.length >= 8 },
        PasswordRule(stringResource(R.string.rule_one_number)) { it.any { c -> c.isDigit() } },
        PasswordRule(stringResource(R.string.rule_one_symbol)) { it.any { c -> !c.isLetterOrDigit() } },
        PasswordRule(stringResource(R.string.rule_mixed_case)) { it.any { c -> c.isUpperCase() } && it.any { c -> c.isLowerCase() } }
    )

    val strength = when {
        rules.count { it.check(newPassword) } <= 1 -> stringResource(R.string.strength_weak) to Color(0xFFEF4444)
        rules.count { it.check(newPassword) } <= 2 -> stringResource(R.string.strength_fair) to Color(0xFFF59E0B)
        rules.count { it.check(newPassword) } <= 3 -> stringResource(R.string.strength_good) to Color(0xFF3B82F6)
        else -> stringResource(R.string.strength_strong) to Color(0xFF22C55E)
    }

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
            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBackIos,
                    contentDescription = "Back",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onBack() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEEEEFD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.set_new_password),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.pick_strong_password),
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

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Text(stringResource(R.string.new_password), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            AuthTextField(
                value = newPassword,
                onValueChange = { newPassword = it; authViewModel.clearError() },
                placeholder = "••••••••••",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = newPasswordVisible,
                onPasswordToggle = { newPasswordVisible = !newPasswordVisible }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Text(stringResource(R.string.confirm_password), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; authViewModel.clearError() },
                placeholder = "••••••••••",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible }
            )

            if (newPassword.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(4) { idx ->
                        val filled = idx < rules.count { it.check(newPassword) }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (filled) strength.second else BorderColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.strength_label),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = strength.first,
                        fontSize = 12.sp,
                        color = strength.second,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rules.take(2).forEach { rule ->
                            PasswordRuleItem(label = rule.label, passed = rule.check(newPassword))
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rules.drop(2).forEach { rule ->
                            PasswordRuleItem(label = rule.label, passed = rule.check(newPassword))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            PrimaryButton(
                text = stringResource(R.string.reset_password_btn),
                isLoading = isLoading,
                onClick = {
                    if (newPassword != confirmPassword) {
                        authViewModel.resetPassword(email, "mismatch")
                        return@PrimaryButton
                    }
                    attemptedSubmit = true
                    authViewModel.resetPassword(email, newPassword)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PasswordRuleItem(label: String, passed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = if (passed) Color(0xFF22C55E) else BorderColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (passed) TextPrimary else TextSecondary
        )
    }
}