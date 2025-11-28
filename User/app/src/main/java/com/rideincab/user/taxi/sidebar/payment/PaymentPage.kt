package com.rideincab.user.taxi.sidebar.payment

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage Side_Bar.payment
 * @category PaymentPage
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.appcompat.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.Enums
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.taxi.views.customize.CustomDialog

import org.json.JSONException
import org.json.JSONObject

import javax.inject.Inject


import com.google.gson.Gson
import com.rideincab.user.taxi.datamodels.PaymentMethodsModel


import com.rideincab.user.common.utils.Enums.REQ_ADD_CARD
import com.rideincab.user.common.utils.Enums.REQ_ADD_PROMO
import com.rideincab.user.common.utils.Enums.REQ_GET_PROMO
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityPaymentPageBinding


/* ************************************************************
    Rider can select the payment method
    *********************************************************** */
class PaymentPage : CommonActivity(), ServiceListener, PaymentMethodAdapter.ItemClickListener {

    private lateinit var binding: AppActivityPaymentPageBinding

    lateinit var dialog2: AlertDialog

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

/*    @BindView(R.id.tv_caller_name)
    lateinit var binding.tvCallerName: TextView

    @BindView(R.id.binding.promoCount)
    lateinit var binding.promoCount: TextView

    @BindView(R.id.tv_add_or_change_card)
    lateinit var binding.tvAddOrChangeCard: TextView

    @BindView(R.id.tv_alreadyAvailableCardNumber)
    lateinit var binding.tvAlreadyAvailableCardNumber: TextView

    @BindView(R.id.arrow)
    lateinit var arrow: ImageView

    @BindView(R.id.wallet_tickimg)
    lateinit var binding.walletTickimg: ImageView

    @BindView(R.id.paypal_tickimg)
    lateinit var binding.paypalTickimg: ImageView

    @BindView(R.id.cash_tickimg)
    lateinit var binding.cashTickimg: ImageView

    @BindView(R.id.imgView_alreadyAvailableCard_tickimg)
    lateinit var binding.imgViewAlreadyAvailableCardTickimg: ImageView

    @BindView(R.id.imgView_alreadyAvailableCardimg)
    lateinit var binding.imgViewAlreadyAvailableCardimg: ImageView

    @BindView(R.id.cash)
    lateinit var binding.cash: RelativeLayout

    @BindView(R.id.binding.wallet)
    lateinit var binding.wallet: RelativeLayout

    @BindView(R.id.alreadyAvailableCreditOrDebitCard)
    lateinit var binding.alreadyAvailableCreditOrDebitCard: RelativeLayout

    @BindView(R.id.binding.promo)
    lateinit var binding.promo: RelativeLayout

    @BindView(R.id.rlt_promotions)
    lateinit var binding.rltPromotions: RelativeLayout

    @BindView(R.id.iv_card_tick)
    lateinit var binding.ivCardTick: ImageView

    @BindView(R.id.rltcard)
    lateinit var binding.rltcard: RelativeLayout

    @BindView(R.id.binding.ivbraintreetick)
    lateinit var binding.ivbraintreetick: ImageView

    @BindView(R.id.rltbraintree)
    lateinit var binding.rltbraintree: RelativeLayout

    @BindView(R.id.rlt_wallet)
    lateinit var binding.rltWallet: RelativeLayout*/


    lateinit var dialog: BottomSheetDialog
    lateinit var input_promo_code: EditText
    lateinit var cancel_promo_btn: Button
    lateinit var add_promo_btn: Button

    protected var isInternetAvailable: Boolean = false
    lateinit private var stripePublishKey: String

    lateinit private var stripeToken: String

    private var clientSecretKey = ""

    private lateinit var paymentmethodadapter: PaymentMethodAdapter
    private var paymentArryalist = ArrayList<PaymentMethodsModel.PaymentMethods>()


    override fun onItemClick() {
        val stripe =
            Intent(this, com.rideincab.user.taxi.views.addCardDetails.AddCardActivity::class.java)
        startActivityForResult(stripe, CommonKeys.REQUEST_CODE_PAYMENT)
    }


    fun cardclick() {
        /**
         * Payment method paypal clicked
         */
        showPaymentTickAccordingToTheSelection()
        sessionManager.walletPaymentMethod = CommonKeys.PAYMENT_CARD
        sessionManager.paymentMethod = CommonKeys.PAYMENT_CARD
        onBackPressed()
    }


    fun brainTreeclick() {
        /**
         * Payment method paypal clicked
         */
        showPaymentTickAccordingToTheSelection()
        sessionManager.walletPaymentMethod = CommonKeys.PAYMENT_BRAINTREE
        sessionManager.paymentMethod = CommonKeys.PAYMENT_BRAINTREE
        onBackPressed()
    }


    fun payPalclick() {
        /**
         * Payment method paypal clicked
         */
        showPaymentTickAccordingToTheSelection()
        sessionManager.walletPaymentMethod = CommonKeys.PAYMENT_PAYPAL
        sessionManager.paymentMethod = CommonKeys.PAYMENT_PAYPAL
        onBackPressed()
    }

    fun cash() {
        /**
         * Payment method binding.cash click
         */
        showPaymentTickAccordingToTheSelection()
        sessionManager.walletPaymentMethod = CommonKeys.PAYMENT_PAYPAL
        sessionManager.paymentMethod = CommonKeys.PAYMENT_CASH
        onBackPressed()
    }

    fun creditOrDebitCard() {
        val stripe =
            Intent(this, com.rideincab.user.taxi.views.addCardDetails.AddCardActivity::class.java)
        startActivityForResult(stripe, CommonKeys.REQUEST_CODE_PAYMENT)

    }

    fun selectPaymentAsCard() {
        showPaymentTickAccordingToTheSelection()
        sessionManager.walletPaymentMethod = CommonKeys.PAYMENT_CARD
        sessionManager.paymentMethod = CommonKeys.PAYMENT_CARD
        onBackPressed()
    }

    fun addPromoclick() {
        /**
         * Add binding.promo code
         */
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        addPromo()
    }

    private var mLastClickTime: Long = 0

    fun wallet() {
        /**
         * Payment method binding.wallet added
         */
        if (!binding.walletTickimg.isEnabled) {
            binding.walletTickimg.visibility = View.VISIBLE
            binding.walletTickimg.isEnabled = true
            sessionManager.isWallet = true
        } else {
            sessionManager.isWallet = false
            binding.walletTickimg.isEnabled = false
            binding.walletTickimg.visibility = View.GONE
        }
    }

    fun promo() {
        /**
         * Promocode add click
         */
        val promo_details = Intent(this, PromoAmountActivity::class.java)
        startActivity(promo_details)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }

    fun arrow() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // common declerations
        super.onCreate(savedInstanceState)
        binding = AppActivityPaymentPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppController.appComponent.inject(this)
        binding.root.applySystemBarsPadding()
        /**Commmon Header Text View */
        commonMethods.setHeaderText(resources.getString(R.string.payment), binding.commonHeader.tvHeadertext)
        dialog2 = commonMethods.getAlertDialog(this)
        isInternetAvailable = commonMethods.isOnline(applicationContext)
        proceedActivityAccordingToType(
            intent.getIntExtra(
                CommonKeys.TYPE_INTENT_ARGUMENT_KEY,
                CommonKeys.StatusCode.startPaymentActivityForView
            )
        )
        paymentmethodadapter = PaymentMethodAdapter(this@PaymentPage, paymentArryalist, this)
        binding.rvPaymentList.adapter = paymentmethodadapter

        getPaymentMethodList()

        /* if(!sessionManager.cardValue.isEmpty())
         {
             binding.alreadyAvailableCreditOrDebitCard.visibility = View.VISIBLE
             binding.imgViewAlreadyAvailableCardimg.setImageDrawable(CommonMethods.getCardImage(sessionManager.cardBrand, resources))

             binding.tvAlreadyAvailableCardNumber.text = "•••• ${sessionManager.cardValue}"
             addCreditOrDebitCardTextViewLabelToChangeCard()
         }else
         {
             binding.alreadyAvailableCreditOrDebitCard.visibility = View.GONE
             addCreditOrDebitCardTextViewLabelToAddCard()
         }*/

        //start activity views according to the type
                
        binding.rltcard.setOnClickListener { cardclick() }
        binding.rltbraintree.setOnClickListener { brainTreeclick() }
        binding.paypal.setOnClickListener { payPalclick() }
        binding.cash.setOnClickListener { cash() }
        binding.addCreditOrDebitCard.setOnClickListener { creditOrDebitCard() }
        binding.alreadyAvailableCreditOrDebitCard.setOnClickListener { selectPaymentAsCard() }
        binding.addPromo.setOnClickListener { addPromoclick() }
        binding.wallet.setOnClickListener { wallet() }
        binding.promo.setOnClickListener { promo() }
        binding.commonHeader.arrow.setOnClickListener { arrow() }

    }

    fun getPaymentMethodList() {
        /* // val paymentdetails=JSONArray( sessionManager.paymentMethodDetail) as PaymentMethodsModel.PaymentMethods
           val collectionType = object : TypeToken<Collection<PaymentMethodsModel.PaymentMethods>>() {}.getType()
           paymentArryalist.addAll(gson.fromJson(sessionManager.paymentMethodDetail,collectionType))
           paymentmethodadapter.notifyDataSetChanged()*/
        //  val paymentMethod:PaymentMethodsModel.PaymentMethods=gson.fromJson(paymentdetails.toString(),PaymentMethodsModel.PaymentMethods::class.java)
        // println("paymentArraylist ${paymentArryalist.size}")
        CommonKeys.isFirstSetpaymentMethod = true
        apiService.getPaymentMethodlist(sessionManager.accessToken!!, CommonKeys.isWallet)
            .enqueue(RequestCallback(Enums.REG_GET_PAYMENTMETHOD, this))
    }

    private fun proceedActivityAccordingToType(@CommonKeys.StatusCode type: Int) {
        when (type) {
            CommonKeys.StatusCode.startPaymentActivityForView -> {
                setVisibilityForCompletelyViewActivity()
            }

            CommonKeys.StatusCode.startPaymentActivityForAddMoneyToWallet -> {
                run { startPaymentActivityForAddMoneyToWallet() }
                run {
                    startPaymentActivityForChangePaymentOption()
                }
            }

            CommonKeys.StatusCode.startPaymentActivityForChangePaymentOption -> {
                startPaymentActivityForChangePaymentOption()
            }

            else -> {
            }
        }
        getPromoCode()
    }

    private fun startPaymentActivityForAddMoneyToWallet() {
        CommonKeys.isWallet = 1
        hideWalletAndPromotionsLayout()


    }

    private fun startPaymentActivityForChangePaymentOption() {
        hideWalletAndPromotionsLayout()
    }

    private fun hideWalletAndPromotionsLayout() {
        binding.rltWallet.visibility = View.GONE
        binding.rltPromotions.visibility = View.GONE
    }

    fun showPaymentTickAccordingToTheSelection() {
        when (sessionManager.paymentMethod) {
            CommonKeys.PAYMENT_PAYPAL -> {
                binding.imgViewAlreadyAvailableCardTickimg.setVisibility(View.GONE);
                binding.ivCardTick.visibility = View.GONE
                binding.cashTickimg.visibility = View.GONE
                binding.paypalTickimg.visibility = View.VISIBLE
                binding.ivbraintreetick.visibility = View.GONE


            }

            CommonKeys.PAYMENT_CARD -> {
                binding.imgViewAlreadyAvailableCardTickimg.visibility = View.VISIBLE
                binding.cashTickimg.visibility = View.GONE
                binding.paypalTickimg.visibility = View.GONE
                binding.ivbraintreetick.visibility = View.GONE
                binding.ivCardTick.visibility = View.VISIBLE
            }

            CommonKeys.PAYMENT_BRAINTREE -> {
                binding.ivbraintreetick.visibility = View.VISIBLE
                binding.cashTickimg.visibility = View.GONE
                binding.paypalTickimg.visibility = View.GONE
                binding.imgViewAlreadyAvailableCardTickimg.setVisibility(View.GONE);
                binding.ivCardTick.visibility = View.VISIBLE
                binding.cashTickimg.visibility = View.GONE
                binding.paypalTickimg.visibility = View.GONE
            }

            CommonKeys.PAYMENT_CASH -> {
                binding.ivbraintreetick.visibility = View.GONE
                binding.cashTickimg.visibility = View.VISIBLE
                binding.paypalTickimg.visibility = View.GONE
                binding.imgViewAlreadyAvailableCardTickimg.visibility = View.GONE
            }

            else -> {
                sessionManager.paymentMethod = CommonKeys.PAYMENT_PAYPAL
                sessionManager.isWallet = true
                binding.imgViewAlreadyAvailableCardTickimg.visibility = View.GONE
                binding.ivbraintreetick.visibility = View.GONE
                binding.cashTickimg.visibility = View.GONE
                binding.paypalTickimg.visibility = View.VISIBLE
            }
        }
    }

    fun setVisibilityForCompletelyViewActivity() {
        CommonKeys.isWallet = 0
        //set binding.wallet symbol and balance
        binding.tvCallerName.text = sessionManager.currencySymbol + sessionManager.walletAmount

        if (sessionManager.promoCount == 0) {
            binding.promo.visibility = View.GONE
        } else if (sessionManager.promoCount > 0) {
            binding.promo.visibility = View.VISIBLE
            binding.promoCount.text = sessionManager.promoCount.toString()
        } else {
            binding.promo.visibility = View.GONE
        }


        if (sessionManager.isWallet) {
            binding.walletTickimg.isEnabled = true
            binding.walletTickimg.visibility = View.VISIBLE
        } else {
            binding.walletTickimg.isEnabled = false
            binding.walletTickimg.visibility = View.GONE
        }

        /*if (getIntent().getStringExtra("type").equals("binding.wallet")) {
            if (sessionManager.getWalletPaymentMethod() != null
                    && sessionManager.getPaymentMethod().equals("PayPal")) {
                binding.paypalTickimg.setVisibility(View.VISIBLE);
            } else {
                binding.paypalTickimg.setVisibility(View.GONE);
            }
            binding.cash.setVisibility(View.GONE);
            binding.wallet.setVisibility(View.GONE);
        }*/
    }

    override fun onBackPressed() {
        super.onBackPressed()  //Back button pressed
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    /**
     * on Activity closed
     */
    public override fun onDestroy() {
        super.onDestroy()
    }
    // method to check if the device is connected to network

    /**
     * Add binding.promo code
     */
    fun addPromo() {

        dialog = BottomSheetDialog(this@PaymentPage, R.style.BottomSheetDialogTheme)
        dialog.setContentView(R.layout.app_add_promo)

        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BottomSheetDialog d = (BottomSheetDialog) dialog;
                        FrameLayout bottomSheet = d.findViewById(R.id.design_bottom_sheet);
                        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                },0);
            }
        });*/

        cancel_promo_btn = (dialog.findViewById<View>(R.id.cancel_promo) as Button?)!!
        add_promo_btn = (dialog.findViewById<View>(R.id.add_promo) as Button?)!!
        input_promo_code = (dialog.findViewById<View>(R.id.input_promo_code) as EditText?)!!
        input_promo_code.clearFocus()

        /**
         * Call binding.promo code check API
         */
        add_promo_btn.setOnClickListener {
            if (input_promo_code.text.length > 0) {
                val promo_code = input_promo_code.text.toString()

                isInternetAvailable = commonMethods.isOnline(applicationContext)
                if (isInternetAvailable) {
                    addPromoCode(promo_code)
                } else {
                    commonMethods.showMessage(
                        this@PaymentPage,
                        dialog2,
                        getString(R.string.no_connection)
                    )
                }

                dialog.dismiss()
            } else {
                dialog.dismiss()
                commonMethods.showMessage(
                    this@PaymentPage,
                    dialog2,
                    getString(R.string.please_enter_promo)
                )
            }
        }
        cancel_promo_btn.setOnClickListener { dialog.dismiss() }

        if (!dialog.isShowing) {
            dialog.show()

        }
    }

    /**
     * Verify binding.promo code API called
     */
    fun getPromoCode() {
        if (isInternetAvailable) {
            commonMethods.showProgressDialog(this)
            apiService.promoDetails(sessionManager.accessToken!!)
                .enqueue(RequestCallback(REQ_GET_PROMO, this))
        } else {
            commonMethods.showMessage(this@PaymentPage, dialog2, getString(R.string.no_connection))
        }
    }

    fun addPromoCode(code: String) {
        commonMethods.showProgressDialog(this)
        apiService.addPromoDetails(sessionManager.accessToken!!, code)
            .enqueue(RequestCallback(REQ_ADD_PROMO, this))
    }

    fun addCardDetail(payKey: String) {
        if (!TextUtils.isEmpty(payKey)) {
            apiService.addCard(payKey, sessionManager.accessToken!!)
                .enqueue(RequestCallback(Enums.REQ_ADD_CARD, this))
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommonKeys.REQUEST_CODE_PAYMENT) {
            if (data != null) {
                val setUp_Id = data?.getStringExtra("S_intentId")
                if (setUp_Id != null && !setUp_Id.isEmpty()) {
                    commonMethods.showProgressDialog(this@PaymentPage)
                    addCardDetail(setUp_Id)
                }
            }
        }
    }

    /**
     * After Stripe payment
     *
     */
    fun paymentCompleted(setUpId: String) {
        if (!TextUtils.isEmpty(setUpId))
            apiService.addCard(setUpId, sessionManager.accessToken!!)
                .enqueue(RequestCallback(Enums.REQ_ADD_CARD, this))
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog2, data)
            return
        }
        when (jsonResp.requestCode) {
            // Get Promo Details
            REQ_GET_PROMO -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                onSuccessPromo(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog2, jsonResp.statusMsg)
            }

            REQ_ADD_CARD -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                onSuccessCard(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog2, jsonResp.statusMsg)
            }

            Enums.REG_GET_PAYMENTMETHOD -> if (jsonResp.isSuccess) {
                val paymentmodel =
                    gson.fromJson(jsonResp.strResponse, PaymentMethodsModel::class.java)
                var isDefaultpaymentmethod = ""
                sessionManager.paymentMethodDetail = gson.toJson(paymentmodel.paymentlist)
                paymentArryalist.addAll(paymentmodel.paymentlist)

                if (sessionManager.paymentMethodkey!!.isNotEmpty()) {
                    for (i in 0 until paymentmodel.paymentlist.size) {
                        CommonKeys.isSetPaymentMethod = true
                        if (sessionManager.paymentMethodkey.equals(
                                paymentmodel.paymentlist.get(i).paymenMethodKey,
                                ignoreCase = true
                            )
                        ) {
                            val paymentmode = paymentmodel.paymentlist.get(i)
                            sessionManager.walletPaymentMethod = paymentmode.paymenMethodvalue
                            sessionManager.paymentMethod = paymentmode.paymenMethodvalue
                            sessionManager.walletPaymentMethodkey = paymentmode.paymenMethodKey
                            sessionManager.paymentMethodkey = paymentmode.paymenMethodKey
                            sessionManager.paymentMethodImage = paymentmode.paymenMethodIcon
                            paymentmethodadapter.notifyDataSetChanged()
                            return
                        } else {
                            if (paymentmodel.paymentlist[i].isDefaultPaymentMethod) {
                                CommonKeys.isSetPaymentMethod = false
                            }
                        }
                    }
                    CommonKeys.isSetPaymentMethod = false
                    sessionManager.paymentMethodkey = ""
                    sessionManager.walletPaymentMethodkey = ""
                } else {
                    for (i in 0 until paymentmodel.paymentlist.size) {
                        if (paymentmodel.paymentlist[i].isDefaultPaymentMethod) {
                            CommonKeys.isSetPaymentMethod = false
                            /*  sessionManager.walletPaymentMethod=paymentmodel.paymentlist[i].paymenMethodvalue
                              sessionManager.walletPaymentMethodkey=paymentmodel.paymentlist[i].paymenMethodKey*/
                            /*   sessionManager.paymentMethod=paymentmodel.paymentlist[i].paymenMethodvalue
                               sessionManager.paymentMethodkey=paymentmodel.paymentlist[i].paymenMethodKey
                               sessionManager.paymentMethodImage=paymentmodel.paymentlist[i].paymenMethodIcon*/

                        }
                    }

                }
                paymentmethodadapter.notifyDataSetChanged()

            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog2, jsonResp.statusMsg)
            }
            // Add Promo Details
            REQ_ADD_PROMO -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                onSuccessPromo(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog2, jsonResp.statusMsg)
            }

            else -> {
            }
        }
    }

    private fun onSuccessCard(jsonResp: JsonResponse) {
        if (jsonResp.isSuccess) {
            commonMethods.hideProgressDialog()
            val response = JSONObject(jsonResp.strResponse)
            if (response.has("last4") && !TextUtils.isEmpty(response.getString("last4"))) {
                val alreadyAvailableCardLast4Digits = response.getString("last4")
                val alreadyAddedCardbrand = response.getString("brand")

                sessionManager.cardValue = alreadyAvailableCardLast4Digits
                sessionManager.cardBrand = alreadyAddedCardbrand
                sessionManager.walletPaymentMethodkey = CommonKeys.PAYMENT_CARD
                sessionManager.paymentMethodkey = CommonKeys.PAYMENT_CARD
                sessionManager.paymentMethod = "•••• $alreadyAvailableCardLast4Digits"
                sessionManager.walletPaymentMethod = "•••• $alreadyAvailableCardLast4Digits"
                sessionManager.paymentMethodImage = ""
                binding.alreadyAvailableCreditOrDebitCard.visibility = View.VISIBLE
                binding.imgViewAlreadyAvailableCardimg.setImageDrawable(
                    CommonMethods.getCardImage(
                        alreadyAddedCardbrand,
                        resources
                    )
                )
                //setCardImage(brand);
                binding.tvAlreadyAvailableCardNumber.text = "•••• $alreadyAvailableCardLast4Digits"
                addCreditOrDebitCardTextViewLabelToChangeCard()
                onBackPressed()
            } else {

                binding.alreadyAvailableCreditOrDebitCard.visibility = View.GONE
                addCreditOrDebitCardTextViewLabelToAddCard()
            }
        }

    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(this, dialog2, jsonResp.statusMsg)
        }
    }

    fun onSuccessPromo(jsonResp: JsonResponse) {
        try {
            //   clientSecretKey = commonMethods.getJsonValue(jsonResp.strResponse, "intent_client_secret", String::class.java) as String

            val response = JSONObject(jsonResp.strResponse)
            if (response.has("promo_details")) {
                val promocount = response.getJSONArray("promo_details").length()
                sessionManager.promoDetail = response.getString("promo_details")
                sessionManager.promoCount = promocount
            }
            if (response.has("stripe_key")) {
                stripePublishKey = response.getString("stripe_key")
            }

            if (response.has("wallet_amount")) {
                try {
                    sessionManager.walletAmount = response.getString("wallet_amount")
                    binding.tvCallerName.text = sessionManager.currencySymbol + sessionManager.walletAmount
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        } catch (j: JSONException) {
            j.printStackTrace()
        }

        if (sessionManager.promoCount == 0) {
            binding.promo.visibility = View.GONE
        } else if (sessionManager.promoCount > 0) {
            binding.promo.visibility = View.VISIBLE
            binding.promoCount.text = sessionManager.promoCount.toString()
        } else {
            binding.promo.visibility = View.GONE
        }
    }

    fun addCreditOrDebitCardTextViewLabelToAddCard() {
        binding.tvAddOrChangeCard.text = getString(R.string.credit_or_debit_card)
    }

    fun addCreditOrDebitCardTextViewLabelToChangeCard() {
        binding.tvAddOrChangeCard.text = getString(R.string.change_credit_or_debit_card)

    }

}
