package must.kdroiders.hustlehub.ui.features.privacy.data.repository

import must.kdroiders.hustlehub.ui.features.privacy.data.remote.PrivacyApiService
import must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto.PrivacySettingsDto
import must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto.UpdatePrivacySettingsRequestDto
import must.kdroiders.hustlehub.ui.features.privacy.domain.repository.PrivacyRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrivacyRepositoryImpl
    @Inject
    constructor(
        private val apiService: PrivacyApiService,
    ) : PrivacyRepository {
        override suspend fun getPrivacySettings(): Result<PrivacySettingsDto> {
            return try {
                val response = apiService.getPrivacySettings()
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching privacy settings")
                Result.failure(e)
            }
        }

        override suspend fun updatePrivacySettings(
            request: UpdatePrivacySettingsRequestDto,
        ): Result<PrivacySettingsDto> {
            return try {
                val response = apiService.updatePrivacySettings(request)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error updating privacy settings")
                Result.failure(e)
            }
        }
    }
