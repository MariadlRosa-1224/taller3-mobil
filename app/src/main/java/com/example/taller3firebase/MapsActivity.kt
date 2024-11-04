package com.example.taller3firebase

import android.Manifest
import android.content.Intent
import android.location.Location
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.example.taller3firebase.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import java.io.File
import com.google.android.gms.location.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    private var currentMarker: Marker? = null

    private val locationPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            startLocationUpdates()
        }
    }

    //  firebase Database

    private lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference

    val USERS = "users/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        auth = FirebaseAuth.getInstance()

        // firebase Database

        database = FirebaseDatabase.getInstance()


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = createLocationRequest()
        locationCallback = createLocationCallback()

        locationPermissionRequest()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_map, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val clicked = item.itemId
        if(clicked == R.id.signOut){
            auth.signOut()
            val i = Intent(this, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }

        if(clicked == R.id.usuariosDisponible){
            establecerDisponibilidad()
        }

        if(clicked == R.id.listaUsuarios){
            val i = Intent(this, UsuariosDisponiblesActivity::class.java)
            startActivity(i)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun establecerDisponibilidad() {
        // Establecer el usuario actual como disponible

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid
            val userRef = database.getReference(USERS).child(userUid)

           // valor de "disponible"

            var disponibilidad = userRef.child("disponible").toString()

            if (disponibilidad == "true") {
                var update = mapOf("disponible" to false)
                userRef.updateChildren(update)
            } else {
                var update = mapOf("disponible" to true)
                userRef.updateChildren(update)
            }


        }


    }



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
        val currentLatLng = LatLng(location.latitude, location.longitude)
        currentMarker?.remove()
        currentMarker = mMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
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
        addMarkersFromJSON()
    }

    private fun addMarkersFromJSON() {
        val filename = "locations.json"
        // crear archivo si no existe



        val file = File(baseContext.getExternalFilesDir(null), filename)

        if (!file.exists()) {
            file.createNewFile()

        }

        val jsonStr = file.readText()
        val jsonObject = JSONObject(jsonStr)
        val locations = jsonObject.getJSONObject("locations")

        for (i in 0 until locations.length()) {
            val location = locations.getJSONObject(i.toString())
            val lat = location.getDouble("latitude")
            val lon = location.getDouble("longitude")
            val name = location.getString("name")
            val latLng = LatLng(lat, lon)

            val originalIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground)
            val scaledIcon = Bitmap.createScaledBitmap(originalIcon, 100, 100, false)

            val markerOptions = MarkerOptions()
                .position(latLng)
                .title(name)
                .icon(BitmapDescriptorFactory.fromBitmap(scaledIcon))

            mMap.addMarker(markerOptions)
        }
    }
}

