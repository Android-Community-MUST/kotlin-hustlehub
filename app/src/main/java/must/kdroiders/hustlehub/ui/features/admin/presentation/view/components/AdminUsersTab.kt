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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.sharedComposables.ProBadge
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminUserItem
import must.kdroiders.hustlehub.ui.features.admin.presentation.viewmodel.AdminActionTarget
import must.kdroiders.hustlehub.ui.features.admin.presentation.viewmodel.AdminUserFilter

@Composable
fun AdminUsersTab(
    users: List<AdminUserItem>,
    searchQuery: String,
    filter: AdminUserFilter,
    onSearchChange: (String) -> Unit,
    onFilterSelect: (AdminUserFilter) -> Unit,
    onActionClick: (AdminActionTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filteredUsers = users.filter { user ->
        val matchesQuery = searchQuery.isBlank() ||
            user.name.contains(searchQuery, ignoreCase = true) ||
            user.email.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (filter) {
            AdminUserFilter.ALL -> true
            AdminUserFilter.ACTIVE -> !user.isSuspended
            AdminUserFilter.SUSPENDED -> user.isSuspended
            AdminUserFilter.PRO -> user.isVerifiedPro
        }
        matchesQuery && matchesFilter
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search by student name or email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
        )

        // Filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = filter == AdminUserFilter.ALL,
                    onClick = { onFilterSelect(AdminUserFilter.ALL) },
                    label = { Text("All (${users.size})") },
                )
            }
            item {
                FilterChip(
                    selected = filter == AdminUserFilter.ACTIVE,
                    onClick = { onFilterSelect(AdminUserFilter.ACTIVE) },
                    label = { Text("Active") },
                )
            }
            item {
                FilterChip(
                    selected = filter == AdminUserFilter.PRO,
                    onClick = { onFilterSelect(AdminUserFilter.PRO) },
                    label = { Text("PRO Verified") },
                )
            }
            item {
                FilterChip(
                    selected = filter == AdminUserFilter.SUSPENDED,
                    onClick = { onFilterSelect(AdminUserFilter.SUSPENDED) },
                    label = { Text("Suspended") },
                )
            }
        }

        if (filteredUsers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "No users found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Try adjusting your search query or filter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filteredUsers, key = { it.id }) { user ->
                    UserAdminCard(user = user, onActionClick = onActionClick)
                }
            }
        }
    }
}

@Composable
private fun UserAdminCard(
    user: AdminUserItem,
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        ProBadge(isVisible = user.isVerifiedPro, modifier = Modifier.padding(start = 6.dp))
                    }
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (user.isSuspended) {
                    Text(
                        text = "SUSPENDED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF1744),
                    )
                } else {
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00C853),
                    )
                }
            }

            if (!user.suspendedReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Suspension Reason: ${user.suspendedReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF1744),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Pro Badge Toggle
                if (user.isVerifiedPro) {
                    TextButton(
                        onClick = {
                            onActionClick(AdminActionTarget.RevokePro(user.id, user.name))
                        },
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text("Revoke Pro", color = Color(0xFFFFB300))
                    }
                } else {
                    TextButton(
                        onClick = {
                            onActionClick(AdminActionTarget.VerifyPro(user.id, user.name))
                        },
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text("Grant Pro", color = Color(0xFF00C853))
                    }
                }

                // Suspend / Unsuspend
                if (user.isSuspended) {
                    OutlinedButton(
                        onClick = {
                            onActionClick(AdminActionTarget.UnsuspendUser(user.id, user.name))
                        },
                    ) {
                        Text("Unsuspend", color = Color(0xFF00C853))
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            onActionClick(AdminActionTarget.SuspendUser(user.id, user.name))
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF1744),
                        ),
                    ) {
                        Text("Suspend")
                    }
                }
            }
        }
    }
}
