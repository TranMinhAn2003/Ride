package com.rideincab.driver.home.firebaseChat

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
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.AppActivityChatBinding
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import com.rideincab.driver.home.pushnotification.NotificationUtils
import org.json.JSONObject
import javax.inject.Inject


class ActivityChat : CommonActivity(), FirebaseChatHandler.FirebaseChatHandlerInterface, ServiceListener {

    lateinit var binding:AppActivityChatBinding

    private var chatJson: String? = null

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var gson: Gson

    /*@BindView(R.id.imgvu_rider_profile)
    lateinit var riderProfileImageView: ImageView

    @BindView(R.id.tv_profile_name)
    lateinit var binding.tvProfileName: TextView

    @BindView(R.id.tv_profile_rating)
    lateinit var binding.tvProfileRating: TextView

    @BindView(R.id.edt_new_msg)
    lateinit var binding.newMessage: EditText

    @BindView(R.id.rv_chat)
    lateinit var binding.rvChat: RecyclerView


    @BindView(R.id.imgvu_emptychat)
    lateinit var noChats: ImageView*/

    lateinit internal var adapterFirebaseRecylcerview: AdapterFirebaseRecylcerview
    lateinit internal var firebaseChatHandler: FirebaseChatHandler
    internal var sourceActivityCode: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        
        AppController.getAppComponent().inject(this)

        binding.imgvuBack.setOnClickListener { onBackPressed() }
        binding.ivSend.setOnClickListener { sendMessage() }
    }

    private fun getIntentFromPush() {
        isOnChat = true
        chatJson = sessionManager.chatJson

        if (chatJson != null && !chatJson.equals("")) {
            val json = JSONObject(chatJson)
            sessionManager.chatJson = ""
            sessionManager.riderName = json.getJSONObject("custom").getJSONObject("chat_notification").getString("user_name")
            sessionManager.tripId = json.getJSONObject("custom").getJSONObject("chat_notification").getString("trip_id")
            sessionManager.riderProfilePic = json.getJSONObject("custom").getJSONObject("chat_notification").getString("user_image")
            sessionManager.riderId = json.getJSONObject("custom").getJSONObject("chat_notification").getString("user_id")
            sessionManager.riderRating = json.getJSONObject("custom").getJSONObject("chat_notification").getString("rating")
        }
    }

    override fun pushMessage(firebaseChatModelClass: FirebaseChatModelClass?) {
        firebaseChatModelClass?.let { adapterFirebaseRecylcerview.updateChat(it) }
        binding.rvChat.scrollToPosition(adapterFirebaseRecylcerview.itemCount - 1)
        binding.rvChat.visibility = View.VISIBLE
        binding.imgvuEmptychat.visibility = View.GONE
    }

    fun sendMessage() {
        if (commonMethods.isOnline(this)) {
            firebaseChatHandler.addMessage(binding.edtNewMsg.text.toString().trim { it <= ' ' })
            apiService.updateChat(getChatParams()).enqueue(RequestCallback(this))
            binding.edtNewMsg.text.clear()
        } else {
            Toast.makeText(this, resources.getString(R.string.network_failure), Toast.LENGTH_SHORT).show()
        }

    }

    private fun getChatParams(): HashMap<String, String> {

        val chatHashMap: java.util.HashMap<String, String> = HashMap()
        chatHashMap["message"] = binding.edtNewMsg.text.toString()
        chatHashMap["trip_id"] = sessionManager.tripId.toString()
        chatHashMap["receiver_id"] = sessionManager.riderId
        chatHashMap["token"] = sessionManager.accessToken!!
        return chatHashMap

    }

    override fun onDestroy() {
        super.onDestroy()
        isOnChat = false
        firebaseChatHandler.unRegister()
    }

    override fun onBackPressed() {
        /*if (sourceActivityCode == CommonKeys.FIREBASE_CHAT_ACTIVITY_REDIRECTED_FROM_RIDER_OR_DRIVER_PROFILE) {
            super.onBackPressed();
        } else {
            CommonMethods.gotoMainActivityFromChatActivity(this);
        }*/
        super.onBackPressed()

    }

    override fun onResume() {
        super.onResume()
        //stopFirebaseChatListenerService(this)

        NotificationUtils.clearNotifications(this)

        getIntentFromPush()
        updateRiderProfileOnHeader()
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        adapterFirebaseRecylcerview = AdapterFirebaseRecylcerview(this)
        binding.rvChat.adapter = adapterFirebaseRecylcerview
        firebaseChatHandler = FirebaseChatHandler(this, CommonKeys.FirebaseChatServiceTriggeredFrom.chatActivity)
        binding.rvChat.visibility = View.GONE
        binding.imgvuEmptychat.visibility = View.VISIBLE
        binding.rvChat.addOnLayoutChangeListener(View.OnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                binding.rvChat.postDelayed(Runnable { binding.rvChat.scrollToPosition(adapterFirebaseRecylcerview.itemCount -1) }, 100)
            }
        })
    }

    override fun onPause() {

        firebaseChatHandler.unRegister()
        /*if (!TextUtils.isEmpty(sessionManager.tripId) && sessionManager.isDriverAndRiderAbleToChat) {
            startFirebaseChatListenerService(this)
        }*/
        isOnChat = false
        super.onPause()
    }

    override fun onSuccess(jsonResp: JsonResponse?, data: String?) {

    }

    override fun onFailure(jsonResp: JsonResponse?, data: String?) {

    }

    companion object {
        var isOnChat = false
    }

    private fun updateRiderProfileOnHeader() {
        val riderProfilePic = sessionManager.riderProfilePic
        val riderName = sessionManager.riderName
        //val riderRating = sessionManager.riderRating
        if (!TextUtils.isEmpty(riderProfilePic)) {
            Picasso.get().load(sessionManager.riderProfilePic).error(R.drawable.car)
                    .into(binding.imgvuRiderProfile)
        }

        if (!TextUtils.isEmpty(riderName)) {
            binding.tvProfileName.text = riderName
        } else {
            binding.tvProfileName.text = resources.getString(R.string.rider)
        }


        try {
            if (!sessionManager.riderRating.isNullOrEmpty() && sessionManager.riderRating?.toFloat()!! > 0) {
                binding.tvProfileRating.visibility = View.VISIBLE
                binding.tvProfileRating.text = sessionManager.riderRating
            } else {
                binding.tvProfileRating.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvProfileRating.visibility = View.GONE
        }
    }
}
