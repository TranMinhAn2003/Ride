package com.rideincab.driver.trips

/**
 * @package com.cloneappsolutions.cabmedriver.home
 * @subpackage home
 * @category RiderProfilePage
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.squareup.picasso.Picasso
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.home.datamodel.TripDetailsModel
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.AppActivityRiderProfileBinding
import javax.inject.Inject

/* ************************************************************
                      RiderProfilePage
Its used to get RiderProfilePage details
*************************************************************** */
class RiderProfilePage : CommonActivity() {

    private lateinit var binding:AppActivityRiderProfileBinding
    lateinit @Inject
    var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

   /* lateinit @BindView(R.id.profile_image1)
    var profileimage: ImageView
    lateinit @BindView(R.id.imgv_rider_accepted_cartypeImage)
    var riderAcceptedCartypeImage: ImageView
    lateinit @BindView(R.id.binding.cancelLay)
    var binding.cancelLay: RelativeLayout
    lateinit @BindView(R.id.rating_layout)
    var rating_layout: RelativeLayout
    lateinit @BindView(R.id.nametext)
    var ridername: TextView
    lateinit @BindView(R.id.ratingtext)
    var ratingtext: TextView
    lateinit @BindView(R.id.adresstext)
    var adresstext: TextView
    lateinit @BindView(R.id.droplocation)
    var droplocation: TextView
    lateinit @BindView(R.id.cartype)
    var cartype: TextView
    lateinit @BindView(R.id.binding.cancelTxt)
    var binding.cancelTxt: TextView*/
    
    
   /* lateinit @BindView(R.id.cancelicon)
    var cancelicon: TextView*/
    
    
    //AcceptedDriverDetails tripDetailsModel;
    lateinit var tripDetailsModel: TripDetailsModel
    private var currentRiderPosition: Int = 0
    

   /* @OnClick(R.id.contact_lay)
    fun contact() {
        val requstreceivepage = Intent(applicationContext, RiderContactActivity::class.java)
        requstreceivepage.putExtra("ridername", tripDetailsModel.riderDetails.get(currentRiderPosition).name)
        requstreceivepage.putExtra("mobile_number", tripDetailsModel.riderDetails.get(currentRiderPosition).mobile_number)
        requstreceivepage.putExtra(CommonKeys.KEY_CALLER_ID, tripDetailsModel.riderDetails.get(currentRiderPosition).riderId)
        startActivity(requstreceivepage)
    }*/

    fun callThisNo() {
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
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data =
                    Uri.parse("tel:${tripDetailsModel.riderDetails.get(currentRiderPosition).mobile_number}")
                startActivity(callIntent)
            }
        } catch (e: Exception) {
            Log.i("TAGA", "callThisNo: Error=${e.localizedMessage}")
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=AppActivityRiderProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.getAppComponent().inject(this)
       /* common header tittle */
        commonMethods.setheaderText(resources.getString(R.string.enroute),binding.commonHeader.headertext)
        val extras = intent.extras
        if (extras != null) {
            tripDetailsModel = intent.getSerializableExtra("riderDetails") as TripDetailsModel //Obtaining data
            currentRiderPosition = intent.getIntExtra("currentRiderPosition",0)
        }

        binding.nametext.text = tripDetailsModel.riderDetails.get(currentRiderPosition).name
        insertRiderInfoToSession()
        if (tripDetailsModel.riderDetails.get(currentRiderPosition).rating == "0.0" || tripDetailsModel.riderDetails.get(currentRiderPosition).rating  == "") {
            binding.ratingLayout.visibility = View.GONE
        } else {
            binding.ratingtext.text = tripDetailsModel.riderDetails.get(currentRiderPosition).rating
        }
        binding.adresstext.text = tripDetailsModel.riderDetails.get(currentRiderPosition).pickupAddress
        val imageUr = tripDetailsModel.riderDetails.get(currentRiderPosition).profileImage
        binding.droplocation.text = tripDetailsModel.riderDetails.get(currentRiderPosition).destAddress
        binding.cartype.text = tripDetailsModel.riderDetails.get(currentRiderPosition).carType

        Picasso.get().load(imageUr)
                .into(binding.profileImage1)

        Picasso.get().load(tripDetailsModel.riderDetails.get(currentRiderPosition).carActiveImage).error(R.drawable.car)
                .into(binding.imgvRiderAcceptedCartypeImage)

        if (sessionManager.tripStatus != null) {

            if (sessionManager.tripStatus == CommonKeys.TripDriverStatus.BeginTrip || sessionManager.tripStatus == CommonKeys.TripDriverStatus.EndTrip) {
                binding.cancelLay.isEnabled = false
                binding.cancelLay.isClickable = false
               // cancelicon.setTextColor(ContextCompat.getColor(applicationContext,R.color.cancel_disable_grey))
                binding.cancelTxt.setTextColor(ContextCompat.getColor(applicationContext,R.color.primary_button_text_color))
            } else {
                binding.cancelLay.isEnabled = true
                binding.cancelLay.isClickable = true
               // cancelicon.setTextColor(ContextCompat.getColor(applicationContext,R.color.app_continue))
                binding.cancelTxt.setTextColor(ContextCompat.getColor(applicationContext,R.color.primary_button_text_color))
            }
        } else {
            binding.cancelLay.isEnabled = true
            binding.cancelLay.isClickable = true
          //  cancelicon.setTextColor(ContextCompat.getColor(applicationContext,R.color.app_continue))
            binding.cancelTxt.setTextColor(ContextCompat.getColor(applicationContext,R.color.primary_button_text_color))
        }
                   /*
                    *  Redirect to trip cancel
                    */
        binding.cancelLay.setOnClickListener {
            val requstreceivepage = Intent(applicationContext, CancelYourTripActivity::class.java)
            startActivity(requstreceivepage)
        }
        
        binding.contactLay.setOnClickListener { callThisNo() } 
        binding.commonHeader.back.setOnClickListener { onBackPressed() }

    }

    private fun insertRiderInfoToSession() {
        sessionManager.riderProfilePic = tripDetailsModel.riderDetails.get(currentRiderPosition).profileImage
        sessionManager.riderRating = tripDetailsModel.riderDetails.get(currentRiderPosition).rating
        sessionManager.riderName = tripDetailsModel.riderDetails.get(currentRiderPosition).name
        sessionManager.riderId = tripDetailsModel.riderDetails.get(currentRiderPosition).riderId!!
    }
}
