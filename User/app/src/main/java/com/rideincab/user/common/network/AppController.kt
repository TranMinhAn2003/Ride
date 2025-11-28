package com.rideincab.user.common.network

/**
 * @package com.cloneappsolutions.cabmeuser
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
import com.google.firebase.messaging.FirebaseMessaging
import com.rideincab.user.R
import com.rideincab.user.common.dependencies.component.AppComponent
import com.rideincab.user.common.dependencies.component.DaggerAppComponent
import com.rideincab.user.common.dependencies.module.AppContainerModule
import com.rideincab.user.common.dependencies.module.ApplicationModule
import com.rideincab.user.common.dependencies.module.NetworkModule
import java.util.*

/* ************************************************************
Retrofit Appcomponent and Bufferknife Added
*************************************************************** */
class AppController : Application() {
    private var locale: Locale? = null
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setLocale()
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        FirebaseApp.initializeApp(this)  // Fire base initialize
        MultiDex.install(this)    // Multiple dex initialize
        instance = this
//        appComponent = DaggerAppComponent.builder().applicationModule(ApplicationModule(this)) // This also corresponds to the name of your module: %component_name%Module
//                .networkModule(NetworkModule(resources.getString(R.string.base_url))).build()
        appComponent = DaggerAppComponent.builder()
            .applicationModule(ApplicationModule(this))
            .networkModule(NetworkModule(resources.getString(R.string.base_url)))
            .appContainerModule(AppContainerModule()) // ✅ Add this line
            .build()
    }

    private fun setLocale() {
        locale = Locale("en")
        Locale.setDefault(locale)
        val configuration= baseContext.resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            configuration.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
        } else
            configuration.setLocale(locale)
        baseContext.createConfigurationContext(configuration)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {

        val TAG = AppController::class.java.simpleName
        @get:Synchronized
        lateinit var appComponent: AppComponent

        var instance: AppController? = null
            private set


        fun getContext():Context{
            return instance!!.applicationContext
        }

    }

    fun appComponent():AppComponent{
        return appComponent
    }
}