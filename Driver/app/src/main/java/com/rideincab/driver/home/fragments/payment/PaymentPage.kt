package com.rideincab.driver.home.fragments.payment

/**
 * @package com.cloneappsolutions.cabmedriver
 * @subpackage fragments.payment
 * @category PaymentPage
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.util.CommonMethods

import javax.inject.Inject
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.databinding.ActivityPaymentPageBinding

/* ************************************************************
                      PaymentPage
Its used get Add the PaymentPage details
*************************************************************** */
class PaymentPage : CommonActivity() {

    lateinit var binding: ActivityPaymentPageBinding

    /*lateinit var dialog: AlertDialog
    @Inject
    lateinit var commonMethods: CommonMethods
    @Inject
    lateinit var sessionManager: SessionManager

    @BindView(R.id.payment_email)
    lateinit var payment_email: TextView
    @BindView(R.id.arrow)
    lateinit var arrow: ImageView

    @BindView(R.id.payment)
    lateinit var payment: RelativeLayout
    @BindView(R.id.back)
    lateinit var back: RelativeLayout
    protected var isInternetAvailable: Boolean = false

    @OnClick(R.id.payment)
    fun payment() {
        val signin = Intent(applicationContext, AddPayment::class.java)
        startActivity(signin)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }

    @OnClick(R.id.back)
    fun back() {
        onBackPressed()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_page)

        
        AppController.getAppComponent().inject(this)


        dialog = commonMethods.getAlertDialog(this)

        *//*
         *  Driver payout paypal email address
         **//*
        if (sessionManager.paypalEmail != "") {
            payment_email.text = sessionManager.paypalEmail
        } else {
            payment_email.text = resources.getString(R.string.addpayout)
            arrow.visibility = View.INVISIBLE
        }

        isInternetAvailable = commonMethods.isOnline(this)
        if (!isInternetAvailable) {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.no_connection))
        }
    }

    override fun onBackPressed() {
        if (sessionManager.paypalEmail != "") {
            super.onBackPressed()
            overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
        }

    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.paypalEmail != "") {
            payment_email.text = sessionManager.paypalEmail
        } else {
            payment_email.text = resources.getString(R.string.addpayout)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
    }
*/

}
