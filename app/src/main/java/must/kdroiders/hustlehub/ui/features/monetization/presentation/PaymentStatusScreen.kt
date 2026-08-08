package must.kdroiders.hustlehub.ui.features.monetization.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import must.kdroiders.hustlehub.ui.features.monetization.domain.usecase.PollPaymentStatusUseCase
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleCard
import must.kdroiders.hustlehub.sharedComposables.HustleCardVariant
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.sharedComposables.LoadingIndicator

/**
 * NavKey: [must.kdroiders.hustlehub.navigation.PaymentStatus]
 *
 * Polls M-Pesa payment status for [checkoutRequestId] using [PollPaymentStatusUseCase].
 * Shows the polling attempt counter, and transitions to success/failure/timeout states.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PaymentStatusScreen(
    checkoutRequestId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onRetryPayment: () -> Unit,
    viewModel: MonetizationViewModel = hiltViewModel(),
) {
    val paymentState by viewModel.paymentState.collectAsState()

    // Start polling when the screen first appears
    LaunchedEffect(checkoutRequestId) {
        viewModel.pollStatus(checkoutRequestId)
    }

    HustleScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Payment Status",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    // Only allow back when in a terminal state
                    if (paymentState !is PaymentUiState.Polling && paymentState !is PaymentUiState.Submitting) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val state = paymentState) {
                is PaymentUiState.Submitting, is PaymentUiState.Idle -> {
                    LoadingIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Sending M-Pesa request…",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }

                is PaymentUiState.Polling -> {
                    LoadingIndicator()
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Waiting for M-Pesa PIN confirmation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Attempt ${state.attempt} of ${PollPaymentStatusUseCase.MAX_ATTEMPTS}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Enter your M-Pesa PIN on your phone to confirm payment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                is PaymentUiState.Success -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(72.dp),
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Payment Confirmed!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "You're now a HustleHub Pro member",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    if (state.receiptNumber.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        HustleCard(variant = HustleCardVariant.Tonal) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "M-Pesa Receipt",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = state.receiptNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                    HustleButton(
                        text = "Go to Profile",
                        onClick = {
                            viewModel.resetPaymentState()
                            onNavigateToProfile()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is PaymentUiState.Failed -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(72.dp),
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Payment Failed",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(32.dp))
                    HustleButton(
                        text = "Try Again",
                        onClick = {
                            viewModel.resetPaymentState()
                            onRetryPayment()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is PaymentUiState.Timeout -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(72.dp),
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Request Timed Out",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "We didn't receive confirmation from M-Pesa. If you were charged, please contact support.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(32.dp))
                    HustleButton(
                        text = "Try Again",
                        onClick = {
                            viewModel.resetPaymentState()
                            onRetryPayment()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    HustleButton(
                        text = "Contact Support",
                        onClick = onNavigateBack,
                        variant = HustleButtonVariant.Outlined,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
