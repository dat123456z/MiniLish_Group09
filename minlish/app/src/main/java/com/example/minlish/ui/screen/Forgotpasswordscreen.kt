package com.example.minlish.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.minlish.notification.InAppNotificationService
import com.example.minlish.ui.component.AuthTextField
import com.example.minlish.ui.component.PrimaryButton
import com.example.minlish.ui.theme.*
import com.example.minlish.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateToVerifyCode: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val noAccountFoundMsg = stringResource(R.string.no_account_found)
    val enterEmailErrorMsg = stringResource(R.string.enter_email_error)
    val invalidEmailErrorMsg = stringResource(R.string.invalid_email_error)

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
                    imageVector = Icons.Outlined.LockOpen,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.forgot_password_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.forgot_password_desc),
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (localError != null) {
                Text(
                    text = localError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Text(
                    text = stringResource(R.string.email_address),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AuthTextField(
                value = email,
                onValueChange = { 
                    email = it 
                    localError = null
                },
                placeholder = "alex@minlish.app",
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(20.dp))

            val resetSubject = stringResource(R.string.reset_code_subject)
            val resetBody = stringResource(R.string.reset_code_body)

            PrimaryButton(
                text = stringResource(R.string.send_reset_code),
                isLoading = isLoading,
                onClick = {
                    if (email.isNotBlank() && email.contains("@")) {
                        isLoading = true
                        authViewModel.checkUserExists(email) { exists ->
                            if (exists) {
                                val otp = (100000..999999).random().toString()
                                scope.launch {
                                    InAppNotificationService.sendAutomatedEmail(
                                        toEmail = email,
                                        subject = resetSubject,
                                        body = resetBody.format(otp)
                                    )
                                    isLoading = false
                                    onNavigateToVerifyCode(email)
                                }
                            } else {
                                isLoading = false
                                localError = noAccountFoundMsg
                            }
                        }
                    } else if (email.isBlank()) {
                        localError = enterEmailErrorMsg
                    } else {
                        localError = invalidEmailErrorMsg
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceWhite)
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                val infoText = buildAnnotatedString {
                    append(stringResource(R.string.code_expires_in))
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                        append(stringResource(R.string.ten_minutes))
                    }
                    append(stringResource(R.string.spam_folder_check))
                }
                Text(text = infoText, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            val backText = buildAnnotatedString {
                append(stringResource(R.string.remembered_it))
                withStyle(SpanStyle(color = LinkColor, fontWeight = FontWeight.SemiBold)) {
                    append(stringResource(R.string.back_to_signin))
                }
            }
            Text(
                text = backText,
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}