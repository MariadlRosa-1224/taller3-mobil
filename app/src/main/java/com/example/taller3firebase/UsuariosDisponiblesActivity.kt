package com.example.taller3firebase

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taller3firebase.databinding.ActivityUsuariosDisponiblesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import model.User
import model.adapterUsers

class UsuariosDisponiblesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUsuariosDisponiblesBinding

    lateinit var auth: FirebaseAuth
    lateinit var myRef: DatabaseReference

    val users = "users/"

    var usersList = mutableListOf<User>()

    val adapterUsers = adapterUsers(this, usersList)

    lateinit var vel : ValueEventListener

    //  firebase Database

    private lateinit var database : FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsuariosDisponiblesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()


        subscribeToUserChanges()

    }



    // leer usuarios disponibles

    fun subscribeToUserChanges(){

        myRef = database.getReference(users)
        var vel = myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //binding.listUsers.removeAllViews()
                for(child in snapshot.children){
                    val user = child.getValue<User>()
                    usersList.add(user!!)

                }

                val adapter = adapterUsers(this@UsuariosDisponiblesActivity, usersList)
                binding.listUsers.adapter = adapter


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

}