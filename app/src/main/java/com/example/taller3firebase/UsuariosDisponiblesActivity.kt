package com.example.taller3firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import java.io.File
import java.io.IOException


class UsuariosDisponiblesActivity : AppCompatActivity() {
    private val CHANNEL_ID = "Canal del estado de los usuarios"
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

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "User Status Channel"
            val descriptionText = "Channel for user status notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(user: User) {
        val intent = Intent(this, MapsDistanceActivity::class.java).apply {
            putExtra("userLat", user.latitud)
            putExtra("userLng", user.longitud)
            putExtra("userName", user.nombre)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, user.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.campana)
            .setContentTitle("Usuario Disponible")
            .setContentText("${user.nombre} está disponible")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(this)) {
                notify(user.hashCode(), builder.build())
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }


    fun subscribeToUserChanges(){

        myRef = database.getReference(users)
        var vel = myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(child in snapshot.children){
                    val user = child.getValue<User>()
                    if(user!!.disponible){
                        downloadFile(child.key!!, user)
                        usersList.add(user)
                        showNotification(user)
                    }


                }

                adapter.updateUsers(usersList)


            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onPause() {
        myRef.removeEventListener(vel)
        super.onPause()
    }
    override fun onStart() {
        super.onStart()

        subscribeToUserChanges()
    }


    @Throws(IOException::class)
    private fun downloadFile(uid : String, user: User) {
        val localFile = File.createTempFile("images", "jpg")
        val imageRef: StorageReference = storageReference.child("images/profile/${uid}/image.jpg")
        imageRef.getFile(localFile)
            .addOnSuccessListener {

                Log.i("FBApp", "succesfully downloaded")

                user.image = localFile

                adapter.updateUsers(usersList)

            }.addOnFailureListener {

            }
    }

}