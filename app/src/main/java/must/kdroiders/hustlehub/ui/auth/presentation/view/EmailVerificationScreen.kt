package must.kdroiders.hustlehub.ui.auth.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleCard
import must.kdroiders.hustlehub.sharedComposables.HustleCardVariant
import must.kdroiders.hustlehub.ui.auth.presentation.viewmodel.EmailVerificationViewModel

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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Verify your email",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "We sent a verification link to:\n$email\n\nPlease check your email and click the link to verify, then tap \"Verify\" below.\n\n(For testing, you can enter the bypass code 123456)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

       HustleCard(
            variant = HustleCardVariant.Elevated
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Verify button
                HustleButton(
                    text = if (uiState.isLoading) "Verifying..." else "Verify",
                    onClick = {
                        val otp = otpValues.joinToString("")
                        viewModel.verifyOtp(otp, onVerified)
                    },
                    loading = uiState.isLoading,
                    enabled = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Resend with 60s countdown
                if (uiState.resendCooldown > 0) {
                    Text(
                        text = "Resend OTP in ${uiState.resendCooldown}s",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    HustleButton(
                        text = "Resend OTP",
                        onClick = { viewModel.resendOtp() },
                        variant = HustleButtonVariant.Outlined,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
