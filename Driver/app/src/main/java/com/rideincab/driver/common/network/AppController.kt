package com.rideincab.driver.common.network

/**
 * @package com.cloneappsolutions.cabmedriver.common.network
 * @subpackage network
 * @category AppController
 * @author SMR IT Solutions
 *
 */

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.rideincab.driver.R
import com.rideincab.driver.common.dependencies.component.AppComponent
import com.rideincab.driver.common.dependencies.component.DaggerAppComponent
import com.rideincab.driver.common.dependencies.module.ApplicationModule
import com.rideincab.driver.common.dependencies.module.NetworkModule
import com.rideincab.driver.common.util.CommonMethods
import java.util.*


class AppController : Application() {
    private var locale: Locale? = null

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        MultiDex.install(this)
        appContext = this
        instance = this
        FirebaseApp.initializeApp(this)  // Fire base initialize
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        setLocale()

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        appComponent = DaggerAppComponent.builder()
            .applicationModule(ApplicationModule(this)) // This also corresponds to the name of your module: %component_name%Module
            .networkModule(NetworkModule(getString(R.string.apiBaseUrl))).build()

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }

    private fun setLocale() {
        locale = Locale("en")
        Locale.setDefault(locale)
        val configuration = baseContext.resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
        } else
            configuration.setLocale(locale)
        baseContext.createConfigurationContext(configuration)
    }

    /*
     * Multidex enable
     */
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {

        val TAG = AppController::class.java.simpleName

        @get:Synchronized
        var instance: AppController? = null
            private set
        private lateinit var appComponent: AppComponent
        lateinit var appContext: Context


        /* public static SinchClient sinchClient = null;
    public static Call call;*/

        fun getAppComponent(): AppComponent {
            CommonMethods.DebuggableLogV("non", "null" + appComponent)
            return appComponent
        }

        // or return instance.getApplicationContext();
        val context: Context?
            get() = instance
    }

    /* public static void createSinchClient(String userCallId, String sinchKey, String sinchSecret){
        if(sinchClient !=null){
            sinchClient.stopListeningOnActiveConnection();
            sinchClient.terminate();
        }


        sinchClient = Sinch.getSinchClientBuilder()
                .context(getContext())
                .userId(userCallId)
                .applicationKey(sinchKey)
                .applicationSecret(sinchSecret)
                .environmentHost("clientapi.sinch.com")
                .build();
        sinchClient.setSupportPushNotifications(true);
        //sinchClient.setPushNotificationDisplayName("you missed a call from");
        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();


        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());

        sinchClient.start();
    }*/


    /*private static class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            call = incomingCall;

            Intent callScreen = new Intent(getContext(), CallProcessingActivity.class);
            callScreen.putExtra(CommonKeys.KEY_TYPE,CallProcessingActivity.CallActivityType.Ringing);
            callScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(callScreen);

        }
    }*/
}