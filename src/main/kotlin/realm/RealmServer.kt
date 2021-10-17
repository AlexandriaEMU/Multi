package realm

import common.Config
import java.io.IOException
import java.net.ServerSocket

class RealmServer : Runnable {

    private var _SS: ServerSocket? = null
    private var thread: Thread? = null
    private val _clients = ArrayList<RealmThread>()

    init {
        try {
            _SS = ServerSocket(Config.REALM_PORT)
            thread = Thread(this)
            thread?.isDaemon = true
            thread?.start()
        } catch (e: IOException) {
            println("RealmServer : " + e.message)
            Config.agregaralogdemulti("RealmServer : " + e.message)
            Config.agregaralogdeerrores("RealmServer : " + e.message)
            Config.cerrarservidores()
        }
    }

    override fun run() {
        while (Config.isRunning) {
            try {
                _clients.add(RealmThread(_SS!!.accept()))
            } catch (e: IOException) {
                try {
                    if (!_SS!!.isClosed) _SS!!.close()
                } catch (e1: IOException) {
                }
                println("RealmServerRun : " + e.message)
                Config.agregaralogdemulti("RealmServerRun : " + e.message)
                Config.agregaralogdeerrores("RealmServerRun : " + e.message)
            }
        }
    }

    fun kickAll() {
        try {
            _SS!!.close()
        } catch (e: Exception) {
            println("RealmServerKickAll : " + e.message)
            Config.agregaralogdemulti("RealmServerKickAll : " + e.message)
            Config.agregaralogdeerrores("RealmServerKickAll : " + e.message)
        }
        val c = ArrayList(_clients)
        for (RT in c) {
            try {
                RT.closeSocket()
            } catch (e: Exception) {
            }
        }
    }

    fun delClient(RT: RealmThread) {
        _clients.remove(RT)
    }
}