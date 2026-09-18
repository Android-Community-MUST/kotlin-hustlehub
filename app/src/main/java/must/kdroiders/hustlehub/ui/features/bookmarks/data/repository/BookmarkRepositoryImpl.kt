package must.kdroiders.hustlehub.ui.features.bookmarks.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.ui.features.bookmarks.BookmarkItem
import must.kdroiders.hustlehub.ui.features.bookmarks.data.local.dao.BookmarkDao
import must.kdroiders.hustlehub.ui.features.bookmarks.data.local.entity.BookmarkEntity
import must.kdroiders.hustlehub.ui.features.bookmarks.data.remote.BookmarkApiService
import must.kdroiders.hustlehub.ui.features.bookmarks.domain.repository.BookmarkRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl
    @Inject
    constructor(
        private val bookmarkDao: BookmarkDao,
        private val bookmarkApiService: BookmarkApiService,
    ) : BookmarkRepository {
        override fun getBookmarksFlow(): Flow<List<BookmarkItem>> {
            return bookmarkDao.getAllBookmarksFlow().map { entities ->
                entities.map { entity ->
                    BookmarkItem(
                        id = entity.serviceId,
                        title = entity.title,
                        category = entity.category,
                        price = entity.priceRange,
                        rating = entity.rating,
                    )
                }
            }
        }

        override fun isBookmarkedFlow(serviceId: String): Flow<Boolean> {
            return bookmarkDao.isBookmarkedFlow(serviceId)
        }

        override suspend fun refreshBookmarks(): Result<Unit> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = bookmarkApiService.getBookmarks()
                    if (response.success && response.data != null) {
                        val entities = response.data.map { serviceDto ->
                            BookmarkEntity(
                                serviceId = serviceDto.serviceId,
                                title = serviceDto.title,
                                category = serviceDto.category,
                                priceRange = serviceDto.priceRange,
                                rating = serviceDto.avgRating,
                                imageUrl = serviceDto.portfolioImages?.firstOrNull(),
                                bookmarkedAt = System.currentTimeMillis(),
                            )
                        }
                        bookmarkDao.clearAll()
                        bookmarkDao.upsertAll(entities)
                    }
                }.onFailure { e ->
                    Timber.w(e, "Failed to refresh bookmarks from server")
                }
            }

        override suspend fun toggleBookmark(
            serviceId: String,
            title: String,
            category: String,
            priceRange: String,
            rating: Double,
            imageUrl: String?,
        ): Result<Boolean> =
            withContext(Dispatchers.IO) {
                val wasBookmarked = bookmarkDao.isBookmarked(serviceId)
                val entity = BookmarkEntity(
                    serviceId = serviceId,
                    title = title,
                    category = category,
                    priceRange = priceRange,
                    rating = rating,
                    imageUrl = imageUrl,
                )

                if (wasBookmarked) {
                    // Optimistic delete
                    bookmarkDao.deleteByServiceId(serviceId)
                    runCatching {
                        bookmarkApiService.removeBookmark(serviceId)
                        false
                    }.onFailure { e ->
                        Timber.e(e, "Failed to remove bookmark remotely, rolling back")
                        bookmarkDao.upsert(entity)
                    }
                } else {
                    // Optimistic insert
                    bookmarkDao.upsert(entity)
                    runCatching {
                        bookmarkApiService.addBookmark(serviceId)
                        true
                    }.onFailure { e ->
                        Timber.e(e, "Failed to add bookmark remotely, rolling back")
                        bookmarkDao.deleteByServiceId(serviceId)
                    }
                }
            }

        override suspend fun removeBookmark(serviceId: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                val existing = bookmarkDao.getAllBookmarks().find { it.serviceId == serviceId }
                bookmarkDao.deleteByServiceId(serviceId)
                runCatching {
                    bookmarkApiService.removeBookmark(serviceId)
                    Unit
                }.onFailure { e ->
                    Timber.e(e, "Failed to remove bookmark remotely, rolling back")
                    if (existing != null) {
                        bookmarkDao.upsert(existing)
                    }
                }
            }
    }
