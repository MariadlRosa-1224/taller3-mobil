package model

import java.io.File

class User {
    var nombre: String? = ""
    var apellido: String? = ""
    var email: String? = ""
    var latitud: Double? = 0.0
    var longitud: Double? = 0.0
    var disponible: Boolean = false
    var image : File? = null

    constructor() {}

    constructor(nombre: String?, apellido: String?, email: String?, latitud: Double?, longitud: Double?) {
        this.nombre = nombre
        this.apellido = apellido
        this.email = email
        this.latitud = latitud
        this.longitud = longitud
        this.disponible = false
        this.image = null
    }

}