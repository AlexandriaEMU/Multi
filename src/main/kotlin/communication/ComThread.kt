package communication

import common.Config
import common.Config.agregaralogdecom
import common.Config.agregaralogdeerrores
import common.Realm
import common.Realm.accountsMap
import common.SQLManager.ADD_BANIP
import objects.GameServer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class ComThread(sock: Socket) : Runnable {

    private var In: BufferedReader? = null
    private var Thr: Thread? = null
    private var Out: PrintWriter? = null
    private var Sock: Socket? = null
    private var Server: GameServer? = null

    init {
        try {
            Sock = sock
            In = BufferedReader(InputStreamReader(Sock!!.getInputStream()))
            Out = PrintWriter(Sock!!.getOutputStream())
            Thr = Thread(this)
            Thr?.isDaemon = true
            Thr?.start()
        } catch (e: IOException) {
            try {
                if (!Sock!!.isClosed) Sock!!.close()
            } catch (ignored: IOException) {
            }
        } finally {
            if (Server != null) {
                Server?.state = 0
                agregaralogdecom("ComThread : Server revoked.")
                println("ComThread : Server revoked.")
                for (acc in accountsMap.values) {
                    if (acc.realmThread == null) continue
                    acc.realmThread!!.refresh()
                }
                Server?.thread = null
            }
        }
    }

    private fun kick() {
        try {
            agregaralogdecom("ComThread : The GameServer has broken the connection.")
            println("ComThread : The GameServer has broken the connection.")
            if (Server != null) {
                Server?.state = 0
                agregaralogdecom("ComThread : Server revoked.")
                println("ComThread : Server revoked.")
                for (acc in accountsMap.values) {
                    if (acc.realmThread == null) continue
                    acc.realmThread!!.refresh()
                }
                Server?.thread = null
            }
            In!!.close()
            Out!!.close()
            if (!Sock!!.isClosed) Sock!!.close()
            Thr!!.interrupt()
        } catch (e: IOException) {
            println("ComThreadKick : " + e.message)
            agregaralogdeerrores("ComThreadKick : " + e.message)
            agregaralogdecom("ComThreadKick : " + e.message)
        }
    }

    fun sendDeco(guid: Int) {
        println("ComThread: Send>>LO$guid")
        println("ComThread : Envoi du paquet de LoginOut ...")
        agregaralogdecom("ComThread: Send>>LO$guid")
        agregaralogdecom("ComThread : Envoi du paquet de LoginOut ...")
        try {
            Out!!.print("LO" + guid + 0x00.toChar()) //LogOut
            Out!!.flush()
            println("ComThread : Envoi OK.")
        } catch (e: Exception) {
            println("ComThreadSendDeco : " + e.message)
            agregaralogdeerrores("ComThreadSendDeco : " + e.message)
            agregaralogdecom("ComThreadSendDeco : " + e.message)
        }
    }

    fun sendAddWaiting(str: String) {
        println("ComThread: Send>>AW$str")
        println("ComThread : Envoi du paquet d'ajout de compte.")
        agregaralogdecom("ComThread: Send>>AW$str")
        agregaralogdecom("ComThread : Envoi du paquet d'ajout de compte.")
        try {
            Out!!.print("AW" + str + 0x00.toChar()) //AddWaiting
            Out!!.flush()
            println("ComThread : Envoi OK.")
        } catch (e: java.lang.Exception) {
            println("ComThreadSendAddWaiting : " + e.message)
            agregaralogdeerrores("ComThreadSendAddWaiting : " + e.message)
            agregaralogdecom("ComThreadSendAddWaiting : " + e.message)
        }
    }

    fun sendGetOnline() {
        println("ComThread: Send>>GO")
        println("ComThread : Envoi du paquet GetOnline ...")
        agregaralogdecom("ComThread: Send>>GO")
        agregaralogdecom("ComThread : Envoi du paquet GetOnline ...")
        try {
            Out!!.print("GO" + 0x00.toChar()) //GetOnline
            Out!!.flush()
            println("ComThread : Envoi OK.")
        } catch (e: java.lang.Exception) {
            println("ComThreadSendGetOnline : " + e.message)
            agregaralogdecom("ComThreadSendGetOnline : " + e.message)
            agregaralogdeerrores("ComThreadSendGetOnline : " + e.message)
        }
    }

    private fun parsePacket(packet: String?) {
        when (packet!![0]) {
            'G' -> {
                when (packet[1]) {
                    'A' -> {
                        //Add
                        agregaralogdecom("ComThread : Packet GA recu, ajout d'un serveur...")
                        println("ComThread : Packet GA recu, ajout d'un serveur...")
                        val key = packet.substring(2)
                        agregaralogdecom("ComThread : Serveur KEY : $key")
                        println("ComThread : Serveur KEY : $key")
                        for (G in Realm.GameServers.values) {
                            if (key.equals(G.key, ignoreCase = true)) Server = G
                        }
                        if (Server == null) {
                            kick()
                            return
                        }
                        Server?.thread = this
                        Server?.state = 1
                        agregaralogdecom("ComThread : Serveur OK!")
                        println("ComThread : Serveur OK!")
                    }
                    'O' -> {
                        //Online
                        if (Server == null) {
                            kick()
                            return
                        }
                        val str = packet.substring(2).split(";".toRegex()).toTypedArray()
                        Server!!.set_PlayerLimit(str[0].toInt())
                        Server!!.set_NumPlayer(str[1].toInt())
                    }
                }
            }
            'S' -> {
                if (Server == null) {
                    kick()
                    return
                }
                when (packet[1]) {
                    'O' -> {
                        //Open
                        agregaralogdecom("ComThread : Packet SO recu, changement d'etat : 1.")
                        println("ComThread : Packet SO recu, changement d'etat : 1.")
                        Server!!.state = 1
                    }
                    'S' -> {
                        //Save
                        agregaralogdecom("ComThread : Packet SS recu, changement d'etat : 2.")
                        println("ComThread : Packet SS recu, changement d'etat : 2.")
                        Server!!.state = 2
                    }
                    'D' -> {
                        //Disconnected
                        agregaralogdecom("ComThread : Packet SD recu, changement d'etat : 0.")
                        println("ComThread : Packet SD recu, changement d'etat : 1.")
                        Server!!.state = 0
                    }
                }
            }
            'R' -> {
                if (Server == null) {
                    kick()
                    return
                }
                when (packet[1]) {
                    'G' -> {
                        //GMLEVEL BLOCK, arg : int[level]
                        agregaralogdecom(
                            "ComThread : Packet RG recu, blocage du serveur au GMlevels < " + packet.substring(
                                2
                            ).toInt()
                        )
                        println(
                            "ComThread : Packet RG recu, blocage du serveur au GMlevels < " + packet.substring(2)
                                .toInt()
                        )
                        Server!!.blockLevel = packet.substring(2).toInt()
                    }
                    'A' -> {
                        //ADD BANIP, arg : String[ip]
                        agregaralogdecom("ComThread : Packet RA recu, ban de l'IP : " + packet.substring(2))
                        println("ComThread : Packet RA recu, ban de l'IP : " + packet.substring(2))
                        ADD_BANIP(packet.substring(2))
                        Realm.BAN_IP += packet.substring(2) + ","
                    }
                }
            }
        }
        for (r in accountsMap.values) {
            r.realmThread?.refresh()
        }
    }

    override fun run() {
        try {
            var packet = StringBuilder()
            val charCur = CharArray(1)
            while (In!!.read(charCur, 0, 1) != -1 && Config.isRunning) {
                if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r') {
                    packet.append(charCur[0])
                } else if (packet.isNotEmpty()) {
                    if (Config.REALM_DEBUG) {
                        println("ComThread: Recv << $packet")
                        agregaralogdecom("ComThread: Recv << $packet")
                    }
                    parsePacket(packet.toString())
                    packet = StringBuilder()
                }
            }
        } catch (e: IOException) {
            try {
                In!!.close()
                Out!!.close()
                if (Server != null) {
                    Server?.state = 0
                    agregaralogdecom("ComThread : Server revoked.")
                    println("ComThread : Server revoked.")
                    for (acc in accountsMap.values) {
                        if (acc.realmThread == null) continue
                        acc.realmThread!!.refresh()
                    }
                    Server?.thread = null
                }
                if (!Sock!!.isClosed) Sock!!.close()
                Thr!!.interrupt()
            } catch (ignored: IOException) {
            }
        }
    }
}