package com.example.taller3firebase

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

        subscribeToUserChanges()
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