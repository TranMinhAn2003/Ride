package com.rideincab.driver.home.interfaces

/**
 * @package com.cloneappsolutions.cabmedriver
 * @subpackage interfaces
 * @category YourTripsListener
 * @author SMR IT Solutions
 *
 */

import android.content.res.Resources

import com.rideincab.driver.trips.tripsdetails.YourTrips


/*****************************************************************
 * YourTripsListener
 */

interface YourTripsListener {

    val res: Resources

    val instance: YourTrips
}
