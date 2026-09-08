package must.kdroiders.hustlehub.ui.features.admin.domain.repository

import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminAnalytics
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminAuditLogItem
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminReportItem
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminUserItem

interface AdminRepository {
    suspend fun getAnalytics(): Result<AdminAnalytics>
    suspend fun getUsers(
        page: Int = 0,
        size: Int = 50,
    ): Result<List<AdminUserItem>>
    suspend fun suspendUser(
        id: String,
        reason: String,
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
        targetType: String,
        targetId: String,
    ): Result<List<AdminAuditLogItem>>
}
