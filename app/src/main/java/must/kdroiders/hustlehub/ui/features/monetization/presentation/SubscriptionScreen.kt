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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.R
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
                        text = stringResource(R.string.sub_title),
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

            val userServices by viewModel.userServices.collectAsState()
            var selectedPlanType by rememberSaveable(serviceId) {
                mutableStateOf(if (serviceId != null) "FEATURED" else "PRO")
            }
            var selectedServiceId by rememberSaveable(serviceId) {
                mutableStateOf(serviceId)
            }
            var showExtendOptions by rememberSaveable { mutableStateOf(false) }

            LaunchedEffect(userServices, selectedServiceId) {
                if (selectedServiceId == null && userServices.size == 1) {
                    selectedServiceId = userServices.first().id
                }
            }

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
                                text = stringResource(R.string.sub_extend_renew),
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
                        text = stringResource(R.string.sub_benefits_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    ProBenefitRow(stringResource(R.string.sub_benefit_priority_listing))
                    ProBenefitRow(stringResource(R.string.sub_benefit_portfolio_photos))
                    ProBenefitRow(stringResource(R.string.sub_benefit_video_pitch))
                    ProBenefitRow(stringResource(R.string.sub_benefit_pro_badge))
                    ProBenefitRow(stringResource(R.string.sub_benefit_map_pin))
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
                            text = stringResource(R.string.sub_feature),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = stringResource(R.string.sub_free),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = stringResource(R.string.sub_pro),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    ComparisonRow(
                        label = stringResource(R.string.sub_comparison_photos),
                        free = stringResource(R.string.sub_comparison_free_photos_val),
                        pro = stringResource(R.string.sub_comparison_pro_photos_val),
                    )
                    ComparisonRow(
                        label = stringResource(R.string.sub_comparison_video_pitch),
                        free = stringResource(R.string.sub_comparison_no),
                        pro = stringResource(R.string.sub_comparison_yes),
                    )
                    ComparisonRow(
                        label = stringResource(R.string.sub_comparison_featured),
                        free = stringResource(R.string.sub_comparison_no),
                        pro = stringResource(R.string.sub_comparison_yes),
                    )
                    ComparisonRow(
                        label = stringResource(R.string.sub_comparison_badge),
                        free = stringResource(R.string.sub_comparison_no),
                        pro = stringResource(R.string.sub_comparison_yes),
                    )
                    ComparisonRow(
                        label = stringResource(R.string.sub_comparison_price),
                        free = stringResource(R.string.sub_free),
                        pro = stringResource(R.string.sub_comparison_price_pro),
                    )
                }
            }

            // Show purchase section only if user does NOT have active PRO or explicitly clicked Extend
            if (!hasActivePro || showExtendOptions) {
                // Selectable Plans Section
                Text(
                    text = if (hasActivePro) {
                        stringResource(R.string.sub_section_extend_plan)
                    } else {
                        stringResource(R.string.sub_section_select_plan)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                // 1. Pro Monthly Card
                HustleCard(
                    variant = if (selectedPlanType == "PRO") {
                        HustleCardVariant.Elevated
                    } else {
                        HustleCardVariant.Outlined
                    },
                    onClick = { selectedPlanType = "PRO" },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.sub_plan_monthly_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(R.string.sub_plan_monthly_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = stringResource(R.string.sub_plan_monthly_price),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }

                // 2. Pro Quarterly Card (Save KES 50!)
                HustleCard(
                    variant = if (selectedPlanType == "PRO_QUARTERLY") {
                        HustleCardVariant.Elevated
                    } else {
                        HustleCardVariant.Outlined
                    },
                    onClick = { selectedPlanType = "PRO_QUARTERLY" },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.sub_plan_quarterly_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = stringResource(R.string.sub_plan_quarterly_save),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                            Text(
                                text = stringResource(R.string.sub_plan_quarterly_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = stringResource(R.string.sub_plan_quarterly_price),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }

                // 3. Featured Boost
                HustleCard(
                    variant = if (selectedPlanType == "FEATURED") {
                        HustleCardVariant.Elevated
                    } else {
                        HustleCardVariant.Outlined
                    },
                    onClick = { selectedPlanType = "FEATURED" },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.sub_plan_featured_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(R.string.sub_plan_featured_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = stringResource(R.string.sub_plan_featured_price),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }

                // If FEATURED is selected, display service selection
                if (selectedPlanType == "FEATURED") {
                    val currentSelectedService = userServices.find { it.id == selectedServiceId }
                    if (serviceId != null || currentSelectedService != null) {
                        val titleToDisplay = currentSelectedService?.title
                            ?: stringResource(R.string.sub_selected_service)
                        HustleCard(variant = HustleCardVariant.Tonal) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.sub_featuring_service),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = titleToDisplay,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    } else if (userServices.isEmpty()) {
                        HustleCard(variant = HustleCardVariant.Outlined) {
                            Text(
                                text = stringResource(R.string.sub_no_services_error),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.sub_choose_service_to_boost),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            userServices.forEach { s ->
                                val isChosen = s.id == selectedServiceId
                                HustleCard(
                                    variant = if (isChosen) {
                                        HustleCardVariant.Elevated
                                    } else {
                                        HustleCardVariant.Outlined
                                    },
                                    onClick = { selectedServiceId = s.id },
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = s.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                            )
                                            Text(
                                                text = s.priceRange,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        if (isChosen) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = stringResource(R.string.sub_selected_cd),
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // M-Pesa payment section
                Text(
                    text = stringResource(R.string.sub_pay_with_mpesa),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                HustleTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = stringResource(R.string.sub_phone_label),
                    placeholder = stringResource(R.string.sub_phone_placeholder),
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
                val selectedServiceName = userServices.find { it.id == selectedServiceId }?.title
                val isServiceNeeded = selectedPlanType == "FEATURED" && selectedServiceId == null

                val buttonText = when (selectedPlanType) {
                    "PRO_QUARTERLY" -> stringResource(R.string.sub_btn_upgrade_quarterly)
                    "FEATURED" -> {
                        if (!selectedServiceName.isNullOrBlank()) {
                            stringResource(R.string.sub_btn_boost_service_format, selectedServiceName)
                        } else {
                            stringResource(R.string.sub_btn_boost_listing)
                        }
                    }
                    else -> stringResource(R.string.sub_btn_upgrade_monthly)
                }

                // Action button
                HustleButton(
                    text = buttonText,
                    onClick = {
                        viewModel.triggerPayment(
                            rawPhone = phoneNumber,
                            planType = selectedPlanType,
                            serviceId = if (selectedPlanType == "FEATURED") selectedServiceId else null,
                        )
                    },
                    loading = isPaymentBusy,
                    enabled = phoneNumber.isNotBlank() && !isPaymentBusy && !isServiceNeeded,
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
                            text = stringResource(R.string.sub_active_pro_member),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                        Text(
                            text = stringResource(R.string.sub_plan_format, planType),
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
                    text = stringResource(R.string.sub_time_remaining),
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

@Composable
private fun formatRemainingTime(expiresAtString: String): String {
    val expiryInstant = runCatching { java.time.Instant.parse(expiresAtString) }.getOrNull()
        ?: return stringResource(R.string.sub_expires_format, expiresAtString)
    val now = java.time.Instant.now()
    val totalSeconds = java.time.Duration
        .between(now, expiryInstant)
        .seconds
    if (totalSeconds <= 0) return stringResource(R.string.sub_expired)

    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600

    val dayUnit = if (days == 1L) {
        stringResource(R.string.sub_day_singular)
    } else {
        stringResource(R.string.sub_day_plural)
    }
    val hourUnit = if (hours == 1L) {
        stringResource(R.string.sub_hour_singular)
    } else {
        stringResource(R.string.sub_hour_plural)
    }

    return when {
        days > 0 -> stringResource(R.string.sub_time_days_hours, days, dayUnit, hours, hourUnit)
        hours > 0 -> stringResource(R.string.sub_time_hours, hours, hourUnit)
        else -> stringResource(R.string.sub_time_less_than_hour)
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
