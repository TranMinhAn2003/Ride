package com.rideincab.driver.datamodel

import com.google.gson.annotations.SerializedName

data class DriverProfileStrResponse(

	@field:SerializedName("status_message")
	val statusMessage: String? = null,

	@field:SerializedName("email_id")
	val emailId: String? = null,

	@field:SerializedName("status_code")
	val statusCode: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("car_app_image")
	val carAppImage: String? = null,

	@field:SerializedName("driver_referral_earning")
	val driverReferralEarning: String? = null,

	@field:SerializedName("city")
	val city: String? = null,

	@field:SerializedName("currency_code")
	val currencyCode: String? = null,

	@field:SerializedName("car_type")
	val carType: String? = null,

	@field:SerializedName("profile_image")
	val profileImage: String? = null,

	@field:SerializedName("car_active_image")
	val carActiveImage: String? = null,

	@field:SerializedName("address_line2")
	val addressLine2: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("address_line1")
	val addressLine1: String? = null,

	@field:SerializedName("car_image")
	val carImage: String? = null,

	@field:SerializedName("driver_documents")
	val driverDocuments: List<DriverDocumentsItem>? = null,

	@field:SerializedName("state")
	val state: String? = null,

	@field:SerializedName("first_name")
	val firstName: String? = null,

	@field:SerializedName("company_id")
	val companyId: String? = null,

	@field:SerializedName("currency_symbol")
	val currencySymbol: String? = null,

	@field:SerializedName("last_name")
	val lastName: String? = null,

	@field:SerializedName("country_code")
	val countryCode: String? = null,

	@field:SerializedName("owe_amount")
	val oweAmount: String? = null,

	@field:SerializedName("vehicle_number")
	val vehicleNumber: String? = null,

	@field:SerializedName("company_name")
	val companyName: String? = null,

	@field:SerializedName("mobile_number")
	val mobileNumber: String? = null,

	@field:SerializedName("postal_code")
	val postalCode: String? = null,

	@field:SerializedName("status")
	val status: Int? = null,

	@field:SerializedName("vehicle_name")
	val vehicleName: String? = null,

	@field:SerializedName("vehicle_details")
	val vehicleDetails: List<VehicleDetailsItem>? = null
)

data class RequestOptionsItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("isSelected")
	val isSelected: Boolean? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class DriverDocumentsItem(

	@field:SerializedName("expiry_required")
	val expiryRequired: Int? = null,

	@field:SerializedName("document")
	val document: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("expired_date")
	val expiredDate: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class Model(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class Make(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class VehicleDetailsItem(

	@field:SerializedName("vehicle_types")
	val vehicleTypes: List<VehicleTypesItem>? = null,

	@field:SerializedName("vehicle_type_id")
	val vehicleTypeId: String? = null,

	@field:SerializedName("is_active")
	val isActive: Int? = null,

	@field:SerializedName("request_options")
	val requestOptions: List<RequestOptionsItem>? = null,

	@field:SerializedName("color")
	val color: String? = null,

	@field:SerializedName("year")
	val year: String? = null,

	@field:SerializedName("vehicleImageURL")
	val vehicleImageURL: String? = null,

	@field:SerializedName("is_default")
	val isDefault: String? = null,

	@field:SerializedName("license_number")
	val licenseNumber: String? = null,

	@field:SerializedName("vechile_documents")
	val vechileDocuments: List<VechileDocumentsItem>? = null,

	@field:SerializedName("model")
	val model: Model? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("make")
	val make: Make? = null,

	@field:SerializedName("vehicle_name")
	val vehicleName: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class VehicleTypesItem(

	@field:SerializedName("location")
	val location: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("type")
	val type: String? = null
)

data class VechileDocumentsItem(

	@field:SerializedName("expiry_required")
	val expiryRequired: Int? = null,

	@field:SerializedName("document")
	val document: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("expired_date")
	val expiredDate: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
