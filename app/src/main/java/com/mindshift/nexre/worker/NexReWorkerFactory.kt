package com.mindshift.nexre.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.mindshift.nexre.domain.repository.LinkRepository
import com.mindshift.nexre.domain.repository.TagRepository
import com.mindshift.nexre.domain.usecase.SaveLinkUseCase
import com.mindshift.nexre.domain.usecase.SaveTextUseCase
import javax.inject.Inject
import javax.inject.Provider

class NexReWorkerFactory @Inject constructor(
    private val saveLinkUseCase: Provider<SaveLinkUseCase>,
    private val saveTextUseCase: Provider<SaveTextUseCase>,
    private val linkRepository: Provider<LinkRepository>,
    private val tagRepository: Provider<TagRepository>,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        StoreLinkWorker::class.java.name ->
            StoreLinkWorker(appContext, workerParameters, saveLinkUseCase.get(), saveTextUseCase.get())
        DailyReminderWorker::class.java.name ->
            DailyReminderWorker(appContext, workerParameters, linkRepository.get())
        WeeklyDigestWorker::class.java.name ->
            WeeklyDigestWorker(appContext, workerParameters, linkRepository.get(), tagRepository.get())
        else -> null
    }
}
