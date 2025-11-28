package com.rideincab.user.taxi.views.search

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage search
 * @category PlaceSearchActivity
 * @author SMR IT Solutions
 * 
 */

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.http.AndroidHttpClient
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.helper.Constants
import com.rideincab.user.common.helper.Permission
import com.rideincab.user.common.map.AppUtils
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogD
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogE
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogI
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogV
import com.rideincab.user.taxi.sidebar.AddHome
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.taxi.views.customize.CustomDialog
import com.rideincab.user.taxi.views.main.MainActivity
import com.rideincab.user.common.network.PermissionCamer
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityPlaceSearchBinding
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicResponseHandler
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import java.util.ArrayList as ArrayList1

/* ***************************************************************
    Search pickup and drop location for send request and Add home and work address
   *************************************************************** */
class PlaceSearchActivity : CommonActivity(), View.OnFocusChangeListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnCameraMoveStartedListener, OnCameraMoveListener, OnCameraMoveCanceledListener, OnCameraIdleListener, GoogleMapPlaceSearchAutoCompleteRecyclerView.AutoCompleteAddressTouchListener {
    val ANDROID_HTTP_CLIENT = AndroidHttpClient.newInstance(PlaceSearchActivity::class.java.name)
    lateinit var dialog: AlertDialog
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var commonMethods: CommonMethods
    @Inject
    lateinit var customDialog: CustomDialog
    lateinit var addressList: MutableList<Address> // Addresslist
    lateinit var address: String // Address
    var countrys: String?=""// Country name
    lateinit var getLocations: LatLng
    lateinit var getAddress: String


   /* @BindView(R.id.binding.destadddress)
    lateinit var binding.destadddress: EditText
    @BindView(R.id.binding.pickupaddress)
    lateinit var binding.pickupaddress: EditText
    @BindView(R.id.binding.destPoint)
    lateinit var binding.destPoint: ImageView
    @BindView(R.id.binding.pickupPoint)
    lateinit var binding.pickupPoint: ImageView
    @BindView(R.id.binding.pinMap)
    lateinit var binding.pinMap: ImageView
    @BindView(R.id.binding.dropDone)
    lateinit var binding.dropDone: TextView
    @BindView(R.id.binding.hometext)
    lateinit var binding.hometext: TextView
    @BindView(R.id.binding.homeaddress)
    lateinit var binding.homeaddress: TextView
    @BindView(R.id.binding.worktext)
    lateinit var binding.worktext: TextView
    @BindView(R.id.binding.workaddress)
    lateinit var binding.workaddress: TextView
    @BindView(R.id.setlocaion_onmap)
    lateinit var setlocaion_onmap: LinearLayout
    @BindView(R.id.binding.homelayoyt)
    lateinit var binding.homelayoyt: LinearLayout
    @BindView(R.id.binding.worklayout)
    lateinit var binding.worklayout: LinearLayout
    @BindView(R.id.location_placesearch)
    lateinit var binding.locationPlacesearch: RecyclerView
    @BindView(R.id.binding.addressSearch)
    lateinit var binding.addressSearch: ScrollView
    @BindView(R.id.binding.scheduleDateTime)
    lateinit var binding.scheduleDateTime: TextView
    @BindView(R.id.pb_address_searchbar_loading)
    lateinit var binding.pbAddressSearchbarLoading: ProgressBar
    @BindView(R.id.binding.schedulerideLayout)
    lateinit var binding.schedulerideLayout: RelativeLayout*/

    lateinit var mapFragment: SupportMapFragment
    lateinit var Schedule_ride: String
    var date: String ? = null
    var counti = 0
    lateinit var lat: String
    lateinit var log: String

    //binding.schedulerideLayout
    lateinit var homegettext: String
    lateinit var workgettext: String
    lateinit var searchlocation: String
    lateinit var objLatLng: LatLng
    lateinit var googleMap: GoogleMap
    lateinit var Listlat: String
    lateinit var Listlong: String
    lateinit var mContext : Context
    var isPickup = false
     var pickipCountry: String?=null
     var destCountry: String?=null
    var markerPoints = ArrayList1<LatLng>()
    var oldstring = ""
    lateinit var pickuplatlng: LatLng
    var isFirst = true
    var isMapMove = false
    lateinit protected var mGoogleApiClient: GoogleApiClient
    protected var isInternetAvailable: Boolean = false
    lateinit internal var placesClient: PlacesClient
    protected val REQUEST_CHECK_SETTINGS = 0x1
    private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    lateinit internal var googleAutoCompleteToken: AutocompleteSessionToken
    lateinit internal var googleMapPlaceSearchAutoCompleteRecyclerView: GoogleMapPlaceSearchAutoCompleteRecyclerView

    private val addressAutoCompletePredictions = ArrayList1<AutocompletePrediction>()

    private val staticLatLng = LatLng(1.0, 1.0)


  
    fun pickupclose() {
        val ll = LatLng(1.0, 1.0)
        markerPoints.set(0, ll)
        isPickup = true
        binding.pickupaddress.setText("")
        binding.pickupaddress.requestFocus()
        binding.dropDone.visibility = View.GONE
    }
    
    fun destclose() {
        val ll = LatLng(1.0, 1.0)
        markerPoints.set(1, ll)
        binding.destadddress.setText("")
        destadddress()
    }

   
    fun destadddress() {
        binding.destadddress.requestFocus()
        binding.dropDone.visibility = View.GONE
        destAddsFocus()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun destAddsFocus() {

        isPickup = false
        binding.pickupaddress.removeTextChangedListener(NameTextWatcher(binding.pickupaddress))
        binding.destadddress.addTextChangedListener(NameTextWatcher(binding.destadddress))

        binding.locationPlacesearch.visibility = View.GONE
        binding.dropDone.visibility = View.GONE
        binding.addressSearch.visibility = View.VISIBLE
        mapFragment.view!!.visibility = View.GONE
        binding.pinMap.visibility = View.GONE
        // code to execute when EditText loses focus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.pickupPoint.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_primary_text)
            binding.destPoint.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_primary_color)
        }
    }
    private lateinit var binding: AppActivityPlaceSearchBinding
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityPlaceSearchBinding.inflate(layoutInflater)
        buildGoogleApiClient()  // Connect Google API client
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)
        dialog = commonMethods.getAlertDialog(this)

        showLocationPermissionDialog()  // Location permission dialog
        val ll = LatLng(1.0, 1.0)

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), resources.getString(R.string.google_maps_key))
        }
        
        binding.arrow.setOnClickListener { onBackPressed() }
        binding.pickupclose.setOnClickListener { pickupclose() }
        binding.destclose.setOnClickListener { destclose() }
        binding.destadddress.setOnClickListener { destadddress() }
        
        objLatLng = intent.extras?.getParcelable("Latlng")!!
        pickipCountry = intent.getStringExtra("Country")
        date = intent.getStringExtra("date")
        placesClient = com.google.android.libraries.places.api.Places.createClient(this)
        googleAutoCompleteToken = AutocompleteSessionToken.newInstance()
        googleMapPlaceSearchAutoCompleteRecyclerView = GoogleMapPlaceSearchAutoCompleteRecyclerView(addressAutoCompletePredictions, this, this)
        try {

            homegettext = intent.getStringExtra("Home").toString()
            workgettext = intent.getStringExtra("Work").toString()
            Schedule_ride = intent.getStringExtra("Schedule").toString()

        } catch (e: Exception) {
            e.printStackTrace()
            homegettext = ""
            workgettext = ""
        }

        if (null != intent.getStringExtra("Schedule"))
            if (intent.getStringExtra("Schedule") == "Schedule") {
                binding.schedulerideLayout.visibility = View.VISIBLE
                binding.scheduleDateTime.text = date
            } else {
                binding.schedulerideLayout.visibility = View.INVISIBLE
            }


        pickuplatlng = LatLng(objLatLng.latitude, objLatLng.longitude)
        markerPoints.add(0, pickuplatlng)
        markerPoints.add(1, ll)


        DebuggableLogD("OUTPUT IS", homegettext)
        DebuggableLogD("OUTPUT IS", workgettext)
        binding.homeaddress.text = homegettext
        if ("" == homegettext) {
            binding.hometext.text = getString(R.string.addhome)
        }
        if ("" == workgettext) {
            binding.worktext.text = getString(R.string.addwork)
        }

        binding.homeaddress.text = homegettext
        binding.workaddress.text = workgettext
        binding.homelayoyt.visibility = View.VISIBLE
        binding.worklayout.visibility = View.VISIBLE

        // Home address layout click
        binding.homelayoyt.setOnClickListener {
            if (isInternetAvailable) {
                if (homegettext != "") {

                    fetchLocation(homegettext, "homeclick")
                } else {
                    val intent = Intent(this, AddHome::class.java)
                    intent.putExtra("searchhome", "searchhome")
                    intent.putExtra("searchwork", "")
                    intent.putExtra("Schedule", getIntent().getStringExtra("Schedule"))
                    intent.putExtra("Latlng", objLatLng)
                    intent.putExtra("Country", pickipCountry)
                    intent.putExtra("Home", homegettext)
                    intent.putExtra("Work", workgettext)
                    intent.putExtra("PickupAddress", getIntent().getStringExtra("PickupAddress"))
                    intent.putExtra("DropAddress", getIntent().getStringExtra("DropAddress"))
                    startActivity(intent)
                    overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
                }
            } else {
                commonMethods.showMessage(this@PlaceSearchActivity, dialog, getString(R.string.no_connection))
            }
        }

        // Work address layout click
        binding.worklayout.setOnClickListener {
            if (isInternetAvailable) {

                if (workgettext != "") {
                    fetchLocation(workgettext, "workclick")
                } else {
                    val intent = Intent(this, AddHome::class.java)
                    intent.putExtra("searchhome", "")
                    intent.putExtra("searchwork", "searchwork")
                    intent.putExtra("Schedule", getIntent().getStringExtra("Schedule"))
                    intent.putExtra("Latlng", objLatLng)
                    intent.putExtra("Country", pickipCountry)
                    intent.putExtra("Home", homegettext)
                    intent.putExtra("Work", workgettext)
                    intent.putExtra("PickupAddress", getIntent().getStringExtra("PickupAddress"))
                    intent.putExtra("DropAddress", getIntent().getStringExtra("DropAddress"))
                    startActivity(intent)
                    overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
                }

            } else {
                commonMethods.showMessage(this@PlaceSearchActivity, dialog, getString(R.string.no_connection))
            }
        }

        binding.pickupaddress.setText(intent.getStringExtra(CommonKeys.PickupAddress))
        binding.destadddress.setText(intent.getStringExtra(CommonKeys.DropAddress))
        binding.destadddress.addTextChangedListener(NameTextWatcher(binding.destadddress))

        if (intent.getSerializableExtra("PickupDrop") != null) {
            markerPoints = intent.getSerializableExtra("PickupDrop") as ArrayList1<LatLng>
            fetchAddress(markerPoints.get(0), "country")  // Fetch Address using if Geocode or Google
        }

        // Done button for map address selection
        binding.dropDone.setOnClickListener {
            isInternetAvailable = commonMethods.isOnline(applicationContext)
            if (!isInternetAvailable) {
                commonMethods.showMessage(this@PlaceSearchActivity, dialog, getString(R.string.no_connection))
            }

            val pickup = binding.pickupaddress.text.toString()
            val destup = binding.destadddress.text.toString()

            if (pickup.matches("".toRegex()) || destup.matches("".toRegex())) {
                binding.addressSearch.visibility = View.VISIBLE
                mapFragment.view!!.visibility = View.GONE
                binding.pinMap.visibility = View.GONE
                binding.dropDone.visibility = View.GONE
                if (pickup.matches("".toRegex())) {
                    binding.pickupaddress.requestFocus()
                }

                if (destup.matches("".toRegex())) {
                    binding.destadddress.requestFocus()
                }

            } else {
                if (binding.pickupaddress.text.toString() == binding.destadddress.text.toString()) {

                    val l1 = markerPoints.get(0)
                    val l2 = markerPoints.get(1)
                    if (distance(l1.latitude, l1.longitude, l2.latitude, l2.longitude) < 0.1) {
                        commonMethods.showMessage(this@PlaceSearchActivity, dialog, resources.getString(R.string.pickup_and_drop_location_is_same))
                    } else {

                        sessionManager.isrequest=true
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("PickupDrop", markerPoints)

                        intent.putExtra(CommonKeys.PickupAddress, binding.pickupaddress.text.toString())
                        intent.putExtra(CommonKeys.DropAddress, binding.destadddress.text.toString())
                        if (getIntent().getStringExtra("Schedule") == "Schedule") {
                            intent.putExtra("Schedule", "Schedule")
                            intent.putExtra("date", date)
                        } else {
                            intent.putExtra("Schedule", "")
                        }

                        startActivity(intent)
                    }
                } else {

                    sessionManager.isrequest=true
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("PickupDrop", markerPoints)

                    intent.putExtra(CommonKeys.PickupAddress, binding.pickupaddress.text.toString())
                    intent.putExtra(CommonKeys.DropAddress, binding.destadddress.text.toString())

                    if (getIntent().getStringExtra("Schedule") == "Schedule") {
                        intent.putExtra("Schedule", "Schedule")
                        intent.putExtra("date", date)
                    } else {
                        intent.putExtra("Schedule", "")
                    }
                    startActivity(intent)
                }
            }
        }


        isInternetAvailable = commonMethods.isOnline(applicationContext)
        if (!isInternetAvailable) {
            commonMethods.showMessage(this, dialog, getString(R.string.no_connection))
        }

        try {
            initilizeMap()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.setlocaionOnmap.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.pickupaddress.windowToken, 0)
            binding.addressSearch.visibility = View.GONE
            mapFragment.view!!.visibility = View.VISIBLE
            binding.pinMap.visibility = View.VISIBLE
        }



        // Check pickup address foucus
        binding.pickupaddress.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // binding.pickupaddress.setText("");
                binding.destadddress.removeTextChangedListener(NameTextWatcher(binding.destadddress))
                binding.pickupaddress.addTextChangedListener(NameTextWatcher(binding.pickupaddress))
                binding.locationPlacesearch.visibility = View.GONE
                binding.dropDone.visibility = View.GONE
                binding.pickupaddress.setTextColor(ContextCompat.getColor(this, R.color.text_black))

                isPickup = true
                binding.addressSearch.visibility = View.VISIBLE
                mapFragment.view!!.visibility = View.GONE
                binding.pinMap.visibility = View.GONE
                // code to execute when EditText loses focus
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    binding.pickupPoint.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_primary_text)
                    binding.destPoint.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_primary_color)
                }

            }
        }

        // Check dest address focus
        binding.destadddress.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                destAddsFocus()
            }
        }


        // Place search list
        binding.locationPlacesearch.visibility = View.GONE
        binding.locationPlacesearch.isNestedScrollingEnabled = false
        val mLinearLayoutManager = LinearLayoutManager(this)
        binding.locationPlacesearch.layoutManager = mLinearLayoutManager
        binding.locationPlacesearch.adapter = googleMapPlaceSearchAutoCompleteRecyclerView
    }

    /**
     * Connect Google APi Client
     */
    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    /**
     * Google APi on Connected
     */
    override fun onConnected(bundle: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        //val mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
            if (it != null)
                changeMap(it)
        }
    }

    /**
     * Google API suspended
     */
    override fun onConnectionSuspended(i: Int) {
        DebuggableLogI(TAG, "Connection suspended")
        mGoogleApiClient.connect()
    }

    /**
     * Google API connection failed
     */
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        DebuggableLogD(TAG, "onConnectionFailed")

    }


    private fun showPermissionDialog() {
        if (!PermissionCamer.checkPermission(mContext)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                    4)
            displayLocationSettingsRequest(mContext)
        } else {
            buildGoogleApiClient()
            // displayLocationSettingsRequest(mContext);
        }
    }


    private fun displayLocationSettingsRequest(context: Context) {
        val googleApiClient = GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build()
        googleApiClient.connect()

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = (5000 / 2).toLong()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { results ->
            val status = results.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> CommonMethods.DebuggableLogI(TAG, "All location settings are satisfied.")
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    DebuggableLogI(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ")

                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        status.startResolutionForResult(mContext as Activity, REQUEST_CHECK_SETTINGS)
                    } catch (e: IntentSender.SendIntentException) {
                       DebuggableLogI(TAG, "PendingIntent unable to execute request.")
                    }

                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> CommonMethods.DebuggableLogI(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.")
                else -> {
                }
            }
        }
    }

    fun checkGPSEnable() {
        if (checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            // Otherwise, prompt user to get valid Play Services APK.
            if (!AppUtils.isLocationEnabled(mContext)) {
                // notify user
                val dialog = AlertDialog.Builder(mContext)
                dialog.setMessage("LOCATION AND PERMISSION not enabled!")
                dialog.setPositiveButton("Open location settings") { _, _ ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                dialog.setNegativeButton("Cancel") { _, _ ->
                    // TODO Auto-generated method stub
                }
                dialog.show()
            }
            buildGoogleApiClient()
        } else {
            buildGoogleApiClient()
            Toast.makeText(mContext, "LOCATION_AND_WRITEPERMISSION_ARRAY not supported in this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPlayServices(): Boolean {
        //        code updated due to deprication, code updated by the reference of @link: https://stackoverflow.com/a/31016761/6899791
        //        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this); this is commented due to depricated
        val googleAPI = GoogleApiAvailability.getInstance()
        val resultCode = googleAPI.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(resultCode)) {
                googleAPI.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)!!.show()
            }
            return false
        }
        return true
    }

    /**
     * Move map to current location and get address while move amp
     */
    private fun changeMap(location: Location) {

        DebuggableLogD(TAG, "Reaching map" + googleMap)


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        // check if map is created successfully or not
        googleMap.uiSettings.isZoomControlsEnabled = false
        val latLong: LatLng
        latLong = LatLng(location.latitude, location.longitude)

        val cameraPosition = CameraPosition.Builder()
                .target(latLong).zoom(16.5f).tilt(0f).build()

        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.moveCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition))
    }

    public override fun onResume() {
        super.onResume()
        if (!mGoogleApiClient.isConnected && !mGoogleApiClient.isConnecting) {
            DebuggableLogV("Google API", "Connecting")
            mGoogleApiClient.connect()
        }
        //initilizeMap()
    }

    public override fun onPause() {
        super.onPause()
        if (mGoogleApiClient.isConnected) {
            DebuggableLogV("Google API", "Dis-Connecting")
            mGoogleApiClient.disconnect()
        }
    }

    /**
     * function to load map. If map is not created it will create it for you
     */
    @SuppressLint("UseRequireInsteadOfGet")
    private fun initilizeMap() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.search_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.view!!.visibility = View.GONE
        binding.pinMap.visibility = View.GONE
    }

    override fun onMapReady(Map: GoogleMap) {
        googleMap = Map

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))

        } catch (e: Resources.NotFoundException) {
            DebuggableLogE("Cabme", "Can't find style. Error: ", e)
        }

        googleMap.setOnCameraIdleListener(this)
        googleMap.setOnCameraMoveStartedListener(this)
        googleMap.setOnCameraMoveListener(this)
        googleMap.setOnCameraMoveCanceledListener(this)

    }

    override fun onBackPressed() {
        try {
            if (!sessionManager.isrequest && !sessionManager.isTrip) {
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    showPermissionDialog() // Do something for lollipop and above versions
                } else {
                    checkGPSEnable()  // do something for phones running on SDK before lollipop
                }*/
                Constants.changedPickupLatlng = null
          }
          super.onBackPressed()
          overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (v.id == R.id.pickupaddress) {
            if (hasFocus) {
                isPickup = true
                binding.pickupPoint.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_primary_text)
                binding.destPoint.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_primary_color)
            } else {
                isPickup = false
                binding.pickupPoint.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_primary_text)
                binding.destPoint.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_primary_color)
            }
        } else {
            if (!hasFocus) {
                isPickup = true
                binding.pickupPoint.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_primary_text)
                binding.destPoint.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_primary_color)
            } else {
                isPickup = false
                binding.pickupPoint.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_primary_text)
                binding.destPoint.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_primary_color)
            }
        }
    }

    /**
     * Show GPS permission Dialog
     */
    private fun showLocationPermissionDialog() {
        if (!Permission.checkPermission(this)) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                    4)
        }
    }

    /**
     * Map Camera move listener
     */
    override fun onCameraMoveStarted(reason: Int) {}

    override fun onCameraMove() {}

    override fun onCameraMoveCanceled() {}

    override fun onCameraIdle() {
        isMapMove =  true
        //Toast.makeText(this, "The camera has stopped moving.",Toast.LENGTH_SHORT).show();
        binding.dropDone.visibility = View.VISIBLE
        val cameraPosition = googleMap.cameraPosition

        if (!isFirst) {

            Listlat = cameraPosition.target.latitude.toString()
            Listlong = cameraPosition.target.longitude.toString()
            DebuggableLogI("centerLat", cameraPosition.target.latitude.toString())
            DebuggableLogI("centerLong", cameraPosition.target.longitude.toString())
            val cur_Latlng = LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude)

            if (isPickup) {
                markerPoints[0] = cur_Latlng
            } else {
                markerPoints[1] = cur_Latlng
            }
            fetchAddress(cur_Latlng, "settext")// Fetch address from latitude and longitude
        } else {
            isFirst = false
        }
    }

    /**
     * calculates the distance between two locations in MILES
     */
    private fun distance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {

        val earthRadius = 3958.75 // in miles, change to 6371 for kilometer output

        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val sindLat = Math.sin(dLat / 2)
        val sindLng = Math.sin(dLng / 2)

        val a = Math.pow(sindLat, 2.0) + (Math.pow(sindLng, 2.0)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)))

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c // output distance, in MILES
    }

    /**
     * Fetch Location from address if Geocode available get from geocode otherwise get location from google
     */
    fun fetchLocation(addresss: String, type: String) {
        getAddress = addresss

        object : AsyncTask<Void, Void, String>() {
            var locations: String = ""


            override fun doInBackground(vararg params: Void): String? {


                if (Geocoder.isPresent())
                // Check geo code available or not
                {
                    try {
                        println("Geocoder.isPresent")
                        val geocoder = Geocoder(applicationContext, Locale.getDefault())
                        val address: List<Address>?

                        // May throw an IOException
                        address = geocoder.getFromLocationName(getAddress, 5)
                        if (address == null) {
                            return null
                        }
                        val location = address[0]

                        countrys = address[0].countryName

                        if (isPickup) {
                            if ("" != countrys)
                                pickipCountry = getAddress

                        } else {
                            destCountry = getAddress

                            if ("" == destCountry)
                                destCountry = pickipCountry

                        }
                        location.latitude
                        location.longitude

                        lat = location.latitude.toString()
                        log = location.longitude.toString()
                        locations = "$lat,$log"
                    } catch (ignored: Exception) {
                        ignored.printStackTrace()
                        // after a while, Geocoder start to throw "Service not availalbe" exception. really weird since it was working before (same device, same Android version etc..
                    }

                }
                return if (locations != null)
                // i.e., Geocoder succeed
                {
                    locations
                } else
                // i.e., Geocoder failed
                {
                    fetchLocationUsingGoogleMap() // If geocode not available or location null call google API
                }
            }

            // Geocoder failed :-(
            // Our B Plan : Google Map
            private fun fetchLocationUsingGoogleMap(): String? {
                println("fetchLocationUsingGoogleMap")
                getAddress = getAddress.replace(" ".toRegex(), "%20")
                val googleMapUrl = ("https://maps.google.com/maps/api/geocode/json?address=" + getAddress + "&sensor=false"
                        + "&key=" + sessionManager.googleMapKey)

                println("GoogleMapUrl ${googleMapUrl}")
                try {
                    val googleMapResponse = JSONObject(ANDROID_HTTP_CLIENT.execute(HttpGet(googleMapUrl),
                            BasicResponseHandler()))

                    // many nested loops.. not great -> use expression instead
                    // loop among all results

                    if (googleMapResponse.length() > 0) {
                        val longitute = (googleMapResponse.get("results") as JSONArray).getJSONObject(0)
                                .getJSONObject("geometry").getJSONObject("location")
                                .getString("lng")

                        val latitude = (googleMapResponse.get("results") as JSONArray).getJSONObject(0)
                                .getJSONObject("geometry").getJSONObject("location")
                                .getString("lat")

                        val len = (googleMapResponse.get("results") as JSONArray).getJSONObject(0)
                                .getJSONArray("address_components").length()
                        for (i in 0 until len) {
                            if ((googleMapResponse.get("results") as JSONArray).getJSONObject(0)
                                            .getJSONArray("address_components").getJSONObject(i).getJSONArray("types").getString(0) == "country") {
                                countrys = (googleMapResponse.get("results") as JSONArray).getJSONObject(0)
                                        .getJSONArray("address_components").getJSONObject(i).getString("long_name")

                            }
                        }

                        if (isPickup) {
                            if ("" != countrys)
                                pickipCountry = countrys

                        } else {
                            destCountry = countrys

                        }
                        return "$latitude,$longitute"
                    } else {
                        return null
                    }

                } catch (ignored: Exception) {
                    ignored.printStackTrace()
                }

                return null
            }

            override fun onPostExecute(location: String?) {
                if (location != null) {
                    try{
                        val parts = location.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val lat = java.lang.Double.valueOf(parts[0])
                        val lng = java.lang.Double.valueOf(parts[1])
                        val latLng = LatLng(lat, lng)

                        if ("homeclick" == type) {
                            if (isPickup) {
                                Constants.changedPickupLatlng = latLng
                                markerPoints.set(0, latLng)
                                binding.pickupaddress.setText(getAddress)
                                homeClick()

                            } else {
                                markerPoints.set(1, latLng)
                                binding.destadddress.setText(getAddress)
                                homeClick()
                            }
                        } else if ("workclick" == type) {
                            if (isPickup) {
                                Constants.changedPickupLatlng = latLng
                                markerPoints.set(0, latLng)
                                binding.pickupaddress.setText(getAddress)
                                workClick()

                            } else {
                                markerPoints.set(1, latLng)
                                binding.destadddress.setText(getAddress)
                                workClick()
                            }

                        } else if ("searchitemclick" == type) {
                            if ("" == countrys)
                                countrys = pickipCountry

                            searchItemClick(latLng, countrys)
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }

                } else {
                    commonMethods.showMessage(this@PlaceSearchActivity, dialog, "Unable to get location please try again...")
                    commonMethods.hideProgressDialog()
                }
            }
        }.execute()
    }

    /**
     * Fetch address from location if Geocode available get from geocode otherwise get location from google
     */
    fun fetchAddress(location: LatLng, type: String) {
        getLocations = location
        address = ""
        object : AsyncTask<Void, Void, String>() {

            override fun doInBackground(vararg params: Void): String? {

                if (Geocoder.isPresent())
                // Check Geo code available or not
                {
                    print("Geo OK Ad")
                    try {

                        val geocoder = Geocoder(applicationContext, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(getLocations.latitude, getLocations.longitude, 1)
                        if (addresses != null) {
                            countrys = addresses[0].countryName

                            val adress0 = addresses[0].getAddressLine(0)
                           // val adress1 = addresses[0].getAddressLine(1)

                            address = adress0 //$adress1" // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()


                        }
                    } catch (ignored: Exception) {
                        // after a while, Geocoder start to throw "Service not availalbe" exception. really weird since it was working before (same device, same Android version etc..
                    }

                }
                return if (address != null)
                // i.e., Geocoder succeed
                {
                    address
                } else
                // i.e., Geocoder failed
                {
                    fetchAddressUsingGoogleMap()
                }
            }

            // Geocoder failed :-(
            // Our B Plan : Google Map
            private fun fetchAddressUsingGoogleMap(): String? {

                addressList = ArrayList1()


                val googleMapUrl = ("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + getLocations.latitude + ","
                        + getLocations.longitude + "&sensor=false" + "&key=" + sessionManager.googleMapKey)

                println("GoogleMapUrl $googleMapUrl")
                try {
                    val googleMapResponse = JSONObject(ANDROID_HTTP_CLIENT.execute(HttpGet(googleMapUrl),
                            BasicResponseHandler()))

                    // many nested loops.. not great -> use expression instead
                    // loop among all results

                    val results = googleMapResponse.get("results") as JSONArray
                    for (i in 0 until results.length()) {


                        val result = results.getJSONObject(i)


                        val indiStr = result.getString("formatted_address")


                        val addr = Address(Locale.getDefault())


                        addr.setAddressLine(0, indiStr)
                        //  country=addr.getCountryName();

                        addressList.add(addr)


                    }

                    val len = (googleMapResponse.get("results") as JSONArray).getJSONObject(0)
                            .getJSONArray("address_components").length()
                    for (i in 0 until len) {
                        if ((googleMapResponse.get("results") as JSONArray).getJSONObject(0)
                                        .getJSONArray("address_components").getJSONObject(i).getJSONArray("types").getString(0) == "country") {
                            countrys = (googleMapResponse.get("results") as JSONArray).getJSONObject(0)
                                    .getJSONArray("address_components").getJSONObject(i).getString("long_name")

                        }
                    }

                    val adress0 = addressList[0].getAddressLine(0)
                    address = adress0//+" "+adress1;
                    //address = adress0+" "+adress1; // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    address.replace("null", "")

                    return address

                } catch (ignored: Exception) {
                    ignored.printStackTrace()
                }

                return null
            }

            override fun onPostExecute(address: String?) {
                if (address != null) {
                    address.replace("null","")
                    if ("settext" == type) {
                        //destCountry=countrys;
                        if (isPickup) {
                            Constants.changedPickupLatlng = location
                            binding.pickupaddress.setText(address)
                            pickipCountry = countrys
                        } else {

                            binding.destadddress.setText(address)
                            destCountry = countrys
                        }
                    } else if ("country" == type) {
                        destCountry = countrys
                    }
                }

            }
        }.execute()
    }

    /**
     * Home Address click
     */
    fun homeClick() {
        val pickup = binding.pickupaddress.text.toString()
        val destup = binding.destadddress.text.toString()


        if ("".matches(pickup.toRegex()) || "".matches(destup.toRegex())) {
            if (isPickup) {
                binding.pickupaddress.removeTextChangedListener(NameTextWatcher(binding.pickupaddress))
                binding.destadddress.requestFocus()
            } else {
                binding.destadddress.removeTextChangedListener(NameTextWatcher(binding.destadddress))
                binding.pickupaddress.requestFocus()
            }
        } else {
            //  if (destCountry.equals(pickipCountry)) {
            val l1 = markerPoints.get(0)
            val l2 = markerPoints.get(1)
            if (distance(l1.latitude, l1.longitude, l2.latitude, l2.longitude) < 0.1) {
                commonMethods.showMessage(this@PlaceSearchActivity, dialog, resources.getString(R.string.pickup_and_drop_location_is_same))
            } else {
                if (markerPoints.get(0) != staticLatLng) {
                    sessionManager.isrequest=true
                    val intent = Intent(this, MainActivity::class.java)
                    if (null != getIntent().getStringExtra("Schedule"))
                        if (getIntent().getStringExtra("Schedule") == "Schedule") {
                            intent.putExtra("Schedule", "Schedule")
                        } else {
                            intent.putExtra("Schedule", "")
                        }
                    intent.putExtra(CommonKeys.PickupAddress, binding.pickupaddress.text.toString())
                    intent.putExtra(CommonKeys.DropAddress, binding.destadddress.text.toString())
                    intent.putExtra("PickupDrop", markerPoints)
                    startActivity(intent)
                } else {
                    commonMethods.showMessage(this@PlaceSearchActivity, dialog, resources.getString(R.string.unable_to_get_location_pickup))
                }
            }
        }
    }

    /**
     * Work address click
     */
    fun workClick() {
        binding.locationPlacesearch.visibility = View.GONE
        val pickup = binding.pickupaddress.text.toString()
        val destup = binding.destadddress.text.toString()
        if (pickup.matches("".toRegex()) || destup.matches("".toRegex())) {
            if (isPickup) {
                binding.pickupaddress.removeTextChangedListener(NameTextWatcher(binding.pickupaddress))
                binding.destadddress.requestFocus()
            } else {
                binding.destadddress.removeTextChangedListener(NameTextWatcher(binding.destadddress))
                binding.pickupaddress.requestFocus()
            }
        } else {
            val l1 = markerPoints.get(0)
            val l2 = markerPoints.get(1)
            // if (destCountry.equals(pickipCountry)) {
            if (distance(l1.latitude, l1.longitude, l2.latitude, l2.longitude) < 0.1) {
                commonMethods.showMessage(this@PlaceSearchActivity, dialog, "Pickup and Drop location is same...")
            } else {
                if (markerPoints.get(0) != staticLatLng) {
                    sessionManager.isrequest=true
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra(CommonKeys.PickupAddress, binding.pickupaddress.text.toString())
                    intent.putExtra(CommonKeys.DropAddress, binding.destadddress.text.toString())
                    if (null != getIntent().getStringExtra("Schedule"))
                        if (getIntent().getStringExtra("Schedule") == "Schedule") {
                            intent.putExtra("Schedule", "Schedule")
                        } else {
                            intent.putExtra("Schedule", "")
                        }
                    intent.putExtra("PickupDrop", markerPoints)
                    startActivity(intent)
                } else {
                    commonMethods.showMessage(this@PlaceSearchActivity, dialog, resources.getString(R.string.unable_to_get_location_pickup))
                }
            }
        }

    }

    /**
     * Place search item click
     */
    fun searchItemClick(cur_Latlng: LatLng, country: String?) {
        val ll = LatLng(1.0, 1.0)
        destCountry = country
        Listlat = cur_Latlng.latitude.toString()
        Listlong = cur_Latlng.longitude.toString()
        // destCountry = getCountry(cur_Latlng.latitude, cur_Latlng.longitude);
        if (isPickup) {
            Constants.changedPickupLatlng = cur_Latlng
            if ("" != destCountry)
                pickipCountry = destCountry
            markerPoints.set(0, cur_Latlng)

            if (markerPoints.get(1) != ll) {

                /*if (destCountry.equals(pickipCountry)) {*/

                if (binding.pickupaddress.text.toString().trim { it <= ' ' }.length == 0 || binding.destadddress.text.toString().trim { it <= ' ' }.length == 0) {
                    commonMethods.hideProgressDialog()
                } else {
                    val l1 = markerPoints.get(0)
                    val l2 = markerPoints.get(1)
                    if (distance(l1.latitude, l1.longitude, l2.latitude, l2.longitude) < 0.1) {
                        commonMethods.showMessage(this@PlaceSearchActivity, dialog, resources.getString(R.string.pickup_and_drop_location_is_same))
                    } else {
                        sessionManager.isrequest=true
                        val intent = Intent(this, MainActivity::class.java)
                        if (getIntent().getStringExtra("Schedule") == "Schedule") {
                            intent.putExtra("Schedule", "Schedule")
                            intent.putExtra("date", date)
                        } else {
                            intent.putExtra("Schedule", "")
                        }
                        intent.putExtra(CommonKeys.PickupAddress, binding.pickupaddress.text.toString())
                        intent.putExtra(CommonKeys.DropAddress, binding.destadddress.text.toString())
                        intent.putExtra("PickupDrop", markerPoints)
                        startActivity(intent)
                    }
                }

            } else {
                binding.destadddress.requestFocus()
            }

        } else {
            markerPoints.set(1, cur_Latlng)
            if (binding.pickupaddress.text.toString().trim { it <= ' ' }.isEmpty() || binding.destadddress.text.toString().trim { it <= ' ' }.length == 0) {
                commonMethods.hideProgressDialog()
            }
            if (binding.pickupaddress.text.toString() == binding.destadddress.text.toString()) {
                val l1 = markerPoints.get(0)
                val l2 = markerPoints.get(1)
                if (distance(l1.latitude, l1.longitude, l2.latitude, l2.longitude) < 0.1) {
                    commonMethods.showMessage(this@PlaceSearchActivity, dialog, resources.getString(R.string.pickup_and_drop_location_is_same))
                }
            } else {
                if (markerPoints.get(0) != staticLatLng) {
                    sessionManager.isrequest=true

                    val intent = Intent(this, MainActivity::class.java)

                    intent.putExtra(CommonKeys.PickupAddress, binding.pickupaddress.text.toString())
                    intent.putExtra(CommonKeys.DropAddress, binding.destadddress.text.toString())
                    if (getIntent().getStringExtra("Schedule") == "Schedule") {
                        intent.putExtra("Schedule", "Schedule")
                        intent.putExtra("date", date)
                    } else {
                        intent.putExtra("Schedule", "")
                    }
                    intent.putExtra("PickupDrop", markerPoints)
                    startActivity(intent)
                } else {
                    commonMethods.showMessage(this@PlaceSearchActivity, dialog, resources.getString(R.string.unable_to_get_location_pickup))
                }
            }
        }

        commonMethods.hideProgressDialog()
        counti = 0
    }

    public override fun onDestroy() {
        super.onDestroy()
        try {
            ANDROID_HTTP_CLIENT.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun selectedAddress(autocompletePrediction: AutocompletePrediction?) {
        var autocompletePrediction = autocompletePrediction

        if (counti > 0) {
            autocompletePrediction = null
        }
        if (autocompletePrediction != null) {
            counti++
            if (!this@PlaceSearchActivity.isFinishing) {
                commonMethods.showProgressDialog(this@PlaceSearchActivity)
            }


            DebuggableLogI("TAG", "Autocomplete item selected: " + autocompletePrediction.getFullText(null))

            searchlocation = autocompletePrediction.getFullText(null).toString()
            if (isPickup) {
                binding.pickupaddress.setText(autocompletePrediction.getFullText(null)).toString()

                oldstring = autocompletePrediction.getPrimaryText(null).toString()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.pickupaddress.windowToken, 0)
                binding.locationPlacesearch.visibility = View.GONE
            } else {
                binding.destadddress.setText(autocompletePrediction.getFullText(null)).toString()
                oldstring = autocompletePrediction.getPrimaryText(null).toString()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.destadddress.windowToken, 0)
            }
            binding.locationPlacesearch.visibility = View.GONE

            fetchLocation(searchlocation, "searchitemclick")

            DebuggableLogI("TAG", "Clicked: " + autocompletePrediction.getPrimaryText(null))
            DebuggableLogI("TAG", "Called getPlaceById to get Place details for " + autocompletePrediction.placeId)
        }
    }

    private inner class NameTextWatcher(private val view: View) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
            if (s.toString() == "") {
                binding.locationPlacesearch.visibility = View.GONE
            } else {
              //  binding.locationPlacesearch.visibility = View.VISIBLE
            }
        }

        override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
            if (s.toString() == "") {
                binding.locationPlacesearch.visibility = View.GONE
            } else {
               //binding.locationPlacesearch.visibility = View.VISIBLE
            }
        }

        override fun afterTextChanged(s: Editable) {
            isInternetAvailable = commonMethods.isOnline(applicationContext)
            if (!isInternetAvailable) {
                commonMethods.showMessage(this@PlaceSearchActivity, dialog, getString(R.string.no_connection))
            }

            if (view.hasFocus()) {
                if (s.toString() != "") {
                    Handler().postDelayed({
                        if (!isMapMove) {
                            if (oldstring != s.toString()) {
                                oldstring = s.toString()
                                showAddressSearchProgressbar()
                                getFullAddressUsingEdittextStringFromGooglePlaceSearchAPI(s.toString())
                            }
                        } else {
                            isMapMove = false
                        }
                    }, 3000)

                } else {
                    clearAddressAndHideRecyclerView()
                }

            } else {
                clearAddressAndHideRecyclerView()
            }
        }
    }

    fun getFullAddressUsingEdittextStringFromGooglePlaceSearchAPI(queryAddress: String) {
        //RectangularBounds bounds = RectangularBounds.newInstance(first, sec);

        var countryNameCode = ""
        if (resources.getString(R.string.country_level_address_search).equals("true", true))
            countryNameCode = sessionManager.countryNameCode.toString()

        var request=FindAutocompletePredictionsRequest.builder()
            .setSessionToken(googleAutoCompleteToken)
            .setQuery(queryAddress)
            .setCountries(countryNameCode)
            .build()

        // This is to set the boundaries for the search
        /*try {
            if (objLatLng != null) {
                val center = LatLng(objLatLng.latitude, objLatLng.longitude)
                val bounds = RectangularBounds.newInstance(
                    LatLng(center.latitude - 0.36, center.longitude - 0.36), // ~40 km in degrees
                    LatLng(center.latitude + 0.36, center.longitude + 0.36))

                request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(googleAutoCompleteToken)
                    .setQuery(queryAddress)
                    .setCountries(countryNameCode) // Restrict by country
                    .setLocationBias(bounds)       // Bias the search within the 40 km box
                    .build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(googleAutoCompleteToken)
                .setQuery(queryAddress)
                .setCountries(countryNameCode)
                .build()
        }*/

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                hideAddressSearchProgressbar()
                println("GoogleMapUrl placeSearch $response")
                if (response.autocompletePredictions.size > 0) {
                    googleMapPlaceSearchAutoCompleteRecyclerView.updateList(response.autocompletePredictions)
                    binding.locationPlacesearch.visibility = View.VISIBLE
                } else {
                    clearAddressAndHideRecyclerView()
                }
            }
            .addOnFailureListener { exception ->
                hideAddressSearchProgressbar()
                if (exception is ApiException) {
                    DebuggableLogE(TAG, "Place not found: " + exception.statusCode)
                }
                exception.printStackTrace()
            }
    }

    fun hideAddressSearchProgressbar() {
        binding.pbAddressSearchbarLoading.visibility = View.INVISIBLE
    }

    fun showAddressSearchProgressbar() {
        binding.pbAddressSearchbarLoading.visibility = View.VISIBLE
    }

    fun clearAddressAndHideRecyclerView() {
        googleMapPlaceSearchAutoCompleteRecyclerView.clearAddresses()
        binding.locationPlacesearch.visibility = View.GONE
    }

    companion object {

        private val BOUNDS_INDIA = LatLngBounds(
                LatLng(-0.0, 0.0), LatLng(0.0, 0.0))
        private val TAG = "MAP LOCATION"
    }
}