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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minlish.ui.component.AuthTextField
import com.example.minlish.ui.component.PrimaryButton
import com.example.minlish.ui.theme.*

data class PasswordRule(val label: String, val check: (String) -> Boolean)

@Composable
fun NewPasswordScreen(
    onNavigateToResetSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val rules = listOf(
        PasswordRule("8+ characters") { it.length >= 8 },
        PasswordRule("One number") { it.any { c -> c.isDigit() } },
        PasswordRule("One symbol") { it.any { c -> !c.isLetterOrDigit() } },
        PasswordRule("Mixed case") { it.any { c -> c.isUpperCase() } && it.any { c -> c.isLowerCase() } }
    )

    val strength = when {
        rules.count { it.check(newPassword) } <= 1 -> "Weak" to Color(0xFFEF4444)
        rules.count { it.check(newPassword) } <= 2 -> "Fair" to Color(0xFFF59E0B)
        rules.count { it.check(newPassword) } <= 3 -> "Good" to Color(0xFF3B82F6)
        else -> "Strong" to Color(0xFF22C55E)
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

            // Icon
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
                text = "Set new password",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pick a strong one you haven't used before.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // New password label
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Text("New password", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            AuthTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = "••••••••••",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = newPasswordVisible,
                onPasswordToggle = { newPasswordVisible = !newPasswordVisible }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm password label
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Text("Confirm password", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "••••••••••",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible }
            )

            // Strength bar (only show when typing)
            if (newPassword.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                // Strength bar
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
                        text = "Strength: ",
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

                // Rules grid
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
                text = "Reset password",
                isLoading = isLoading,
                onClick = {
                    isLoading = true
                    onNavigateToResetSuccess()
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
