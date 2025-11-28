package com.rideincab.user.taxi.views.customize

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage view.customize
 * @category CustomDialog
 * @author SMR IT Solutions
 * 
 */

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView

import androidx.core.content.ContextCompat

import com.rideincab.user.R

/*****************************************************************
 * Custom dialog box fragment
 */
@SuppressLint("ValidFragment")
class CustomDialog : BaseDialogFragment {
    private var title = ""
    private var message = ""
    private var positiveBtnTxt = ""
    private var negativeBtnTxt = ""
    private var confirmTxt = ""
    private var index = -1
    private lateinit var tvAllow: TextView
    private lateinit var tvDeny: TextView
    private lateinit var allowClickListener: BtnAllowClick
    private lateinit var denyClickListener: BtnDenyClick

    private var isProgressDialog = false



    constructor(message: String, confirmTxt: String, okClickListener: BtnAllowClick) {
        this.message = message
        this.confirmTxt = confirmTxt
        this.allowClickListener = okClickListener
        this.mActivity = null
        this.index = -1
        setLayoutId(R.layout.activity_custom_dialog)
    }

    constructor(isProgressDialog: Boolean) {
        this.isProgressDialog = isProgressDialog
        this.mActivity = null
        this.index = -1
        setAnimation(false)

        setLayoutId(R.layout.activity_custom_dialog)
    }

    constructor(isProgressDialog: Boolean,message: String) {
        this.isProgressDialog = isProgressDialog
        this.mActivity = null
        this.index = -1
        this.message = message
        setAnimation(false)
        setLayoutId(R.layout.activity_custom_dialog)
    }

    constructor(message: String, positiveBtnTxt: String, negativeBtnTxt: String, allowClickListener: BtnAllowClick, denyClickListener: BtnDenyClick) {
        this.message = message
        this.confirmTxt = ""
        this.positiveBtnTxt = positiveBtnTxt
        this.negativeBtnTxt = negativeBtnTxt
        this.allowClickListener = allowClickListener
        this.denyClickListener = denyClickListener
        this.mActivity = null
        this.index = -1
        setLayoutId(R.layout.activity_custom_dialog)
    }


    constructor(title: String, message: String, confirmTxt: String, okClickListener: BtnAllowClick) {
        this.title = title
        this.message = message
        this.confirmTxt = confirmTxt
        this.allowClickListener = okClickListener
        this.mActivity = null
        this.index = -1
        setLayoutId(R.layout.activity_custom_dialog)
    }

    constructor(title: String, message: String, positiveBtnTxt: String, negativeBtnTxt: String, allowClickListener: BtnAllowClick, denyClickListener: BtnDenyClick) {
        this.title = title
        this.message = message
        this.confirmTxt = ""
        this.positiveBtnTxt = positiveBtnTxt
        this.negativeBtnTxt = negativeBtnTxt
        this.allowClickListener = allowClickListener
        this.denyClickListener = denyClickListener
        this.mActivity = null
        this.index = -1
        setLayoutId(R.layout.activity_custom_dialog)
    }

    constructor(index: Int, title: String, message: String, positiveBtnTxt: String, negativeBtnTxt: String, allowClickListener: BtnAllowClick, denyClickListener: BtnDenyClick) {
        this.title = title
        this.message = message
        this.confirmTxt = ""
        this.positiveBtnTxt = positiveBtnTxt
        this.negativeBtnTxt = negativeBtnTxt
        this.allowClickListener = allowClickListener
        this.denyClickListener = denyClickListener
        this.mActivity = null
        this.index = index
        setAnimation(false)
        setLayoutId(R.layout.activity_custom_dialog)
    }

    constructor() : super()

    override fun initViews(v: View) {
        super.initViews(v)
        val tvTitle = v.findViewById<View>(R.id.tv_dialog_title) as TextView
        val tvMessage = v.findViewById<View>(R.id.tv_loading) as TextView
        this.tvAllow = v.findViewById<View>(R.id.tv_allow) as TextView
        this.tvDeny = v.findViewById<View>(R.id.tv_deny) as TextView
        val alertDialogLayout = v.findViewById<View>(R.id.rlt_alert_dialog_layout) as RelativeLayout
        val progressLayout = v.findViewById<View>(R.id.llt_progress_dialog) as RelativeLayout
        tvMessage.text = message

        if (isProgressDialog) {
            progressLayout.visibility = View.VISIBLE
            alertDialogLayout.visibility = View.GONE
        } else {
            progressLayout.visibility = View.GONE
            alertDialogLayout.visibility = View.VISIBLE
            if (!TextUtils.isEmpty(title)) {
                tvTitle.text = title
                tvTitle.visibility = View.VISIBLE
            }
            if (!TextUtils.isEmpty(confirmTxt)) {
                this.tvDeny.visibility = View.GONE
                this.tvAllow.visibility = View.VISIBLE
                this.tvAllow.text = confirmTxt
            } else {
                this.tvAllow.visibility = View.VISIBLE
                this.tvDeny.visibility = View.VISIBLE
                this.tvAllow.text = positiveBtnTxt
                this.tvDeny.text = negativeBtnTxt
            }

            if (index >= 0) {
                this.tvAllow.setTextColor(ContextCompat.getColor(mActivity!!, R.color.colorAccent))
            }
        }
        initEvent()
        isCancelable = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mActivity = if (context is Activity) context else null
    }

    private fun initEvent() {
        tvAllow.setOnClickListener {
            allowClickListener.clicked()
            dismiss()
        }

        tvDeny.setOnClickListener {
            denyClickListener.clicked()
            dismiss()
        }
    }

    interface BtnAllowClick {
        fun clicked()
    }


    interface BtnDenyClick {
        fun clicked()
    }
}