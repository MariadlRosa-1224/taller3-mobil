package model

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cursoradapter.widget.CursorAdapter
import com.example.taller3firebase.MapsDistanceActivity
import com.example.taller3firebase.R
import com.google.android.play.integrity.internal.c

class adapterUsers (private val context: Context?, private var users: List<User>) : BaseAdapter(){

    override fun getCount(): Int {
        return users.size
    }

    override fun getItem(position: Int): User {
        return users[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.usuario, parent, false)

        val tvname = view!!.findViewById<TextView>(R.id.nombre)
        val image = view.findViewById<ImageView>(R.id.userimage)
        val button = view.findViewById<Button>(R.id.button4)

        val user : User = getItem(position)
        tvname.text = user.nombre
        if (user.image != null) {
            val bitmap = BitmapFactory.decodeFile(user.image!!.absolutePath)
            image.setImageBitmap(bitmap)
        }else {
            image.setImageResource(R.drawable.user_black)
        }

        button.setOnClickListener {
            val intent = Intent(context, MapsDistanceActivity::class.java)
            intent.putExtra("userLat", user.latitud)
            intent.putExtra("userLng", user.longitud)
            intent.putExtra("userName", user.nombre)
            context?.startActivity(intent)
        }

        return view
    }

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}