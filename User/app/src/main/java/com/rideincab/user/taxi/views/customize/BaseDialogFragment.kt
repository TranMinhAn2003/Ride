package com.rideincab.user.taxi.views.customize

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage view.customize
 * @category BaseDialogFragment
 * @author SMR IT Solutions
 * 
 */

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

import com.rideincab.user.R

/*****************************************************************
 * Custom dialog base fragment
 */
open class BaseDialogFragment : DialogFragment() {
    protected var mActivity: Activity ?=null
    private var layoutId: Int = 0
    private var isNeedAnimation = true

    fun setLayoutId(layoutId: Int) {
        this.layoutId = layoutId
    }

    fun setAnimation(isNeedAnimation: Boolean) {
        this.isNeedAnimation = isNeedAnimation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isNeedAnimation) {
            setStyle(STYLE_NO_TITLE, R.style.share_dialog)
        } else {
            setStyle(STYLE_NO_TITLE, R.style.progress_dialog)
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.commitAllowingStateLoss()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(layoutId, container, false)
        initViews(v)
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = (context) as Activity
    }

    open fun initViews(v: View) {
        dialog?.setCanceledOnTouchOutside(false)
    }
}