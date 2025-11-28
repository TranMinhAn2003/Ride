package com.rideincab.user.taxi.views.emergency

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.google.gson.Gson
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.taxi.views.customize.CustomDialog
import javax.inject.Inject
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.ActivitySosBinding


class SosActivity : CommonActivity(), ServiceListener {
    private lateinit var binding: ActivitySosBinding
    lateinit var dialog: AlertDialog

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

/*    @BindView(R.id.binding.messageIcon)
    lateinit var binding.messageIcon: ImageView
    @BindView(R.id.binding.loader)
    lateinit var binding.loader: ImageView
    @BindView(R.id.close)
    lateinit var close: TextView
    @BindView(R.id.alertyourcontact)
    lateinit var alert_your_contact: LinearLayout*/

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    protected var isInternetAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)
        commonMethods.setHeaderText(resources.getString(R.string.sos), binding.commonHeader.tvHeadertext)
        isInternetAvailable = commonMethods.isOnline(applicationContext)
        val imageViewTarget = DrawableImageViewTarget(binding.loader)
        Glide.with(applicationContext).load(R.drawable.dot_loader).into(imageViewTarget)
        
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
    
        binding.commonHeader.back.setOnClickListener { onBackPressed() }
    
        binding.alertyourcontact.setOnClickListener { emergencyDialog() }
    }

    /**
     * Alert Dialog
     */
    @SuppressLint("InflateParams")
    fun emergencyDialog() {

        val dialogBuilder = AlertDialog.Builder(this@SosActivity)
        val inflater = this@SosActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_emergency_dialog, null)
        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()
        val continuebutton = dialogView.findViewById<View>(R.id.continuebutton) as TextView
        val cancelbutton = dialogView.findViewById<View>(R.id.cancel) as TextView
        /**
         * Continue Button to call api
         */
        continuebutton.setOnClickListener {
            sendSos()
            alertDialog.dismiss()
        }

        /**
         * cancel button
         */
        cancelbutton.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (jsonResp.isSuccess) {
            commonMethods.hideProgressDialog()
            onSuccessSOS(jsonResp)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {}

    private fun onSuccessSOS(jsonResp: JsonResponse) {
        if (jsonResp.statusCode.matches("1".toRegex())) {
            binding.loader.visibility = View.GONE
            binding.messageIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.checksymbol))
            Toast.makeText(applicationContext, getString(R.string.sucessmessfe), Toast.LENGTH_SHORT)
                .show()
        } else if (jsonResp.statusCode.matches("2".toRegex())) {
            binding.loader.visibility = View.GONE
            binding.messageIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.checksymbol))
            Toast.makeText(
                applicationContext,
                getString(R.string.messagesenttoadmin),
                Toast.LENGTH_SHORT
            ).show()
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    /**
     * Cancel reason API called
     */
    private fun sendSos() {
        commonMethods.showProgressDialog(this)
        apiService.sosalert(sessionManager.accessToken!!, latitude.toString(), longitude.toString())
            .enqueue(RequestCallback(this))
    }
}