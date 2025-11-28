package com.rideincab.driver.trips.tripsdetails

/**
 * @package com.cloneappsolutions.cabmedriver.trips.tripsdetails
 * @subpackage tripsdetails model
 * @category TripsDetails
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.database.Sqlite
import com.rideincab.driver.common.helper.Constants
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.CommonMethods.Companion.showInternetNotAvailableForStoredDataViewer
import com.rideincab.driver.common.util.CommonMethods.Companion.showNoInternetAlert
import com.rideincab.driver.common.util.Enums.REQ_TRIP_DETAILS
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ActivityTripDetailsBinding
import com.rideincab.driver.databinding.AppActivityTripDetailsBinding
import com.rideincab.driver.home.datamodel.InvoiceModel
import com.rideincab.driver.home.datamodel.TripDetailsModel
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import com.rideincab.driver.trips.rating.PriceRecycleAdapter
import com.rideincab.driver.trips.rating.Riderrating
import org.json.JSONException
import java.lang.Double
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/* ************************************************************
                TripsDetails
Its used to show all the trips details information to view the page
*************************************************************** */
class TripDetails : CommonActivity(), ServiceListener {

    private lateinit var binding: AppActivityTripDetailsBinding
    private var isViewUpdatedWithLocalDB: Boolean = false
    lateinit var tripId: String

    @Inject
    lateinit var sessionManager: SessionManager
    var dialog: AlertDialog? = null

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var dbHelper: Sqlite

    /* @BindView(R.id.adminamountlayout)
     lateinit var adminamountlayout: RelativeLayout
 */
    /*  @BindView(R.id.oweamountlayout)
      lateinit var oweamountlayout: RelativeLayout*/

    /*   @BindView(R.id.driverpayoutlayout)
       lateinit var driverpayoutlayout: RelativeLayout

       @BindView(R.id.cashcollectamountlayout)
       lateinit var cashcollectamountlayout: RelativeLayout*/
    /*
        @BindView(R.id.basefare_amount)
        lateinit var basefare_amount: TextView

        @BindView(R.id.distance_fare)
        lateinit var distance_fare: TextView

        @BindView(R.id.time_fare)
        lateinit var time_fare: TextView

        @BindView(R.id.fee)
        lateinit var fee: TextView

        @BindView(R.id.totalamount)
        lateinit var totalamount: TextView

        @BindView(R.id.total_payouts)
        lateinit var total_payouts: TextView

        @BindView(R.id.cashcollectamount)
        lateinit var cashcollectamount: TextView

        @BindView(R.id.cashcollectamount_txt)
        lateinit var cashcollectamount_txt: TextView

        @BindView(R.id.oweamount)
        lateinit var oweamount: TextView

        @BindView(R.id.driverpayout)
        lateinit var driverpayout: TextView

        @BindView(R.id.adminamount)
        lateinit var adminamount: TextView*/

    /* @BindView(R.id.binding.tripAmount)
     lateinit var binding.tripAmount: TextView

     @BindView(R.id.trip_km)
     lateinit var trip_km: TextView

     @BindView(R.id.trip_duration)
     lateinit var trip_duration: TextView

     @BindView(R.id.binding.tvDropAddress)
     lateinit var drop_address: TextView

     @BindView(R.id.binding.tvPickAddress)
     lateinit var pickup_address: TextView

     @BindView(R.id.binding.seatcount)
     lateinit var binding.seatcount: TextView

     @BindView(R.id.trip_date)
     lateinit var trip_date: TextView

     @BindView(R.id.binding.routeImage)
     lateinit var binding.routeImage: ImageView

     @BindView(R.id.rvPrice)
     lateinit var binding.rvPrice: RecyclerView

     @BindView(R.id.rlt_mapview)
     lateinit var binding.rltMapview: RelativeLayout

     @BindView(R.id.binding.btnrate)
     lateinit var binding.btnrate: Button

     @BindView(R.id.carname)
     lateinit var carname: TextView

     @BindView(R.id.tv_tripstatus)
     lateinit var tripstatus: TextView

     @BindView(R.id.tv_tripid)
     lateinit var tvTripid: TextView*/

    var payment_method: String? = null
    var currencysymbol: String? = null

    internal var invoiceModels = ArrayList<InvoiceModel>()

    /*@BindView(R.id.rlt_mapview)
    lateinit var rltImageView: RelativeLayout*/

    /* @BindView(R.id.basrfarelayout)
     lateinit var farelayout: RelativeLayout*/
    lateinit var tripDetailsModels: TripDetailsModel


    fun rate() {
        sessionManager.tripId = tripDetailsModels?.riderDetails?.get(0)?.tripId!!.toString()
        val rating = Intent(this, Riderrating::class.java)
        rating.putExtra("imgprofile", tripDetailsModels?.riderDetails?.get(0)?.profileImage)
        rating.putExtra("back", 1)
        startActivity(rating)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityTripDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppController.getAppComponent().inject(this)
        binding.root.applySystemBarsPadding()

        /**Commmon Header Text View */
        commonMethods.setheaderText(
            resources.getString(R.string.tripsdetails),
            binding.commonHeader.headertext
        )
        /*  drop_address.visibility = View.GONE
          pickup_address.visibility = View.GONE*/
        currencysymbol = sessionManager.currencySymbol
        val intent = intent
        tripId = intent.getStringExtra("tripId").toString()
        //  farelayout.visibility = View.GONE

        binding.rvPrice.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        binding.rvPrice.setLayoutManager(layoutManager)

        loadTripDetails()
        binding.commonHeader.back.setOnClickListener {
            onBackPressed()
        }
        binding.btnrate.setOnClickListener {
            rate()
        }
    }

    private fun loadTripDetails() {

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

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(this)) {
            commonMethods.showProgressDialog(this)
            getTripDetails()
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

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    private fun getTripDetails() {
        if (commonMethods.isOnline(this)) {
            apiService.getTripDetails(sessionManager.accessToken!!, tripId)
                .enqueue(RequestCallback(REQ_TRIP_DETAILS, this))
        } else {
            showInternetNotAvailableForStoredDataViewer(this)
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

            REQ_TRIP_DETAILS -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                dbHelper.insertWithUpdate(
                    Constants.DB_KEY_TRIP_DETAILS.toString() + tripId,
                    jsonResp.strResponse
                )
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

        tripDetailsModels = gson.fromJson(jsonResponse, TripDetailsModel::class.java)
        invoiceModels.clear()
        with(tripDetailsModels) { this?.riderDetails?.get(0)?.invoice }?.let {
            invoiceModels.addAll(
                it
            )
        }
        binding.rvPrice.removeAllViewsInLayout()
        val adapter = PriceRecycleAdapter(this, invoiceModels)
        binding.rvPrice.setAdapter(adapter)

        if (tripDetailsModels?.isPool!! && tripDetailsModels?.seats != 0) {
            binding.seatcount.visibility = View.VISIBLE
            binding.seatcount.setText(resources.getString(R.string.seat_count) + " " + tripDetailsModels?.seats)
        } else {
            binding.seatcount.visibility = View.GONE
        }

        binding.carname.setText(tripDetailsModels?.vehicleName)
        binding.tvTripstatus.text = with(tripDetailsModels) { this?.riderDetails?.get(0)?.status }



        if (tripDetailsModels?.riderDetails?.size == 0)
            return


        binding.tripKm.text =
            with(tripDetailsModels) { this?.riderDetails?.get(0)?.totalKm } + " " + resources.getString(
                R.string.km_value
            )
        binding.tripDuration.text =
            with(tripDetailsModels) { this?.riderDetails?.get(0)?.totalTime } + " " + resources.getString(
                R.string.mins_value
            )
        binding.tvPickAddress.text =
            with(tripDetailsModels) { this?.riderDetails?.get(0)?.pickupAddress }
        binding.tvDropAddress.text =
            with(tripDetailsModels) { this?.riderDetails?.get(0)?.destAddress }
        binding.tvTripid.text = resources.getString(R.string.trip_id) + with(tripDetailsModels) {
            this?.riderDetails?.get(0)?.tripId
        }

        if (sessionManager.userType != null && !TextUtils.isEmpty(sessionManager.userType) && !sessionManager.userType.equals(
                "0",
                ignoreCase = true
            ) && !sessionManager.userType.equals("1", ignoreCase = true)
        ) {
            // Company
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (java.lang.Float.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverPayout }!!) > 0) {
                    binding.tripAmount.text = Html.fromHtml(
                        with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverEarnings }.toString(),
                        Html.FROM_HTML_MODE_LEGACY
                    )
                } else {
                    binding.tripAmount.text =
                        Html.fromHtml(sessionManager.currencySymbol!! + with(tripDetailsModels) {
                            this?.riderDetails?.get(0)?.totalFare
                        }.toString(), Html.FROM_HTML_MODE_LEGACY)
                }

            } else {
                if (java.lang.Float.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverPayout }!!) > 0) {
                    binding.tripAmount.text =
                        Html.fromHtml(with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverPayout }.toString())
                } else {
                    binding.tripAmount.text =
                        Html.fromHtml(sessionManager.currencySymbol!! + with(tripDetailsModels) {
                            this?.riderDetails?.get(0)?.totalFare
                        }.toString())
                }
            }
        } else {

            // Normal Driver
            if (java.lang.Float.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverPayout }!!) > 0) {
                binding.tripAmount.text =
                    with(tripDetailsModels) { this?.riderDetails?.get(0)?.driverEarnings }
            } else {
                binding.tripAmount.text =
                    sessionManager.currencySymbol!! + with(tripDetailsModels) {
                        this?.riderDetails?.get(0)?.totalFare
                    }
            }
        }

        if (with(tripDetailsModels) { this?.riderDetails?.get(0)?.status }.equals(
                CommonKeys.TripStatus.Rating,
                ignoreCase = true
            )
        ) {
            binding.btnrate.setVisibility(View.VISIBLE)
        } else {
            binding.btnrate.setVisibility(View.GONE)
        }


        var startdate = ""
        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val targetFormat = SimpleDateFormat("EEEE, dd-MM-yyyy")
        try {
            val date =
                originalFormat.parse(with(tripDetailsModels) { this?.riderDetails?.get(0)?.createdAt })
            startdate = targetFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }


        binding.tripDate.text = startdate

        if (tripDetailsModels?.riderDetails?.get(0)?.mapImage != null && !tripDetailsModels?.riderDetails?.get(
                0
            )?.mapImage.equals("")
        ) {
            Picasso.get().load(tripDetailsModels?.riderDetails?.get(0)?.mapImage)
                .into(binding.routeImage)
        }


        val pikcuplatlng =
            LatLng(
                java.lang.Double.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.pickup_lat }!!),
                java.lang.Double.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.pickup_lng }!!)
            )
        val droplatlng =
            LatLng(
                Double.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.drop_lat }!!),
                Double.valueOf(with(tripDetailsModels) { this?.riderDetails?.get(0)?.drop_lng }!!)
            )

        if (TextUtils.isEmpty(tripDetailsModels.riderDetails[0].mapImage)) {
            val pathString = "&path=color:0x000000ff%7Cweight:4%7Cenc:" + with(tripDetailsModels) {
                this?.riderDetails?.get(0)?.tripPath
            }
            val pickupstr = pikcuplatlng.latitude.toString() + "," + pikcuplatlng.longitude
            val dropstr = droplatlng.latitude.toString() + "," + droplatlng.longitude
            val positionOnMap =
                "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "pickup.png|" + pickupstr
            val positionOnMap1 =
                "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "drop.png|" + dropstr

            /* pickup_address.visibility = View.GONE
             drop_address.visibility = View.GONE*/
            binding.rltMapview.visibility = View.VISIBLE
            if (resources.getString(R.string.layout_direction).equals("1")) {
                binding.rltMapview.rotationY = 180f
            }

            try {
                var staticMapURL: String
                if (with(tripDetailsModels) { this?.riderDetails?.get(0)?.tripPath } == "") {
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
                    .into(binding.routeImage)
            } catch (e: Exception) {
                Log.i("TripDetails", "onSuccessTripDetail: Error=${e.localizedMessage}")
            }

        } else {
            /*  pickup_address.visibility = View.VISIBLE
              drop_address.visibility = View.VISIBLE*/
            binding.rltMapview.visibility = View.VISIBLE
            Picasso.get().load(with(tripDetailsModels) { this?.riderDetails?.get(0)?.mapImage })
                .placeholder(R.drawable.trip_map_placeholder)
                .into(binding.routeImage)
        }

        checkAddress(pikcuplatlng, droplatlng)

        if (isViewUpdatedWithLocalDB) {
            isViewUpdatedWithLocalDB = false
            getTripDetails()
        }
    }

    private fun checkAddress(
        pikcuplatlng: LatLng,
        droplatlng: LatLng
    ) {
        try {
            if (tripDetailsModels?.riderDetails?.get(0)?.pickupAddress.equals("") || tripDetailsModels?.riderDetails?.get(
                    0
                )?.destAddress.equals("")
            ) {
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
            } else {
                binding.tvPickAddress.text = tripDetailsModels?.riderDetails?.get(0)?.pickupAddress
                binding.tvDropAddress.text = tripDetailsModels?.riderDetails?.get(0)?.destAddress
            }
        } catch (e: Exception) {
            Log.i("TripDetails", "onSuccessTripDetail: Error=${e.localizedMessage}")
        }
    }
}
