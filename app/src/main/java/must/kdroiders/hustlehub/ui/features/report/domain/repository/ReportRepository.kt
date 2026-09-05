package must.kdroiders.hustlehub.ui.features.report.domain.repository

import must.kdroiders.hustlehub.ui.features.report.domain.model.Report

interface ReportRepository {
    suspend fun submitReport(
        targetId: String,
        targetType: String,
        reason: String,
        description: String?,
    ): Result<Report>
}
