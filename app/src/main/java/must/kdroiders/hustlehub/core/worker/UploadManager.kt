package must.kdroiders.hustlehub.core.worker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val workManager = WorkManager.getInstance(context)

        fun enqueueUpload(filePath: String): UUID {
            val constraints = Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val inputData = Data
                .Builder()
                .putString(MediaUploadWorker.KEY_FILE_PATH, filePath)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<MediaUploadWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10,
                    TimeUnit.SECONDS,
                ).build()

            workManager.enqueueUniqueWork(
                "upload_${filePath.hashCode()}",
                ExistingWorkPolicy.KEEP,
                workRequest,
            )

            return workRequest.id
        }

        fun getWorkInfoLiveData(workId: UUID): LiveData<WorkInfo?> {
            return workManager.getWorkInfoByIdLiveData(workId)
        }
    }
