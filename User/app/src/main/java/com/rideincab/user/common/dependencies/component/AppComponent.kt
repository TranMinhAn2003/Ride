package com.rideincab.user.common.dependencies.component

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage dependencies.component
 * @category AppComponent
 * @author SMR IT Solutions
 *
 */


import com.rideincab.user.taxi.ScheduleRideDetailActivity
import com.rideincab.user.taxi.adapters.CarDetailsListAdapter

import com.rideincab.user.taxi.adapters.PastTripsPaginationAdapter
import com.rideincab.user.taxi.adapters.PriceRecycleAdapter
import com.rideincab.user.taxi.adapters.UpcomingAdapter
import com.rideincab.user.taxi.adapters.UpcomingTripsPaginationAdapter
import com.rideincab.user.common.backgroundtask.ImageCompressAsyncTask
import com.rideincab.user.common.configs.RunTimePermission
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.taxi.database.AddFirebaseDatabase
import com.rideincab.user.common.dependencies.module.AppContainerModule
import com.rideincab.user.common.dependencies.module.ApplicationModule
import com.rideincab.user.common.dependencies.module.NetworkModule
import com.rideincab.user.common.helper.CommonDialog
import com.rideincab.user.common.drawpolyline.DownloadTask
import com.rideincab.user.common.pushnotification.MyFirebaseMessagingService
import com.rideincab.user.taxi.sendrequest.CancelYourTripActivity
import com.rideincab.user.taxi.sendrequest.DriverNotAcceptActivity
import com.rideincab.user.taxi.sendrequest.DriverRatingActivity
import com.rideincab.user.taxi.sendrequest.PaymentAmountPage
import com.rideincab.user.taxi.sendrequest.SendingRequestActivity
import com.rideincab.user.taxi.sidebar.AddHome
import com.rideincab.user.taxi.sidebar.DriverContactActivity
import com.rideincab.user.taxi.sidebar.EnRoute
import com.rideincab.user.taxi.sidebar.FareBreakdown
import com.rideincab.user.taxi.sidebar.Profile
import com.rideincab.user.taxi.sidebar.Setting
import com.rideincab.user.taxi.sidebar.payment.*
import com.rideincab.user.taxi.sidebar.referral.ShowReferralOptions
import com.rideincab.user.taxi.sidebar.trips.Past
import com.rideincab.user.taxi.sidebar.trips.Receipt
import com.rideincab.user.taxi.sidebar.trips.TripDetails
import com.rideincab.user.taxi.sidebar.trips.Upcoming
import com.rideincab.user.taxi.sidebar.trips.YourTrips
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.utils.userchoice.UserChoice
import com.rideincab.user.common.views.*
import com.rideincab.user.taxi.views.addCardDetails.AddCardActivity
import com.rideincab.user.taxi.views.facebookAccountKit.FacebookAccountKitActivity
import com.rideincab.user.taxi.views.emergency.EmergencyContact
import com.rideincab.user.taxi.views.emergency.SosActivity
import com.rideincab.user.taxi.views.firebaseChat.ActivityChat
import com.rideincab.user.taxi.views.firebaseChat.AdapterFirebaseRecylcerview
import com.rideincab.user.taxi.views.firebaseChat.FirebaseChatHandler
import com.rideincab.user.taxi.views.main.MainActivity
import com.rideincab.user.taxi.views.main.filter.FeaturesInVehicleAdapter
import com.rideincab.user.taxi.views.peakPricing.PeakPricing
import com.rideincab.user.taxi.views.search.PlaceSearchActivity
import com.rideincab.user.taxi.views.signinsignup.*
import com.rideincab.user.taxi.views.splash.SplashActivity

import javax.inject.Singleton

import dagger.Component


/*****************************************************************
 * App Component
 */
@Singleton
@Component(modules = [NetworkModule::class, ApplicationModule::class, AppContainerModule::class])
interface AppComponent {

    fun inject(splashActivity: SplashActivity)

    fun inject(mainActivity: MainActivity)

    fun inject(scheduleRideDetailActivity: ScheduleRideDetailActivity)

    fun inject(sendingRequestActivity: SendingRequestActivity)

    fun inject(driverNotAcceptActivity: DriverNotAcceptActivity)

    fun inject(mainActivity: PlaceSearchActivity)

    fun inject(signin_signup_activity: SigninSignupActivity)

    fun inject(ssResetPassword: SSResetPassword)

    fun inject(ssSocialDetailsActivity: SSRegisterActivity)

    fun inject(driverContactActivity: DriverContactActivity)

    fun inject(addHome: AddHome)

    fun inject(paymentPage: PaymentPage)

    fun inject(paymentAmountPage: PaymentAmountPage)

    fun inject(fareBreakdown: FareBreakdown)

    fun inject(addWalletActivity: AddWalletActivity)

    fun inject(promoAmountActivity: PromoAmountActivity)

    fun inject(yourTrips: YourTrips)

    fun inject(tripDetails: TripDetails)

    fun inject(enRoute: EnRoute)

    fun inject(sos_activity: SosActivity)

    fun inject(driverRatingActivity: DriverRatingActivity)

    fun inject(cancelYourTripActivity: CancelYourTripActivity)

    fun inject(commonDialog: CommonDialog)

    fun inject(setting: Setting)

    fun inject(profile: Profile)


    fun inject(emergencyContact: EmergencyContact)

    fun inject(activityChat: ActivityChat)

    fun inject(facebookAccountKitActivity: FacebookAccountKitActivity)

    fun inject(loginActivity: SSLoginActivity)

    fun inject(peakPricing: PeakPricing)

    fun inject(showReferralOptions: ShowReferralOptions)


    // Fragments
    fun inject(past: Past)

    fun inject(upcoming: Upcoming)

    fun inject(receipt: Receipt)

    // Utilities
    fun inject(runTimePermission: RunTimePermission)

    fun inject(sessionManager: SessionManager)

    fun inject(commonMethods: CommonMethods)

    fun inject(requestCallback: RequestCallback)

    // Adapters

    fun inject(upcomingAdapter: UpcomingAdapter)


    fun inject(promoAmountAdapter: PromoAmountAdapter)

    fun inject(carDetailsListAdapter: CarDetailsListAdapter)

    fun inject(myFirebaseMessagingService: MyFirebaseMessagingService)

    fun inject(firebaseChatHandler: FirebaseChatHandler)

    fun inject(adapterFirebaseRecylcerview: AdapterFirebaseRecylcerview)


    // AsyncTask
    fun inject(imageCompressAsyncTask: ImageCompressAsyncTask)

    fun inject(downloadTask: DownloadTask)

    fun inject(firebaseDatabase: AddFirebaseDatabase)


    fun inject(priceRecycleAdapter: PriceRecycleAdapter)

    fun inject(pastTripsPaginationAdapter: PastTripsPaginationAdapter)

    fun inject(upcomingTripsPaginationAdapter: UpcomingTripsPaginationAdapter)
     fun inject(addCardActivity: AddCardActivity)
    fun inject(paymentMethodAdapter: PaymentMethodAdapter)
    fun inject(featuresInVehicleAdapter: FeaturesInVehicleAdapter)

    fun inject(supportActivityCommon: SupportActivityCommon)

    fun inject(supportAdapter: SupportAdapter)

    fun inject(paymentWebViewActivity: PaymentWebViewActivity)

    fun inject(commonActivity: CommonActivity)

    fun inject(userChoice: UserChoice)

}
