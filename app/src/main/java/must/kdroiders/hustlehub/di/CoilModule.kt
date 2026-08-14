package must.kdroiders.hustlehub.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import must.kdroiders.hustlehub.BuildConfig
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoilModule {
    private const val DISK_CACHE_SIZE_BYTES = 100L * 1024L * 1024L
    private const val MEMORY_FRACTION = 0.25
    private const val CROSSFADE_DURATION_MS = 300

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
    ): ImageLoader {
        return ImageLoader
            .Builder(context)
            .memoryCache {
                MemoryCache
                    .Builder(context)
                    .maxSizePercent(MEMORY_FRACTION)
                    .build()
            }.diskCache {
                DiskCache
                    .Builder()
                    .directory(File(context.cacheDir, "coil"))
                    .maxSizeBytes(DISK_CACHE_SIZE_BYTES)
                    .build()
            }.crossfade(CROSSFADE_DURATION_MS)
            .okHttpClient(okHttpClient)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }.build()
    }
}
