package com.rideincab.driver.home.payouts

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ActivityBankDetailsBinding
import com.rideincab.driver.home.datamodel.BankDetailsModel
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import java.util.*
import javax.inject.Inject


class BankDetailsActivity : CommonActivity(), ServiceListener {

    // New ViewBinding code
    private var binding: ActivityBankDetailsBinding? = null

    lateinit var bankDetailsModel: BankDetailsModel

//    @BindView(R.id.edt_acc_name)
//    lateinit var edtAccName: EditText

//    @BindView(R.id.edt_acc_num)
//    lateinit var edtAccNum: EditText

//    @BindView(R.id.edt_bank_acc)
//    lateinit var edtBankAcc: EditText

//    @BindView(R.id.edt_bank_loc)
//    lateinit var edtBankLoc: EditText

//    @BindView(R.id.edt_swift_code)
//    lateinit var edtSwiftCode: EditText


    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var dialog: AlertDialog


    @Inject
    lateinit var apiService: ApiService
    private var handlers: MyClickHandlers? = null

    @Inject
    lateinit var customDialog: CustomDialog
    private lateinit var accName: String
    private lateinit var accNum: String
    private lateinit var bankName: String
    private lateinit var bankLoc: String
    private lateinit var swiftCode: String


    /**
     * Bank Details params
     *
     * @return hash Map contains Bank Details
     */
    val bankDetailsHaspMap: HashMap<String, String>
        get() {
            val bankHashMap = HashMap<String, String>()
            bankHashMap["token"] = sessionManager.accessToken!!
            bankHashMap["account_holder_name"] = accName
            bankHashMap["account_number"] = accNum
            bankHashMap["bank_name"] = bankName
            bankHashMap["bank_location"] = bankLoc
            bankHashMap["bank_code"] = swiftCode
            bankHashMap["payout_method"] = "bank_transfer"
            return bankHashMap
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBankDetailsBinding.inflate(layoutInflater);
        setContentView(binding!!.getRoot());
        binding!!.root.applySystemBarsPadding()
        AppController.getAppComponent().inject(this)

        dialog = commonMethods.getAlertDialog(this)

        commonMethods.setheaderText(resources.getString(R.string.bank_details), binding!!.commonHeader.headertext)
        bankDetailsModel = intent.getSerializableExtra("bankdetailsModel") as BankDetailsModel
        //  bankDetailsModel = intent.getSerializableExtra("bankDetailsModel") as PayoutDetailsListModel
        binding!!.bankDetails = bankDetailsModel
        handlers = MyClickHandlers(this)
        binding!!.handlers = handlers

        binding!!.commonHeader.arrow.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(this, dialog, data)
            return
        }
        if (jsonResp.isSuccess) {
            onSuccessUpdateBankDetails()
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }

    }

    private fun onSuccessUpdateBankDetails() {
        val alertDialog = AlertDialog.Builder(this@BankDetailsActivity).create()
        alertDialog.setMessage(resources.getString(R.string.bank_details_success_message))
        alertDialog.setCancelable(false)
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, resources.getString(R.string.ok)
        ) { dialog, which ->
            dialog.dismiss()
            onBackPressed()
        }
        alertDialog.show()
    }


    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
    }


    inner class MyClickHandlers(internal var context: Context) {

        fun onButtonClick(view: View) {

            if (!bankDetailsEmptyCheck()) {
                updateBankDetails()
            }

        }

        fun onBackClicked(view: View) {

            onBackPressed()

        }
    }


    /**
     * To Check wheather Bank Details Empty or not
     *
     * @return
     */


    private fun bankDetailsEmptyCheck(): Boolean {

        accName = binding!!.edtAccName.text.toString()
        accNum = binding!!.edtAccNum.text.toString()
        bankName = binding!!.edtBankAcc.text.toString()
        bankLoc = binding!!.edtBankLoc.text.toString()
        swiftCode = binding!!.edtSwiftCode.text.toString()

        if (accName == "") {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.account_holder_name) + " " + getString(R.string.required))
            return true
        } else if (accNum == "") {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.account_number) + " " + getString(R.string.required))

            return true
        } else if (bankName == "") {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.name_of_bank) + " " + getString(R.string.required))

            return true
        } else if (bankLoc == "") {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.bank_location) + " " + getString(R.string.required))

            return true
        } else if (swiftCode == "") {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.bic_swift_code) + " " + getString(R.string.required))

            return true
        }

        return false
    }


    /*
     * To update Bank Details
     * */
    private fun updateBankDetails() {
        commonMethods.showProgressDialog(this as AppCompatActivity)
        apiService.UpdatePayoutDetails(bankDetailsHaspMap).enqueue(RequestCallback(this))
    }

}
