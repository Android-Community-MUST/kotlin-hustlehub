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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.miniapp.data.remote.dto.MiniAppRemoteDto

@Composable
fun AdminMiniAppsTab(
    pendingMiniApps: List<MiniAppRemoteDto>,
    isLoading: Boolean,
    onPreviewMiniApp: (appId: String, title: String, url: String) -> Unit,
    onApprove: (id: String) -> Unit,
    onReject: (id: String, reason: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var rejectingMiniApp by remember { mutableStateOf<MiniAppRemoteDto?>(null) }
    var rejectReason by remember { mutableStateOf("") }
    var approvingMiniApp by remember { mutableStateOf<MiniAppRemoteDto?>(null) }

    rejectingMiniApp?.let { app ->
        AlertDialog(
            onDismissRequest = {
                rejectingMiniApp = null
                rejectReason = ""
            },
            title = {
                Text(
                    text = "Reject Mini-App",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Provide feedback explaining why \"${app.title}\" was rejected:")
                    HustleTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = "Feedback / Reason",
                        placeholder = "e.g. Broken HTTPS certificate, non-functional bridge...",
                        singleLine = false,
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReject(app.id, rejectReason)
                        rejectingMiniApp = null
                        rejectReason = ""
                    },
                    enabled = rejectReason.isNotBlank() && !isLoading,
                ) {
                    Text("Reject", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        rejectingMiniApp = null
                        rejectReason = ""
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    approvingMiniApp?.let { app ->
        AlertDialog(
            onDismissRequest = { approvingMiniApp = null },
            title = {
                Text(
                    text = "Approve Mini-App",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to approve \"${app.title}\"? It will immediately go live on the campus feed.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onApprove(app.id)
                        approvingMiniApp = null
                    },
                    enabled = !isLoading,
                ) {
                    Text("Approve")
                }
            },
            dismissButton = {
                TextButton(onClick = { approvingMiniApp = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (pendingMiniApps.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "No Pending Applications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "All submitted mini-apps have been reviewed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(pendingMiniApps, key = { it.id }) { app ->
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = app.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SuggestionChip(
                                onClick = {},
                                label = { Text(app.category) },
                            )
                        }

                        Text(
                            text = "ID: ${app.appId}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = app.entryUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Text(
                            text = app.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            HustleButton(
                                text = "Preview",
                                onClick = {
                                    onPreviewMiniApp(app.appId, app.title, app.entryUrl)
                                },
                                icon = Icons.Default.Visibility,
                                variant = HustleButtonVariant.Outlined,
                                modifier = Modifier.weight(1f),
                            )

                            HustleButton(
                                text = "Reject",
                                onClick = {
                                    rejectingMiniApp = app
                                },
                                variant = HustleButtonVariant.Outlined,
                                modifier = Modifier.weight(1f),
                            )

                            HustleButton(
                                text = "Approve",
                                onClick = {
                                    approvingMiniApp = app
                                },
                                variant = HustleButtonVariant.Primary,
                                loading = isLoading && approvingMiniApp?.id == app.id,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}
