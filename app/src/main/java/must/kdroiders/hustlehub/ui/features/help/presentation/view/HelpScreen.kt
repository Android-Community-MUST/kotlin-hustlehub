package must.kdroiders.hustlehub.ui.features.help.presentation.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayLesson
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.BuildConfig
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsDivider
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsGroup
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsRowExternalLink
import must.kdroiders.hustlehub.ui.features.settings.presentation.components.SettingsSectionLabel
import timber.log.Timber

private const val SUPPORT_EMAIL = "kdroiders.hustlehub@gmail.com"
private const val BUG_EMAIL = "kdroiders.hustlehub@gmail.com"
private const val TERMS_URL = "https://hustlehub.app/terms"
private const val PRIVACY_URL = "https://hustlehub.app/privacy"

private data class FaqItem(val question: String, val answer: String)

private data class FaqCategory(
    val title: String,
    val icon: ImageVector,
    val items: List<FaqItem>,
)

@Composable
private fun rememberFaqCategories(): List<FaqCategory> {
    val gettingStarted = listOf(
        FaqItem(
            question = stringResource(R.string.faq_qs_create_service),
            answer = stringResource(R.string.faq_ans_create_service),
        ),
        FaqItem(
            question = stringResource(R.string.faq_qs_student_verify),
            answer = stringResource(R.string.faq_ans_student_verify),
        ),
    )
    val services = listOf(
        FaqItem(
            question = stringResource(R.string.faq_qs_manage_service),
            answer = stringResource(R.string.faq_ans_manage_service),
        ),
        FaqItem(
            question = stringResource(R.string.faq_qs_service_removed),
            answer = stringResource(R.string.faq_ans_service_removed),
        ),
    )
    val messaging = listOf(
        FaqItem(
            question = stringResource(R.string.faq_qs_share_phone),
            answer = stringResource(R.string.faq_ans_share_phone),
        ),
        FaqItem(
            question = stringResource(R.string.faq_qs_cannot_message),
            answer = stringResource(R.string.faq_ans_cannot_message),
        ),
    )
    val payments = listOf(
        FaqItem(
            question = stringResource(R.string.faq_qs_mpesa_pay),
            answer = stringResource(R.string.faq_ans_mpesa_pay),
        ),
        FaqItem(
            question = stringResource(R.string.faq_qs_payment_unconfirmed),
            answer = stringResource(R.string.faq_ans_payment_unconfirmed),
        ),
    )
    val safety = listOf(
        FaqItem(
            question = stringResource(R.string.faq_qs_report_user),
            answer = stringResource(R.string.faq_ans_report_user),
        ),
        FaqItem(
            question = stringResource(R.string.faq_qs_block_user),
            answer = stringResource(R.string.faq_ans_block_user),
        ),
    )

    val gettingStartedTitle = stringResource(R.string.faq_cat_getting_started)
    val servicesTitle = stringResource(R.string.faq_cat_services)
    val messagingTitle = stringResource(R.string.faq_cat_messaging)
    val paymentsTitle = stringResource(R.string.faq_cat_payments)
    val safetyTitle = stringResource(R.string.faq_cat_safety)

    return remember(
        gettingStartedTitle,
        servicesTitle,
        messagingTitle,
        paymentsTitle,
        safetyTitle,
    ) {
        listOf(
            FaqCategory(gettingStartedTitle, Icons.Default.PlayLesson, gettingStarted),
            FaqCategory(servicesTitle, Icons.Default.RateReview, services),
            FaqCategory(messagingTitle, Icons.Default.SupportAgent, messaging),
            FaqCategory(paymentsTitle, Icons.Default.AttachMoney, payments),
            FaqCategory(safetyTitle, Icons.Default.Security, safety),
        )
    }
}

private fun launchEmail(
    context: Context,
    to: String,
    subject: String = "",
    body: String = "",
) {
    runCatching {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            if (subject.isNotBlank()) putExtra(Intent.EXTRA_SUBJECT, subject)
            if (body.isNotBlank()) putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, "Send Email"))
    }.onFailure { e ->
        Timber.e(e, "Failed to launch email intent")
    }
}

private fun launchBugReport(context: Context) {
    val deviceInfo =
        "Device: ${Build.MANUFACTURER} ${Build.MODEL}\n" +
            "Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n" +
            "App: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n\n" +
            "Describe the bug:\n"
    launchEmail(
        context = context,
        to = BUG_EMAIL,
        subject = "Bug Report — HustleHub ${BuildConfig.VERSION_NAME}",
        body = deviceInfo,
    )
}

private fun openBrowser(
    context: Context,
    url: String,
) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }.onFailure { e ->
        Timber.e(e, "Failed to open browser for URL: %s", url)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val faqCategories = rememberFaqCategories()
    // Tracks which FAQ categories are expanded; key = category title
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }

    HustleScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.help_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back),
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // FAQ Section
            SettingsSectionLabel(stringResource(R.string.help_section_faq))
            Spacer(Modifier.height(8.dp))

            faqCategories.forEachIndexed { index, category ->
                val isExpanded = expandedCategories[category.title] ?: false
                FaqCategoryCard(
                    category = category,
                    isExpanded = isExpanded,
                    onToggle = {
                        expandedCategories[category.title] = !isExpanded
                    },
                )
                if (index < faqCategories.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Support Section
            SettingsSectionLabel(stringResource(R.string.help_section_support))
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowExternalLink(
                    icon = Icons.Default.SupportAgent,
                    label = stringResource(R.string.help_contact_support),
                    onClick = {
                        launchEmail(
                            context = context,
                            to = SUPPORT_EMAIL,
                            subject = "HustleHub Support Request",
                        )
                    },
                )
                SettingsDivider()
                SettingsRowExternalLink(
                    icon = Icons.Default.Security,
                    label = stringResource(R.string.help_report_bug),
                    onClick = { launchBugReport(context) },
                )
            }

            Spacer(Modifier.height(24.dp))

            // Legal Section
            SettingsSectionLabel(stringResource(R.string.help_section_legal))
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRowExternalLink(
                    icon = Icons.Default.RateReview,
                    label = stringResource(R.string.help_terms_of_service),
                    onClick = { openBrowser(context, TERMS_URL) },
                )
                SettingsDivider()
                SettingsRowExternalLink(
                    icon = Icons.Default.Security,
                    label = stringResource(R.string.help_privacy_policy),
                    onClick = { openBrowser(context, PRIVACY_URL) },
                )
            }

            Spacer(Modifier.height(24.dp))

            // App version footer
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) · HustleHub",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FaqCategoryCard(
    category: FaqCategory,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // Category header row — always visible, tappable to expand/collapse
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "${category.title}, ${if (isExpanded) "expanded" else "collapsed"}"
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse ${category.title}" else "Expand ${category.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        // Expandable FAQ items
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                category.items.forEachIndexed { index, faqItem ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp)
                            .height(0.5.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant),
                    )
                    FaqItemView(item = faqItem)
                }
            }
        }
    }
}

@Composable
private fun FaqItemView(item: FaqItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = item.question,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.answer,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp,
        )
    }
}
