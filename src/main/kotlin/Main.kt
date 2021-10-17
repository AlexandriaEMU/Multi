import common.Config.REALM_COM_PORT
import common.Config.REALM_PORT
import common.Config.agregaralogdecom
import common.Config.agregaralogdeerrores
import common.Config.agregaralogdemulti
import common.Config.cargarconfiguracion
import common.Config.cerrarservidores
import common.Config.comServer
import common.Config.isInit
import common.Config.realmServer
import common.Realm.cargandomulti
import common.SQLManager.setUpConnexion
import communication.ComServer
import realm.RealmServer
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    Runtime.getRuntime().addShutdownHook(Thread { cerrarservidores() })
    println("==============================================================")
    println("AlexandriaEMU - MULTI")
    println("Por Player-xD, basado en AncestraR v54 - Gracias DIABU")
    println("Traducido a Kotlin por Oxakromax")
    println("==============================================================\n")
    print("Cargando el archivo de configuracion: ")
    cargarconfiguracion()
    println("OK")
    isInit = true
    print("Conectando a la base de datos: ")
    if (setUpConnexion()) {
        println("OK")
    } else {
        println("Conexion invalida")
        agregaralogdeerrores("SQL: Fallo la conexion")
        exitProcess(0)
    }
    cargandomulti()
    print(
        """
    
    
    Creacion de REALM en el puerto $REALM_PORT
    """.trimIndent()
    )
    realmServer = RealmServer()
    println(": OK")
    print("Creacion de COM en el puerto $REALM_COM_PORT")
    comServer = ComServer()
    println(": OK")
    println("\nAtento a nuevas conexiones\n")
    agregaralogdemulti("REALM iniciado: Atento a nuevas conexiones")
    agregaralogdecom("COM iniciado: Atento a nuevas conexiones")
}