package com.example.zanieczyszczeniepowietrza

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class NotificationService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val NOTIFICATION_DELAY = 30 * 1000L //2 * 60 * 60 * 1000L powiadomienie 2h od zalogowania się do aplikacji
    companion object {
        const val CHANNEL_ID = "delayed_notification_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scheduleNotification()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Opóźnione powiadomienia"
            val descriptionText = "Kanał dla powiadomień wysyłanych z opóźnieniem"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleNotification() {
        handler.postDelayed({
            showNotification()
        }, NOTIFICATION_DELAY)
    }

    private fun showNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("Zanieczyszczenie powietrza!")
            .setContentText("Minął już jakiś czas odkąd sprawdzałeś stan zanieczyszczenia powietrza! Sprawdź ponownie!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}