package must.kdroiders.hustlehub.ui.features.report.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.ui.features.report.data.remote.ReportApiService
import must.kdroiders.hustlehub.ui.features.report.data.remote.dto.ReportResponse
import must.kdroiders.hustlehub.ui.features.report.data.remote.dto.SubmitReportRequest
import must.kdroiders.hustlehub.ui.features.report.domain.model.Report
import must.kdroiders.hustlehub.ui.features.report.domain.repository.ReportRepository
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val apiService: ReportApiService,
) : ReportRepository {

    override suspend fun submitReport(
        targetId: String,
        targetType: String,
        reason: String,
        description: String?,
    ): Result<Report> = withContext(Dispatchers.IO) {
        runCatching {
            val request = SubmitReportRequest(
                targetId = targetId,
                targetType = targetType,
                reason = reason,
                description = description,
            )
            val response = apiService.submitReport(request)
            check(response.success && response.data != null) { response.message ?: "Failed to submit report" }
            response.data.toDomain()
        }.recoverCatching { e ->
            if (e is CancellationException) throw e
            if (e is HttpException && (e.code() == 409 || e.code() == 400)) {
                throw Exception("You have already reported this item.")
            } else {
                Timber.w(e, "ReportRepositoryImpl.submitReport failed for targetId='$targetId'")
                throw e
            }
        }
    }
}

private fun ReportResponse.toDomain(): Report =
    Report(
        id = id,
        reporterId = reporterId,
        targetId = targetId,
        targetType = targetType,
        reason = reason,
        description = description,
        status = status,
        createdAt = createdAt,
    )
