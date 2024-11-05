package com.example.taller3firebase

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taller3firebase.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    // Autenticación de Firebase
    private lateinit var auth: FirebaseAuth

    private val notificationsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ActivityResultCallback {
            if (it) {
                Toast.makeText(this, "Permission granted to post notifications", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission denied to post notifications", Toast.LENGTH_LONG).show()
            }
        }
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationsPermissionRequest(android.Manifest.permission.POST_NOTIFICATIONS)
        }


        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()


        // Iniciar sesión

        binding.loginButton.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            if(validateForm(email, password)){
                signIn(email, password)
            }
        }

        binding.registerButton.setOnClickListener {
            val i = Intent(this, registerActivity::class.java)
            startActivity(i)
        }

    }

    private fun notificationsPermissionRequest(permission: String) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            if (shouldShowRequestPermissionRationale(permission)) {
                Toast.makeText(this, "The permission is needed", Toast.LENGTH_LONG).show()
            }
            notificationsPermission.launch(permission)
        } else {
            Toast.makeText(this, "Permission granted to post notifications", Toast.LENGTH_LONG).show()
        }
    }

    // Si el usuario ya ha iniciado sesión, ir al MapsActivity

    override fun onStart(){
        super.onStart()
        updateUI(auth.currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if(currentUser!=null) {
//Already signed in
            val i = Intent(this, MapsActivity::class.java)
            i.putExtra("email", currentUser.email.toString())
            startActivity(i)
        }
    }


    // Iniciar sesión con correo electrónico y contraseña

    private fun signIn(email:String, password:String){
        if(validEmailAddress(email) && password!=null){
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if(it.isSuccessful){
                    updateUI(auth.currentUser)
                }else{
                    val message = it.exception!!.message
                    Toast.makeText(this, message, Toast.LENGTH_LONG ).show()
                    Log.w(TAG, "signInWithEmailAndPassword:failure", it.exception)
                    binding.email.text.clear()
                    binding.password.text.clear()
                }
            }
        }
    }


    // Validar formulario

    private fun validateForm(email : String, password: String) : Boolean {
        var valid = false
        if (email.isEmpty()) {
            binding.email.setError("Required!")
        } else if (!validEmailAddress(email)) {
            binding.email.setError("Invalid email address")
        } else if (password.isEmpty()) {
            binding.password.setError("Required!")
        } else if (password.length < 6){
            binding.password.setError("Password should be at least 6 characters long!")
        }else {
            valid = true
        }
        return valid
    }

    // validar dirección de correo electrónico

    private fun validEmailAddress(email:String):Boolean{
        val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return email.matches(regex.toRegex())
    }




}