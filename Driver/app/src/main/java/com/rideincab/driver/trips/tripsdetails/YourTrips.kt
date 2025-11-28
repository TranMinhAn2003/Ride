package com.rideincab.driver.trips.tripsdetails

/**
 * @package com.cloneappsolutions.cabmedriver.trips.tripsdetails
 * @subpackage tripsdetails model
 * @category YourTrips
 * @author SMR IT Solutions
 *
 */

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager

import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.AppActivityYourTipsBinding
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.YourTripsListener
import javax.inject.Inject

/* ************************************************************
                YourTrips page
Its used to your current all the trips to show the fuction
*************************************************************** */
class YourTrips : CommonActivity(), TabLayout.OnTabSelectedListener, YourTripsListener {

    private lateinit var binding:AppActivityYourTipsBinding

    lateinit var dialog: AlertDialog

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog

   /* //This is our tablayout
    @BindView(R.id.tabLayout)
    lateinit var tabLayout: TabLayout

    //This is our viewPager
    @BindView(R.id.pager)
    lateinit var viewPager: ViewPager*/

    protected var isInternetAvailable: Boolean = false

    override val res: Resources
        get() = this@YourTrips.resources

    override val instance: YourTrips
        get() = this@YourTrips


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityYourTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.getAppComponent().inject(this)
        sessionManager.isTrip = false
        commonMethods.setheaderText(resources.getString(R.string.triphistory),binding.commonHeader.headertext)
        dialog = commonMethods!!.getAlertDialog(this)
        isInternetAvailable = commonMethods!!.isOnline(this)
        setupViewPager(binding.pager!!)
        binding.tabLayout!!.setupWithViewPager(binding.pager)
        binding.commonHeader.back.setOnClickListener { onBackPressed() }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(PendingTripsFragment(), getString(R.string.pending_trips))
        adapter.addFragment(CompletedTripsFragments(), getString(R.string.completed_trips))
        viewPager.adapter = adapter
        val layoutDirection = getString(R.string.layout_direction)
        if ("1" == layoutDirection)
            viewPager.rotationY = 180f
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        binding.pager!!.currentItem = tab.position
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}
    override fun onTabReselected(tab: TabLayout.Tab) {}

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }
}