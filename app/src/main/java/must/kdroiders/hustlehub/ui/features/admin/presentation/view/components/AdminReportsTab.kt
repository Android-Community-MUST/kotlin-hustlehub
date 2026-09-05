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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                    label = { Text("All Reports") },
                )
            }
            item {
                FilterChip(
                    selected = selectedStatus == "OPEN",
                    onClick = { onStatusSelect("OPEN") },
                    label = { Text("Open (Pending)") },
                )
            }
            item {
                FilterChip(
                    selected = selectedStatus == "RESOLVED",
                    onClick = { onStatusSelect("RESOLVED") },
                    label = { Text("Resolved") },
                )
            }
            item {
                FilterChip(
                    selected = selectedStatus == "DISMISSED",
                    onClick = { onStatusSelect("DISMISSED") },
                    label = { Text("Dismissed") },
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
                    text = "No reports found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "All student reports under this filter are clean.",
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
        modifier = Modifier.fillMaxWidth(),
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
                    text = "Reporter: ${report.reporterName}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                val statusColor = when (report.status) {
                    "OPEN" -> Color(0xFFFF1744)
                    "RESOLVED" -> Color(0xFF00C853)
                    else -> Color(0xFF757575)
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
                text = "Reason: ${report.reason}",
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
                    text = "Admin Note: ${report.adminNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2196F3),
                )
            }

            if (report.status == "OPEN") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = {
                            onActionClick(AdminActionTarget.DismissReport(report.id, report.reason))
                        },
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = {
                            onActionClick(AdminActionTarget.ResolveReport(report.id, report.reason))
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF00C853),
                        ),
                    ) {
                        Text("Resolve")
                    }
                }
            }
        }
    }
}
