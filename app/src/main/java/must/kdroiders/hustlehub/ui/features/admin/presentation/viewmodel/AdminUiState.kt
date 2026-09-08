package must.kdroiders.hustlehub.ui.features.admin.presentation.viewmodel

import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminAnalytics
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminAuditLogItem
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminReportItem
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminUserItem

enum class AdminTab {
    OVERVIEW,
    REPORTS,
    USERS,
    SERVICES,
    AUDIT_LOGS,
}

enum class AdminUserFilter {
    ALL,
    ACTIVE,
    SUSPENDED,
    PRO,
}

sealed class AdminActionTarget {
    data class SuspendUser(val userId: String, val userName: String) : AdminActionTarget()
    data class UnsuspendUser(val userId: String, val userName: String) : AdminActionTarget()
    data class VerifyPro(val userId: String, val userName: String) : AdminActionTarget()
    data class RevokePro(val userId: String, val userName: String) : AdminActionTarget()
    data class DelistService(val serviceId: String, val serviceTitle: String) : AdminActionTarget()
    data class RelistService(val serviceId: String, val serviceTitle: String) : AdminActionTarget()
    data class ResolveReport(val reportId: String, val reportReason: String) : AdminActionTarget()
    data class DismissReport(val reportId: String, val reportReason: String) : AdminActionTarget()
}

data class AdminUiState(
    val selectedTab: AdminTab = AdminTab.OVERVIEW,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val analytics: AdminAnalytics = AdminAnalytics(),
    val users: List<AdminUserItem> = emptyList(),
    val userSearchQuery: String = "",
    val userFilter: AdminUserFilter = AdminUserFilter.ALL,
    val reports: List<AdminReportItem> = emptyList(),
    val reportStatusFilter: String? = null,
    val auditLogs: List<AdminAuditLogItem> = emptyList(),
    val activeActionTarget: AdminActionTarget? = null,
    val isActionLoading: Boolean = false,
)
