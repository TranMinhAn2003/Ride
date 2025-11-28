package com.rideincab.user.common.helper

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage helper
 * @category CommonDialog
 * @author SMR IT Solutions
 *
 */

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.databinding.ActivityCommonDialogBinding
import com.rideincab.user.databinding.AppActivitySupportCommonBinding
import com.rideincab.user.taxi.views.main.MainActivity
import java.util.*
import javax.inject.Inject

/* ************************************************************
                CommonDialog
Common dialog for firebase service its show dialog like (Arrive now , Begin trip, Payment)
*************************************************************** */
class CommonDialog : Activity(), View.OnClickListener {

    lateinit var commonBinding: ActivityCommonDialogBinding
    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        this.setFinishOnTouchOutside(false)
        commonBinding= ActivityCommonDialogBinding.inflate(layoutInflater)
        setContentView(commonBinding.root)
        
        AppController.appComponent.inject(this)
        commonBinding.message.text = intent.getStringExtra("message")
        commonBinding.okBtnId.text = resources.getString(R.string.ok_c)
        commonBinding.okBtnId.setOnClickListener(this)
    }

    override fun attachBaseContext(newBase: Context?) {
        AppController.appComponent.inject(this)
        super.attachBaseContext(updateLocale(newBase))
    }

    fun updateLocale(newBase: Context?): Context? {
        var newBase = newBase
        val lang: String = sessionManager.languageCode!! // your language or load from SharedPref
        val locale = Locale(lang)
        val config = Configuration(newBase?.resources?.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        newBase = newBase?.createConfigurationContext(config)
        newBase?.resources?.updateConfiguration(config, newBase.resources.displayMetrics)
        return newBase
    }

    /*
    *  Get driver rating and feed back details API Called
    */
    override fun onClick(v: View) {

        when (v.id) {
            R.id.ok_btn_id -> {
                if (intent.getIntExtra("type", 0) == 0) {
                    sessionManager.isrequest = false
                    sessionManager.isTrip = true
                } else {
                    sessionManager.isrequest = false
                    sessionManager.isTrip = false
                    sessionManager.isDriverAndRiderAbleToChat = false
                    CommonMethods.stopFirebaseChatListenerService(this)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }

                this.finish()
            }
        }
    }

    override fun onBackPressed() {

    }
}
