package must.kdroiders.hustlehub.ui.features.privacy.domain.repository

import must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto.PrivacySettingsDto
import must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto.UpdatePrivacySettingsRequestDto

interface PrivacyRepository {
    suspend fun getPrivacySettings(): Result<PrivacySettingsDto>
    suspend fun updatePrivacySettings(request: UpdatePrivacySettingsRequestDto): Result<PrivacySettingsDto>
}
