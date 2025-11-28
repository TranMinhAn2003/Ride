package com.rideincab.driver.home.managevehicles

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView

import com.google.gson.Gson
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.home.managevehicles.adapter.ManageDocumentsAdapter
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.views.applySystemBarsBottomPadding
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ManageDocumentsFragmentBinding
import com.rideincab.driver.home.datamodel.DriverProfileModel
import javax.inject.Inject

class ManageVehicleDocumentFragment : Fragment(), ManageDocumentsAdapter.OnClickListener {

    private lateinit var binding: ManageDocumentsFragmentBinding

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var sessionManager: SessionManager

    lateinit var driverProfileModel: DriverProfileModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ManageDocumentsFragmentBinding.inflate(inflater, container, false)

        AppController.getAppComponent().inject(this)
//        binding.root.applySystemBarsBottomPadding()
        driverProfileModel =
            Gson().fromJson(sessionManager.profileDetail, DriverProfileModel::class.java)

        Log.i(
            "ManageDocuments",
            "onCreateView: ManageVehicleDocument-> driverProfileModel=${
                Gson().toJson(driverProfileModel)
            }"
        )

        initRecyclerView()

        return binding.root
    }


    private fun initRecyclerView() {

        val adapter = ManageDocumentsAdapter(
            requireContext(),
            driverProfileModel.vehicle.get((activity as ManageVehicles).documentClickedPosition!!).document,
            this
        )
        binding.rvDocs.adapter = adapter

        (activity as ManageVehicles).setHeader(getString(R.string.manage_documents))
        (activity as ManageVehicles).hideAddButton()

        if (driverProfileModel.vehicle.size == 0)
            binding.tvNoDocument.visibility = View.VISIBLE
        else
            binding.tvNoDocument.visibility = View.GONE

    }

    override fun onClick(pos: Int) {
        (activity as ManageVehicles).documentPosition = pos
        findNavController().navigate(R.id.action_documentFragment_to_viewDocumentFragment2)

    }

}
