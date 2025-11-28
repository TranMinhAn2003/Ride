package com.rideincab.user.taxi.views.firebaseChat

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.helper.Constants.chatPageVisible
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.pushnotification.NotificationUtils
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityChatBinding
import com.rideincab.user.taxi.views.customize.CustomDialog
import javax.inject.Inject


class ActivityChat : CommonActivity(), FirebaseChatHandler.FirebaseChatHandlerInterface, ServiceListener {
    
    private lateinit var binding:AppActivityChatBinding
    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

   /* @BindView(R.id.edt_new_msg)
    lateinit var binding.edtNewMsg: EditText

    @BindView(R.id.rv_chat)
    lateinit var binding.rvChat: RecyclerView

    @BindView(R.id.tv_profile_rating)
    lateinit var driverRating: TextView

    @BindView(R.id.tv_profile_name)
    lateinit var driverName: TextView

    @BindView(R.id.imgvu_driver_profile)
    lateinit var driverProfilePicture: ImageView

    @BindView(R.id.imgvu_emptychat)
    lateinit var noChats: ImageView*/

    /*   @BindView(R.id.chat_layout)
       lateinit var chatLayout: RelativeLayout*/


    private lateinit var adapterFirebaseRecylcerview: AdapterFirebaseRecylcerview
    private lateinit var firebaseChatHandler: FirebaseChatHandler
    private var sourceActivityCode: Int = 0



    override fun onStart() {
        super.onStart()
        chatPageVisible = true
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (jsonResp.isSuccess) {
            //commonMethods.hideProgressDialog()
            //onSuccessChat(jsonResp)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            //commonMethods.hideProgressDialog()
            //commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }


    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            //commonMethods.hideProgressDialog()
            //commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        AppController.appComponent.inject(this)
        binding.imgvuBack.setOnClickListener { super.onBackPressed() }

        binding.ivSend.setOnClickListener { sendMessage() }

    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseChatHandler.unRegister()
    }

    override fun onPause() {
        chatPageVisible = false
        firebaseChatHandler.unRegister()
        /*if (!TextUtils.isEmpty(sessionManager.tripId) && sessionManager.isDriverAndRiderAbleToChat) {
            startFirebaseChatListenerService(this)
        }*/

        super.onPause()
    }

    private fun updateDriverProfileOnHeader() {
        val driverProfilePic = sessionManager.driverProfilePic
        val driverName = sessionManager.driverName
        val driverRating = sessionManager.driverRating
        if (!TextUtils.isEmpty(driverProfilePic)) {
            Picasso.get().load(driverProfilePic).error(R.drawable.car)
                    .into(binding.imgvuDriverProfile)
        }

        if (!TextUtils.isEmpty(driverName)) {
            binding.tvProfileName.text = driverName
        } else {
            binding.tvProfileName.text = resources.getString(R.string.driver)
        }


        try {
            if (!driverRating.isNullOrEmpty() && driverRating.toFloat() > 0) {
                binding.tvProfileRating.visibility = View.VISIBLE
                binding.tvProfileRating.text = driverRating
            } else {
                binding.tvProfileRating.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvProfileRating.visibility = View.GONE
        }
    }

    override fun pushMessage(firebaseChatModelClass: FirebaseChatModelClass?) {
        adapterFirebaseRecylcerview.updateChat(firebaseChatModelClass!!)
        binding.rvChat.scrollToPosition(adapterFirebaseRecylcerview.itemCount - 1)
        binding.rvChat.visibility = View.VISIBLE
        binding.imgvuEmptychat.visibility = View.GONE
    }

    fun sendMessage() {
        if (commonMethods.isOnline(this)) {
            apiService.updateChat(getChatParams()).enqueue(RequestCallback(this))
            firebaseChatHandler.addMessage(binding.edtNewMsg.text.toString().trim { it <= ' ' })
            binding.edtNewMsg.text?.clear()
        } else {
            Toast.makeText(this, resources.getString(R.string.network_failure), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getChatParams(): HashMap<String, String> {

        val chatHashMap: java.util.HashMap<String, String> = HashMap()
        chatHashMap["message"] = binding.edtNewMsg.text.toString()
        chatHashMap["trip_id"] = sessionManager.tripId.toString()
        chatHashMap["receiver_id"] = sessionManager.driverId.toString()
        chatHashMap["token"] = sessionManager.accessToken.toString()
        return chatHashMap

    }

    override fun onResume() {
        super.onResume()
        //stopFirebaseChatListenerService(this)
        NotificationUtils.clearNotifications(this)

        sourceActivityCode = intent.getIntExtra(CommonKeys.FIREBASE_CHAT_ACTIVITY_SOURCE_ACTIVITY_TYPE_CODE, CommonKeys.FIREBASE_CHAT_ACTIVITY_REDIRECTED_FROM_NOTIFICATION)

        updateDriverProfileOnHeader()
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        adapterFirebaseRecylcerview = AdapterFirebaseRecylcerview(this)
        binding.rvChat.adapter = adapterFirebaseRecylcerview
        firebaseChatHandler = FirebaseChatHandler(this, CommonKeys.FirebaseChatserviceTriggeredFrom.chatActivity)
        binding.rvChat.visibility = View.GONE
        binding.imgvuEmptychat.visibility = View.VISIBLE
        binding.rvChat.addOnLayoutChangeListener(View.OnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                binding.rvChat.postDelayed(Runnable { binding.rvChat.scrollToPosition(adapterFirebaseRecylcerview.itemCount - 1) }, 100)
            }
        })
    }
}