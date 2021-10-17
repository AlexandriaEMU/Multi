package communication

import common.Config
import java.io.IOException
import java.net.ServerSocket

class ComServer : Runnable {

    private var _SS: ServerSocket? = null
    private var thread: Thread? = null

    init {
        try {
            _SS = ServerSocket(Config.REALM_COM_PORT)
            thread = Thread(this)
            thread?.isDaemon = true
            thread?.start()
        } catch (e: IOException) {
            println("COMServer: " + e.message)
            Config.agregaralogdecom("COMServer: " + e.message)
            Config.agregaralogdeerrores("COMServer: " + e.message)
            Config.cerrarservidores()
        }
    }

    override fun run() {
        while (Config.isRunning) {
            try {
                ComThread(_SS!!.accept())
            } catch (e: IOException) {
                try {
                    if (!_SS!!.isClosed) _SS!!.close()
                } catch (e1: IOException) {
                }
                println("COMServer Ejecutando: " + e.message)
                Config.agregaralogdecom("COMServer Ejecutando: " + e.message)
                Config.agregaralogdeerrores("COMServer Ejecutando: " + e.message)
            }
        }
    }

    fun kickAll() {
        try {
            _SS!!.close()
        } catch (e: Exception) {
            println("ComServerKickAll : " + e.message)
            Config.agregaralogdecom("ComServerKickAll : " + e.message)
            Config.agregaralogdeerrores("ComServerKickAll : " + e.message)
        }
    }
}