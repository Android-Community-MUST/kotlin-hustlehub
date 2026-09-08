package must.kdroiders.hustlehub.core.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T? = null,
)

@Keep
data class PageResponse<T>(
    @SerializedName("content")
    val content: List<T>,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("totalElements")
    val totalElements: Long,
    @SerializedName("totalPages")
    val totalPages: Int,
)
