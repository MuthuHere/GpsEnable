package com.muthu.enablegps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.d
import com.muthu.gps.GpsStatusChecker
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), GpsStatusChecker.GpsStatusDetectorCallBack {

    private var gpsChecker: GpsStatusChecker by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //init
        gpsChecker = GpsStatusChecker(this)

        //request
        gpsChecker.checkGpsStatus()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        gpsChecker.checkOnActivityResult(requestCode, resultCode);

    }

    override fun onGpsSettingStatus(enabled: Boolean) {
        d("LOCATION", "--> $enabled")

    }

    override fun onGpsAlertCanceledByUser() {
        d("LOCATION", "--> Canceled")
        //request again
        gpsChecker.checkGpsStatus()
    }
}