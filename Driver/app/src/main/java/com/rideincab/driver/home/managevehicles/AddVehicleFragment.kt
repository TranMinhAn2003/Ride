package com.rideincab.driver.home.managevehicles

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.gson.Gson
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.Enums.ADD_UPDATE_VEHICLE
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.applySystemBarsBottomPadding
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.AddVehicleLayoutBinding
import com.rideincab.driver.home.datamodel.AddedVehiclesModel
import com.rideincab.driver.home.datamodel.Make
import com.rideincab.driver.home.datamodel.Model
import com.rideincab.driver.home.datamodel.VehicleTypes
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import com.rideincab.driver.home.managevehicles.adapter.MakeAdapter
import com.rideincab.driver.home.managevehicles.adapter.ModelAdapter
import com.rideincab.driver.home.managevehicles.adapter.YearAdapter
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet


class AddVehicleFragment : androidx.fragment.app.Fragment(), VehicleTypeAdapter.OnClickListener, ServiceListener, FeatureSelectListener {

    private lateinit var binding:AddVehicleLayoutBinding

    private var addedVehicleDetails = AddedVehiclesModel()

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

    var make: Make? = null
    var model: Model? = null

    private var dialog: AlertDialog? = null
    private var makePosition: Int = 0
    private lateinit var yearAdapter: YearAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var makeAdapter: MakeAdapter
    private lateinit var modelAdapter: ModelAdapter

    private var yearList = ArrayList<Int>()
    private lateinit var rvVehicleDesc: RecyclerView
    var vehicleDescriptionDialog: android.app.AlertDialog? = null

    lateinit var addVehicles: View
    lateinit var vehicleTypeAdapter: VehicleTypeAdapter

    lateinit var featuresInVehicleAdapter: FeaturesInVehicleAdapter
    private var selectedIds = LinkedHashSet<Int>()

    fun onBtnAdd() {


        vehTypeSelcIds = ""

        for (i in (activity as ManageVehicles).makeModelDetails.vehicleTypes.indices) {

            if ((activity as ManageVehicles).makeModelDetails.vehicleTypes.get(i).isChecked) {
                if (vehTypeSelcIds.equals("")) {
                    vehTypeSelcIds = (activity as ManageVehicles).makeModelDetails.vehicleTypes.get(i).id.toString()
                } else {
                    vehTypeSelcIds = vehTypeSelcIds + "," + (activity as ManageVehicles).makeModelDetails.vehicleTypes.get(i).id.toString()
                }
            }

        }



        if (isParamsNotEmpty()) {
            updateVehicleApi()
        }


    }

    private fun isParamsNotEmpty(): Boolean {

        if (binding.tvMakeType.text.toString().equals("")) {
            Toast.makeText(context, resources.getString(R.string.please_choose_make), Toast.LENGTH_LONG).show()
            binding.tvMakeType.setError(resources.getString(R.string.please_choose_make))
            binding.tvMakeType.requestFocus()
            return false
        }
        if (binding.tvModelType.text.toString().equals("")) {
            Toast.makeText(context, resources.getString(R.string.please_choose_model), Toast.LENGTH_LONG).show()
            binding.tvModelType.setError(resources.getString(R.string.please_choose_model))
            binding.tvModelType.requestFocus()
            return false
        }
        if (binding.tvYearType.text.toString().equals("")) {
            Toast.makeText(context, resources.getString(R.string.please_choose_year), Toast.LENGTH_LONG).show()
            binding.tvYearType.setError(resources.getString(R.string.please_choose_year))
            binding.tvYearType.requestFocus()
            return false
        }
        if (binding.tvLicenseType.text.toString().equals("")) {
            Toast.makeText(context, resources.getString(R.string.please_choose_license_number), Toast.LENGTH_LONG).show()
            binding.tvLicenseType.setError(resources.getString(R.string.please_choose_license_number))
            binding.tvLicenseType.requestFocus()
            return false
        }
        /* if(binding.tvVehicleName.text.toString().equals("")){
              Toast.makeText(context,resources.getString(R.string.please_enter_vehicle_name),Toast.LENGTH_LONG).show()
             binding.tvVehicleName.setError(resources.getString(R.string.please_enter_vehicle_name))
             binding.tvVehicleName.requestFocus()
             return false
         }*/
        if (binding.tvVehicleColor.text.toString().equals("")) {
            Toast.makeText(context, resources.getString(R.string.please_enter_vehicle_color), Toast.LENGTH_LONG).show()
            binding.tvVehicleColor.setError(resources.getString(R.string.please_enter_vehicle_color))
            binding.tvVehicleColor.requestFocus()
            return false
        }
        if (vehTypeSelcIds.equals("")) {
            Toast.makeText(context, resources.getString(R.string.please_choose_vehicle_type), Toast.LENGTH_LONG).show()
            return false
        }

        return true

    }


    var vehTypeSelcIds: String = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding= AddVehicleLayoutBinding.inflate(inflater,container,false)
        AppController.getAppComponent().inject(this)
//        binding.root.applySystemBarsBottomPadding()

        initRecyclerView()
        initRequestOptionviews()
        initViews()
        (activity as ManageVehicles).hideAddButton()


        binding.tvModelType.addTextChangedListener(TextCheckwatcher(binding.tvModelType))
        binding.tvMakeType.addTextChangedListener(TextCheckwatcher(binding.tvMakeType))
        binding.tvYearType.addTextChangedListener(TextCheckwatcher(binding.tvYearType))
        binding.tvVehicleColor.addTextChangedListener(TextCheckwatcher(binding.tvVehicleColor))
        binding.tvVehicleName.addTextChangedListener(TextCheckwatcher(binding.tvVehicleName))

        binding.rltYear.setOnClickListener {
            vehicleDescPopup(2)
        }

        binding.rltMake.setOnClickListener {
            vehicleDescPopup(0)
        }

        binding.rltModel.setOnClickListener {
            vehicleDescPopup(1)
        }

        binding.btnAddVehicle.setOnClickListener {
            onBtnAdd()
        }

        return binding.root
    }

    class TextCheckwatcher(val textview: TextView) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            textview.setError(null)
        }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun initViews() {
        dialog = commonMethods.getAlertDialog(context!!)
        if ((activity as ManageVehicles).vehicleClickPosition != null) {
            binding.tvMakeType.text = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).make?.name
            binding.tvModelType.text = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).model?.name
            binding.tvYearType.text = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).year
            binding.tvLicenseType.setText( (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).licenseNumber)
            binding.tvVehicleColor.setText( (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).vehicleColor)
            binding.tvVehicleName.setText( (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).vehicleName)
            (activity as ManageVehicles).setHeader(getString(R.string.update_vehicles))
            binding.btnAddVehicle.setText(getString(R.string.update_vehicle))
            getMakePosition()
            make = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).make
            model = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).model


        } else {
            (activity as ManageVehicles).setHeader(getString(R.string.add_vehicles))
            binding.btnAddVehicle.setText(getString(R.string.add_vehicles))
        }
    }

    private fun getMakePosition() {

        for (i in (activity as ManageVehicles).makeModelDetails.make.indices) {
            if ((activity as ManageVehicles).makeModelDetails.make.get(i).name.equals(binding.tvMakeType.text.toString())) {
                makePosition = i
                break
            }
        }
    }


    /**
     * type 1 : Make
     */

    // Load currency list deatils in dialog
    fun vehicleDescPopup(type: Int) {
        rvVehicleDesc = RecyclerView((activity as ManageVehicles).getContFromAct())
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.header, null)
        val header = view.findViewById<TextView>(R.id.header)



        if (type == 0) {
            initMakeRv()
            header.text = getString(R.string.choose_make)
        } else if (type == 1) {
            initModelRv()
            header.text = getString(R.string.choose_model)
        } else if (type == 2) {
            initYearRv()
            header.text = getString(R.string.choose_year)
        }




        vehicleDescriptionDialog = android.app.AlertDialog.Builder(context)
                .setCustomTitle(view)
                .setView(rvVehicleDesc)
                .setCancelable(true)
                .show()


    }


    private fun initYearRv() {

        var year = (activity as ManageVehicles).makeModelDetails.year
        yearList.clear()

        while (year <= Calendar.getInstance().get(Calendar.YEAR)) {
            yearList.add(year)
            year++

        }

        yearAdapter = YearAdapter((activity as ManageVehicles).getContFromAct())
        yearAdapter.initYearModel(yearList)
        //(activity as ManageVehicles).vehicleClickPosition?.let { (activity as ManageVehicles).vehicleDetails.get(it).year.let { yearAdapter.initCurrentYear(it) } }

        yearAdapter.initCurrentYear(binding.tvYearType.text.toString())
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvVehicleDesc.layoutManager = linearLayoutManager
        rvVehicleDesc.adapter = yearAdapter

        /*  val laydir = getString(R.string.layout_direction)
          if ("1" == laydir)
              rvVehicleDesc.rotationY = 180f*/



        yearAdapter.setOnYearClickListner(object : YearAdapter.onYearClickListener {
            override fun setYearClick(year: Int, position: Int) {
                vehicleDescriptionDialog?.dismiss()
                /*if ((activity as ManageVehicles).vehicleClickPosition != null)
                    (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).year = yearList.get(position).toString()*/
                binding.tvYearType.text = yearList.get(position).toString()
            }
        })
    }


    private fun initMakeRv() {


        makeAdapter = MakeAdapter((activity as ManageVehicles).getContFromAct())
        makeAdapter.initMakeModel((activity as ManageVehicles).makeModelDetails.make)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        linearLayoutManager.reverseLayout = true
        rvVehicleDesc.layoutManager = linearLayoutManager
        rvVehicleDesc.setHasFixedSize(true)
        rvVehicleDesc.smoothScrollToPosition(0)
        rvVehicleDesc.adapter = makeAdapter
        /*  val laydir = getString(R.string.layout_direction)
          if ("1" == laydir)
              rvVehicleDesc.rotationX = 180f
  */
        makeAdapter.initCurrentMake(binding.tvMakeType.text.toString())


        makeAdapter.setOnMakeClickListner(object : MakeAdapter.onMakeClickListener {
            override fun setMakeClick(selectedMake: Make, position: Int) {
                makePosition = position
                make = selectedMake
                vehicleDescriptionDialog?.dismiss()
                binding.tvMakeType.text = (activity as ManageVehicles).makeModelDetails.make.get(position).name
                (activity as ManageVehicles).deselectMake()
                emptyYear()
                binding.tvModelType.text = ""
                (activity as ManageVehicles).makeModelDetails.make.get(position).isSelected = true
            }
        })
    }

    fun emptyYear() {
        binding.tvYearType.text = ""
    }


    private fun updateVehicleApi() {
        (activity as ManageVehicles).vehicleUpdate = false
        commonMethods.showProgressDialog((activity as ManageVehicles).getAppCompatActivity())
        apiService.updateVehicle(getVehicleHashMap()).enqueue(RequestCallback(ADD_UPDATE_VEHICLE, this))

    }

    private fun getVehicleHashMap(): LinkedHashMap<String, String> {

        var ids = ""
        var makeId: String? = null
        var modelId: String? = null

        if ((activity as ManageVehicles).vehicleClickPosition != null) {
            ids = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).id
        }

        if (make == null) {
            makeId = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).make?.id
        } else {
            makeId = make!!.id
        }

        if (make == null) {
            modelId = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).model?.id
        } else {
            modelId = model!!.id
        }


        val hashMap = LinkedHashMap<String, String>()
        hashMap["token"] = sessionManager.accessToken!!
        hashMap["id"] = ids
        hashMap["make_id"] = makeId!!
        hashMap["model_id"] = modelId!!
        hashMap["year"] = binding.tvYearType.text.toString()
        hashMap["license_no"] = binding.tvLicenseType.text.toString()
        hashMap["name"] = binding.tvVehicleName.text.toString()
        hashMap["color"] = binding.tvVehicleColor.text.toString()
        hashMap["vehicle_type"] = vehTypeSelcIds
        hashMap["options"] = TextUtils.join(",", selectedIds)

        return hashMap
    }


    private fun initModelRv() {


        modelAdapter = ModelAdapter((activity as ManageVehicles).getContFromAct())
        modelAdapter.initModel((activity as ManageVehicles).makeModelDetails.make.get(makePosition).model)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvVehicleDesc.layoutManager = linearLayoutManager
        rvVehicleDesc.setHasFixedSize(true)
        rvVehicleDesc.smoothScrollToPosition(0)
        rvVehicleDesc.adapter = modelAdapter
        /* val laydir = getString(R.string.layout_direction)
         if ("1" == laydir)
             rvVehicleDesc.rotationY = 180f*/
        modelAdapter.initCurrentModel(binding.tvModelType.text.toString())




        modelAdapter.setOnModelClickListner(object : ModelAdapter.onModelClickListener {
            override fun setModelClick(selectedModel: Model, position: Int) {
                vehicleDescriptionDialog?.dismiss()
                model = selectedModel
                binding.tvModelType.text = (activity as ManageVehicles).makeModelDetails.make.get(makePosition).model.get(position).name
                (activity as ManageVehicles).deselectModel(makePosition)
                emptyYear()
                (activity as ManageVehicles).makeModelDetails.make.get(makePosition).model.get(position).isSelected = true
            }
        })
    }


    override fun onClick(pos: Int, isChecked: Boolean) {


    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {

        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(context, dialog, data)
            return
        }

        when (jsonResp.requestCode) {
            ADD_UPDATE_VEHICLE -> if (jsonResp.isSuccess) {
                onSuccessVehicleUpdated(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(context, dialog, jsonResp.statusMsg)
            }

            else -> {
            }
        }
    }

    private fun onSuccessVehicleUpdated(jsonResp: JsonResponse) {
        addedVehicleDetails = gson.fromJson(jsonResp.strResponse, AddedVehiclesModel::class.java)
        (activity as ManageVehicles).vehicleDetails.clear()
        (activity as ManageVehicles).vehicleDetails.addAll(addedVehicleDetails.vehicle)

//        addVehicle()
        (activity as ManageVehicles).deselectVehicleType()
        (activity as ManageVehicles).deselectMake()
        (activity as ManageVehicles).deselectModel(makePosition)
        emptyYear()
        (activity as ManageVehicles).onBackPressed()

    }


    override fun onFailure(jsonResp: JsonResponse?, data: String?) {

    }

    fun initRecyclerView() {
        var vehicleTypes = ArrayList<VehicleTypes>()
        if ((activity as ManageVehicles).vehicleClickPosition != null) {

            vehicleTypes = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).vehicleTypes
        }
        vehicleTypeAdapter = VehicleTypeAdapter(requireContext(), vehicleTypes, (activity as ManageVehicles).makeModelDetails.vehicleTypes, this)
        binding.rvVehicleType.adapter = vehicleTypeAdapter
    }


    fun initRequestOptionviews() {
        var isAddNewVehicle = false
        if ((activity as ManageVehicles).makeModelDetails.requestOptions.isNotEmpty()) {
            if ((activity as ManageVehicles).vehicleClickPosition != null) {
                for (i in 0 until (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).requestOptions.size) {
                    (activity as ManageVehicles).makeModelDetails.requestOptions[i].isSelected = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).requestOptions[i].isSelected
                }
            } else {
                isAddNewVehicle = true
                for (i in 0 until (activity as ManageVehicles).makeModelDetails.requestOptions.size) {
                    (activity as ManageVehicles).makeModelDetails.requestOptions[i].isSelected = false
                }
            }
            featuresInVehicleAdapter = FeaturesInVehicleAdapter(isAddNewVehicle, (activity as ManageVehicles).makeModelDetails.requestOptions, this)
            binding.rvFeaturesList.adapter = featuresInVehicleAdapter
            binding.rvFeaturesList.visibility = VISIBLE
        } else {
            binding.rvFeaturesList.visibility = GONE
        }

        if (!isAddNewVehicle) {
            for (i in 0 until (activity as ManageVehicles).makeModelDetails.requestOptions.size) {
                if ((activity as ManageVehicles).makeModelDetails.requestOptions[i].isSelected) {
                    selectedIds.add((activity as ManageVehicles).makeModelDetails.requestOptions[i].id)
                }
            }
        }
    }

    override fun onFeatureChoosed(id: Int, isSelected: Boolean) {
        if (!isSelected) {
            selectedIds.remove(id)
        } else {
            selectedIds.add(id)
        }
    }

}