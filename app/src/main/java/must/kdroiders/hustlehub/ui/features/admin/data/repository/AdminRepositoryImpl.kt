package must.kdroiders.hustlehub.ui.features.admin.data.repository

import must.kdroiders.hustlehub.ui.features.admin.data.remote.AdminApiService
import must.kdroiders.hustlehub.ui.features.admin.data.remote.dto.AdminActionRequestDto
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminAnalytics
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminAuditLogItem
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminReportItem
import must.kdroiders.hustlehub.ui.features.admin.domain.model.AdminUserItem
import must.kdroiders.hustlehub.ui.features.admin.domain.repository.AdminRepository
import javax.inject.Inject

class AdminRepositoryImpl
    @Inject
    constructor(
        private val apiService: AdminApiService,
    ) : AdminRepository {
        override suspend fun getAnalytics(): Result<AdminAnalytics> =
            runCatching {
                val response = apiService.getAnalytics()
                val data = response.data ?: throw IllegalStateException(response.message)
                AdminAnalytics(
                    totalUsers = data.totalUsers,
                    totalServices = data.totalServices,
                    totalProSubscribers = data.totalProSubscribers,
                    openReportsCount = data.openReportsCount,
                    monthlyRevenue = data.monthlyRevenue,
                )
            }

        override suspend fun getUsers(
            page: Int,
            size: Int,
        ): Result<List<AdminUserItem>> =
            runCatching {
                val response = apiService.getUsers(page = page, size = size)
                val data = response.data ?: throw IllegalStateException(response.message)
                data.content.map { dto ->
                    AdminUserItem(
                        id = dto.id,
                        name = dto.name,
                        email = dto.email,
                        userRole = dto.userRole,
                        isSuspended = dto.isSuspended,
                        suspendedReason = dto.suspendedReason,
                        isVerifiedPro = dto.isVerifiedPro,
                        reportCount = dto.reportCount,
                        createdAt = dto.createdAt,
                    )
                }
            }

        override suspend fun suspendUser(
            id: String,
            reason: String,
        ): Result<Unit> =
            runCatching {
                val response = apiService.suspendUser(id, AdminActionRequestDto(reason))
                if (!response.success) throw IllegalStateException(response.message)
            }

        override suspend fun unsuspendUser(
            id: String,
            reason: String,
        ): Result<Unit> =
            runCatching {
                val response = apiService.unsuspendUser(id, AdminActionRequestDto(reason))
                if (!response.success) throw IllegalStateException(response.message)
            }

        override suspend fun verifyPro(
            id: String,
            reason: String,
        ): Result<Unit> =
            runCatching {
                val response = apiService.verifyPro(id, AdminActionRequestDto(reason))
                if (!response.success) throw IllegalStateException(response.message)
            }

        override suspend fun revokePro(
            id: String,
            reason: String,
        ): Result<Unit> =
            runCatching {
                val response = apiService.revokePro(id, AdminActionRequestDto(reason))
                if (!response.success) throw IllegalStateException(response.message)
            }

        override suspend fun delistService(
            id: String,
            reason: String,
        ): Result<Unit> =
            runCatching {
                val response = apiService.delistService(id, AdminActionRequestDto(reason))
                if (!response.success) throw IllegalStateException(response.message)
            }

        override suspend fun relistService(
            id: String,
            reason: String,
        ): Result<Unit> =
            runCatching {
                val response = apiService.relistService(id, AdminActionRequestDto(reason))
                if (!response.success) throw IllegalStateException(response.message)
            }

        override suspend fun getReports(
            status: String?,
            page: Int,
            size: Int,
        ): Result<List<AdminReportItem>> =
            runCatching {
                val response = apiService.getReports(status = status, page = page, size = size)
                val data = response.data ?: throw IllegalStateException(response.message)
                data.content.map { dto ->
                    AdminReportItem(
                        id = dto.id,
                        reporterName = dto.reporterName,
                        reportedUserId = dto.reportedUserId,
                        reportedServiceId = dto.reportedServiceId,
                        reason = dto.reason,
                        details = dto.details,
                        status = dto.status,
                        adminNotes = dto.adminNotes,
                        createdAt = dto.createdAt,
                    )
                }
            }

        override suspend fun resolveReport(
            id: String,
            reason: String,
        ): Result<Unit> =
            runCatching {
                val response = apiService.resolveReport(id, AdminActionRequestDto(reason))
                if (!response.success) throw IllegalStateException(response.message)
            }

        override suspend fun dismissReport(
            id: String,
            reason: String,
        ): Result<Unit> =
            runCatching {
                val response = apiService.dismissReport(id, AdminActionRequestDto(reason))
                if (!response.success) throw IllegalStateException(response.message)
            }

        override suspend fun getAuditLogs(
            targetType: String,
            targetId: String,
        ): Result<List<AdminAuditLogItem>> =
            runCatching {
                val response = apiService.getAuditLogs(targetType = targetType, targetId = targetId)
                val data = response.data ?: throw IllegalStateException(response.message)
                data.content.map { dto ->
                    AdminAuditLogItem(
                        id = dto.id,
                        adminId = dto.adminId,
                        action = dto.action,
                        targetType = dto.targetType,
                        targetId = dto.targetId,
                        reason = dto.reason,
                        createdAt = dto.createdAt,
                    )
                }
            }
    }
