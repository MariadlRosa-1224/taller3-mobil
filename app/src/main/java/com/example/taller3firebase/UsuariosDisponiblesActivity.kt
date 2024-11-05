package com.example.taller3firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.taller3firebase.databinding.ActivityUsuariosDisponiblesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import model.User
import model.adapterUsers
import services.UserAvailabilityService
import java.io.File
import java.io.IOException


class UsuariosDisponiblesActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Permiso de notificación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var binding: ActivityUsuariosDisponiblesBinding

    lateinit var auth: FirebaseAuth
    private lateinit var myRef: DatabaseReference

    lateinit var storageReference: StorageReference

    val users = "users/"

    var usersList = mutableListOf<User>()

    private lateinit var adapter: adapterUsers


    lateinit var vel : ValueEventListener

    //  firebase Database

    private lateinit var database : FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsuariosDisponiblesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        storageReference = FirebaseStorage.getInstance().reference

        adapter = adapterUsers(this@UsuariosDisponiblesActivity, usersList)
        binding.listUsers.adapter = adapter

        createNotificationChannel()  // Crear el canal de notificación
        checkNotificationPermission()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "User Availability Channel"
            val descriptionText = "Notificaciones de disponibilidad de usuarios"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("UserAvailabilityChannel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Solo para Android 13+
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }



    // leer usuarios disponibles

    fun subscribeToUserChanges(){

        myRef = database.getReference(users)
        var vel = myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //binding.listUsers.removeAllViews()
                for(child in snapshot.children){
                    val user = child.getValue<User>()
                    if(user!!.disponible){
                        downloadFile(child.key!!, user)
                        usersList.add(user)
                    }


                }

                adapter.updateUsers(usersList)


            }
            override fun onCancelled(error: DatabaseError) {
//Log error
            }
        })
    }

    override fun onPause() {
        myRef.removeEventListener(vel)
        super.onPause()
    }


    override fun onStart() {
        super.onStart()
        subscribeToUserChanges() // Suscripción a cambios en la lista de usuarios

        // Iniciar el servicio
        val serviceIntent = Intent(this, UserAvailabilityService::class.java)
        startService(serviceIntent)
    }



    @Throws(IOException::class)
    private fun downloadFile(uid : String, user: User) {
        val localFile = File.createTempFile("images", "jpg")
        val imageRef: StorageReference = storageReference.child("images/profile/${uid}/image.jpg")
        imageRef.getFile(localFile)
            .addOnSuccessListener {
                // Successfully downloaded data to local file
// ...
                Log.i("FBApp", "succesfully downloaded")

                // put the image in the user object

                user.image = localFile

                adapter.updateUsers(usersList)

            }.addOnFailureListener {
                // Handle failed download
// ...
            }
    }

}