package com.rideincab.user.taxi.sidebar

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage Side_Bar
 * @category FareBreakdown
 * @author SMR IT Solutions
 *
 */

import android.os.Bundle
import android.widget.TextView

import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityFareBreakdownBinding
import com.rideincab.user.taxi.datamodels.main.NearestCar
import java.util.*
import javax.inject.Inject

/* ************************************************************
   Price break for selected car details
    *********************************************************** */
class FareBreakdown : CommonActivity() {
    
    private lateinit var binding: AppActivityFareBreakdownBinding
    
    @Inject
    lateinit var sessionManager: SessionManager

    /*@BindView(R.id.binding.amount1)
    lateinit var binding.amount1: TextView

    @BindView(R.id.binding.amount2)
    lateinit var binding.amount2: TextView

    @BindView(R.id.binding.amount3)
    lateinit var binding.amount3: TextView

    @BindView(R.id.binding.amount4)
    lateinit var binding.amount4: TextView*/
    
    lateinit var searchlist: ArrayList<NearestCar>
    var position: Int = 0

    @Inject
    lateinit var commonMethods: CommonMethods



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityFareBreakdownBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)
        commonMethods.setHeaderText(resources.getString(R.string.farebreakdown), binding.commonHeader.tvHeadertext)
        searchlist = intent.getSerializableExtra("list") as ArrayList<NearestCar>
        position = intent.getIntExtra("position", 0)

        /**
         * Show price breakdown for selected car before send request
         */
        binding.amount1.text = sessionManager.currencySymbol + searchlist[position].baseFare
        binding.amount2.text = sessionManager.currencySymbol + searchlist[position].baseFare
        binding.amount3.text = sessionManager.currencySymbol + searchlist[position].perMin
        binding.amount4.text = sessionManager.currencySymbol + searchlist[position].perKm
        binding.commonHeader.back.setOnClickListener { super.onBackPressed() }
    }

    /**
     * Back button to close
     */
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }
}
