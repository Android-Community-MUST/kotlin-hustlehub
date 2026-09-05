package must.kdroiders.hustlehub.ui.features.admin.domain.model

import java.math.BigDecimal

data class AdminAnalytics(
    val totalUsers: Long = 0,
    val totalServices: Long = 0,
    val totalProSubscribers: Long = 0,
    val openReportsCount: Long = 0,
    val monthlyRevenue: BigDecimal = BigDecimal.ZERO,
)

data class AdminUserItem(
    val id: String,
    val name: String,
    val email: String,
    val userRole: String,
    val isSuspended: Boolean,
    val suspendedReason: String? = null,
    val isVerifiedPro: Boolean = false,
    val reportCount: Long = 0,
    val createdAt: String? = null,
)

data class AdminReportItem(
    val id: String,
    val reporterName: String,
    val reportedUserId: String? = null,
    val reportedServiceId: String? = null,
    val reason: String,
    val details: String? = null,
    val status: String,
    val adminNotes: String? = null,
    val createdAt: String? = null,
)

data class AdminAuditLogItem(
    val id: String,
    val adminId: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val reason: String? = null,
    val createdAt: String? = null,
)
