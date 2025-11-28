package com.rideincab.user.taxi.views.firebaseChat

import android.app.Service
import android.content.Intent
import android.os.IBinder

import com.rideincab.user.common.pushnotification.NotificationUtils
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.common.utils.CommonMethods

class FirebaseChatNotificationService : Service(), FirebaseChatHandler.FirebaseChatHandlerInterface {
    
    private lateinit var firebaseChatHandler: FirebaseChatHandler
    private lateinit var notificationUtils: NotificationUtils

    override fun onBind(intent: Intent?): IBinder? {
        // TODO: Return the communication channel to the service.
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // initializing firebaseChatHandler
        /*try {
            firebaseChatHandler.unRegister()
        } catch (e: Exception) {
            e.printStackTrace()
        }*/

        //firebaseChatHandler = FirebaseChatHandler(this, CommonKeys.FirebaseChatserviceTriggeredFrom.backgroundService)
        notificationUtils = NotificationUtils(this)
        CommonMethods.DebuggableLogE("service started", "started")


    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseChatHandler.unRegister()
    }

    override fun pushMessage(firebaseChatModelClass: FirebaseChatModelClass?) {
        CommonMethods.DebuggableLogE("service started", "message received & " + firebaseChatModelClass?.type)
        if (firebaseChatModelClass?.type != null && firebaseChatModelClass.type == CommonKeys.FIREBASE_CHAT_TYPE_DRIVER) {
            //notificationUtils.generateFirebaseChatNotification(this, firebaseChatModelClass.message)
        }
    }
}