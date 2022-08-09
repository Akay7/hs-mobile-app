package org.hyperskill.app.android.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import com.russhwolf.settings.Settings
import org.hyperskill.app.android.core.extensions.DateTimeHelper
import org.hyperskill.app.android.notification.receiver.AlarmReceiver
import ru.nobird.android.view.base.ui.extension.scheduleCompat

class HyperskillNotificationManagerImpl(
    private val context: Context,
    private val settings: Settings
) : HyperskillNotificationManager {

    private val alarmManager: AlarmManager by lazy { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    private val notificationManager: NotificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun scheduleNotification(id: String, millis: Long) {
        val intent = AlarmReceiver
            .createIntent(context, id)

        val pendingIntent = PendingIntent
            .getBroadcast(context, AlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        alarmManager.cancel(pendingIntent)
        // TODO change interval from 0 to FIFTEEN_MINUTES
        alarmManager.scheduleCompat(millis, 0, pendingIntent)

        settings.putLong(id, millis)
    }

    override fun rescheduleActiveNotification(id: String) {
        val millis = settings.getLong(id)
        if (millis > 0L && millis > DateTimeHelper.nowUtc()) {
            scheduleNotification(id, millis)
        }
    }

    override fun showNotification(id: Long, notification: Notification) {
        notificationManager.notify(id.toInt(), notification)
    }
}