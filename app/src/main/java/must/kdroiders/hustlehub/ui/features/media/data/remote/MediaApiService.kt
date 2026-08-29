package must.kdroiders.hustlehub.ui.features.media.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class MediaUploadResponse(
    val mediaId: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val type: String,
    val durationSeconds: Int? = null,
)

interface MediaApiService {
    @Multipart
    @POST("media/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part type: MultipartBody.Part,
        @Part entityId: MultipartBody.Part,
    ): ApiResponse<MediaUploadResponse>

    @Multipart
    @POST("media/upload/voice")
    suspend fun uploadVoiceNote(
        @Part file: MultipartBody.Part,
        @Part conversationId: MultipartBody.Part,
    ): ApiResponse<MediaUploadResponse>
}
