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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
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
        // Search bar using HustleTextField
        HustleTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = stringResource(R.string.admin_users_search_placeholder),
            leadingIcon = Icons.Default.Search,
            trailingIcon = if (searchQuery.isNotBlank()) {
                {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.action_clear),
                        )
                    }
                }
            } else {
                null
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
                    label = { Text(stringResource(R.string.admin_users_filter_all_format, users.size)) },
                )
            }
            item {
                FilterChip(
                    selected = filter == AdminUserFilter.ACTIVE,
                    onClick = { onFilterSelect(AdminUserFilter.ACTIVE) },
                    label = { Text(stringResource(R.string.admin_users_filter_active)) },
                )
            }
            item {
                FilterChip(
                    selected = filter == AdminUserFilter.PRO,
                    onClick = { onFilterSelect(AdminUserFilter.PRO) },
                    label = { Text(stringResource(R.string.admin_users_filter_pro)) },
                )
            }
            item {
                FilterChip(
                    selected = filter == AdminUserFilter.SUSPENDED,
                    onClick = { onFilterSelect(AdminUserFilter.SUSPENDED) },
                    label = { Text(stringResource(R.string.admin_users_filter_suspended)) },
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
                    text = stringResource(R.string.admin_users_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.admin_users_empty_desc),
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
                        text = stringResource(R.string.admin_user_status_suspended),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.admin_user_status_active),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            if (!user.suspendedReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.admin_user_suspension_reason_format, user.suspendedReason),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
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
                    HustleButton(
                        text = stringResource(R.string.admin_user_action_revoke_pro),
                        onClick = {
                            onActionClick(AdminActionTarget.RevokePro(user.id, user.name))
                        },
                        variant = HustleButtonVariant.Secondary,
                        modifier = Modifier.height(36.dp),
                    )
                } else {
                    HustleButton(
                        text = stringResource(R.string.admin_user_action_grant_pro),
                        onClick = {
                            onActionClick(AdminActionTarget.VerifyPro(user.id, user.name))
                        },
                        variant = HustleButtonVariant.Primary,
                        modifier = Modifier.height(36.dp),
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Suspend / Unsuspend
                if (user.isSuspended) {
                    HustleButton(
                        text = stringResource(R.string.admin_user_action_unsuspend),
                        onClick = {
                            onActionClick(AdminActionTarget.UnsuspendUser(user.id, user.name))
                        },
                        variant = HustleButtonVariant.Primary,
                        modifier = Modifier.height(36.dp),
                    )
                } else {
                    HustleButton(
                        text = stringResource(R.string.admin_user_action_suspend),
                        onClick = {
                            onActionClick(AdminActionTarget.SuspendUser(user.id, user.name))
                        },
                        variant = HustleButtonVariant.Outlined,
                        modifier = Modifier.height(36.dp),
                    )
                }
            }
        }
    }
}
