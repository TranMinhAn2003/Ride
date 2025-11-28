package com.rideincab.user.taxi.sidebar.trips

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage Side_Bar.trips
 * @category TripDetails
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.database.SqLiteDb
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.helper.Constants
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.CommonMethods.Companion.showInternetNotAvailableForStoredDataViewer
import com.rideincab.user.common.utils.CommonMethods.Companion.showNoInternetAlert
import com.rideincab.user.common.utils.Enums.REQ_TRIP_DETAIL
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityTripDetailsBinding

import com.rideincab.user.taxi.adapters.ViewPagerAdapter
import com.rideincab.user.taxi.datamodels.trip.Riders
import com.rideincab.user.taxi.datamodels.trip.TripDetailsModel
import com.rideincab.user.taxi.sendrequest.DriverRatingActivity
import com.rideincab.user.taxi.views.customize.CustomDialog
import org.json.JSONException
import com.rideincab.user.taxi.adapters.PriceRecycleAdapter
import java.util.Locale

import javax.inject.Inject

/* ************************************************************
    Selected Trip details
    *********************************************************** */
class TripDetails : CommonActivity(), ServiceListener {

    private lateinit var binding: AppActivityTripDetailsBinding
    @Inject
    lateinit var dbHelper: SqLiteDb
    private var isViewUpdatedWithLocalDB: Boolean = false

    lateinit var tripId: String

    @Inject
    lateinit var sessionManager: SessionManager
    lateinit var dialog: AlertDialog

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    //This is our tablayout
    /*   @BindView(R.id.tabLayout2)
    lateinit var tabLayouttripdetails: TabLayout*/
    //This is our viewPager
    /* @BindView(R.id.pager2)
    lateinit var viewPagertripdetails: ViewPager*/

    /* @BindView(R.id.addressone)
    lateinit var addressone: TextView

    @BindView(R.id.adresstwo)
    lateinit var adresstwo: TextView*/
    /*  @BindView(R.id.static_map)
    lateinit var staticmapview: ImageView*/

/*    @BindView(R.id.binding.carname)
    lateinit var binding.carname: TextView

    @BindView(R.id.rlt_mapview)
    lateinit var binding.rltMapview: RelativeLayout

    @BindView(R.id.binding.driverName)
    lateinit var binding.driverName: TextView

    @BindView(R.id.binding.datetime)
    lateinit var binding.datetime: TextView

    @BindView(R.id.binding.amountcard)
    lateinit var binding.amountcard: TextView

    @BindView(R.id.binding.mapimage)
    lateinit var binding.mapimage: ImageView

    @BindView(R.id.iv_profileimage)
    lateinit var ProfileImage: ImageView

    @BindView(R.id.binding.profilelayout)
    lateinit var binding.profilelayout: RelativeLayout

    @BindView(R.id.binding.btnrate)
    lateinit var binding.btnrate: Button

    @BindView(R.id.binding.seatcount)
    lateinit var binding.seatcount: TextView

    @BindView(R.id.binding.rvPrice)
    lateinit var binding.rvPrice: RecyclerView

    @BindView(R.id.tv_tripid)
    lateinit var binding.tvTripid: TextView

    @BindView(R.id.tv_tripstatus)
    lateinit var binding.tvTripstatus: TextView*/


    fun rate() {
        val rating = Intent(this, DriverRatingActivity::class.java)
        rating.putExtra("imgprofile", tripDetailsModel.driverThumbImage)
        rating.putExtra("tripid", ridersDetails.tripId)
        rating.putExtra("back", 1)
        startActivity(rating)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityTripDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppController.appComponent.inject(this)
        binding.root.applySystemBarsPadding()
        dialog = commonMethods.getAlertDialog(this)
        /**Commmon Header Text View */
        commonMethods.setHeaderText(resources.getString(R.string.tripDetails), binding.commonHeader.tvHeadertext)
        val intent = intent
        tripId = intent.getStringExtra("tripId").toString()
        getTripDetails()

        binding.commonHeader.back.setOnClickListener { onBackPressed() }
        binding.btnrate.setOnClickListener { rate() }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(Receipt(), getString(R.string.receipt))
        viewPager.adapter = adapter
    }

    /* override fun onTabSelected(tab: TabLayout.Tab) {
        viewPagertripdetails.currentItem = tab.position
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        DebuggableLogI("Cabme", "Tab")
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        DebuggableLogI("Cabme", "Tab")
    }*/

    private fun getTripDetails() {
        val allHomeDataCursor: Cursor = dbHelper.getDocument(Constants.DB_KEY_TRIP_DETAILS + tripId)
        if (allHomeDataCursor.moveToFirst()) {
            isViewUpdatedWithLocalDB = true
            //tvOfflineAnnouncement.setVisibility(View.VISIBLE)
            try {
                onSuccessTripDetail(allHomeDataCursor.getString(0))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            followProcedureForNoDataPresentInDB()
        }
    }

    private fun getUserTripsDetail() {
        if (commonMethods.isOnline(this)) {
            apiService.getTripDetails(sessionManager.accessToken!!, tripId).enqueue(RequestCallback(REQ_TRIP_DETAIL, this))
        } else {
            showInternetNotAvailableForStoredDataViewer(this)
        }
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(this)) {
            commonMethods.showProgressDialog(this)
            getUserTripsDetail()
        } else {
            showNoInternetAlert(this, object : CommonMethods.INoInternetCustomAlertCallback {
                override fun onOkayClicked() {
                    finish()
                }

                override fun onRetryClicked() {
                    followProcedureForNoDataPresentInDB()
                }

            })
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {

            REQ_TRIP_DETAIL -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                dbHelper.insertWithUpdate(Constants.DB_KEY_TRIP_DETAILS.toString() + tripId, jsonResp.strResponse)
                onSuccessTripDetail(jsonResp.strResponse)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        commonMethods.showMessage(this, dialog, data)
    }

    private fun onSuccessTripDetail(jsonResponse: String) {
        tripDetailsModel = gson.fromJson(jsonResponse, TripDetailsModel::class.java)
        ridersDetails = tripDetailsModel!!.riders?.get(0)!!
        binding.rvPrice.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        binding.rvPrice.layoutManager = layoutManager
        val invoiceModels = tripDetailsModel.riders.get(0).invoice
        if (invoiceModels.size <= 0) {
            binding.profilelayout.visibility = View.GONE
        }
        val adapter = PriceRecycleAdapter(this, invoiceModels)
        binding.rvPrice.adapter = adapter
        binding.tvPickAddress.text = ridersDetails.pickup
        binding.tvDropAddress.text = ridersDetails.drop
        binding.tvTripid.text = resources.getString(R.string.trip_id) + ridersDetails.tripId.toString()
        binding.tvTripstatus.text = ridersDetails.status
        if ("1".equals(resources.getString(R.string.layout_direction))){
            binding.tvTripstatus.gravity = Gravity.START
        }else{
            binding.tvTripstatus.gravity = Gravity.END
        }
        /*setupViewPager(viewPagertripdetails)

        tabLayouttripdetails.setupWithViewPager(viewPagertripdetails)*/
        val pikcuplatlng = LatLng(java.lang.Double.valueOf(ridersDetails.pickupLat), java.lang.Double.valueOf(ridersDetails.pickupLng))
        val droplatlng = LatLng(java.lang.Double.valueOf(ridersDetails.dropLat), java.lang.Double.valueOf(ridersDetails.dropLng))

        if (TextUtils.isEmpty(ridersDetails.mapImage)) {

            val pathString = "&path=color:0x000000ff%7Cweight:4%7Cenc:" + ridersDetails.tripPath
            val pickupstr = pikcuplatlng.latitude.toString() + "," + pikcuplatlng.longitude
            val dropstr = droplatlng.latitude.toString() + "," + droplatlng.longitude
            val positionOnMap = "&markers=size:mid|icon:" + resources.getString(R.string.image_url) + "pickup.png|" + pickupstr
            val positionOnMap1 = "&markers=size:mid|icon:" + resources.getString(R.string.image_url) + "drop.png|" + dropstr

            //staticmapview.visibility=View.VISIBLE
//            binding.rltMapview.visibility = View.GONE
            binding.rltMapview.visibility = View.VISIBLE


            /* addressdetailayout.visibility=View.GONE
            addressdetailayout2.visibility=View.GONE*/


            var staticMapURL: String
            if (ridersDetails.tripPath == "") {
                staticMapURL = "https://maps.googleapis.com/maps/api/staticmap?size=640x250&" +
                        pikcuplatlng.latitude + "," + pikcuplatlng.longitude +
                        "" + positionOnMap + "" + positionOnMap1 + //"&zoom=14" +

                        "&key=" + sessionManager.googleMapKey + "&language=" + Locale.getDefault()
            } else {
                staticMapURL = "https://maps.googleapis.com/maps/api/staticmap?size=640x250&" +
                        pikcuplatlng.latitude + "," + pikcuplatlng.longitude +
                        pathString + "" + positionOnMap + "" + positionOnMap1 + //"&zoom=14" +

                        "&key=" + sessionManager.googleMapKey + "&language=" + Locale.getDefault()
            }
            Log.i("DEEEEPAKKKK", "onSuccessTripDetail: staticMapURL=$staticMapURL")
            Picasso.get().load(staticMapURL).placeholder(R.drawable.trip_map_placeholder)
                .into(binding.mapimage)
        } else {
            /*  addressdetailayout.visibility=View.VISIBLE
            addressdetailayout2.visibility=View.VISIBLE*/
            //  staticmapview.visibility=View.INVISIBLE
            binding.rltMapview.visibility = View.VISIBLE
            Picasso.get().load(ridersDetails.mapImage).placeholder(R.drawable.trip_map_placeholder)
                .into(binding.mapimage)
        }

        checkAddress(pikcuplatlng, droplatlng)

        if (tripDetailsModel.isPool!! && tripDetailsModel.seats != 0) {
            binding.seatcount.visibility = View.VISIBLE
            binding.seatcount.setText(resources.getString(R.string.seat_count) + " " + tripDetailsModel.seats.toString())
        } else {
            binding.seatcount.visibility = View.GONE
        }
        binding.carname.text = tripDetailsModel.vehicleName

        binding.datetime.setText(ridersDetails.creatdate);
        binding.amountcard.text = sessionManager.currencySymbol + ridersDetails.totalFare

        binding.driverName.text = resources.getString(R.string.your_ride_with) + " " + tripDetailsModel.driverName

        /*if (tripDetailsModel.getStatus().equalsIgnoreCase("Rating")){
            binding.btnrate.setVisibility(View.VISIBLE);
        }else {
            binding.btnrate.setVisibility(View.GONE);
        }*/
        val profileurl = tripDetailsModel.driverThumbImage


        if (profileurl != "") {
            Picasso.get().load(profileurl)
                .into(binding.ivProfileimage)
        }

        if (invoiceModels.size <= 0) {
            binding.profilelayout.visibility = View.GONE
        }
        if (isViewUpdatedWithLocalDB) {
            isViewUpdatedWithLocalDB = false
            getUserTripsDetail()
        }
    }

    private fun checkAddress(pikcuplatlng: LatLng, droplatlng: LatLng) {

        try {
            if (ridersDetails?.pickup.equals("") || ridersDetails.drop.equals(""))
            {
                pikcuplatlng?.let {
                    binding.tvPickAddress.text = commonMethods.getAddressFromLatLng(
                        this,
                        pikcuplatlng.latitude,
                        pikcuplatlng.longitude
                    )
                }
                droplatlng?.let {
                    binding.tvDropAddress.text = commonMethods.getAddressFromLatLng(
                        this,
                        droplatlng.latitude,
                        droplatlng.longitude
                    )
                }
            }else{
                binding.tvPickAddress.text = ridersDetails?.pickup
                binding.tvDropAddress.text = ridersDetails?.drop
            }
        } catch (e: Exception) {
            Log.i("TripDetails", "onSuccessTripDetail: Error=${e.localizedMessage}")
        }

    }


    companion object {

        lateinit var tripDetailsModel: TripDetailsModel

        lateinit var ridersDetails: Riders
    }
}
