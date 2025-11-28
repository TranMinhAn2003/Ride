package com.rideincab.driver.common.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView

import com.rideincab.driver.R
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ActivityManualBookingDialogBinding
import com.rideincab.driver.home.MainActivity
import java.util.Timer
import java.util.TimerTask

class ManualBookingDialog : Activity() {

    private lateinit var binding: ActivityManualBookingDialogBinding

   /* lateinit @BindView(R.id.tv_manual_booking_status_header)
    var tvManualBookingStatus: TextView

    lateinit @BindView(R.id.tv_rider_name)
    var tvRiderName: TextView

    lateinit @BindView(R.id.tv_rider_contact_number)
    var tvRiderContactNumber: TextView

    lateinit @BindView(R.id.tv_rider_pickup_location)
    var tvRiderPickupLocation: TextView

    lateinit @BindView(R.id.tv_rider_pickup_time)
    var tvRiderPickupDateAndTime: TextView*/

    internal var type = 0
    internal var riderName = ""
    internal var riderContactNumber = "*****"
    internal var riderPickupLocation = ""
    internal var riderPickupDateAndTime = ""
    lateinit internal var mPlayer: MediaPlayer


    fun okButtonPressed() {
        mPlayer.release()
        if (type == CommonKeys.ManualBookingPopupType.cancel) {
            val requestaccept = Intent(applicationContext, MainActivity::class.java)
            requestaccept.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            requestaccept.putExtra("manualBooking", true)
            startActivity(requestaccept)
        }
        this.finish()
    }


    fun contactCardPressed() {
        try {
            if (type != CommonKeys.ManualBookingPopupType.cancel) {
                val uri = "tel:" + binding.tvRiderContactNumber.text.toString().trim { it <= ' ' }
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse(uri)
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        //getWindow().setWindowAnimations(R.style.activity_popup_animation);
        binding=ActivityManualBookingDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)


        this.setFinishOnTouchOutside(false)

        CommonMethods().increaseVolume(this)
        playNotificationSoundAndVibrate()
        try {
            type = intent.getIntExtra(CommonKeys.KEY_TYPE, CommonKeys.ManualBookingPopupType.cancel)
            when (type) {
                CommonKeys.ManualBookingPopupType.bookedInfo -> {
                    binding.tvManualBookingStatusHeader.text = getString(R.string.manually_booked)
                }

                CommonKeys.ManualBookingPopupType.reminder -> {
                    binding.tvManualBookingStatusHeader.text = getString(R.string.manual_booking_reminder)
                }

                CommonKeys.ManualBookingPopupType.cancel -> {
                    binding.tvManualBookingStatusHeader.text = getString(R.string.manual_booking_cancelled)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            riderName = intent.getStringExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_NAME).toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            if (type != CommonKeys.ManualBookingPopupType.cancel) {
                riderContactNumber =
                    intent.getStringExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_CONTACT_NUMBER)
                        .toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            riderPickupLocation =
                intent.getStringExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_PICKU_LOCATION).toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            riderPickupDateAndTime =
                intent.getStringExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_PICKU_DATE_AND_TIME)
                    .toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            binding.tvRiderName.text = riderName
            binding.tvRiderContactNumber.text = riderContactNumber
            binding.tvRiderPickupLocation.text = riderPickupLocation
            binding.tvRiderPickupTime.text = riderPickupDateAndTime
        } catch (e: Exception) {

        }

        binding.btnManualBookingOk.setOnClickListener {
            okButtonPressed()
        }
        binding.cvRiderContactNumber.setOnClickListener {
            contactCardPressed()
        }
    }

    private fun playNotificationSoundAndVibrate() {
        try {
            mPlayer = MediaPlayer.create(this, R.raw.cabme_4)
            mPlayer.start()
            mPlayer.isLooping = true

            Timer().schedule(object : TimerTask() {
                override fun run() {
                    mPlayer.stop()
                    mPlayer.release()
                }
            }, 2000)
        } catch (e: Exception) {
            e.printStackTrace()
        }


        try {
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                //deprecated in API 26
                v.vibrate(500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
