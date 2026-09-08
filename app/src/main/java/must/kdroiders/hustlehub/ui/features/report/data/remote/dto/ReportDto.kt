package must.kdroiders.hustlehub.ui.features.report.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SubmitReportRequest(
    @SerializedName("targetId")
    val targetId: String,
    @SerializedName("targetType")
    val targetType: String, // "user" or "service" or "message"
    @SerializedName("reason")
    val reason: String,
    @SerializedName("description")
    val description: String?,
)

@Keep
data class ReportResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("reporterId")
    val reporterId: String,
    @SerializedName("targetId")
    val targetId: String,
    @SerializedName("targetType")
    val targetType: String,
    @SerializedName("reason")
    val reason: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("status")
    val status: String,
    @SerializedName("createdAt")
    val createdAt: Long,
)
