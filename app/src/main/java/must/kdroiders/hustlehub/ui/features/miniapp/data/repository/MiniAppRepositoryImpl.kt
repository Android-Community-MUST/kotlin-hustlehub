package must.kdroiders.hustlehub.ui.features.miniapp.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.ui.features.home.presentation.components.CampusMiniAppItem
import must.kdroiders.hustlehub.ui.features.home.presentation.components.defaultCampusMiniApps
import must.kdroiders.hustlehub.ui.features.miniapp.data.local.dao.MiniAppDao
import must.kdroiders.hustlehub.ui.features.miniapp.data.local.entity.MiniAppLocalEntity
import must.kdroiders.hustlehub.ui.features.miniapp.data.remote.MiniAppApiService
import must.kdroiders.hustlehub.ui.features.miniapp.domain.repository.MiniAppRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MiniAppRepositoryImpl
    @Inject
    constructor(
        private val miniAppDao: MiniAppDao,
        private val miniAppApiService: MiniAppApiService,
    ) : MiniAppRepository {
        override fun getActiveMiniAppsFlow(): Flow<List<CampusMiniAppItem>> {
            return miniAppDao.getActiveMiniApps().map { entities ->
                if (entities.isEmpty()) {
                    defaultCampusMiniApps
                } else {
                    entities.map { entity ->
                        CampusMiniAppItem(
                            appId = entity.appId,
                            title = entity.title,
                            description = entity.description,
                            url = entity.entryUrl,
                            iconUrl = entity.iconUrl,
                            coverPhotoUrl = entity.coverPhotoUrl,
                        )
                    }
                }
            }
        }

        override suspend fun refreshMiniApps(): Result<Unit> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = miniAppApiService.getActiveMiniApps()
                    if (response.success && response.data != null) {
                        val entities = response.data.map { dto ->
                            MiniAppLocalEntity(
                                appId = dto.appId,
                                title = dto.title,
                                description = dto.description,
                                entryUrl = dto.entryUrl,
                                iconUrl = dto.iconUrl,
                                coverPhotoUrl = dto.coverPhotoUrl,
                                category = dto.category,
                                status = dto.status,
                                createdAt = dto.createdAt,
                            )
                        }
                        miniAppDao.clearMiniApps()
                        miniAppDao.insertMiniApps(entities)
                    }
                }.onFailure { e ->
                    Timber.w(e, "Failed to refresh mini-apps from backend; continuing with cache")
                }
            }
    }
