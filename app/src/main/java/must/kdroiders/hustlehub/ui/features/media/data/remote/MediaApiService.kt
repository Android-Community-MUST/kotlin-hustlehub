package must.kdroiders.hustlehub.ui.features.media.data.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import must.kdroiders.hustlehub.core.api.ApiResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

@Keep
data class MediaUploadResponse(
    @SerializedName("mediaId")
    val mediaId: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String? = null,
    @SerializedName("type")
    val type: String,
    @SerializedName("durationSeconds")
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
