package must.kdroiders.hustlehub.ui.features.admin.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleBackButton
import must.kdroiders.hustlehub.sharedComposables.HustlePullToRefreshBox
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.ui.features.admin.presentation.view.components.AdminActionDialog
import must.kdroiders.hustlehub.ui.features.admin.presentation.view.components.AdminAuditLogsTab
import must.kdroiders.hustlehub.ui.features.admin.presentation.view.components.AdminOverviewTab
import must.kdroiders.hustlehub.ui.features.admin.presentation.view.components.AdminReportsTab
import must.kdroiders.hustlehub.ui.features.admin.presentation.view.components.AdminServicesTab
import must.kdroiders.hustlehub.ui.features.admin.presentation.view.components.AdminUsersTab
import must.kdroiders.hustlehub.ui.features.admin.presentation.viewmodel.AdminTab
import must.kdroiders.hustlehub.ui.features.admin.presentation.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    HustleScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.admin_center_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    HustleBackButton(onClick = onNavigateBack)
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.cd_refresh_data),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val tabs = listOf(
                AdminTab.OVERVIEW to stringResource(R.string.admin_tab_overview),
                AdminTab.REPORTS to stringResource(
                    R.string.admin_tab_reports_format,
                    state.reports.count { it.status == "OPEN" },
                ),
                AdminTab.USERS to stringResource(R.string.admin_tab_users),
                AdminTab.SERVICES to stringResource(R.string.admin_tab_services),
                AdminTab.AUDIT_LOGS to stringResource(R.string.admin_tab_audit_logs),
            )

            PrimaryScrollableTabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                tabs.forEachIndexed { index, (tab, title) ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (state.selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }

            HustlePullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (state.selectedTab) {
                        AdminTab.OVERVIEW -> AdminOverviewTab(analytics = state.analytics)
                        AdminTab.REPORTS -> AdminReportsTab(
                            reports = state.reports,
                            selectedStatus = state.reportStatusFilter,
                            onStatusSelect = viewModel::setReportStatusFilter,
                            onActionClick = viewModel::openActionDialog,
                        )
                        AdminTab.USERS -> AdminUsersTab(
                            users = state.users,
                            searchQuery = state.userSearchQuery,
                            filter = state.userFilter,
                            onSearchChange = viewModel::setUserSearchQuery,
                            onFilterSelect = viewModel::setUserFilter,
                            onActionClick = viewModel::openActionDialog,
                        )
                        AdminTab.SERVICES -> AdminServicesTab(
                            onActionClick = viewModel::openActionDialog,
                        )
                        AdminTab.AUDIT_LOGS -> AdminAuditLogsTab(
                            logs = state.auditLogs,
                        )
                    }
                }
            }
        }
    }

    // Action Confirmation / Reason Dialog
    state.activeActionTarget?.let { target ->
        AdminActionDialog(
            target = target,
            isLoading = state.isActionLoading,
            onConfirm = { reason, durationHours -> viewModel.executeAction(reason, durationHours) },
            onDismiss = viewModel::dismissActionDialog,
        )
    }
}
