package must.kdroiders.hustlehub.core.api

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
)

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
