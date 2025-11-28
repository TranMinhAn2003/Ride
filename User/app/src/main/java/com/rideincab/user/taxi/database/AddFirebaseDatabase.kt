package com.rideincab.user.taxi.database

import android.content.Context
import android.text.TextUtils
import com.google.firebase.database.*
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.network.AppController
import com.rideincab.user.taxi.firebase_keys.FirebaseDbKeys
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogE
import org.json.JSONObject
import javax.inject.Inject
import com.rideincab.user.R
class AddFirebaseDatabase {
    @Inject
    internal lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    private var mFirebaseDatabase: DatabaseReference? = null
    private var mSearchedDriverReferenceListener: ValueEventListener? = null
    private var query: Query? = null
    private val TAG = "Android_Debug"
    private var firebaseReqListener: IFirebaseReqListener? = null

    init {
        AppController.appComponent.inject(this)
    }

    fun createRequestTable(firebaseReqListener: IFirebaseReqListener,context : Context) {
        this.firebaseReqListener = firebaseReqListener
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
        mFirebaseDatabase!!.child(FirebaseDbKeys.Rider).child(sessionManager.userId!!).child(FirebaseDbKeys.TripId).setValue("0")
        query = mFirebaseDatabase!!.child(sessionManager.userId!!)
        addRequestChangeListener()
    }

    fun removeRequestTable() {
        try {
            mFirebaseDatabase!!.database.getReference(FirebaseDbKeys.Rider).child(sessionManager.userId!!).removeValue()
            query!!.removeEventListener(mSearchedDriverReferenceListener!!)
            mFirebaseDatabase!!.removeEventListener(mSearchedDriverReferenceListener!!)
            mSearchedDriverReferenceListener = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeNodesAfterCompletedTrip(context : Context) {
        try {
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
            mFirebaseDatabase!!.child(FirebaseDbKeys.TRIP_PAYMENT_NODE).child(sessionManager.tripId!!).removeValue()
            query!!.removeEventListener(mSearchedDriverReferenceListener!!)
            mFirebaseDatabase!!.removeEventListener(mSearchedDriverReferenceListener!!)
            mSearchedDriverReferenceListener = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    fun firebasePushLisener(context : Context){
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
        pushNotifQuery = mFirebaseDatabase!!.child(FirebaseDbKeys.Notification).child(sessionManager.userId!!)

        firebaseDbPush = pushNotifQuery!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                println("JSON FROM DB : ")

                if (dataSnapshot.key.equals(sessionManager.userId!!)) {
                    val pushJson = dataSnapshot.getValue(String::class.java)
                    println("JSON FROM DB : "+pushJson)
                    if (!TextUtils.isEmpty(pushJson)) {
                        val json = JSONObject(pushJson)
                        commonMethods.handleDataMessage(json,context)

                        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
                        mFirebaseDatabase!!.child(FirebaseDbKeys.Notification).child(sessionManager.userId!!).removeValue()

                    }
                } else {
                    pushNotifQuery!!.removeEventListener(this)
                    mFirebaseDatabase!!.removeEventListener(this)
                    mFirebaseDatabase!!.onDisconnect()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                DebuggableLogE(TAG, "Failed to read user", error.toException())
            }
        })

    }

    private fun addRequestChangeListener() {
        mSearchedDriverReferenceListener = query!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild(FirebaseDbKeys.TripId)) {
                    val id = dataSnapshot.child(FirebaseDbKeys.TripId).getValue(String::class.java)
                    println("get Trip id " + id!!)
                    if (!TextUtils.isEmpty(id) && !id.equals("0", ignoreCase = true)) {
                        if (!id.equals(sessionManager.tripId, ignoreCase = true)) {
                            sessionManager.tripId = id
                            firebaseReqListener!!.RequestListener(id)
                        }
                    }
                } else {
                    query!!.removeEventListener(this)
                    mFirebaseDatabase!!.removeEventListener(this)
                    mFirebaseDatabase!!.onDisconnect()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                DebuggableLogE(TAG, "Failed to read user", error.toException())
            }
        })
    }

    fun updatePaymentChangedNode(isWalletAvail: String, paymentType: String,context: Context) {
        var value = FirebaseDbKeys.PaymentChangeMode.TYPE_PAYPAL

        when (paymentType) {
            CommonKeys.PAYMENT_PAYPAL -> {
                value = FirebaseDbKeys.PaymentChangeMode.TYPE_PAYPAL
            }
            CommonKeys.PAYMENT_CARD -> {
                value = FirebaseDbKeys.PaymentChangeMode.TYPE_STRIPE
            }
            CommonKeys.PAYMENT_CASH -> {
                value = FirebaseDbKeys.PaymentChangeMode.TYPE_CASH
            }
            CommonKeys.PAYMENT_BRAINTREE -> {
                value = FirebaseDbKeys.PaymentChangeMode.TYPE_BRAINTREE
            }
        }

        if (isWalletAvail.equals("yes", ignoreCase = true)) {
            value = value + FirebaseDbKeys.PaymentChangeMode.WALLET_ADDED
        }
        if (sessionManager.promoCount > 0) {
            value = value + FirebaseDbKeys.PaymentChangeMode.PROMO_ADDED
        }
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
        mFirebaseDatabase!!.child(FirebaseDbKeys.TRIP_PAYMENT_NODE).child(sessionManager.tripId!!).child(FirebaseDbKeys.TRIP_PAYMENT_NODE_REFRESH_PAYMENT_TYPE_KEY).setValue(value)
    }

    companion object{
        private var firebaseDbPush: ValueEventListener? = null
        private var query: Query? = null
        private var pushNotifQuery: Query? = null
    }

}
