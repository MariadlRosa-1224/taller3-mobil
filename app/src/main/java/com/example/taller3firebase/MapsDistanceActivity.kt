package com.example.taller3firebase

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.taller3firebase.databinding.ActivityMapsDistanceBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import java.util.Date

class MapsDistanceActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsDistanceBinding
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    private var currentMarker: Marker? = null
    private val RADIUS_OF_EARTH_KM = 6371.0

    private val locationPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            startLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     binding = ActivityMapsDistanceBinding.inflate(layoutInflater)
     setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = createLocationRequest()
        locationCallback = createLocationCallback()

        locationPermissionRequest()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(5000)
            .build()
    }

    private fun createLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                val location = result.lastLocation
                if (location != null) {
                    currentLocation = location
                    updateUI(location)
                }
            }
        }
    }

    private fun updateUI(location: Location) {
        val currentLocation = LatLng(location.latitude, location.longitude)

        currentMarker?.remove()
        currentMarker = mMap.addMarker(MarkerOptions().position(currentLocation).title("Ubicación actual"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12f))

        // Usuario seleccionado
        val userLat = intent.getDoubleExtra("userLat", 0.0)
        val userLng = intent.getDoubleExtra("userLng", 0.0)
        val userName = intent.getStringExtra("userName")
        val userLatLng = LatLng(userLat, userLng)

        val markerIcon = BitmapFactory.decodeResource(resources, R.drawable.marker2)
        val scaledIcon = Bitmap.createScaledBitmap(markerIcon, 100, 100, false) // Adjust the size as needed

        val userMarker = BitmapDescriptorFactory.fromBitmap(scaledIcon)
        mMap.addMarker(MarkerOptions().position(userLatLng).title(userName).icon(userMarker))

        val distance = distance(currentLocation.latitude, currentLocation.longitude, userLatLng.latitude, userLatLng.longitude)
        Toast.makeText(this, "Distancia: $distance km", Toast.LENGTH_LONG).show()
    }

    fun distance(lat1 : Double, long1: Double, lat2:Double, long2:Double) : Double{
        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)+
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        val result = RADIUS_OF_EARTH_KM * c;
        return Math.round(result*100.0)/100.0;
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun locationPermissionRequest() {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

        if (ContextCompat.checkSelfPermission(this, fineLocationPermission) == PackageManager.PERMISSION_DENIED ||
            ContextCompat.checkSelfPermission(this, coarseLocationPermission) == PackageManager.PERMISSION_DENIED) {

            if (shouldShowRequestPermissionRationale(fineLocationPermission) ||
                shouldShowRequestPermissionRationale(coarseLocationPermission)) {
                Toast.makeText(this, "The location permissions are needed", Toast.LENGTH_LONG).show()
            }
            locationPermissions.launch(arrayOf(fineLocationPermission, coarseLocationPermission))
        } else {
            startLocationUpdates()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        locationPermissionRequest()
    }

}