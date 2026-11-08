package must.kdroiders.hustlehub.ui.features.miniapp.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MiniAppRemoteDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("appId")
    val appId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("entryUrl")
    val entryUrl: String,
    @SerializedName("iconUrl")
    val iconUrl: String? = null,
    @SerializedName("coverPhotoUrl")
    val coverPhotoUrl: String? = null,
    @SerializedName("category")
    val category: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("developerId")
    val developerId: String,
    @SerializedName("adminNotes")
    val adminNotes: String? = null,
    @SerializedName("createdAt")
    val createdAt: String,
)
