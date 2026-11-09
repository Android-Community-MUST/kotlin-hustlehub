package must.kdroiders.hustlehub.ui.features.admin.domain.repository

import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminAnalytics
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminAuditLogItem
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminReportItem
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminUserItem
import must.kdroiders.hustlehub.ui.features.miniapp.data.remote.dto.MiniAppRemoteDto

interface AdminRepository {
    suspend fun getAnalytics(): Result<AdminAnalytics>
    suspend fun getUsers(
        page: Int = 0,
        size: Int = 50,
    ): Result<List<AdminUserItem>>
    suspend fun suspendUser(
        id: String,
        reason: String,
        durationHours: Long? = null,
    ): Result<Unit>
    suspend fun unsuspendUser(
        id: String,
        reason: String,
    ): Result<Unit>
    suspend fun verifyPro(
        id: String,
        reason: String,
    ): Result<Unit>
    suspend fun revokePro(
        id: String,
        reason: String,
    ): Result<Unit>
    suspend fun delistService(
        id: String,
        reason: String,
    ): Result<Unit>
    suspend fun relistService(
        id: String,
        reason: String,
    ): Result<Unit>
    suspend fun getReports(
        status: String? = null,
        page: Int = 0,
        size: Int = 50,
    ): Result<List<AdminReportItem>>
    suspend fun resolveReport(
        id: String,
        reason: String,
    ): Result<Unit>
    suspend fun dismissReport(
        id: String,
        reason: String,
    ): Result<Unit>
    suspend fun getAuditLogs(
        targetType: String? = null,
        targetId: String? = null,
    ): Result<List<AdminAuditLogItem>>
    suspend fun getPendingMiniApps(
        page: Int = 0,
        size: Int = 50,
    ): Result<List<MiniAppRemoteDto>>
    suspend fun reviewMiniApp(
        id: String,
        status: String,
        adminNotes: String? = null,
    ): Result<MiniAppRemoteDto>
}
