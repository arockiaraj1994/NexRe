package com.mindshift.nexre

import android.app.Application
import androidx.work.Configuration
import com.mindshift.nexre.worker.NexReWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NexReApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: NexReWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
