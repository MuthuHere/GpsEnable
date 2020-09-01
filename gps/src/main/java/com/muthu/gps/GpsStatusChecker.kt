package com.muthu.gps

import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.location.LocationManager
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import java.lang.ref.WeakReference

class GpsStatusChecker {
    private var mActivityWeakReference: WeakReference<Activity?>
    private var mCallBackWeakReference: WeakReference<GpsStatusDetectorCallBack?>

    constructor(activity: Activity?) {
        mActivityWeakReference = WeakReference(activity)
        mCallBackWeakReference = WeakReference(activity as GpsStatusDetectorCallBack?)
    }

    constructor(fragment: Fragment) {
        mActivityWeakReference = WeakReference(fragment.activity as Activity?)
        mCallBackWeakReference = WeakReference(fragment as GpsStatusDetectorCallBack)
    }

    fun checkGpsStatus() {
        val activity = mActivityWeakReference.get()
        val callBack = mCallBackWeakReference.get()
        if (activity == null || callBack == null) {
            return
        }
        if (isGpsEnabled(activity)) {
            callBack.onGpsSettingStatus(true)
        } else {
            setLocationRequest(activity, callBack)
        }
    }

    private fun isGpsEnabled(activity: Activity): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun setLocationRequest(activity: Activity, callBack: GpsStatusDetectorCallBack) {
        val mGoogleApiClient = GoogleApiClient.Builder(activity)
            .addApi(LocationServices.API).build()
        mGoogleApiClient.connect()
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(30 * 1000.toLong())
            .setFastestInterval(5 * 1000.toLong())
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true) // important!
            .build()
        val result = LocationServices.SettingsApi
            .checkLocationSettings(mGoogleApiClient, locationSettingsRequest)
        result.setResultCallback { result ->
            val status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> callBack.onGpsSettingStatus(true)
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    status.startResolutionForResult(activity, REQUEST_CODE)
                } catch (e: SendIntentException) {
                    callBack.onGpsSettingStatus(false)
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> callBack.onGpsSettingStatus(
                    false
                )
            }
            mGoogleApiClient.disconnect() // If you do not disconnect, causes a memory leak
        }
    }

    fun checkOnActivityResult(requestCode: Int, resultCode: Int) {
        val activity = mActivityWeakReference.get()
        val callBack = mCallBackWeakReference.get()
        if (activity == null || callBack == null) {
            return
        }
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                callBack.onGpsSettingStatus(true)
            } else {
                callBack.onGpsSettingStatus(false)
                callBack.onGpsAlertCanceledByUser()
            }
        }
    }

    interface GpsStatusDetectorCallBack {
        fun onGpsSettingStatus(enabled: Boolean)
        fun onGpsAlertCanceledByUser()
    }

    companion object {
        private const val REQUEST_CODE = 2
    }
}