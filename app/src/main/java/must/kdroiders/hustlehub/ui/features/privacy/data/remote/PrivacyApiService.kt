package must.kdroiders.hustlehub.ui.features.privacy.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto.PrivacySettingsDto
import must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto.UpdatePrivacySettingsRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface PrivacyApiService {
    @GET("users/me/privacy")
    suspend fun getPrivacySettings(): ApiResponse<PrivacySettingsDto>

    @PUT("users/me/privacy")
    suspend fun updatePrivacySettings(
        @Body request: UpdatePrivacySettingsRequestDto,
    ): ApiResponse<PrivacySettingsDto>
}
