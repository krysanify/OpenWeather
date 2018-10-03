package com.krysanify.openweather

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentSender
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes.RESOLUTION_REQUIRED
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_LOW_POWER
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStates
import com.google.android.gms.location.LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.krysanify.openweather.BuildConfig.APPLICATION_ID
import java.lang.System.currentTimeMillis
import java.util.concurrent.TimeUnit.HOURS
import java.util.concurrent.TimeUnit.MINUTES

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val keyLastLocation = "last_location"
    private val keyLastUpdate = "last_update"
    private val keyRequestingUpdate = "requesting_update"
    private val reqCodeCheckSettings = 1
    private val reqCodeLocationPermit = 2

    private val locationRequest by lazy {
        LocationRequest().apply {
            interval = HOURS.toMillis(1L)
            fastestInterval = MINUTES.toMillis(15L)
            priority = PRIORITY_LOW_POWER
        }
    }

    private val settingsRequest by lazy {
        LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
    }

    private val settingsClient by lazy {
        LocationServices.getSettingsClient(this@MapsActivity)
    }

    private val onCompleteListener = object : OnSuccessListener<LocationSettingsResponse>,
        OnFailureListener {

        @SuppressLint("MissingPermission")
        override fun onSuccess(response: LocationSettingsResponse?) {
            response ?: return
//            val states = response.locationSettingsStates
//            val locationPresent = states.isLocationPresent
//            val locationUsable = states.isLocationUsable
            locationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }

        override fun onFailure(exception: Exception) {
            if (exception is ApiException) when (exception.statusCode) {
                RESOLUTION_REQUIRED -> try {
                    (exception as ResolvableApiException)
                        .startResolutionForResult(this@MapsActivity, reqCodeCheckSettings)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
                SETTINGS_CHANGE_UNAVAILABLE -> isRequestingLocationUpdates = false
            }

            mapLocation(lastLocation, lastUpdate)
        }
    }

    private val locationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this@MapsActivity)
    }

    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                result ?: return
                mapLocation(result.lastLocation, currentTimeMillis())
            }
        }
    }

    private val locationPermit = arrayOf(ACCESS_COARSE_LOCATION)
    private val requestLocationPermit = View.OnClickListener {
        ActivityCompat.requestPermissions(this@MapsActivity, locationPermit, reqCodeLocationPermit)
    }

    private var isRequestingLocationUpdates = true
    private var lastUpdate = 0L
    private var lastLocation: Location? = null
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        findViewById<View>(android.R.id.content).isClickable = false

        @Suppress("CAST_NEVER_SUCCEEDS")
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState ?: return
        val location = savedInstanceState.getParcelable<Location>(keyLastLocation)
        val timeMillis = savedInstanceState.getLong(keyLastUpdate)
        isRequestingLocationUpdates = savedInstanceState.getBoolean(keyRequestingUpdate, false)
        mapLocation(location, timeMillis)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState ?: return
        outState.putParcelable(keyLastLocation, lastLocation)
        outState.putLong(keyLastUpdate, lastUpdate)
        outState.putBoolean(keyRequestingUpdate, isRequestingLocationUpdates)
    }

    override fun onResume() {
        super.onResume()

        val hasCoarseLocation = grantedLocationPermit()
        if (isRequestingLocationUpdates && hasCoarseLocation) {
            startLocationUpdates()
        } else if (!hasCoarseLocation) {
            requestLocationPermit()
        }

        mapLocation(lastLocation, lastUpdate)
    }

    override fun onPause() {
        super.onPause()
        // Remove location updates to save battery
        stopLocationUpdates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val states = LocationSettingsStates.fromIntent(intent)
        // The user was asked to change settings, but chose not to
        if (reqCodeCheckSettings == requestCode && RESULT_CANCELED == resultCode) {
            isRequestingLocationUpdates = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (reqCodeLocationPermit != requestCode || grantResults.isEmpty()) return
        if (PERMISSION_GRANTED == grantResults[0]) {
            if (isRequestingLocationUpdates) startLocationUpdates()
            return
        }

        // permission denied
        val view: View = findViewById(android.R.id.content)
        val startAppSettings = View.OnClickListener {
            startActivity(Intent().apply {
                action = ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", APPLICATION_ID, null)
                flags = FLAG_ACTIVITY_NEW_TASK
            })
        }

        Snackbar.make(view, R.string.permit_location_denied, LENGTH_INDEFINITE)
            .setAction(android.R.string.ok, startAppSettings).show()
    }

    private fun grantedLocationPermit() =
        PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)

    private fun requestLocationPermit() {
        val view: View = findViewById(android.R.id.content)
        val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(
            this, ACCESS_COARSE_LOCATION
        )

        if (shouldShow) {
            Snackbar.make(view, R.string.permit_location_rationale, LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, requestLocationPermit).show()
            return
        }

        requestLocationPermit.onClick(view)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.setOnMapClickListener {
            Log.d("onMapReady", "lorem")
        }
        mapLocation(lastLocation, lastUpdate)
    }

    private fun mapLocation(location: Location?, timeMillis: Long) {
        lastLocation = location ?: return
        lastUpdate = timeMillis

        // Add a marker in last location and move the camera
        googleMap?.let {
            val last = LatLng(location.latitude, location.longitude)
            it.addMarker(MarkerOptions().position(last).title("Last Marker"))
            it.moveCamera(CameraUpdateFactory.newLatLng(last))
        }
    }

    private fun startLocationUpdates() {
        settingsClient.checkLocationSettings(settingsRequest).apply {
            addOnSuccessListener(onCompleteListener)
            addOnFailureListener(onCompleteListener)
//            addOnCompleteListener(onCompleteListener)
        }
    }

    private fun stopLocationUpdates() {
        if (isRequestingLocationUpdates) {
            locationClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(this, onRequestRemoval)
        }
    }

    private val onRequestRemoval = OnCompleteListener<Void> {
        isRequestingLocationUpdates = false
    }
}
