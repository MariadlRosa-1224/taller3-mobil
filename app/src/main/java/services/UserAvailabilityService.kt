package services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taller3firebase.R
import com.example.taller3firebase.UsuariosDisponiblesActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import model.User

class UserAvailabilityService : Service() {

    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var listener: ValueEventListener

    override fun onCreate() {
        super.onCreate()

        // Crear el canal de notificación
        createNotificationChannel()

        // Inicializar Firebase y obtener referencia a la lista de usuarios
        database = FirebaseDatabase.getInstance()
        userRef = database.getReference("users")

        // Configurar el listener para escuchar cambios en la disponibilidad de los usuarios
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    user?.let {
                        if (user.disponible) {
                            // Enviar notificación si el usuario está disponible
                            sendAvailabilityNotification(user)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserAvailabilityService, "Error al leer los datos", Toast.LENGTH_SHORT).show()
            }
        }

        // Añadir el listener a la referencia
        userRef.addValueEventListener(listener)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "User Availability Channel"
            val descriptionText = "Notifications for user availability"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("UserAvailabilityChannel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendAvailabilityNotification(user: User) {
        // Verificar si el permiso está concedido
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED) {
            return // Salir si no hay permiso
        }

        // Construir la notificación
        val notification = NotificationCompat.Builder(this, "UserAvailabilityChannel")
            .setContentTitle("Usuario disponible")
            .setContentText("${user.nombre} ahora está disponible")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Mostrar la notificación
        NotificationManagerCompat.from(this).notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Eliminar el listener al destruir el servicio
        userRef.removeEventListener(listener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
