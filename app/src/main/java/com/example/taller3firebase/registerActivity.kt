package com.example.taller3firebase

import android.Manifest
import android.R
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.taller3firebase.databinding.ActivityRegisterBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import model.User
import java.io.File


class registerActivity : AppCompatActivity() {

    lateinit var binding: ActivityRegisterBinding
    lateinit var user: User
    lateinit var auth: FirebaseAuth

    //localizacion del usuario

    private var currentLocation: LatLng = LatLng(0.0, 0.0)

    //fuseLocationClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    // foto de galeria

    val getContentGallery = registerForActivityResult(
        ActivityResultContracts.GetContent(), ActivityResultCallback {
            loadImage(it!!)
        })

    // foto de camara

    lateinit var uriCamera: Uri // Uri para guardar la imagen de la camara

    val getContentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture(), ActivityResultCallback {
            if (it){
                loadImage(uriCamera)
            }
        })


    // solicitar permiso

    val getSimplePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(), ActivityResultCallback {
            if(it) {//granted
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        })

    //  firebase Database

    private lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference

    val USERS = "users/"




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //auth

        auth = FirebaseAuth.getInstance()

        //localizacion

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            //permiso

        getSimplePermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)




        // Obtener info del usuario con los campos del formulario

        binding.registerButton.setOnClickListener {
            val name = binding.nombre.text.toString()
            val surname = binding.apellido.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            //validar campos
            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor llene todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                register(email, password, name, surname)
            }

        }

        // Obtener foto del usuario (galeria y camara)

        binding.ImageButton.setOnClickListener {
            getContentGallery.launch("image/*")
        }

            // file for saving image from camera
            val file = File(getFilesDir(),"picFromCamera")
            uriCamera = FileProvider.getUriForFile(baseContext,baseContext.packageName + ".fileprovider",file)

        binding.tomarFoto.setOnClickListener {
            getContentCamera.launch(uriCamera)
        }

        // database

        database = FirebaseDatabase.getInstance()




    }



    fun updateUI(user: FirebaseUser) {
        //lleva al mapa
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)

    }

    fun register(email: String, password: String, nombre: String, apellido: String) {

        // obtener ubicacion
        obtenerUbicacion()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                this,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)
                        val firebaseUser: FirebaseUser? = auth.getCurrentUser()
                        if (firebaseUser != null) { //Update user Info
                            val upcrb = UserProfileChangeRequest.Builder()
                            upcrb.setDisplayName(
                                nombre + " " + apellido
                                    .toString()
                            )
                            upcrb.setPhotoUri(Uri.parse("path/to/pic")) //fake uri, use Firebase Storage
                            firebaseUser.updateProfile(upcrb.build())
                            updateUI(firebaseUser)

                            //crear usuario

                            user = User(nombre, apellido, email, currentLocation.latitude, currentLocation.longitude)

                            // guardar en la base de datos

                            val uid = firebaseUser.uid


                                myRef = database.reference.child(USERS).child(uid)
                                myRef.setValue(user)

                                // ir al mapa

                                val intent = Intent(this, MapsActivity::class.java)
                                startActivity(intent)




                        }
                    }
                    if (!task.isSuccessful) {
                        Toast.makeText(
                            this@registerActivity,
                            "Registro Fallido" + task.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, task.exception!!.message!!)
                    }














                })


    }

    // Obtener la ubicacion del usuario

    private fun obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                }
            }
    }



    // Cargar imagen de la galeria

    private fun loadImage(it: Uri) {

    }




}