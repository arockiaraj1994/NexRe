package com.mindshift.nexre.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindshift.nexre.MainActivity
import com.mindshift.nexre.domain.model.LinkStatus
import com.mindshift.nexre.domain.repository.LinkRepository
import kotlinx.coroutines.flow.first

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val linkRepository: LinkRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val unreadCount = linkRepository.getLinksByStatus(LinkStatus.UNREAD).first().size
        if (unreadCount == 0) return Result.success()

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "Reading Reminders", NotificationManager.IMPORTANCE_DEFAULT)
        nm.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pi = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("NexRe")
            .setContentText("You have $unreadCount unread link${if (unreadCount != 1) "s" else ""}")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "nexre_reminders"
        const val NOTIFICATION_ID = 1001
    }
}
