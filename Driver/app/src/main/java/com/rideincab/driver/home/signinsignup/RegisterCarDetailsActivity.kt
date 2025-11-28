package com.rideincab.driver.home.signinsignup

/**
 * @package com.cloneappsolutions.cabmedriver
 * @subpackage signinsignup model
 * @category RegisterCarDetailsActivity
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.databinding.ActivityRegisterCarDetailsBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

/* ************************************************************
                RegisterCarDetailsActivity
Its used to get the register car details function
*************************************************************** */
class RegisterCarDetailsActivity : CommonActivity(), ServiceListener {

    /* lateinit var binding: ActivityRegisterCarDetailsBinding

    lateinit var dialog: AlertDialog
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var commonMethods: CommonMethods
    @Inject
    lateinit var gson: Gson
    @Inject
    lateinit var customDialog: CustomDialog

    @BindView(R.id.vehicle_type)
    lateinit var vehicle_type: Spinner
    @BindView(R.id.vehicle_name_edit)
    lateinit var vehicle_name_edit: EditText
    @BindView(R.id.vehicle_number_edit)
    lateinit var vehicle_number_edit: EditText
    @BindView(R.id.vehicle_name_lay)
    lateinit var vehicle_name_lay: TextInputLayout
    @BindView(R.id.vehicle_number_lay)
    lateinit var vehicle_number_lay: TextInputLayout
    @BindView(R.id.dochome_back)
    lateinit var dochome_back: ImageView
    @BindView(R.id.btn_continue)
    lateinit var btn_continue: Button
    protected var isInternetAvailable: Boolean = false

    @OnClick(R.id.btn_continue)
    fun btnContinue() {
        updateVehicleDetails()
    }

    @OnClick(R.id.dochome_back)
    fun dochomeBack() {
        onBackPressed()
    }

    private var isFinish: Int = 0
    lateinit var cardetails: JSONArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_car_details)


        
        AppController.getAppComponent().inject(this)

        dialog = commonMethods.getAlertDialog(this)
        commonMethods.imageChangeforLocality(this,dochome_back)
        isInternetAvailable = commonMethods.isOnline(this)

        vehicle_name_edit.addTextChangedListener(NameTextWatcher(vehicle_name_edit))
        vehicle_number_edit.addTextChangedListener(NameTextWatcher(vehicle_number_edit))

        isFinish = intent.getIntExtra("finish", 0)
        initVehicleTypeSpinner()
    }

    private fun initVehicleTypeSpinner() {
        val carTypeList = ArrayList<String>()

        try {
            val carType = sessionManager.carType
            cardetails = JSONArray(carType)

            carTypeList.add(resources.getString(R.string.vehicle_type))
            for (i in 0 until cardetails.length()) {
                var cartype: JSONObject?
                cartype = cardetails.getJSONObject(i)
                carTypeList.add(cartype.getString("car_name"))
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val arrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            carTypeList
        )

        vehicle_type.adapter = arrayAdapter
    }


    fun updateVehicleDetails() {
        isInternetAvailable = commonMethods.isOnline(this)
        if (!validateText(vehicle_name_lay, vehicle_name_edit)) {
            return
        }
        if (!validateText(vehicle_number_lay, vehicle_number_edit)) {
            return
        }

        if (vehicle_type.selectedItemId != 0L) {
            var selectedVehicleID: Long
            try {
                selectedVehicleID =
                    cardetails.getJSONObject(vehicle_type.selectedItemPosition - 1).getLong("id")
            } catch (e: JSONException) {
                selectedVehicleID = 1
                e.printStackTrace()
            }

            sessionManager.vehicle_id = selectedVehicleID.toString()

            val vehicle_name_editstr = vehicle_name_edit.text.toString()
            val vehicle_number_editstr = vehicle_number_edit.text.toString()



            if (isInternetAvailable) {
                commonMethods.showProgressDialog(this)

                apiService.vehicleDetails(
                    selectedVehicleID,
                    vehicle_name_editstr,
                    vehicle_type.selectedItem.toString(),
                    vehicle_number_editstr,
                    sessionManager.accessToken!!
                ).enqueue(RequestCallback(this))

            } else {
                commonMethods.showMessage(
                    this,
                    dialog,
                    resources.getString(R.string.Interneterror)
                )
            }
        } else {
            commonMethods.showMessage(
                this,
                dialog,
                resources.getString(R.string.error_msg_vehicletype)
            )
        }


    }


    private fun validateText(inputLayout: TextInputLayout?, editText: EditText): Boolean {
        if (editText.text.toString().trim { it <= ' ' }.isEmpty()) {
            if (editText.id == R.id.vehicle_name_edit)
                inputLayout?.error = getString(R.string.error_msg_vehiclename)
            else
                inputLayout?.error = getString(R.string.error_msg_vehiclenumber)
            requestFocus(editText)
            return false
        } else {
            inputLayout?.isErrorEnabled  = false
        }

        return true
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        if (jsonResp.isSuccess) {
            onSuccessCarDetails(jsonResp)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)

        }
    }

    override fun onFailure(onFailure: JsonResponse, data: String) {

        CommonMethods.DebuggableLogI("onFailureResponse ", "")
    }


    private fun onSuccessCarDetails(jsonResp: JsonResponse) {



    }

    override fun onBackPressed() {
        if (isFinish == 1) {
            finishAffinity()
        } else if (isFinish == 2) {
            val signin = Intent(applicationContext, SigninSignupHomeActivity::class.java)
            startActivity(signin)
            overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
            finish()
        } else {
            super.onBackPressed()
            val signin = Intent(applicationContext, SigninSignupHomeActivity::class.java)
            startActivity(signin)
            overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
        }
    }


    *//*
     *   Text watcher for validate document name and number field
     *//*
    private inner class NameTextWatcher(private val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            CommonMethods.DebuggableLogI("onFailureResponse ", Integer.toString(i))
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            CommonMethods.DebuggableLogI("onFailureResponse ", Integer.toString(i))
        }

        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                R.id.vehicle_name_edit -> validateText(vehicle_name_lay, vehicle_name_edit)
                R.id.vehicle_number_edit -> validateText(vehicle_number_lay, vehicle_number_edit)
                else -> {
                }
            }
        }
    }

*/
    override fun onSuccess(jsonResp: JsonResponse?, data: String?) {
        TODO("Not yet implemented")
    }

    override fun onFailure(jsonResp: JsonResponse?, data: String?) {
        TODO("Not yet implemented")
    }
}
