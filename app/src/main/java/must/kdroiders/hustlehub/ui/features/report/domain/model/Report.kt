package must.kdroiders.hustlehub.ui.features.report.domain.model

data class Report(
    val id: String,
    val reporterId: String,
    val targetId: String,
    val targetType: String,
    val reason: String,
    val description: String?,
    val status: String,
    val createdAt: Long,
)
