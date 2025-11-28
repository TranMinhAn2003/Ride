package com.rideincab.user.common.pushnotification

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage pushnotification
 * @category NotificationUtils
 * @author SMR IT Solutions
 *
 */

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.util.Patterns
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.rideincab.user.R
import com.rideincab.user.common.helper.Constants
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.taxi.views.firebaseChat.ActivityChat
import com.rideincab.user.taxi.views.main.MainActivity
import java.io.IOException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection

/* ************************************************************
   NotificationUtils for generate and design notification
   *************************************************************** */
@Suppress("DEPRECATION")
class NotificationUtils(private val mContext: Context) {
    var defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    //private val notificationChannelName = mContext.resources.getString(R.string.app_name) + "RiderChat"
    private val notificationChannelName = "Chat"
    private val notificationChannelID = "125001"

    @JvmOverloads
    fun showNotificationMessage(title: String, message: String?, timeStamp: String, intent: Intent, imageUrl: String? = null) {
        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return


        // notification icon
        val icon = Constants.notificationIcon

        CommonMethods.DebuggableLogD("Icon", "Iconone")

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val resultPendingIntent = PendingIntent.getActivity(
                mContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        )

        val mBuilder = NotificationCompat.Builder(
                mContext, notificationChannelName)


        if (!TextUtils.isEmpty(imageUrl)) {

            if (imageUrl != null && imageUrl.length > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {

                val bitmap = getBitmapFromURL(imageUrl)

                if (bitmap != null) {
                    showBigNotification(bitmap, mBuilder, icon, title, message, timeStamp, resultPendingIntent)
                } else {
                    showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent)
                }
            }
        } else {
            showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent)
            //playNotificationSound();
        }
    }

    private fun showSmallNotification(mBuilder: NotificationCompat.Builder, icon: Int, title: String, message: String?, timeStamp: String, resultPendingIntent: PendingIntent) {

        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.addLine(message)

        val notificationManager = mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        /* mBuilder.setSmallIcon(icon);
        mBuilder.setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(R.drawable.ic_app_notification)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .setColor(ContextCompat.getColor(mContext, R.color.ub__black))
                .setContentText(message);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelID, notificationChannelName, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{1000,1000});
            assert notificationManager != null;
            mBuilder.setChannelId("10010");
            notificationManager.createNotificationChannel(notificationChannel);
        }



        assert notificationManager != null;
        notificationManager.notify(Config.NOTIFICATION_ID, mBuilder.build());*/

        //mBuilder = new NotificationCompat.Builder(mContext,notificationChannelName);
        mBuilder.setSmallIcon(icon)
        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX)
                .setSound(null)
                .setDefaults(0)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(resultPendingIntent).color = ContextCompat.getColor(mContext, R.color.app_primary_color)
        /*.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message));*/



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(notificationChannelID, notificationChannelName, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = ContextCompat.getColor(mContext, R.color.app_primary_color)
            notificationChannel.enableVibration(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationChannel.vibrationPattern = longArrayOf(1000, 1000)
            val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            //notificationChannel.setSound(defaultSoundUri, audioAttributes)
            notificationChannel.setSound(null, null)
            mBuilder.setChannelId(notificationChannelID)
            notificationManager.createNotificationChannel(notificationChannel)


        }
        notificationManager.notify(0 /* Request Code */, mBuilder.build())
        soundNotification()

    }

    private fun showBigNotification(bitmap: Bitmap, mBuilder: NotificationCompat.Builder, icon: Int, title: String, message: String?, timeStamp: String, resultPendingIntent: PendingIntent) {
        val bigPictureStyle = NotificationCompat.BigPictureStyle()
        bigPictureStyle.setBigContentTitle(title)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            bigPictureStyle.setSummaryText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY).toString())
        } else {
            bigPictureStyle.setSummaryText(Html.fromHtml(message).toString())
        }
        bigPictureStyle.bigPicture(bitmap)

        val notificationManager = mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(Constants.notificationIcon)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.resources, icon))
                .setContentText(message)


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(notificationChannelID, notificationChannelName, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = ContextCompat.getColor(mContext, R.color.app_primary_color)
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(1000, 1000)
            mBuilder.setChannelId(notificationChannelID)
            notificationManager.createNotificationChannel(notificationChannel)
        }


        notificationManager.notify(0 /* Request Code */, mBuilder.build())
    }

    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    fun getBitmapFromURL(strURL: String): Bitmap? {
        try {
            val url = URL(strURL)
            val connection = url.openConnection() as HttpsURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

    }

    // Playing notification sound
    fun playNotificationSound() {
        /*try {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            *//* Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + mContext.getPackageName() + "/raw/notification");*//*
            val r = RingtoneManager.getRingtone(mContext, alarmSound)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }*/

    }

    private fun soundNotification() {
        println("soundNotification @")
        try {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(mContext, alarmSound)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun generateNotification(context: Context, message: String, title: String) {


        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        //String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());

        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        showNotificationMessage(title, message, timeStamp, notificationIntent, null)


        CommonMethods.DebuggableLogD("Icon", "Iconfour")

    }

    fun generateFirebaseChatNotification(context: Context, message: String?) {

        val title = context.getString(R.string.app_name)

        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        //String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());

        val notificationIntent = Intent(context, ActivityChat::class.java)
        notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_ACTIVITY_SOURCE_ACTIVITY_TYPE_CODE, CommonKeys.FIREBASE_CHAT_ACTIVITY_REDIRECTED_FROM_NOTIFICATION)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        showNotificationMessage(title, message, timeStamp, notificationIntent, null)


        CommonMethods.DebuggableLogD("Icon", "Iconfour")

    }

    companion object {
        /**
         * Method checks if the app is in background or not
         */
        @SuppressLint("NewApi")
        fun isAppIsInBackground(context: Context): Boolean {
            var isInBackground = true
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                val runningProcesses = am.runningAppProcesses
                for (processInfo in runningProcesses) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (activeProcess in processInfo.pkgList) {
                            if (activeProcess == context.packageName) {
                                isInBackground = false
                            }
                        }
                    }
                }
            } else {
                val taskInfo = am.getRunningTasks(1)
                val componentInfo = taskInfo[0].topActivity
                if (componentInfo != null) {
                    if (componentInfo.packageName == context.packageName) {
                        isInBackground = false
                    }
                }
            }

            return isInBackground
        }

        // Clears notification tray messages
        fun clearNotifications(context: Context) {
            try {
                val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancelAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun getTimeMilliSec(timeStamp: String): Long {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            try {
                val date = format.parse(timeStamp)
                return date.time
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return 0
        }
    }


}
