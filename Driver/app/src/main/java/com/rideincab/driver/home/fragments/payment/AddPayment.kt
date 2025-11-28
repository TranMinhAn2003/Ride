package com.rideincab.driver.home.fragments.payment

/**
 * @package com.cloneappsolutions.cabmedriver
 * @subpackage fragments.payment
 * @category AddPayment
 * @author SMR IT Solutions
 *
 */

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.app.AlertDialog
import android.text.TextUtils
import android.widget.EditText
import android.widget.TextView

import com.google.gson.Gson
import com.rideincab.driver.home.MainActivity
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.home.datamodel.VehicleDetails
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.RequestCallback

import javax.inject.Inject

import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.views.CommonActivity

/* ************************************************************
                      AddPayment
Its used get Add the payment
*************************************************************** */
class AddPayment : CommonActivity(), ServiceListener {

    /*lateinit var dialog: AlertDialog

    lateinit @Inject
    var apiService: ApiService
    lateinit @Inject
    var sessionManager: SessionManager
    lateinit @Inject
    var commonMethods: CommonMethods
    lateinit @Inject
    var gson: Gson
    lateinit @Inject
    var customDialog: CustomDialog


    lateinit @BindView(R.id.payment_msg)
    var payment_msg: TextView
    lateinit @BindView(R.id.emailName)
    var emailName: TextInputLayout
    lateinit @BindView(R.id.emaitext)
    var emaitext: EditText
    protected var isInternetAvailable: Boolean = false

    *//*
     *  Check is email valid or not
     **//*
    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    @OnClick(R.id.emailclose)
    fun payment() {
        emaitext.setText("")
    }

    @OnClick(R.id.back)
    fun back() {
        onBackPressed()
    }

    @OnClick(R.id.save)
    fun save() {
        addPaymentApi()
    }

    @OnClick(R.id.arrow)
    fun arrow() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_payment)

        
        AppController.getAppComponent().inject(this)
        dialog = commonMethods.getAlertDialog(this)

        *//*
          *  Common loader and internet check
          **//*
        isInternetAvailable = commonMethods.isOnline(this)


        //val appName = resources.getString(R.string.app_name)
        payment_msg.text = resources.getString(R.string.addpayment_msg)

        if (sessionManager.paypalEmail != "") {
            emaitext.setText(sessionManager.paypalEmail)
        }


    }

    private fun addPaymentApi() {

        isInternetAvailable = commonMethods.isOnline(this)

        if (!validateEmail()) {
            return
        }

        if (isInternetAvailable) {
            commonMethods.showProgressDialog(this)
            apiService.addPayout(emaitext.text.toString(), sessionManager.type!!, sessionManager.accessToken!!).enqueue(RequestCallback(this))
        } else {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.Interneterror))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        if (jsonResp.isSuccess) {
            onSuccessPayment(jsonResp)

        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

    }

    fun onSuccessPayment(jsonResp: JsonResponse) {

        val vehicleResultModel = gson.fromJson(jsonResp.strResponse, VehicleDetails::class.java)
        if (vehicleResultModel != null) {


            if (sessionManager.paypalEmail?.length!! > 0) {
                sessionManager.paypalEmail = emaitext.text.toString()
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                onBackPressed()
            } else {
                sessionManager.paypalEmail = emaitext.text.toString()
                val x = Intent(applicationContext, MainActivity::class.java)
                x.putExtra("signinup", true)
                val bndlanimation = ActivityOptions.makeCustomAnimation(applicationContext, R.anim.cb_fade_in, R.anim.cb_face_out).toBundle()
                startActivity(x, bndlanimation)
                finish()
            }


        }
    }

    *//*
     *   Validate email address
     **//*
    private fun validateEmail(): Boolean {
        val email = emaitext.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || !isValidEmail(email)) {
            emailName.error = getString(R.string.error_msg_email)
            return false
        } else {
            emailName.isErrorEnabled = false
        }

        return true
    }

*/
    override fun onSuccess(jsonResp: JsonResponse?, data: String?) {
        TODO("Not yet implemented")
    }

    override fun onFailure(jsonResp: JsonResponse?, data: String?) {
        TODO("Not yet implemented")
    }
}
