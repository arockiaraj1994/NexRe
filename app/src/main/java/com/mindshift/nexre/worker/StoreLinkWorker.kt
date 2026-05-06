package com.mindshift.nexre.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindshift.nexre.domain.usecase.SaveLinkUseCase
import com.mindshift.nexre.domain.usecase.SaveTextUseCase

class StoreLinkWorker(
    context: Context,
    params: WorkerParameters,
    private val saveLinkUseCase: SaveLinkUseCase,
    private val saveTextUseCase: SaveTextUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL)
        val text = inputData.getString(KEY_TEXT)
        return when {
            url != null -> handleUrl(url)
            text != null -> handleText(text)
            else -> Result.failure()
        }
    }

    private suspend fun handleUrl(url: String): Result = try {
        val link = saveLinkUseCase(url)
        showSaveNotification(link.title)
        Result.success()
    } catch (e: Exception) {
        if (runAttemptCount < 2) Result.retry() else Result.failure()
    }

    private suspend fun handleText(text: String): Result = try {
        val link = saveTextUseCase(text)
        showSaveNotification(link.title)
        Result.success()
    } catch (e: Exception) {
        Result.failure()
    }

    private fun showSaveNotification(title: String) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "Save Confirmation", NotificationManager.IMPORTANCE_LOW)
        nm.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("Saved")
            .setContentText(title.take(60))
            .setAutoCancel(true)
            .build()
        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val KEY_URL = "url"
        const val KEY_TEXT = "text"
        private const val CHANNEL_ID = "nexre_save"
    }
}
