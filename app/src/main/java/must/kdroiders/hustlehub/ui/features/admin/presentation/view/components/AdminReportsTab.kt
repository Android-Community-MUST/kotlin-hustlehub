package must.kdroiders.hustlehub.ui.features.admin.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminReportItem
import must.kdroiders.hustlehub.ui.features.admin.presentation.viewmodel.AdminActionTarget

@Composable
fun AdminReportsTab(
    reports: List<AdminReportItem>,
    selectedStatus: String?,
    onStatusSelect: (String?) -> Unit,
    onActionClick: (AdminActionTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { onStatusSelect(null) },
                    label = { Text(stringResource(R.string.admin_reports_filter_all)) },
                )
            }
            item {
                FilterChip(
                    selected = selectedStatus == "OPEN",
                    onClick = { onStatusSelect("OPEN") },
                    label = { Text(stringResource(R.string.admin_reports_filter_open)) },
                )
            }
            item {
                FilterChip(
                    selected = selectedStatus == "RESOLVED",
                    onClick = { onStatusSelect("RESOLVED") },
                    label = { Text(stringResource(R.string.admin_reports_filter_resolved)) },
                )
            }
            item {
                FilterChip(
                    selected = selectedStatus == "DISMISSED",
                    onClick = { onStatusSelect("DISMISSED") },
                    label = { Text(stringResource(R.string.admin_reports_filter_dismissed)) },
                )
            }
        }

        if (reports.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.admin_reports_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.admin_reports_empty_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(reports, key = { it.id }) { report ->
                    ReportCard(report = report, onActionClick = onActionClick)
                }
            }
        }
    }
}

@Composable
private fun ReportCard(
    report: AdminReportItem,
    onActionClick: (AdminActionTarget) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.admin_report_reporter_format, report.reporterName),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                val statusColor = when (report.status) {
                    "OPEN" -> MaterialTheme.colorScheme.error
                    "RESOLVED" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(
                    text = report.status,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.admin_report_reason_format, report.reason),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (!report.details.isNullOrBlank()) {
                Text(
                    text = report.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!report.adminNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.admin_report_admin_note_format, report.adminNotes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (report.status == "OPEN") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HustleButton(
                        text = stringResource(R.string.admin_report_action_dismiss),
                        onClick = {
                            onActionClick(AdminActionTarget.DismissReport(report.id, report.reason))
                        },
                        variant = HustleButtonVariant.Outlined,
                        modifier = Modifier.height(36.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    HustleButton(
                        text = stringResource(R.string.admin_report_action_resolve),
                        onClick = {
                            onActionClick(AdminActionTarget.ResolveReport(report.id, report.reason))
                        },
                        variant = HustleButtonVariant.Primary,
                        modifier = Modifier.height(36.dp),
                    )
                }
            }
        }
    }
}
