package must.kdroiders.hustlehub.ui.features.monetization.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import must.kdroiders.hustlehub.sharedComposables.HustleBackButton
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleCard
import must.kdroiders.hustlehub.sharedComposables.HustleCardVariant
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.sharedComposables.LoadingIndicator
import must.kdroiders.hustlehub.sharedComposables.ProBadge

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
                    HustleBackButton(onClick = onNavigateBack)
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            var selectedPlanType by rememberSaveable { mutableStateOf("PRO") }
            var showExtendOptions by rememberSaveable { mutableStateOf(false) }

            val activeSub = (subscriptionState as? SubscriptionUiState.Success)?.data
            val hasActivePro = activeSub != null && (activeSub.isActive || activeSub.status == "ACTIVE")

            // Active subscription status card
            when (val state = subscriptionState) {
                is SubscriptionUiState.Loading -> LoadingIndicator()
                is SubscriptionUiState.Error -> ErrorView(message = state.message, onRetry = viewModel::loadSubscription)
                is SubscriptionUiState.Success -> {
                    val subscription = state.data
                    if (subscription != null && (subscription.isActive || subscription.status == "ACTIVE")) {
                        ActiveSubscriptionCard(
                            planType = subscription.planType,
                            expiresAt = subscription.endDate,
                        )
                        if (!showExtendOptions) {
                            HustleButton(
                                text = "Extend / Renew Subscription",
                                onClick = { showExtendOptions = true },
                                variant = HustleButtonVariant.Outlined,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
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
                        Text(
                            "Feature",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "Free",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "Pro",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    ComparisonRow(label = "Photos", free = "3", pro = "15")
                    ComparisonRow(label = "Video pitch", free = "No", pro = "Yes")
                    ComparisonRow(label = "Featured listing", free = "No", pro = "Yes")
                    ComparisonRow(label = "PRO badge", free = "No", pro = "Yes")
                    ComparisonRow(label = "Price", free = "Free", pro = "KES 150/mo")
                }
            }

            // Show purchase section only if user does NOT have active PRO or explicitly clicked Extend
            if (!hasActivePro || showExtendOptions) {
                // Selectable Plans Section
                Text(
                    text = if (hasActivePro) "Extend Subscription" else "Select Subscription Plan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                // 1. Pro Monthly Card
                HustleCard(
                    variant = if (selectedPlanType == "PRO") HustleCardVariant.Elevated else HustleCardVariant.Outlined,
                    onClick = { selectedPlanType = "PRO" },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "HustleHub Pro (1 Month)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Full Pro benefits & badge for 30 days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "KES 150",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }

                // 2. Pro Quarterly Card (Save KES 50!)
                HustleCard(
                    variant = if (selectedPlanType == "PRO_QUARTERLY") HustleCardVariant.Elevated else HustleCardVariant.Outlined,
                    onClick = { selectedPlanType = "PRO_QUARTERLY" },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "HustleHub Pro (3 Months)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "SAVE KES 50",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                            Text(
                                text = "Full Pro benefits & badge for 90 days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "KES 400",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }

                // 3. Featured Boost
                HustleCard(
                    variant = if (selectedPlanType == "FEATURED") HustleCardVariant.Elevated else HustleCardVariant.Outlined,
                    onClick = { selectedPlanType = "FEATURED" },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Featured Listing Boost",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Boost service to top of search for 3 days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "KES 50",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
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

                val buttonText = when (selectedPlanType) {
                    "PRO_QUARTERLY" -> "Upgrade to Pro (3 Months) — KES 400"
                    "FEATURED" -> "Boost Listing (3 Days) — KES 50"
                    else -> "Upgrade to Pro (1 Month) — KES 150"
                }

                // Action button
                HustleButton(
                    text = buttonText,
                    onClick = {
                        viewModel.triggerPayment(
                            rawPhone = phoneNumber,
                            planType = selectedPlanType,
                            serviceId = serviceId,
                        )
                    },
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
    val remainingFormatted = formatRemainingTime(expiresAt)
    HustleCard(variant = HustleCardVariant.Tonal) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                    Column {
                        Text(
                            text = "ACTIVE PRO MEMBER",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                        Text(
                            text = "Plan: $planType",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                ProBadge(isVisible = true)
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Time Remaining:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = remainingFormatted,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun formatRemainingTime(expiresAtString: String): String {
    val expiryInstant = runCatching { java.time.Instant.parse(expiresAtString) }.getOrNull()
        ?: return "Expires: $expiresAtString"
    val now = java.time.Instant.now()
    val totalSeconds = java.time.Duration
        .between(now, expiryInstant)
        .seconds
    if (totalSeconds <= 0) return "Subscription Expired"

    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600

    return when {
        days > 0 -> "$days ${if (days == 1L) "day" else "days"}, $hours ${if (hours == 1L) "hour" else "hours"} left"
        hours > 0 -> "$hours ${if (hours == 1L) "hour" else "hours"} left"
        else -> "< 1 hour left"
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
        Text(
            text = free,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = pro,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
        )
    }
}
