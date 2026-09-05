package must.kdroiders.hustlehub.ui.features.admin.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class AdminAnalyticsDto(
    @SerializedName("totalUsers") val totalUsers: Long = 0,
    @SerializedName("totalServices") val totalServices: Long = 0,
    @SerializedName("totalProSubscribers") val totalProSubscribers: Long = 0,
    @SerializedName("openReportsCount") val openReportsCount: Long = 0,
    @SerializedName("monthlyRevenue") val monthlyRevenue: BigDecimal = BigDecimal.ZERO,
)

data class UserAdminViewDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("userRole") val userRole: String,
    @SerializedName("isSuspended") val isSuspended: Boolean,
    @SerializedName("suspendedReason") val suspendedReason: String? = null,
    @SerializedName("isVerifiedPro") val isVerifiedPro: Boolean = false,
    @SerializedName("reportCount") val reportCount: Long = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
)

data class ReportResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("reporterName") val reporterName: String,
    @SerializedName("reportedUserId") val reportedUserId: String? = null,
    @SerializedName("reportedServiceId") val reportedServiceId: String? = null,
    @SerializedName("reason") val reason: String,
    @SerializedName("details") val details: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("adminNotes") val adminNotes: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
)

data class AuditLogResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("adminId") val adminId: String,
    @SerializedName("action") val action: String,
    @SerializedName("targetType") val targetType: String,
    @SerializedName("targetId") val targetId: String,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
)

data class AdminActionRequestDto(
    @SerializedName("reason") val reason: String,
)
