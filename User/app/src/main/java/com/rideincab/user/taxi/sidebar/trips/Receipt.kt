package com.rideincab.user.taxi.sidebar.trips

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage Side_Bar.trips
 * @category Receipt
 * @author SMR IT Solutions
 *
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.rideincab.user.R
import com.rideincab.user.taxi.adapters.PriceRecycleAdapter
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.network.AppController
import com.rideincab.user.databinding.AppActivityReceiptBinding
import com.rideincab.user.taxi.sidebar.trips.TripDetails.Companion.tripDetailsModel
import javax.inject.Inject


/* ************************************************************
    Receipt view page tap fragment
    *********************************************************** */
class Receipt : Fragment() {
    
    private lateinit var binding:AppActivityReceiptBinding
    @Inject
    lateinit var sessionManager: SessionManager

/*    @BindView(R.id.binding.basefareAmount)
    lateinit var binding.basefareAmount: TextView

    @BindView(R.id.binding.distanceFare)
    lateinit var binding.distanceFare: TextView

    @BindView(R.id.binding.timeFare)
    lateinit var binding.timeFare: TextView

    @BindView(R.id.binding.fee)
    lateinit var binding.fee: TextView

    @BindView(R.id.binding.totalamount)
    lateinit var binding.totalamount: TextView

    @BindView(R.id.walletamount)
    lateinit var walletamount: TextView

    @BindView(R.id.promoamount)
    lateinit var promoamount: TextView

    @BindView(R.id.payableamount)
    lateinit var payableamount: TextView

    @BindView(R.id.walletamountlayout)
    lateinit var walletamountlayout: RelativeLayout

    @BindView(R.id.promoamountlayout)
    lateinit var promoamountlayout: RelativeLayout

    @BindView(R.id.payableamountlayout)
    lateinit var payableamountlayout: RelativeLayout

    @BindView(R.id.rvPrice)
    lateinit var binding.rvPrice: RecyclerView*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AppActivityReceiptBinding.inflate(inflater, container, false)
        AppController.appComponent.inject(this)

        binding.rvPrice.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(activity)
        binding.rvPrice.layoutManager = layoutManager
        val invoiceModels = tripDetailsModel.riders.get(0).invoice
        val adapter = PriceRecycleAdapter(requireActivity(), invoiceModels)
        binding.rvPrice.adapter = adapter

        /**
         * Show the receipt details for the trip
         */

        return binding.root
    }
}
