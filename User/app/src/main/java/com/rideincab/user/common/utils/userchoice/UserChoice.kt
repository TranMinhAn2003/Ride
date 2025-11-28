package com.rideincab.user.common.utils.userchoice

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.settings.CurrencyListModel
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.Enums
import com.rideincab.user.taxi.sidebar.currency.CurrencyModel
import java.util.ArrayList
import javax.inject.Inject


class UserChoice {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    private var context: Context? = null
    private lateinit var languageList: MutableList<CurrencyModel>
    private lateinit var currencyList: ArrayList<CurrencyListModel>
    private var type: String? = null
    private var userChoiceSuccessResponse: UserChoiceSuccessResponse? = null
    private var bottomSheetDialog: BottomSheetDialog? = null


    fun getUsersLanguages(context: Context?, languageList: MutableList<CurrencyModel>, type: String?, userChoiceSuccessResponse: UserChoiceSuccessResponse?) {
        this.context = context
        this.languageList = languageList
        this.type = type
        this.userChoiceSuccessResponse = userChoiceSuccessResponse
        showBottomSheet()
    }
    fun getUserCurrency(context: Context?,currencyList: ArrayList<CurrencyListModel>,type: String?,userChoiceSuccessResponse: UserChoiceSuccessResponse?){
        this.context = context
        this.currencyList =currencyList
        this.type = type
        this.userChoiceSuccessResponse = userChoiceSuccessResponse
        showBottomSheet()
    }


    init {
        AppController.appComponent.inject(this)
    }

    /**
     * init BottomSheet
     */
    @SuppressLint("InflateParams")
    private fun showBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(context!!, R.style.BottomSheetDialogTheme)
        bottomSheetDialog!!.setContentView(R.layout.app_bottom_sheet_for_language_currency)
        val lltUserChoice = bottomSheetDialog!!.findViewById<LinearLayout>(R.id.llt_user_choice)
        val tvTitle = bottomSheetDialog!!.findViewById<TextView>(R.id.tv_title)
        val ivClose = bottomSheetDialog!!.findViewById<ImageView>(R.id.iv_close)
        if (type.equals(Enums.USER_CHOICE_LANGUAGE, ignoreCase = true)) {
            tvTitle!!.text = context!!.resources.getString(R.string.select_view, context!!.resources.getString(R.string.language))
        } else if (type.equals(Enums.USER_CHOICE_CURRENCY, ignoreCase = true)) {
            tvTitle!!.text = context!!.resources.getString(R.string.select_view, context!!.resources.getString(R.string.currency))
        }
        ivClose!!.setOnClickListener { bottomSheetDialog!!.dismiss() }
        if (!bottomSheetDialog!!.isShowing) {
            bottomSheetDialog!!.show()
            lltUserChoice!!.removeAllViews()
        }
        /**
         * User's Choice
         */
        when {
            type.equals(Enums.USER_CHOICE_LANGUAGE, ignoreCase = true) -> {
                for (i in languageList.indices) {
                    val view = LayoutInflater.from(context).inflate(R.layout.app_user_choice_items, null)
                    val rltUserChoice = view.findViewById<RelativeLayout>(R.id.rltUserChoice)
                    val tvName = view.findViewById<TextView>(R.id.tvname)
                    val tvCode = view.findViewById<TextView>(R.id.tv_code)
                    val ivTick = view.findViewById<RadioButton>(R.id.ivTick)
                    tvCode.visibility = View.GONE
                    tvName.text = languageList[i].currencyName
                    if (sessionManager.language.equals(languageList[i].currencyName, ignoreCase = true)) {
                        ivTick.isChecked = true
                    }
                    rltUserChoice.tag = i
                    rltUserChoice.setOnClickListener {
                        sessionManager.language = languageList[rltUserChoice.tag as Int].currencyName
                        sessionManager.languageCode = languageList[rltUserChoice.tag as Int].currencySymbol
                        ivTick.isChecked = true
                        userChoiceSuccessResponse!!.onSuccessUserSelected(type, languageList[rltUserChoice.tag as Int].currencyName, languageList[rltUserChoice.tag as Int].currencySymbol)
                        bottomSheetDialog!!.dismiss()
                    }
                    lltUserChoice?.addView(view)
                }
            }
            type.equals(Enums.USER_CHOICE_CURRENCY,ignoreCase = true) -> {
                for (i in currencyList.indices) {
                    val view = LayoutInflater.from(context).inflate(R.layout.app_user_choice_items, null)
                    val rltUserChoice = view.findViewById<RelativeLayout>(R.id.rltUserChoice)
                    val tvName = view.findViewById<TextView>(R.id.tvname)
                    val tvCode = view.findViewById<TextView>(R.id.tv_code)
                    val ivTick = view.findViewById<RadioButton>(R.id.ivTick)
                    tvCode.visibility = View.VISIBLE
                    tvName.text = currencyList[i].code
                    tvCode.text = currencyList[i].symbol
                    if (sessionManager.currencyCode.equals(currencyList[i].code, ignoreCase = true)) {
                        ivTick.isChecked = true
                    }
                    rltUserChoice.tag = i
                    rltUserChoice.setOnClickListener {
                        sessionManager.currencyCode = currencyList[rltUserChoice.tag as Int].code
                        sessionManager.currencySymbol = currencyList[rltUserChoice.tag as Int].symbol
                        ivTick.isChecked = true
                        userChoiceSuccessResponse!!.onSuccessUserSelected(type, currencyList[rltUserChoice.tag as Int].code, currencyList[rltUserChoice.tag as Int].symbol)
                        bottomSheetDialog!!.dismiss()
                    }
                    lltUserChoice?.addView(view)
                }
                }
            }
        }
    }
