package com.rideincab.driver.trips

/**
 * @package com.cloneappsolutions.cabmedriver.home
 * @subpackage home
 * @category RiderContactActivity
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat

import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ActivityRiderContactBinding
import com.rideincab.driver.home.firebaseChat.ActivityChat
import javax.inject.Inject

/* ************************************************************
                      RiderContactActivity
Its used to get RiderContactActivity details
*************************************************************** */
class RiderContactActivity : CommonActivity() {

    private lateinit var binding: ActivityRiderContactBinding
    lateinit @Inject
    var sessionManager: SessionManager
    lateinit @Inject
    var commonMethods: CommonMethods

   /* lateinit @BindView(R.id.mobilenumbertext)
    var mobilenumbertext: TextView
    lateinit @BindView(R.id.ridername)
    var ridername: TextView
    lateinit @BindView(R.id.ll_message)
    var llMessage: LinearLayout*/


    fun call() {
        try {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.CALL_PHONE
                    ),
                    1
                )
                return;
            } else {
                val intent =  Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + binding.mobilenumbertext.getText().toString()));
                startActivity(intent);
            }
        } catch (e: Exception) {
            Log.i("TAGA", "callThisNo: Error=${e.localizedMessage}")
        }


    }


    fun startChatActivity() {
        sessionManager.chatJson = ""

        startActivity(Intent(this, ActivityChat::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiderContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.getAppComponent().inject(this)
        commonMethods.setheaderText(resources.getString(R.string.contact_C),binding.commonHeader.headertext)
        binding.ridername.text = intent.getStringExtra("ridername")
        binding.mobilenumbertext.setText(getIntent().getStringExtra("mobile_number"))
        println("mobilenumbertext ${binding.mobilenumbertext.text.toString()}")
        if (sessionManager.bookingType == CommonKeys.RideBookedType.manualBooking) {
            binding.llMessage.visibility = View.GONE
        }

        binding.callbutton.setOnClickListener {
            call()
        }
        binding.llMessage.setOnClickListener {
            startChatActivity()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    companion object {

        val CALL = 0x2
    }
}
