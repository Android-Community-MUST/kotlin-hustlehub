package must.kdroiders.hustlehub.ui.features.monetization.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.sharedComposables.ErrorView
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleCard
import must.kdroiders.hustlehub.sharedComposables.HustleCardVariant
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.sharedComposables.LoadingIndicator

/**
 * NavKey: [must.kdroiders.hustlehub.navigation.Subscription]
 * Subscription upgrade screen — lets users pay for HustleHub Pro or Featured Listing via M-Pesa.
 *
 * @param serviceId Non-null when opened from a service card to boost a specific listing.
 *                  Shows the "Boost This Service" button pre-selected when set.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SubscriptionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPaymentStatus: (checkoutRequestId: String) -> Unit,
    serviceId: String? = null,
    viewModel: MonetizationViewModel = hiltViewModel(),
) {
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()
    val pendingCheckoutId by viewModel.pendingCheckoutId.collectAsState()

    var phoneNumber by rememberSaveable { mutableStateOf("") }

    // Navigate to PaymentStatus once the STK push is accepted by the backend
    LaunchedEffect(pendingCheckoutId) {
        val checkoutId = pendingCheckoutId
        if (!checkoutId.isNullOrBlank()) {
            viewModel.consumePendingCheckoutId()
            onNavigateToPaymentStatus(checkoutId)
        }
    }

    HustleScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HustleHub Pro",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Active subscription status card
            when (val state = subscriptionState) {
                is SubscriptionUiState.Loading -> LoadingIndicator()
                is SubscriptionUiState.Error -> ErrorView(message = state.message, onRetry = viewModel::loadSubscription)
                is SubscriptionUiState.Success -> {
                    val subscription = state.data
                    if (subscription?.isActive == true) {
                        ActiveSubscriptionCard(
                            planType = subscription.planType,
                            expiresAt = subscription.endDate,
                        )
                        HorizontalDivider()
                    }
                }
            }

            // Benefits section
            HustleCard(variant = HustleCardVariant.Tonal) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "What you get with Pro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    ProBenefitRow("Priority listing in discovery feed")
                    ProBenefitRow("Up to 15 portfolio photos (Free: 3)")
                    ProBenefitRow("Video pitch upload")
                    ProBenefitRow("Verified PRO badge on profile")
                    ProBenefitRow("Featured campus map pin")
                }
            }

            // Free vs Pro comparison
            HustleCard(variant = HustleCardVariant.Outlined) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Feature", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Free", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Pro", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
                    }
                    ComparisonRow(label = "Photos", free = "3", pro = "15")
                    ComparisonRow(label = "Video pitch", free = "No", pro = "Yes")
                    ComparisonRow(label = "Featured listing", free = "No", pro = "Yes")
                    ComparisonRow(label = "PRO badge", free = "No", pro = "Yes")
                    ComparisonRow(label = "Price", free = "Free", pro = "KES 150/mo")
                }
            }

            // M-Pesa payment section
            Text(
                text = "Pay with M-Pesa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            HustleTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = "Phone Number",
                placeholder = "e.g. 0712345678",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )

            // Payment error
            if (paymentState is PaymentUiState.Failed) {
                Text(
                    text = (paymentState as PaymentUiState.Failed).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            val isPaymentBusy = paymentState is PaymentUiState.Submitting

            // Upgrade to Pro button
            HustleButton(
                text = "Upgrade to Pro — KES 150/month",
                onClick = {
                    viewModel.triggerPayment(
                        rawPhone = phoneNumber,
                        planType = "PRO",
                        serviceId = null,
                    )
                },
                loading = isPaymentBusy,
                enabled = phoneNumber.isNotBlank() && !isPaymentBusy,
                modifier = Modifier.fillMaxWidth(),
            )

            // Boost listing button (shown when coming from a service context)
            if (serviceId != null) {
                HustleButton(
                    text = "Boost This Service — KES 50 / 3 days",
                    onClick = {
                        viewModel.triggerPayment(
                            rawPhone = phoneNumber,
                            planType = "FEATURED",
                            serviceId = serviceId,
                        )
                    },
                    variant = HustleButtonVariant.Outlined,
                    loading = isPaymentBusy,
                    enabled = phoneNumber.isNotBlank() && !isPaymentBusy,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActiveSubscriptionCard(
    planType: String,
    expiresAt: String,
) {
    HustleCard(variant = HustleCardVariant.Tonal) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
            )
            Column {
                Text(
                    text = "Active: $planType Plan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    text = "Renews on $expiresAt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ProBenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    free: String,
    pro: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(text = free, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(
            text = pro,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
        )
    }
}
