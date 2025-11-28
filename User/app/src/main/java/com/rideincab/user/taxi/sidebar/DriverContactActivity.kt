package com.rideincab.user.taxi.sidebar

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage Side_Bar
 * @category DriverContactActivity
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonKeys.KEY_CALLER_ID
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityDriverContactBinding
import javax.inject.Inject

/* ************************************************************
   Current trip driver details and contact page
    *********************************************************** */
/*
* Editor:Umasankar
* on: 24/12/2018
 * Edited: Android action call permission removed and moved to Dial (Intent.ACTION_CALL -> Intent.ACTION_DIAL)
* purpose to reduse the no.of permission
* */
class DriverContactActivity : CommonActivity() {

    private lateinit var binding: AppActivityDriverContactBinding

/*    @BindView(R.id.binding.driverNameContact)
    lateinit var binding.driverNameContact: TextView

    @BindView(R.id.binding.driverPhoneContact)
    lateinit var binding.driverPhoneContact: TextView

    @BindView(R.id.binding.callbutton)
    lateinit var binding.callbutton: LinearLayout

    @BindView(R.id.binding.messageButton)
    lateinit var binding.messageButton: LinearLayout*/

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    var callerId = ""


    fun messageDriver() {
        CommonMethods.startChatActivityFrom(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityDriverContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        binding.commonHeader.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)
        commonMethods.setHeaderText(resources.getString(R.string.contact), binding.commonHeader.tvHeadertext)

        binding.driverNameContact.text = intent.getStringExtra("drivername")
        binding.driverPhoneContact.text = intent.getStringExtra("drivernumber")

        if (sessionManager.bookingType.equals("Manual Booking", ignoreCase = true)) {
            binding.messageButton.visibility = View.GONE
        }
        /**
         * Call driver
         */
        callerId = intent.getStringExtra(KEY_CALLER_ID).toString()
        println("Caller is : " + callerId)

        binding.callbutton.setOnClickListener {

            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:" + binding.driverPhoneContact.getText().toString())
            );
            startActivity(intent);
        }

        binding.commonHeader.back.setOnClickListener { onBackPressed() }
        binding.messageButton.setOnClickListener { messageDriver() }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
        finish()
    }

    companion object {

        val CALL = 0x2
    }

}
