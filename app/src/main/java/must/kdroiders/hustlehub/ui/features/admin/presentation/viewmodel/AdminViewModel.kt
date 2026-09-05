package must.kdroiders.hustlehub.ui.features.admin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.admin.domain.repository.AdminRepository
import javax.inject.Inject

@HiltViewModel
class AdminViewModel
    @Inject
    constructor(
        private val adminRepository: AdminRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AdminUiState())
        val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

        init {
            loadInitialData()
        }

        fun selectTab(tab: AdminTab) {
            _uiState.update { it.copy(selectedTab = tab, error = null) }
            when (tab) {
                AdminTab.OVERVIEW -> loadAnalytics()
                AdminTab.REPORTS -> loadReports()
                AdminTab.USERS -> loadUsers()
                AdminTab.SERVICES -> { /* services loaded through search/filters */ }
                AdminTab.AUDIT_LOGS -> loadAuditLogs()
            }
        }

        fun refresh() {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            viewModelScope.launch {
                when (_uiState.value.selectedTab) {
                    AdminTab.OVERVIEW -> loadAnalytics()
                    AdminTab.REPORTS -> loadReports()
                    AdminTab.USERS -> loadUsers()
                    AdminTab.SERVICES -> { /* refresh */ }
                    AdminTab.AUDIT_LOGS -> loadAuditLogs()
                }
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }

        fun setUserSearchQuery(query: String) {
            _uiState.update { it.copy(userSearchQuery = query) }
        }

        fun setUserFilter(filter: AdminUserFilter) {
            _uiState.update { it.copy(userFilter = filter) }
        }

        fun setReportStatusFilter(status: String?) {
            _uiState.update { it.copy(reportStatusFilter = status) }
            loadReports()
        }

        fun openActionDialog(target: AdminActionTarget) {
            _uiState.update { it.copy(activeActionTarget = target, error = null) }
        }

        fun dismissActionDialog() {
            _uiState.update { it.copy(activeActionTarget = null, isActionLoading = false) }
        }

        fun executeAction(reason: String) {
            val target = _uiState.value.activeActionTarget ?: return
            if (reason.isBlank()) {
                _uiState.update { it.copy(error = "A reason is required for admin moderation actions.") }
                return
            }

            _uiState.update { it.copy(isActionLoading = true, error = null) }
            viewModelScope.launch {
                val result = when (target) {
                    is AdminActionTarget.SuspendUser -> adminRepository.suspendUser(target.userId, reason)
                    is AdminActionTarget.UnsuspendUser -> adminRepository.unsuspendUser(target.userId, reason)
                    is AdminActionTarget.VerifyPro -> adminRepository.verifyPro(target.userId, reason)
                    is AdminActionTarget.RevokePro -> adminRepository.revokePro(target.userId, reason)
                    is AdminActionTarget.DelistService -> adminRepository.delistService(target.serviceId, reason)
                    is AdminActionTarget.RelistService -> adminRepository.relistService(target.serviceId, reason)
                    is AdminActionTarget.ResolveReport -> adminRepository.resolveReport(target.reportId, reason)
                    is AdminActionTarget.DismissReport -> adminRepository.dismissReport(target.reportId, reason)
                }

                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isActionLoading = false,
                                activeActionTarget = null,
                                successMessage = "Action completed successfully.",
                            )
                        }
                        // Refresh current tab data
                        when (_uiState.value.selectedTab) {
                            AdminTab.USERS -> loadUsers()
                            AdminTab.REPORTS -> loadReports()
                            AdminTab.OVERVIEW -> loadAnalytics()
                            else -> {}
                        }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isActionLoading = false,
                                error = err.message ?: "Failed to perform admin action.",
                            )
                        }
                    },
                )
            }
        }

        fun clearMessages() {
            _uiState.update { it.copy(error = null, successMessage = null) }
        }

        private fun loadInitialData() {
            loadAnalytics()
            loadReports()
            loadUsers()
        }

        private fun loadAnalytics() {
            _uiState.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch {
                adminRepository.getAnalytics().fold(
                    onSuccess = { data ->
                        _uiState.update { it.copy(analytics = data, isLoading = false) }
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(isLoading = false, error = err.message) }
                    },
                )
            }
        }

        private fun loadUsers() {
            viewModelScope.launch {
                adminRepository.getUsers().fold(
                    onSuccess = { list ->
                        _uiState.update { it.copy(users = list) }
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(error = err.message) }
                    },
                )
            }
        }

        private fun loadReports() {
            viewModelScope.launch {
                adminRepository.getReports(status = _uiState.value.reportStatusFilter).fold(
                    onSuccess = { list ->
                        _uiState.update { it.copy(reports = list) }
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(error = err.message) }
                    },
                )
            }
        }

        private fun loadAuditLogs() {
            viewModelScope.launch {
                adminRepository.getAuditLogs("USER", "ALL").fold(
                    onSuccess = { list ->
                        _uiState.update { it.copy(auditLogs = list) }
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(error = err.message) }
                    },
                )
            }
        }
    }
