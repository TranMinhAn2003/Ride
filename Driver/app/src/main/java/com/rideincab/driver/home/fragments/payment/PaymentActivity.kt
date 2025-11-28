package com.rideincab.driver.home.fragments.payment

/**
 * @package com.cabme
 * @subpackage views.main.paytoadmin
 * @category Payment Activity
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

import com.google.gson.Gson

import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.Enums
import com.rideincab.driver.common.util.RequestCallback



import javax.inject.Inject


import com.rideincab.driver.home.datamodel.PaymentMethodsModel
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ActivityPaymentBinding


/*************************************************************
 * PaymentActivity
 * Its used to show the Payment details for the Driver
 */

class PaymentActivity : CommonActivity(), View.OnClickListener, ServiceListener,PaymentMethodAdapter.ItemOnClickListener {

    lateinit var binding: ActivityPaymentBinding

    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var commonMethods: CommonMethods
    @Inject
    lateinit var customDialog: CustomDialog
    @Inject
    lateinit var customDialog1: CustomDialog
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var gson: Gson

    /*@BindView(R.id.ivCardTick)
    lateinit var ivCardTick: ImageView
    @BindView(R.id.ivCard)
    lateinit var ivCard: ImageView
    @BindView(R.id.tvCard)
    lateinit var tvCard: TextView
    @BindView(R.id.rltCard)
    lateinit var rltCard: RelativeLayout
    @BindView(R.id.rltAddCard)
    lateinit var rltAddCard: RelativeLayout*/
    
    
    lateinit private var stripePublishKey: String
    lateinit private var dialog: AlertDialog
    private var clientSecretKey = ""

    private lateinit var paymentmethodadapter:PaymentMethodAdapter
    private var paymentArryalist=ArrayList<PaymentMethodsModel.PaymentMethods>()


    override fun onItemClick() {
        val stripe = Intent(applicationContext, AddCardActivity::class.java)
        startActivityForResult(stripe, REQUEST_CODE_PAYMENT)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppController.getAppComponent().inject(this)
        binding.root.applySystemBarsPadding()
        commonMethods.setheaderText(resources.getString(R.string.payment),binding.commonHeader.headertext)
        dialog = commonMethods.getAlertDialog(this)
        //showPaymentTickAccordingToTheSelection()
        paymentmethodadapter=PaymentMethodAdapter(this@PaymentActivity,paymentArryalist,this)
        binding.rvPaymentList.adapter=paymentmethodadapter
      /*  if (!TextUtils.isEmpty(sessionManager.cardValue))
        {
            rltCard.visibility = View.VISIBLE
            setCardImage(sessionManager.cardBrand)
            tvCard.text = "•••• ${sessionManager.cardValue}"

        }*/

        getPaymentMethodList()


        /**
         * View Card Details
         */
      //  commonMethods.showProgressDialog(this)
      //  commonMethods.rotateArrow(ivBack, this)
        println("Token " + sessionManager.accessToken)
     //   apiService.viewCard(sessionManager.accessToken!!).enqueue(RequestCallback(Enums.REQ_VIEW_PAYMENT, this))

        binding.rltbraintree.setOnClickListener {
            sessionManager.paymentMethodkey = CommonKeys.PAYMENT_BRAINTREE
            binding.ivbraintreetick.visibility=View.VISIBLE
            binding.ivCardTick.visibility = View.GONE
            binding.paypalTickimg.visibility=View.GONE
            onBackPressed()
        }

        binding.paypal.setOnClickListener {
            sessionManager.paymentMethodkey = CommonKeys.PAYMENT_PAYPAL
            binding.ivbraintreetick.visibility=View.GONE
            binding.ivCardTick.visibility = View.GONE
            binding.paypalTickimg.visibility=View.VISIBLE
            onBackPressed()
        }

        binding.rltCard.setOnClickListener {
            sessionManager.paymentMethodkey = CommonKeys.PAYMENT_CARD
            binding.ivbraintreetick.visibility=View.GONE
            binding.ivCardTick.visibility = View.VISIBLE
            binding.paypalTickimg.visibility=View.GONE
            onBackPressed()
        }

        binding.rltAddCard.setOnClickListener {
            val stripe = Intent(applicationContext, AddCardActivity::class.java)
            startActivityForResult(stripe, REQUEST_CODE_PAYMENT)
        }
    }

    fun  getPaymentMethodList()
    {
        commonMethods.showProgressDialog(this);
        apiService.getPaymentMethodlist(sessionManager.accessToken!!,CommonKeys.isWallet).enqueue(RequestCallback(Enums.REG_GET_PAYMENTMETHOD, this))
       /* paymentArryalist.clear()
        for(i in 0..10)
        {
            if(i==3) paymentArryalist.add(PaymentMethodsModel(i.toString(),"Cash",true))
            paymentArryalist.add(PaymentMethodsModel(i.toString(),"Paypal",false))
        }*/
    }



    /**
     * Result form Add card
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if(data !=null)
            {
                val setUp_Id = data.getStringExtra("S_intentId")
                if (setUp_Id != null && !setUp_Id.isEmpty()) {
                    commonMethods.showProgressDialog(this@PaymentActivity)
                    addcard(setUp_Id)
                }
            }
        }
    }

    /**
     * After Stripe payment
     */
    fun addcard(payKey: String) {
        if (!TextUtils.isEmpty(payKey)) {
            //            commonMethods.showProgressDialog(this);
            apiService.addCard(payKey, sessionManager.accessToken!!).enqueue(RequestCallback(Enums.REQ_ADD_CARD, this))
        }
    }
    /**
     * On Success From API
     */
    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {

            Enums.REG_GET_PAYMENTMETHOD-> if(jsonResp.isSuccess)
            {
                val paymentmodel = gson.fromJson(jsonResp.strResponse, PaymentMethodsModel::class.java)
                var isDefaultpaymentmethod=""
                paymentArryalist.addAll(paymentmodel.paymentlist)

                if (sessionManager.paymentMethodkey.isNotEmpty()) {
                    for (i in 0 until paymentmodel.paymentlist.size) {

                        CommonKeys.isSetPaymentMethod=true
                        if (sessionManager.paymentMethodkey.equals(paymentmodel.paymentlist.get(i).paymenMethodKey)) {
                            paymentmethodadapter.notifyDataSetChanged()
                            return
                        } else {
                            if (paymentmodel.paymentlist[i].isDefaultPaymentMethod) {
                                CommonKeys.isSetPaymentMethod=false
                            }
                        }
                    }
                    sessionManager.paymentMethodkey=""
                } else {
                    for (i in 0 until paymentmodel.paymentlist.size) {
                        if (paymentmodel.paymentlist[i].isDefaultPaymentMethod) {
                            CommonKeys.isSetPaymentMethod=false
                           /* sessionManager.paymentMethodkey=paymentmodel.paymentlist[i].paymenMethodKey
                            sessionManager.paymentMethod=paymentmodel.paymentlist[i].paymenMethodvalue
                            sessionManager.paymentMethodImage=paymentmodel.paymentlist[i].paymenMethodIcon*/
                        }
                    }
                }
                paymentmethodadapter.notifyDataSetChanged()

            }else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            Enums.REQ_ADD_CARD -> if (jsonResp.isSuccess) {

                val brand = commonMethods.getJsonValue(jsonResp.strResponse, "brand", String::class.java) as String
                val last4 = commonMethods.getJsonValue(jsonResp.strResponse, "last4", String::class.java) as String

                if (!TextUtils.isEmpty(last4)) {
                    binding.rltCard.visibility = View.VISIBLE
                    setCardImage(brand)
                    binding.tvCard.text = "•••• $last4"
                    binding.ivCardTick.visibility = View.VISIBLE
                    binding.ivbraintreetick.visibility=View.GONE
                    binding.paypalTickimg.visibility=View.GONE
                    sessionManager.paymentMethod = CommonKeys.PAYMENT_CARD
                    sessionManager.paymentMethodkey = CommonKeys.PAYMENT_CARD
                    sessionManager.paymentMethodImage=""
                    sessionManager.walletCard = 1
                    sessionManager.cardValue = last4
                    sessionManager.cardBrand = brand
                } else {
                    binding.rltCard.visibility = View.GONE
                }
                 onBackPressed();
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            Enums.REQ_VIEW_PAYMENT -> if (jsonResp.isSuccess) {
                clientSecretKey = commonMethods.getJsonValue(jsonResp.strResponse, "intent_client_secret", String::class.java) as String
                val brand = commonMethods.getJsonValue(jsonResp.strResponse, "brand", String::class.java) as String
                val last4 = commonMethods.getJsonValue(jsonResp.strResponse, "last4", String::class.java) as String

                if (!TextUtils.isEmpty(last4)) {
                    binding.rltCard.visibility = View.VISIBLE
                    setCardImage(brand)
                    binding.tvCard.text = "•••• $last4"
                    sessionManager.cardValue = last4
                    sessionManager.cardBrand = brand
                    sessionManager.walletCard = 1
                    binding.ivCardTick.visibility = View.GONE
                } else {
                    binding.rltCard.visibility = View.GONE
                }
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg) && jsonResp.statusMsg.equals("No record found", ignoreCase = true)) {
                clientSecretKey = commonMethods.getJsonValue(jsonResp.strResponse, "intent_client_secret", String::class.java) as String
            } else {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            else -> {
            }
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    /**
     * Set Card Images
     */
    fun setCardImage(brand: String?) {
        if ("Visa".contains(brand!!)) {
            binding.ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_visa))
        } else if ("MasterCard".contains(brand)) {
            binding.ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_master))
        } else if ("Discover".contains(brand)) {
            binding.ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_discover))
        } else if (brand.contains("Amex") || brand.contains("American Express")) {
            binding.ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_amex))
        } else if (brand.contains("JCB") || brand.contains("JCP")) {
            binding.ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_jcp))
        } else if (brand.contains("Diner") || brand.contains("Diners")) {
            binding.ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_diner))
        } else if ("Union".contains(brand) || "UnionPay".contains(brand)) {
            binding.ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.card_unionpay))
        } else {
            binding.ivCard.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.ic_card))
        }
    }

    override fun onClick(v: View) {

    }

    companion object {


        private val REQUEST_CODE_PAYMENT = 1
    }


}




