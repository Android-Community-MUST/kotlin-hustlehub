package must.kdroiders.hustlehub.ui.auth.presentation.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import must.kdroiders.hustlehub.ui.auth.presentation.view.components.OtpInputField
import must.kdroiders.hustlehub.ui.auth.presentation.viewmodel.EmailVerificationViewModel
import must.kdroiders.hustlehub.sharedComposables.HustleButton

@Composable
fun EmailVerificationScreen(
    email: String,
    onVerified: () -> Unit,
    viewModel: EmailVerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val otpValues = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }

    // Pass the email into the ViewModel as soon as screen loads
    LaunchedEffect(email) {
        viewModel.setEmail(email)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Verify your email",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter the 6-digit code sent to\n$email",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 6-digit OTP boxes
        OtpInputField(
            otpValues = otpValues,
            focusRequesters = focusRequesters,
            onOtpValueChange = { index, value ->
                otpValues[index] = value
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error message
        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Verify button
        HustleButton(
            text = if (uiState.isLoading) "Verifying..." else "Verify",
            onClick = {
                val otp = otpValues.joinToString("")
                viewModel.verifyOtp(otp, onVerified)
            },
            enabled = otpValues.all { it.isNotEmpty() } && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Resend with 60s countdown
        if (uiState.resendCooldown > 0) {
            Text(
                text = "Resend OTP in ${uiState.resendCooldown}s",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            TextButton(onClick = { viewModel.resendOtp() }) {
                Text("Resend OTP")
            }
        }
    }
}
