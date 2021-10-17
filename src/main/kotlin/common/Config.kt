package common

import communication.ComServer
import realm.RealmServer
import java.io.*
import java.util.*
import kotlin.system.exitProcess

object Config {

    var CLIENT_VERSION = "1.29.1"
    private const val CONFIG = "config.txt"
    private var Log_Com: BufferedWriter? = null
    private var Log_Errors: BufferedWriter? = null

    /* LOGS */
    private var Log_Realm: BufferedWriter? = null
    var REALM_COM_PORT = -1
    var REALM_DB_COMMIT = 30 * 1000
    var REALM_DB_HOST: String? = null
    var REALM_DB_NAME: String? = null
    var REALM_DB_PASSWORD: String? = null
    var REALM_DB_USER: String? = null


    var REALM_DEBUG = false
    var REALM_IGNORE_VERSION = false
    var REALM_PORT = -1
    var USE_SUBSCRIBE = false
    var comServer: ComServer? = null
    var isInit = false


    var isRunning = false

    /* THEARDS */
    var realmServer: RealmServer? = null


    @Synchronized
    fun agregaralogdecom(str: String) {
        try {
            val date = Calendar.HOUR_OF_DAY.toString() + ":" + Calendar.MINUTE + ":" + Calendar.SECOND
            Log_Com!!.write("$date: $str")
            Log_Com!!.newLine()
            Log_Com!!.flush()
        } catch (e: Exception) {
            println("No se pudieron escribir los logs")
        }
    }


    @Synchronized
    fun agregaralogdeerrores(str: String) {
        try {
            val date = Calendar.HOUR_OF_DAY.toString() + ":" + Calendar.MINUTE + ":" + Calendar.SECOND
            Log_Errors!!.write("$date: $str")
            Log_Errors!!.newLine()
            Log_Errors!!.flush()
        } catch (e: Exception) {
            println("No se pudieron escribir los logs")
        }
    }

    @Synchronized
    fun agregaralogdemulti(str: String) {
        try {
            val date = Calendar.HOUR_OF_DAY.toString() + ":" + Calendar.MINUTE + ":" + Calendar.SECOND
            Log_Realm!!.write("$date: $str")
            Log_Realm!!.newLine()
            Log_Realm!!.flush()
        } catch (e: Exception) {
            println("No se pudieron escribir los logs")
        }
    }

    fun cargarconfiguracion() {
        try {
            val config = BufferedReader(FileReader(CONFIG))
            config.readLines().forEach lop@{ it ->
                if (it.split("=".toRegex()).toTypedArray().size == 1) return@lop
                val param = it.split("=".toRegex()).toTypedArray()[0].trim { it <= ' ' }
                val value = it.split("=".toRegex()).toTypedArray()[1].trim { it <= ' ' }
                if (param.equals("REALM_DB_COMMIT", ignoreCase = true)) {
                    REALM_DB_COMMIT = value.toInt()
                } else if (param.equals("CLIENT_VERSION", ignoreCase = true)) {
                    CLIENT_VERSION = value
                } else if (param.equals("REALM_PORT", ignoreCase = true)) {
                    try {
                        REALM_PORT = value.toInt()
                    } catch (e: Exception) {
                        println("REALM_PORT doit etre un entier!")
                        exitProcess(1)
                    }
                } else if (param.equals("REALM_DB_HOST", ignoreCase = true)) {
                    REALM_DB_HOST = value
                } else if (param.equals("REALM_IGNORE_VERSION", ignoreCase = true)) {
                    REALM_IGNORE_VERSION = value.equals("true", ignoreCase = true)
                } else if (param.equals("REALM_DB_USER", ignoreCase = true)) {
                    REALM_DB_USER = value
                } else if (param.equals("REALM_DB_PASSWORD", ignoreCase = true)) {
                    REALM_DB_PASSWORD = value
                } else if (param.equals("REALM_DB_NAME", ignoreCase = true)) {
                    REALM_DB_NAME = value
                } else if (param.equals("REALM_DEBUG", ignoreCase = true)) {
                    REALM_DEBUG = value.equals("true", ignoreCase = true)
                } else if (param.equals("REALM_COM_PORT", ignoreCase = true)) {
                    REALM_COM_PORT = value.toInt()
                } else if (param.equals("USE_SUBSCRIBE", ignoreCase = true)) {
                    USE_SUBSCRIBE = value.equals("true", ignoreCase = true)
                }
            }
            if (REALM_DB_NAME == null || REALM_DB_HOST == null || REALM_DB_PASSWORD == null || REALM_DB_USER == null || REALM_PORT == -1 || REALM_COM_PORT == -1) {
                throw Exception()
            }
        } catch (e: Exception) {
            println(e.message)
            println("Fichier de configuration non existant ou illisible !")
            println("Fermeture du serveur de connexion.")
            exitProcess(1)
        }
        try {
            val date =
                Calendar.getInstance()[Calendar.DAY_OF_MONTH].toString() + "-" + (Calendar.getInstance()[Calendar.MONTH] + 1) + "-" + Calendar.getInstance()[Calendar.YEAR]
            if (!File("Realm_logs").exists()) {
                File("Realm_logs").mkdir()
            }
            if (!File("Error_logs").exists()) {
                File("Error_logs").mkdir()
            }
            if (!File("Com_logs").exists()) {
                File("Com_logs").mkdir()
            }
            Log_Realm = BufferedWriter(FileWriter("Realm_logs/$date.txt", true))
            Log_Com = BufferedWriter(FileWriter("Com_logs/$date.txt", true))
            Log_Errors = BufferedWriter(FileWriter("Error_logs/$date.txt", true))
        } catch (e: IOException) {
            println("No se pudieron crear los logs")
            println(e.message)
            exitProcess(0)
        }
    }

    fun cerrarservidores() {
        if (isRunning) {
            agregaralogdemulti("REALM cerrado: Eliminando conexiones")
            isRunning = false
            try {
                if (realmServer != null) realmServer!!.kickAll()
            } catch (e: Exception) {
                println(e.message)
            }
            agregaralogdecom("COM cerrado: Eliminando conexiones")
            try {
                if (comServer != null) comServer!!.kickAll()
            } catch (e: Exception) {
                println(e.message)
            }
        }
        isRunning = false
        println("Cerrando el multi: OK")
    }


}