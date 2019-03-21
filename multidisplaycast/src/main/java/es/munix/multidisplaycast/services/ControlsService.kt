package es.munix.multidisplaycast.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import es.munix.multidisplaycast.CastManager
import es.munix.multidisplaycast.R
import es.munix.multidisplaycast.helpers.NotificationsHelper

class ControlsService : Service() {

    private val ID = 6158
    private var isPause = false
    private var title = ""
    private var subtitle = ""
    private var image: Bitmap? = null
    private val whenLong = System.currentTimeMillis()


    override fun onCreate() {
        startForeground(ID, notification)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.hasExtra("image") == true) {
            val array = intent.getByteArrayExtra("image")
            image = BitmapFactory.decodeByteArray(array, 0, array.size)
        }
        intent?.getStringExtra("title")?.let { title = it }
        intent?.getStringExtra("subtitle")?.let { subtitle = it }
        updateNotification()
        when (intent?.getStringExtra("action")) {
            "cancel" -> stopSelf()
            "togglePause" -> updateNotification(intent.getBooleanExtra("isPaused", false))
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun updateNotification(isPaused: Boolean = isPause) {
        isPause = isPaused
        (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.notify(ID, notification)
    }

    private val notification: Notification
        get() {
            val notification = NotificationCompat.Builder(this, NotificationsHelper.CHANNEL).apply {
                setContentTitle(title)
                setContentText(subtitle)
                setOngoing(true)
                setAutoCancel(false)
                setWhen(whenLong)
                setSmallIcon(R.drawable.cast_mc_on)
                if (image != null)
                    setLargeIcon(image)
                setStyle(MediaStyle())
                val castActivityIntent = Intent(this@ControlsService, CastManager.getInstance().controlsClass)
                val castActivityPendingIntent = PendingIntent.getActivity(this@ControlsService, ID + 1, castActivityIntent, 0)
                setContentIntent(castActivityPendingIntent)
                val disconnectIntent = Intent(this@ControlsService, CastReceiver::class.java)
                disconnectIntent.putExtra("action", "disconnect")
                addAction(R.drawable.ic_mc_stop, "Detener", PendingIntent.getBroadcast(this@ControlsService, ID + 2, disconnectIntent, 0))
                val pauseIntent = Intent(this@ControlsService, CastReceiver::class.java)
                pauseIntent.putExtra("action", "pause")
                val pausePendingIntent = PendingIntent.getBroadcast(this@ControlsService, ID + 3, pauseIntent, 0)
                if (!isPause)
                    addAction(R.drawable.ic_mc_pause, "Pausar", pausePendingIntent)
                else
                    addAction(R.drawable.ic_mc_play, "Reanudar", pausePendingIntent)
            }
            return notification.build()
        }
}