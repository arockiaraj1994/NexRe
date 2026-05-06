package com.mindshift.nexre.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindshift.nexre.domain.model.LinkStatus
import com.mindshift.nexre.domain.repository.LinkRepository
import com.mindshift.nexre.domain.repository.TagRepository
import kotlinx.coroutines.flow.first

class WeeklyDigestWorker(
    context: Context,
    params: WorkerParameters,
    private val linkRepository: LinkRepository,
    private val tagRepository: TagRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val since = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        val allLinks = linkRepository.getAllLinks().first()
        val savedThisWeek = allLinks.count { it.savedAt >= since }
        val readThisWeek = allLinks.count { it.status == LinkStatus.READ && it.openedAt >= since }
        val topTag = tagRepository.getTagsWithCounts().first().firstOrNull()?.name ?: "General"

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(DailyReminderWorker.CHANNEL_ID, "Reading Reminders", NotificationManager.IMPORTANCE_DEFAULT)
        nm.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, DailyReminderWorker.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("Your week in NexRe")
            .setContentText("$savedThisWeek saved, $readThisWeek read. Top tag: $topTag")
            .setAutoCancel(true)
            .build()

        nm.notify(1002, notification)
        return Result.success()
    }
}
