package model

class User {
    private var nombre: String? = null
    private var apellido: String? = null
    private var email: String? = null
    private var password: String? = null
    private var latitud: Double? = null
    private var longitud: Double? = null
    private var disponible: Boolean = false

    constructor() {}

    constructor(nombre: String?, apellido: String?, email: String?, password: String?, latitud: Double?, longitud: Double?) {
        this.nombre = nombre
        this.apellido = apellido
        this.email = email
        this.password = password
        this.latitud = latitud
        this.longitud = longitud
        this.disponible = false
    }

    // Establecer como disponible

    public fun setDisponible(disponible: Boolean) {
        this.disponible = disponible
    }
}