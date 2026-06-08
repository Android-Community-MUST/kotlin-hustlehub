package must.kdroiders.hustlehub.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class MediaUploadResponse(
    val mediaId: String,
    val url: String,
    val thumbnailUrl: String?,
    val type: String,
)

interface MediaApiService {
    @Multipart
    @POST("media/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody,
        @Part("entityId") entityId: RequestBody? = null,
    ): ApiResponse<MediaUploadResponse>
}
