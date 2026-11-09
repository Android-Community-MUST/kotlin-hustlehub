package must.kdroiders.hustlehub.ui.features.miniapp.domain.repository

import kotlinx.coroutines.flow.Flow
import must.kdroiders.hustlehub.ui.features.home.presentation.components.CampusMiniAppItem
import must.kdroiders.hustlehub.ui.features.miniapp.data.remote.dto.MiniAppRemoteDto
import must.kdroiders.hustlehub.ui.features.miniapp.data.remote.dto.UpdateMiniAppRequestDto

interface MiniAppRepository {
    fun getActiveMiniAppsFlow(): Flow<List<CampusMiniAppItem>>

    suspend fun refreshMiniApps(): Result<Unit>

    suspend fun getMyMiniApps(): Result<List<MiniAppRemoteDto>>

    suspend fun updateMiniApp(
        id: String,
        request: UpdateMiniAppRequestDto,
    ): Result<MiniAppRemoteDto>

    suspend fun deleteMiniApp(id: String): Result<Unit>
}
