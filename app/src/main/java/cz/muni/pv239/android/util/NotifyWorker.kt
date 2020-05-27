package cz.muni.pv239.android.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import cz.muni.pv239.android.R
import cz.muni.pv239.android.ui.activities.EventDetailActivity
import cz.muni.pv239.android.ui.activities.MainActivity


class NotifyWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams){

    companion object{
        const val TAG = "NotifyWorker"
    }

    override fun doWork(): Result {
        triggerNotification()
        return Result.success()
    }


    private fun triggerNotification(){
        val eventId = inputData.getLong("arg_event_id", 0)
        val eventName = inputData.getString("arg_event_name")

        Log.i(TAG, "$eventId $eventName")

        val notifyIntent = Intent(applicationContext, EventDetailActivity::class.java)
        notifyIntent.putExtra(EventDetailActivity.EVENT_ID_ARG, eventId)

        val pendingIntent: PendingIntent? = applicationContext?.let {
            TaskStackBuilder.create(it).run {
                addNextIntentWithParentStack(notifyIntent)
                getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        val builder = applicationContext?.let {
            NotificationCompat.Builder(it, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Event reminder")
                .setContentText("\"$eventName\" starts in 30 minutes. Get Ready!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(longArrayOf(1000, 1000))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }

        if (builder != null) {
            with(applicationContext?.let { NotificationManagerCompat.from(it) }) {
                this?.notify(eventId.toInt(), builder.build())
            }
        }
    }

}