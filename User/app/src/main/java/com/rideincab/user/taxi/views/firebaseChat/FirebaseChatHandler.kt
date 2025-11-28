package com.rideincab.user.taxi.views.firebaseChat

import android.text.TextUtils
import com.google.firebase.database.*
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.taxi.firebase_keys.FirebaseDbKeys
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.network.AppController.Companion.getContext
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.common.utils.CommonMethods
import java.util.*
import javax.inject.Inject
import com.rideincab.user.R
class FirebaseChatHandler(private val callbackListener: FirebaseChatHandlerInterface, @param:CommonKeys.FirebaseChatserviceTriggeredFrom @field:CommonKeys.FirebaseChatserviceTriggeredFrom

private var firebaseChatserviceTriggeredFrom: Int) {


    @Inject
    lateinit var sessionManager: SessionManager

    private val root: DatabaseReference

    private var isChatTriggerable: Boolean? = false


    private val childEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            if (firebaseChatserviceTriggeredFrom == CommonKeys.FirebaseChatserviceTriggeredFrom.backgroundService) {
                CommonMethods.DebuggableLogI("isChatTriggerableState", isChatTriggerable!!.toString())

                if (isChatTriggerable!!) {
                    callbackListener.pushMessage(dataSnapshot.getValue(FirebaseChatModelClass::class.java))
                }
            } else {
                callbackListener.pushMessage(dataSnapshot.getValue(FirebaseChatModelClass::class.java))
            }

        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onCancelled(databaseError: DatabaseError) {}
    }

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            isChatTriggerable = true
        }

        override fun onCancelled(databaseError: DatabaseError) {}
    }

    init {
        AppController.appComponent.inject(this)
        root = FirebaseDatabase.getInstance().reference.child(getContext().getString(R.string.real_time_db)).child(FirebaseDbKeys.chatFirebaseDatabaseName).child(sessionManager.tripId!!)

        if (firebaseChatserviceTriggeredFrom == CommonKeys.FirebaseChatserviceTriggeredFrom.backgroundService) {
            root.addListenerForSingleValueEvent(valueEventListener)
            root.addChildEventListener(childEventListener)
        } else if (firebaseChatserviceTriggeredFrom == CommonKeys.FirebaseChatserviceTriggeredFrom.chatActivity) {
            root.addChildEventListener(childEventListener)
        }
    }

    internal fun addMessage(message: String) {
        try {
            if (!TextUtils.isEmpty(message)) {
                val map = HashMap<String, Any>()
                val tempKey = root.push().key
                root.updateChildren(map)

                val messageRoot = root.child(tempKey!!)
                val map2 = HashMap<String, Any>()
                map2[CommonKeys.FIREBASE_CHAT_MESSAGE_KEY] = message
                map2[CommonKeys.FIREBASE_CHAT_TYPE_KEY] = CommonKeys.FIREBASE_CHAT_TYPE_RIDER

                messageRoot.updateChildren(map2)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    internal fun unRegister() {
        root.removeEventListener(childEventListener)
        firebaseChatserviceTriggeredFrom = CommonKeys.FirebaseChatserviceTriggeredFrom.chatActivity
        isChatTriggerable = false
    }

    interface FirebaseChatHandlerInterface {
        fun pushMessage(firebaseChatModelClass: FirebaseChatModelClass?)
    }
}