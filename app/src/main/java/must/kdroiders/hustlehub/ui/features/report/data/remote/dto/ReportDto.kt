package must.kdroiders.hustlehub.ui.features.report.data.remote.dto

data class SubmitReportRequest(
    val targetId: String,
    val targetType: String, // "user" or "service" or "message"
    val reason: String,
    val description: String?,
)

data class ReportResponse(
    val id: String,
    val reporterId: String,
    val targetId: String,
    val targetType: String,
    val reason: String,
    val description: String?,
    val status: String,
    val createdAt: Long,
)
