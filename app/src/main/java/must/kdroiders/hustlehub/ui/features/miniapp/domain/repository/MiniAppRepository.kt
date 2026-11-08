package must.kdroiders.hustlehub.ui.features.miniapp.domain.repository

import kotlinx.coroutines.flow.Flow
import must.kdroiders.hustlehub.ui.features.home.presentation.components.CampusMiniAppItem

interface MiniAppRepository {
    fun getActiveMiniAppsFlow(): Flow<List<CampusMiniAppItem>>

    suspend fun refreshMiniApps(): Result<Unit>
}
