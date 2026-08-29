package com.marlena.martins.sellcieapplication.data.payment

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.marlena.martins.sellcieapplication.R

class PaymentForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL, "Pagamento", NotificationManager.IMPORTANCE_LOW))
        }
        startForeground(NOTIFICATION_ID, NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Pagamento em andamento")
            .setContentText("Aguardando a confirmação da maquininha.")
            .setOngoing(true)
            .build())
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL = "payment"
        private const val NOTIFICATION_ID = 7001
        fun start(context: Context) {
            val intent = Intent(context, PaymentForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
            else context.startService(intent)
        }
        fun stop(context: Context) = context.stopService(Intent(context, PaymentForegroundService::class.java))
    }
}
