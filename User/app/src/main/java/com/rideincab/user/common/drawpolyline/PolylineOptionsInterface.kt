package com.rideincab.user.common.drawpolyline

/**
 * @package com.cloneappsolutions.cabmeuserdriver
 * @subpackage map.drawpolyline
 * @category PolylineOptionsInterface
 * @author SMR IT Solutions
 * 
 */

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.rideincab.user.taxi.datamodels.StepsClass
import java.util.*

interface PolylineOptionsInterface {
    fun getPolylineOptions(output: PolylineOptions, points: ArrayList<LatLng>, distance: String, overviewPolyline: String, arrivalTime: String, stepPoints: ArrayList<StepsClass>, totalDuration : Int)
}
