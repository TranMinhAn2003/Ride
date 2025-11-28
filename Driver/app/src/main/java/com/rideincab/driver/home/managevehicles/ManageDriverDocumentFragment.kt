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
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ManageDocumentsFragmentBinding
import com.rideincab.driver.home.datamodel.DriverProfileModel
import javax.inject.Inject

class ManageDriverDocumentFragment : Fragment(), ManageDocumentsAdapter.OnClickListener {

    lateinit var manageDocuments: ManageDocumentsFragmentBinding


    @Inject
    lateinit var sessionManager: SessionManager

    lateinit var driverProfileModel: DriverProfileModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        manageDocuments = ManageDocumentsFragmentBinding.inflate(inflater, container, false)
        AppController.getAppComponent().inject(this)
        driverProfileModel = Gson().fromJson(sessionManager.profileDetail, DriverProfileModel::class.java)

        Log.i("ManageDocuments", "onCreateView:ManageDriverDocument-> driverProfileModel=${Gson().toJson(driverProfileModel)}")
        Log.i("ManageDocuments", "onCreateView:ManageDriverDocument-> driverProfileModel driverDocuments=${Gson().toJson(driverProfileModel.driverDocuments)}")

        initRecyclerView()
        (activity as DocumentDetails).setHeader(resources.getString(R.string.manage_driver_document))

        return manageDocuments.root
    }


    private fun initRecyclerView() {

        val adapter = ManageDocumentsAdapter(requireContext(), driverProfileModel.driverDocuments, this)
        manageDocuments.rvDocs.adapter = adapter


        if(driverProfileModel.driverDocuments.size == 0)
            manageDocuments.tvNoDocument.visibility = View.VISIBLE
        else
            manageDocuments.tvNoDocument.visibility = View.GONE

    }

    override fun onClick(pos: Int) {
        (activity as DocumentDetails).documentPosition = pos
        findNavController().navigate(R.id.action_documentFragment_to_viewDocumentFragment)

    }

}
