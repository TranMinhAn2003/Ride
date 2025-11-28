package com.rideincab.user.taxi.sidebar.trips

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage Side_Bar.trips
 * @category YourTrips
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AlertDialog

import com.google.gson.Gson
import com.rideincab.user.R
import com.rideincab.user.taxi.adapters.ViewPagerAdapter
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.taxi.interfaces.YourTripsListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.taxi.views.customize.CustomDialog
import com.rideincab.user.taxi.views.main.MainActivity

import javax.inject.Inject


import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogI
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityYourTripsBinding

/* ************************************************************
    YourTrips connect view pager
    *********************************************************** */
class YourTrips : CommonActivity(), TabLayout.OnTabSelectedListener, YourTripsListener {

    private lateinit var binding: AppActivityYourTripsBinding
    lateinit var dialog: AlertDialog

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson


/*    //This is our tablayout
    @BindView(R.id.binding.tabLayout)
    lateinit var binding.tabLayout: TabLayout

    //This is our binding.pager
    @BindView(R.id.pager)
    lateinit var binding.pager: ViewPager*/
    
    protected var isInternetAvailable: Boolean = false

    override val res: Resources
        get() = this@YourTrips.resources

    override val instance: YourTrips
        get() = this@YourTrips


    fun onBack() {
        if (intent.getStringExtra("upcome") == "upcome") {
            val intent = Intent(this@YourTrips, MainActivity::class.java)
            intent.putExtra("upcome", "upcome")
            startActivity(intent)
        } else {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        if (intent.getStringExtra("upcome") == "upcome") {
            val intent = Intent(this@YourTrips, MainActivity::class.java)
            intent.putExtra("upcome", "upcome")
            startActivity(intent)
        }
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityYourTripsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)


        /**Commmon Header Text View */
        commonMethods.setHeaderText(resources.getString(R.string.YourTrips), binding.commonHeader.tvHeadertext)

        isInternetAvailable = commonMethods.isOnline(applicationContext)
        dialog = commonMethods.getAlertDialog(this)

        if (!isInternetAvailable) {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.no_connection))

        } else {
            setupViewPager(binding.pager)
            binding.tabLayout.setupWithViewPager(binding.pager)
        }
        if (intent.getStringExtra("upcome") == "upcome") {
            switchTab(1)
        } else {
            switchTab(0)
        }


        setupViewPager(binding.pager)
        binding.tabLayout.setupWithViewPager(binding.pager)
        binding.commonHeader.back.setOnClickListener { onBack() }
    }

    /**
     * Setup tab
     */

    private fun setupViewPager(pager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(Past(), resources.getString(R.string.pasttrip))
        adapter.addFragment(Upcoming(), resources.getString(R.string.upcomingtrips))
        pager.adapter = adapter
        if (intent.getStringExtra("upcome") == "upcome") {
            switchTab(1)
        } else {
            switchTab(0)
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        binding.pager.currentItem = tab.position

    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        DebuggableLogI("Cabme", "Tab")
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        DebuggableLogI("Cabme", "Tab")
    }

    fun switchTab(tabno: Int) {
        binding.pager.currentItem = tabno
    }


}
