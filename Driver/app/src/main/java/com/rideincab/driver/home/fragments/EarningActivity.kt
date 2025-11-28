/*
 * Copyright (c) 2017. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package com.rideincab.driver.home.fragments

/**
 * @package com.cloneappsolutions.cabmedriver.home.fragments
 * @subpackage fragments
 * @category EarningActivity
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.hadiidbouk.charts.BarData
import com.hadiidbouk.charts.ChartProgressBar
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.home.datamodel.EarningModel
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.home.paymentstatement.PaymentStatementActivity
import com.rideincab.driver.trips.tripsdetails.YourTrips
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ActivityEarningsBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.jvm.Throws
import kotlin.plus
import kotlin.text.equals


/* ************************************************************
                      EarningActivity
Its used get home screen earning fragment details
*************************************************************** */
class EarningActivity : CommonActivity(), ServiceListener {

    lateinit var binding:ActivityEarningsBinding

    var dialog: AlertDialog? = null
    lateinit @Inject
    var commonMethods: CommonMethods
    lateinit @Inject
    var apiService: ApiService
    lateinit @Inject
    var sessionManager: SessionManager
    lateinit @Inject
    var gson: Gson
    lateinit @Inject
    var customDialog: CustomDialog




   /* lateinit @BindView(R.id.binding.barChart)
    var binding.barChart: ChartProgressBar
    lateinit @BindView(R.id.triphistorylayout)
    var triphistorylayout: RelativeLayout
    lateinit @BindView(R.id.paystatementlayout)
    var paystatementlayout: RelativeLayout
    lateinit @BindView(R.id.binding.horizontalScrollView)
    var binding.horizontalScrollView: RelativeLayout
    lateinit @BindView(R.id.weekly_fare)
    var weekly_fare: TextView
    lateinit @BindView(R.id.last_trip_amount)
    var last_trip_amount: TextView
    lateinit @BindView(R.id.most_resent_payout)
    var most_resent_payout: TextView
    lateinit @BindView(R.id.show_date)
    var show_date: TextView
    lateinit @BindView(R.id.value_mid)
    var value_mid: TextView
    lateinit @BindView(R.id.value_bottom)
    var value_bottom: TextView
    lateinit @BindView(R.id.value_top)
    var value_top: TextView
    lateinit @BindView(R.id.binding.chatEmpty)
    var binding.chatEmpty: TextView
    lateinit @BindView(R.id.before_search)
    var before_search: TextView
    lateinit @BindView(R.id.next_search)
    var next_search: TextView
    lateinit @BindView(R.id.tv_total_pay)
    var binding.tvTotalPay: TextView
    lateinit @BindView(R.id.driver_earn_amount)
    var driverEarningAmount: TextView
    lateinit @BindView(R.id.triphistoryarrow)
    var tv_triphistoryarrow: TextView
    lateinit @BindView(R.id.paystatementarrow)
    var tv_paystatementarrow: TextView*/
    
    lateinit var current_date: String
    var farelist = ArrayList<Int>()
    lateinit var day: Array<String?>
    lateinit var dates: Array<String?>
    var dataList: ArrayList<BarData> = ArrayList()
    var days = arrayOfNulls<String>(7)
    lateinit var fare: DoubleArray
    var max: Double = 0.toDouble()
    lateinit var currency_code: String
    lateinit var currency_symbol: String
    var last_trip: String? = null
    var recent_payout: String? = null
    var total_week_amount: String? = null
    lateinit var now: Calendar
    lateinit var currencysymbol: String
    protected var isInternetAvailable: Boolean = false

    val earningsChart: HashMap<String, String>
        get() {
            val locationHashMap = HashMap<String, String>()
            locationHashMap["user_type"] = sessionManager.type!!
            locationHashMap["start_date"] = days[0]!!
            locationHashMap["end_date"] = days[days.size - 1]!!
            locationHashMap["token"] = sessionManager.accessToken!!

            return locationHashMap
        }

    fun paystatementLayout() {
        val signin = Intent(this, PaymentStatementActivity::class.java)
        signin.putExtra("start_date", days[0])
        signin.putExtra("end_date", days[days.size - 1])
        startActivity(signin)
        this!!.overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }

    fun tripHistoryLayout() {
        val signin = Intent(this, YourTrips::class.java)
        startActivity(signin)
        this!!.overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }

    fun nextSearch() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var date: Date? = null
        try {
            date = dateFormat.parse(days[days.size - 1])
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        now.time = date
        now.add(Calendar.DATE, 1)
        //val nextday = dateFormat.format(now.time)


        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)


        val delta = -now.get(GregorianCalendar.DAY_OF_WEEK) + 2 //add 2 if your week start on monday
        now.add(Calendar.DAY_OF_MONTH, delta)
        for (i in 0..6) {
            days[i] = format.format(now.time)
            now.add(Calendar.DAY_OF_MONTH, 1)
        }



        if (isInternetAvailable) {

            updateEarningChart()
        } else {
            commonMethods!!.showMessage(this, dialog, resources.getString(R.string.go_online))
        }
    }

    fun beforeSearch() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var date: Date? = null
        try {
            date = dateFormat.parse(days[0])
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        now.time = date
        now.add(Calendar.DATE, -2)
        //val yesterdayAsString = dateFormat.format(now.time)


        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val delta = -now.get(GregorianCalendar.DAY_OF_WEEK) + 2 //add 2 if your week start on monday
        now.add(Calendar.DAY_OF_MONTH, delta)
        for (i in 0..6) {
            days[i] = format.format(now.time)
            now.add(Calendar.DAY_OF_MONTH, 1)
        }


        if (isInternetAvailable) {

            updateEarningChart()

        } else {

            commonMethods.showMessage(this, dialog, resources.getString(R.string.go_online))
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityEarningsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.getAppComponent().inject(this)


        isInternetAvailable = commonMethods.isOnline(this)
        commonMethods.setheaderText(resources.getString(R.string.earnings),binding.commonHeader.headertext)
        //commonMethods.imageChangeforLocality(this,tv_triphistoryarrow)
        //commonMethods.imageChangeforLocality(this,ivBack)
        //commonMethods.imageChangeforLocality(this,tv_paystatementarrow)
        now = Calendar.getInstance()


        currencysymbol = sessionManager.currencySymbol!!

        if (!TextUtils.isEmpty(sessionManager.userType) && !sessionManager.userType.equals("0", ignoreCase = true) && !sessionManager.userType.equals("1", ignoreCase = true)) {
            binding.tvTotalPay.text = resources.getString(R.string.total_trip_amount)
        }


        val linearLayout = binding.horizontalScrollView
        val viewTreeObserver = linearLayout.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                linearLayout.viewTreeObserver.removeGlobalOnLayoutListener(this)
                //linearLayout.viewTreeObserver.removeOnGlobalLayoutListener { this }


            }
        })

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)


        if (now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            now.add(Calendar.DATE, -2)
        }

        val delta = -now.get(GregorianCalendar.DAY_OF_WEEK) + 2 //add 2 if your week start on monday
        now.add(Calendar.DAY_OF_MONTH, delta)
        for (i in 0..6) {
            days[i] = format.format(now.time)

            now.add(Calendar.DAY_OF_MONTH, 1)
        }


        current_date = days[0]!!

        if (isInternetAvailable) {

            updateEarningChart()

        } else {

            dialogfunction()

        }
        binding.commonHeader.back.setOnClickListener { onBackPressed() }
        binding.paystatementlayout.setOnClickListener { paystatementLayout() }
        binding.triphistorylayout.setOnClickListener { tripHistoryLayout() }
        binding.nextSearch.setOnClickListener { nextSearch() }
        binding.beforeSearch.setOnClickListener { beforeSearch() }

    }


    fun updateEarningChart() {
        commonMethods.showProgressDialog(this as AppCompatActivity)
        apiService.updateEarningChart(earningsChart).enqueue(RequestCallback(this))

    }

    /*
    *  Set bar chart days
    **/

    private fun randomSet(barChart: ChartProgressBar?,maxValue:Int) {
        try {
            var laydir = getString(R.string.layout_direction);
            if (laydir.equals("1")) {
                dataList.clear()
                val monFare = farelist[0]
                val tuesFare = farelist[1]
                val wedFare = farelist[2]
                val thurFare = farelist[3]
                val friFare = farelist[4]
                val satFare = farelist[5]
                val sunFare = farelist[6]
                var data = BarData("SU", monFare.toFloat(), currency_symbol + monFare.toString())
                dataList.add(data)
                data = BarData("SA", tuesFare.toFloat(), currency_symbol + tuesFare.toString())
                dataList.add(data)
                data = BarData("F", wedFare.toFloat(), currency_symbol + wedFare.toString())
                dataList.add(data)
                data = BarData("TH", thurFare.toFloat(), currency_symbol + thurFare.toString())
                dataList.add(data)
                data = BarData("W", friFare.toFloat(), currency_symbol + friFare.toString())
                dataList.add(data)
                data = BarData("Tu", satFare.toFloat(), currency_symbol + satFare.toString())
                dataList.add(data)
                data = BarData("M", sunFare.toFloat(), currency_symbol + sunFare.toString())
                dataList.add(data)
                barChart!!.setDataList(dataList)
                barChart.setMaxValue(maxValue.toFloat())
                barChart.build()
            } else {
                dataList.clear()
                val monFare = farelist[0]
                val tuesFare = farelist[1]
                val wedFare = farelist[2]
                val thurFare = farelist[3]
                val friFare = farelist[4]
                val satFare = farelist[5]
                val sunFare = farelist[6]
                var data = BarData("M", monFare.toFloat(), currency_symbol + monFare.toString())
                dataList.add(data)
                data = BarData("TU", tuesFare.toFloat(), currency_symbol + tuesFare.toString())
                dataList.add(data)
                data = BarData("W", wedFare.toFloat(), currency_symbol + wedFare.toString())
                dataList.add(data)
                data = BarData("TH", thurFare.toFloat(), currency_symbol + thurFare.toString())
                dataList.add(data)
                data = BarData("F", friFare.toFloat(), currency_symbol + friFare.toString())
                dataList.add(data)
                data = BarData("SA", satFare.toFloat(), currency_symbol + satFare.toString())
                dataList.add(data)
                data = BarData("SU", sunFare.toFloat(), currency_symbol + sunFare.toString())
                dataList.add(data)
                barChart!!.setDataList(dataList)
                barChart.setMaxValue(maxValue.toFloat())
                barChart.build()
            }
        } catch (e: Exception) {
            Log.i("EarningActivity", "randomSet: Error=${e.localizedMessage}")
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        if (jsonResp.isSuccess) {
            try {
                    onSuccessChartEarning(jsonResp)

            } catch (e: JSONException) {
                e.printStackTrace()
            }

        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            binding.chatEmpty.visibility = View.VISIBLE
            binding.horizontalScrollView.visibility = View.INVISIBLE
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            commonMethods.hideProgressDialog()

        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        binding.chatEmpty.visibility = View.VISIBLE
        binding.horizontalScrollView.visibility = View.INVISIBLE
    }


    @Throws(JSONException::class)
    private fun onSuccessChartEarning(jsonResp: JsonResponse) {
        try {
            val earningModel = gson.fromJson(jsonResp.strResponse, EarningModel::class.java)
            if (earningModel != null) {


                last_trip = earningModel.lastTrip

                try {
                    val df = DecimalFormat("0.00")
                    last_trip = df.format(java.lang.Float.valueOf(last_trip!!)).toString()
                } catch (e: Exception) {
                    last_trip = "0.00"
                }

                currency_code = earningModel.currencyCode
                currency_symbol = earningModel.currencySymbol
                recent_payout = earningModel.recentPayout
                total_week_amount = earningModel.totalWeekAmount
                println("Total Week Amount" + total_week_amount!!)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    currency_symbol =
                        Html.fromHtml(currency_symbol, Html.FROM_HTML_MODE_LEGACY).toString()
                } else {
                    currency_symbol = Html.fromHtml(currency_symbol).toString()
                }
                sessionManager.currencyCode = currency_code
                sessionManager.currencySymbol = currency_symbol
                if (java.lang.Float.valueOf(
                        earningModel.totalWeekAmount!!.replace(
                            ",".toRegex(),
                            ""
                        )
                    ) > 0
                ) {

                    binding.chatEmpty.visibility = View.INVISIBLE
                    binding.horizontalScrollView.visibility = View.VISIBLE
                    val tripDetails = gson.toJson(earningModel.tripDetails)

                    val trip_details = JSONArray(tripDetails)
                    day = arrayOfNulls(trip_details.length())
                    dates = arrayOfNulls(trip_details.length())
                    fare = DoubleArray(trip_details.length())

                    farelist.clear()


                    for (i in 0 until trip_details.length()) {
                        val trip_obj = trip_details.get(i) as JSONObject
                        day[i] = trip_obj.getString("day")
                        dates[i] = trip_obj.getString("created_at")
                        fare[i] = java.lang.Double.valueOf(
                            trip_obj.getString("daily_fare").replace(",".toRegex(), "")
                        )
                        farelist.add(
                            Math.round(
                                java.lang.Double.valueOf(
                                    trip_obj.getString("daily_fare").replace(",".toRegex(), "")
                                )
                            ).toInt()
                        )
                    }
                    if (resources.getString(R.string.layout_direction) == "1") {
                        Collections.reverse(farelist)
                    }
                    max = fare[0]

                    for (i in 1 until fare.size) {
                        if (fare[i] > max) {
                            max = fare[i]
                        }
                    }

                    val high = (max / 10 + max).toInt()
                    val mid = high / 2
                    val bottom = mid / 2
                    binding.valueMid.text = currencysymbol + mid.toString()
                    binding.valueBottom.text = currencysymbol + bottom.toString()
                    binding.valueTop.text = currencysymbol + high.toString()

                    Log.i("EarningActivity", "onSuccessChartEarning: high=${high}")

                    if (high>0)
                        randomSet(binding.barChart, high)
                    /*var laydir = getString(R.string.layout_direction);
                                       if (laydir.equals("1")){
                                           binding.barChart.setRotationX(360f);
                                           binding.barChart.setRotationY(360f);
                                       }*/
                } else {
                    binding.chatEmpty.visibility = View.VISIBLE
                    binding.horizontalScrollView.visibility = View.INVISIBLE
                }

                binding.weeklyFare.text = currencysymbol + "" + total_week_amount
                binding.driverEarnAmount.text = currency_symbol + "" + total_week_amount
                if (this != null) {
                    if (resources.getString(R.string.layout_direction) == "1") {

                        binding.lastTripAmount.text =
                            resources.getString(R.string.last_trip) + "" + last_trip + "" + currencysymbol
                        binding.mostResentPayout.text =
                            resources.getString(R.string.most_recent) + "" + recent_payout + "" + currencysymbol
                    } else {
                        binding.lastTripAmount.text =
                            resources.getString(R.string.last_trip) + "" + currencysymbol + "" + last_trip
                        binding.mostResentPayout.text =
                            resources.getString(R.string.most_recent) + "" + currencysymbol + "" + recent_payout
                    }

                }


                if (days[0] == current_date) {
                    binding.showDate.text = resources.getString(R.string.thisweek)
                    binding.nextSearch.visibility = View.GONE
                } else {
                    var date: Date?
                    var date1: Date?
                    var startdate = ""
                    var endate = ""
                    val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val targetFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
                    try {
                        date = originalFormat.parse(days[0])
                        date1 = originalFormat.parse(days[days.size - 1])
                        startdate = targetFormat.format(date)
                        endate = targetFormat.format(date1)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }

                    binding.showDate.text = "$startdate - $endate"
                    binding.nextSearch.visibility = View.VISIBLE
                }


                commonMethods.hideProgressDialog()


            }
        } catch (e: Exception) {
            Log.i("EarningActivity", "onSuccessChartEarning: Error=${e.localizedMessage}")
        }
    }


    /*
     *  Show dialog for no internet connection
     **/
    fun dialogfunction() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.turnoninternet))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok)) { _, _ -> builder.setCancelable(true) }

        val alert = builder.create()
        alert.show()
    }



}
