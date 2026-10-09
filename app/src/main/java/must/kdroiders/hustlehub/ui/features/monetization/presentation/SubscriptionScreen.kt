package must.kdroiders.hustlehub.ui.features.monetization.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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

private const val PLAN_PRO = "PRO"
private const val PLAN_PRO_QUARTERLY = "PRO_QUARTERLY"
private const val PLAN_FEATURED = "FEATURED"

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
    val userServices by viewModel.userServices.collectAsState()

    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var selectedPlanType by rememberSaveable(serviceId) {
        mutableStateOf(if (serviceId != null) PLAN_FEATURED else PLAN_PRO)
    }
    var selectedServiceId by rememberSaveable(serviceId) {
        mutableStateOf(serviceId)
    }
    var showExtendOptions by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(pendingCheckoutId) {
        val checkoutId = pendingCheckoutId
        if (!checkoutId.isNullOrBlank()) {
            viewModel.consumePendingCheckoutId()
            onNavigateToPaymentStatus(checkoutId)
        }
    }

    LaunchedEffect(userServices, selectedServiceId) {
        if (selectedServiceId == null && userServices.size == 1) {
            selectedServiceId = userServices.first().id
        }
    }

    val activeSub = (subscriptionState as? SubscriptionUiState.Success)?.data
    val hasActivePro = activeSub != null && (activeSub.isActive || activeSub.status == "ACTIVE")
    val showPurchase = !hasActivePro || showExtendOptions

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
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                when (val state = subscriptionState) {
                    is SubscriptionUiState.Loading -> LoadingIndicator()
                    is SubscriptionUiState.Error -> ErrorView(
                        message = state.message,
                        onRetry = viewModel::loadSubscription,
                    )
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
                        }
                    }
                }

                BenefitsCard()

                ComparisonCard()

                if (showPurchase) {
                    SectionTitle(
                        text = if (hasActivePro) {
                            stringResource(R.string.sub_section_extend_plan)
                        } else {
                            stringResource(R.string.sub_section_select_plan)
                        },
                    )

                    Column {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            PlanOption(
                                title = stringResource(R.string.sub_plan_monthly_title),
                                description = stringResource(R.string.sub_plan_monthly_desc),
                                price = stringResource(R.string.sub_plan_monthly_price),
                                selected = selectedPlanType == PLAN_PRO,
                                onClick = { selectedPlanType = PLAN_PRO },
                            )
                            PlanOption(
                                title = stringResource(R.string.sub_plan_quarterly_title),
                                description = stringResource(R.string.sub_plan_quarterly_desc),
                                price = stringResource(R.string.sub_plan_quarterly_price),
                                selected = selectedPlanType == PLAN_PRO_QUARTERLY,
                                onClick = { selectedPlanType = PLAN_PRO_QUARTERLY },
                                badge = stringResource(R.string.sub_plan_quarterly_save),
                            )
                            PlanOption(
                                title = stringResource(R.string.sub_plan_featured_title),
                                description = stringResource(R.string.sub_plan_featured_desc),
                                price = stringResource(R.string.sub_plan_featured_price),
                                selected = selectedPlanType == PLAN_FEATURED,
                                onClick = { selectedPlanType = PLAN_FEATURED },
                            )
                        }

                        AnimatedVisibility(visible = selectedPlanType == PLAN_FEATURED) {
                            Column(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                val currentSelectedService = userServices.find { it.id == selectedServiceId }
                                if (serviceId != null || currentSelectedService != null) {
                                    val titleToDisplay = currentSelectedService?.title
                                        ?: stringResource(R.string.sub_selected_service)
                                    HustleCard(variant = HustleCardVariant.Tonal) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                                            SelectableSurface(
                                                selected = isChosen,
                                                onClick = { selectedServiceId = s.id },
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                                                            tint = MaterialTheme.colorScheme.tertiary,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle(text = stringResource(R.string.sub_pay_with_mpesa))
                        HustleTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = stringResource(R.string.sub_phone_label),
                            placeholder = stringResource(R.string.sub_phone_placeholder),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            if (showPurchase) {
                val isPaymentBusy = paymentState is PaymentUiState.Submitting
                val selectedServiceName = userServices.find { it.id == selectedServiceId }?.title
                val isServiceNeeded = selectedPlanType == PLAN_FEATURED && selectedServiceId == null

                val buttonText = when (selectedPlanType) {
                    PLAN_PRO_QUARTERLY -> stringResource(R.string.sub_btn_upgrade_quarterly)
                    PLAN_FEATURED -> {
                        if (!selectedServiceName.isNullOrBlank()) {
                            stringResource(R.string.sub_btn_boost_service_format, selectedServiceName)
                        } else {
                            stringResource(R.string.sub_btn_boost_listing)
                        }
                    }
                    else -> stringResource(R.string.sub_btn_upgrade_monthly)
                }

                CheckoutBar(
                    errorMessage = (paymentState as? PaymentUiState.Failed)?.message,
                    buttonText = buttonText,
                    loading = isPaymentBusy,
                    enabled = phoneNumber.isNotBlank() && !isPaymentBusy && !isServiceNeeded,
                    onClick = {
                        viewModel.triggerPayment(
                            rawPhone = phoneNumber,
                            planType = selectedPlanType,
                            serviceId = if (selectedPlanType == PLAN_FEATURED) selectedServiceId else null,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun SelectableSurface(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.large
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.tertiary else colors.outlineVariant,
        label = "selectableBorder",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) lerp(colors.surface, colors.tertiary, 0.08f) else colors.surface,
        label = "selectableContainer",
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        shape = shape,
        color = containerColor,
        border = BorderStroke(width = if (selected) 2.dp else 1.dp, color = borderColor),
    ) {
        content()
    }
}

@Composable
private fun PlanOption(
    title: String,
    description: String,
    price: String,
    selected: Boolean,
    onClick: () -> Unit,
    badge: String? = null,
) {
    SelectableSurface(selected = selected, onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.tertiary,
                ),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (badge != null) {
                    PlanBadge(text = badge)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun PlanBadge(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.tertiary,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun CheckoutBar(
    errorMessage: String?,
    buttonText: String,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            HustleButton(
                text = buttonText,
                onClick = onClick,
                loading = loading,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun BenefitsCard() {
    HustleCard(variant = HustleCardVariant.Tonal) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                SectionTitle(text = stringResource(R.string.sub_benefits_title))
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProBenefitRow(stringResource(R.string.sub_benefit_priority_listing))
                ProBenefitRow(stringResource(R.string.sub_benefit_portfolio_photos))
                ProBenefitRow(stringResource(R.string.sub_benefit_video_pitch))
                ProBenefitRow(stringResource(R.string.sub_benefit_pro_badge))
                ProBenefitRow(stringResource(R.string.sub_benefit_map_pin))
            }
        }
    }
}

@Composable
private fun ProBenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(20.dp),
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ComparisonCard() {
    HustleCard(variant = HustleCardVariant.Outlined) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.sub_feature),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.4f),
                )
                Text(
                    text = stringResource(R.string.sub_free),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.sub_pro),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ComparisonRow(
                label = stringResource(R.string.sub_comparison_price),
                free = stringResource(R.string.sub_free),
                pro = stringResource(R.string.sub_comparison_price_pro),
            )
        }
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
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1.4f),
        )
        Text(
            text = free,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = pro,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ActiveSubscriptionCard(
    planType: String,
    expiresAt: String,
) {
    val remainingFormatted = formatRemainingTime(expiresAt)
    HustleCard(variant = HustleCardVariant.Tonal) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp),
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
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
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
