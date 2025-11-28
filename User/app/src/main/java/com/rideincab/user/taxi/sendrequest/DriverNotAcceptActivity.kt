package com.rideincab.user.taxi.sendrequest

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage sendrequest
 * @category DriverNotAcceptActivity
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogV
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityDriverNotAcceptBinding
import com.rideincab.user.taxi.views.main.MainActivity
import java.util.*
import javax.inject.Inject

/* ************************************************************
   Drivers Not Accept the request rider can give request again otherwise goto home page
    *************************************************************** */
class DriverNotAcceptActivity : CommonActivity(), ServiceListener {

    private lateinit var binding: AppActivityDriverNotAcceptBinding
    lateinit var dialog: AlertDialog
    var requestSend: Boolean = false

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var gson: Gson

    private var overviewPolylines: String = ""

    var polylinepoints = ArrayList<LatLng>()

/*    @BindView(R.id.binding.tryAgain)
    lateinit var binding.tryAgain: TextView

    @BindView(R.id.binding.drivernotacceptBack)
    lateinit var binding.drivernotacceptBack: ImageView

    @BindView(R.id.binding.tvCall)
    lateinit var binding.tvCall: TextView

    @BindView(R.id.binding.rltContactAdmin)
    lateinit var binding.rltContactAdmin: RelativeLayout*/

    lateinit var locationHashMap: HashMap<String, String>
    protected var isInternetAvailable: Boolean = false

    fun callAdmin() {
        val callnumber = sessionManager.adminContact
        val intent2 = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$callnumber"))
        startActivity(intent2)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityDriverNotAcceptBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)
        dialog = commonMethods.getAlertDialog(this)
        isInternetAvailable = commonMethods.isOnline(applicationContext)
        locationHashMap = intent.getSerializableExtra("hashMap") as HashMap<String, String>


        // Back button click
        if (!TextUtils.isEmpty(sessionManager.adminContact)) {
            binding.tvCall.text = resources.getString(R.string.call_admin, sessionManager.adminContact)
        } else {
            binding.tvCall.text = resources.getString(R.string.no_contact_found)
            binding.rltContactAdmin.isEnabled = false
        }

        binding.drivernotacceptBack.setOnClickListener { activityBackPressHandler() }

        // Try again button click
        binding.tryAgain.setOnClickListener {
            if (!isInternetAvailable) {
                commonMethods.showMessage(this@DriverNotAcceptActivity, dialog, resources.getString(R.string.no_connection))
            } else {

                if (!requestSend) {
                    requestSend = true
                    sendRequest()
                }
                //new SendRequest().execute(getIntent().getStringExtra("url"));
            }
        }

        binding.rltContactAdmin.setOnClickListener { callAdmin() }
    }

    /**
     * Send car request to rider again
     */
    private fun sendRequest() {
        // commonMethods.showProgressDialog(this);
        val sendrequst = Intent(this, SendingRequestActivity::class.java)
        sendrequst.putExtra("loadData", "load")
        sendrequst.putExtra("carname", intent.getStringExtra("carname"))
        sendrequst.putExtra("url", intent.getStringExtra("url"))
        //        sendrequst.putExtra("fare_estimation", getIntent().getStringExtra("fare_estimation"));
        //sendrequst.putExtra("mapurl", getIntent().getStringExtra("mapurl"));
        sendrequst.putExtra("totalcar", intent.getIntExtra("totalcar", 0))
        sendrequst.putExtra("hashMap", locationHashMap)
        startActivity(sendrequst)
        //        if (locationHashMap != null) {
        //            locationHashMap.put("fare_estimation", getIntent().getStringExtra("fare_estimation"));
        //        }
        apiService.sendRequest(locationHashMap).enqueue(RequestCallback(this))
        finish()
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        DebuggableLogV("DriverNotAccept", "jsonResp")
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        DebuggableLogV("DriverNotAccept", "jsonResp" + jsonResp.statusMsg)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        activityBackPressHandler()
    }


    override fun onResume() {
        super.onResume()
        requestSend = false
    }

    private fun activityBackPressHandler() {
        sessionManager.isrequest = false
        val main = Intent(this, MainActivity::class.java)
        main.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(main)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
        finish()
    }
}
