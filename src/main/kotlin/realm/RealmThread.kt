package realm

import common.Config
import common.Realm
import common.SQLManager
import common.SocketManager
import objects.Account
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*

class RealmThread(sock: Socket?) : Runnable {


    private var _accountName: String? = null
    private var _compte: Account? = null
    private var _hashKey: String? = null
    private var _in: BufferedReader? = null
    private var _out: PrintWriter? = null
    private var _packetNum = 0
    private var _s: Socket? = null
    private var _t: Thread? = null

    init {
        try {
            _s = sock
            _in = BufferedReader(InputStreamReader(_s!!.getInputStream()))
            _out = PrintWriter(_s!!.getOutputStream())
            _t = Thread(this)
            _t?.isDaemon = true
            _t?.start()
        } catch (e: IOException) {
            try {
                if (!_s!!.isClosed) _s!!.close()
            } catch (e1: IOException) {
            }
        } finally {
            if (_compte != null) {
                _compte!!.realmThread = null
                Realm.deleteAccount(_compte!!)
            }
        }
    }

    fun closeSocket() {
        try {
            _s!!.close()
        } catch (e: IOException) {
        }
    }

    private fun kick() {
        try {
            Config.realmServer?.delClient(this)
            Config.agregaralogdemulti("Client was kicked by the server.")
            println("Client was kicked by the server.")
            _in!!.close()
            _out!!.close()
            if (_compte != null) {
                _compte!!.realmThread = null
                Realm.deleteAccount(_compte!!)
            }
            if (!_s!!.isClosed) _s!!.close()
            _t!!.interrupt()
        } catch (e: IOException) {
            println("RealmThreadKick : " + e.message)
            Config.agregaralogdemulti("RealmThreadKick : " + e.message)
            Config.agregaralogdeerrores("RealmThreadKick : " + e.message)
        }
    }

    private fun parsePacket(packet: String) {
        when (_packetNum) {
            1 -> if (!packet.equals(Config.CLIENT_VERSION, ignoreCase = true) && !Config.REALM_IGNORE_VERSION) {
                SocketManager.SEND_REQUIRED_VERSION(_out)
                kick()
            }
            2 -> _accountName = packet.lowercase(Locale.getDefault())
            3 -> {
                if (!packet.substring(0, 2).equals("#1", ignoreCase = true)) {
                    kick()
                    return
                }
                val acc = _accountName?.let { Realm.getCompteByName(it) }
                if (acc != null && acc.isValidPass(
                        packet,
                        _hashKey
                    )
                ) //Si il existe alors il est connect� au Realm && mot de passe OK
                {
                    SocketManager.SEND_ALREADY_CONNECTED(acc.realmThread!!._out)
                    SocketManager.SEND_ALREADY_CONNECTED(_out)
                    return
                }
                if (acc != null && !acc.isValidPass(
                        packet,
                        _hashKey
                    )
                ) //Si il existe alors il est connect� au Realm && mot de passe Invalide
                {
                    SocketManager.SEND_LOGIN_ERROR(_out)
                    return
                }
                _accountName?.let { SQLManager.LOAD_ACCOUNT_BY_USER(it) } //On le "charge"
                _compte = _accountName?.let { Realm.getCompteByName(it) }
                if (_compte == null) //Il n'existe pas
                {
                    SocketManager.SEND_LOGIN_ERROR(_out)
                    return
                }
                if (!_compte!!.isValidPass(packet, _hashKey)) //Mot de passe invalide
                {
                    SocketManager.SEND_LOGIN_ERROR(_out)
                    return
                }
                if (_compte!!.isBanned) //Compte Ban
                {
                    SocketManager.SEND_BANNED(_out)
                    return
                }
                val ip = _s!!.inetAddress.hostAddress
                if (Realm.IPcompareToBanIP(ip)) //IP Ban
                {
                    SocketManager.SEND_BANNED(_out)
                    return
                }
                for ((_, value) in Realm.GameServers)  //On v�rifie qu'il n'est pas connect� dans un GameThread
                {
                    if (value.thread == null) continue
                    value.thread!!.sendDeco(_compte!!.get_GUID()) //On le d�connete du GameThread
                }
                if (_compte!!.realmThread != null) //Ne devrait pas arriver
                {
                    SocketManager.SEND_ALREADY_CONNECTED(_out)
                    SocketManager.SEND_ALREADY_CONNECTED(_compte!!.realmThread!!._out)
                    return
                }
                _compte!!.realmThread = this
                _compte!!.setCurIP(ip)
                SQLManager.UPDATE_ACCOUNT(ip, false, _compte!!.get_subscriberTime(), _compte!!.get_GUID())
                SocketManager.SEND_Ad_Ac_AH_AlK_AQ_PACKETS(
                    _out,
                    _compte!!.get_pseudo(),
                    if (_compte!!.get_gmLvl() > 0) 1 else 0,
                    _compte!!.get_question(),
                    _compte!!.get_gmLvl()
                )
            }
            else -> {
                val ip2 = _s!!.inetAddress.hostAddress
                when (packet.substring(0, 2)) {
                    "Af" -> {
                        val queueID = 1
                        val position = 1
                        _packetNum--
                        SocketManager.SEND_Af_PACKET(_out, position, 1, 1, 0, queueID)
                    }
                    "Ax" -> {
                        if (_compte == null) return
                        SocketManager.SEND_PERSO_LIST(_out, _compte!!.get_subscriberTime(), _compte!!.get_GUID())
                    }
                    "AX" -> {
                        val number = packet.substring(2, 3).toInt()
                        Realm.GameServers[number]!!.thread!!.sendGetOnline()
                        try {
                            Thread.sleep(2000)
                        } catch (e: Exception) {
                        }
                        val ActualP = Realm.GameServers[number]!!.get_NumPlayer()
                        val MaxP = Realm.GameServers[number]!!.get_PlayerLimit()
                        if (ActualP >= MaxP) {
                            SocketManager.SEND_TOO_MANY_PLAYER_ERROR(_out)
                            return
                        }
                        println("RealmThreadOUT : Connexion to the server with the following ip:$ip2")
                        Config.agregaralogdemulti("RealmThreadOUT : Connexion to the server with the following ip:$ip2")
                        SocketManager.SEND_GAME_SERVER_IP(_out, _compte!!.get_GUID(), number)
                    }
                }
            }
        }
    }

    fun refresh() {
        try {
            Config.agregaralogdemulti("RealmThread : Refreshing server list.")
            SocketManager.refresh(_out)
            println("RealmThread : Refreshing server list.")
        } catch (e: Exception) {
            println("RealmThreadRefresh : " + e.message)
            Config.agregaralogdemulti("RealmThreadRefresh : " + e.message)
            Config.agregaralogdeerrores("RealmThreadRefresh : " + e.message)
        }
    }

    override fun run() {
        try {
            var packet = StringBuilder()
            val charCur = CharArray(1)
            SocketManager.SEND_POLICY_FILE(_out)
            _hashKey = SocketManager.SEND_HC_PACKET(_out)
            while (_in!!.read(charCur, 0, 1) != -1 && Config.isRunning) {
                if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r') {
                    packet.append(charCur[0])
                } else if (packet.isNotEmpty()) {
                    Config.agregaralogdemulti("Realm: Recv << $packet")
                    if (Config.REALM_DEBUG) {
                        println("Realm: Recv << $packet")
                        Config.agregaralogdemulti("Realm: Recv << $packet")
                    }
                    _packetNum++
                    parsePacket(packet.toString())
                    packet = StringBuilder()
                }
            }
        } catch (e: IOException) {
            try {
                _in!!.close()
                _out!!.close()
                if (_compte != null) {
                    SQLManager.UPDATE_ACCOUNT("", false, _compte!!.get_subscriberTime(), _compte!!.get_GUID())
                    _compte!!.realmThread = null
                    Realm.deleteAccount(_compte!!)
                }
                if (!_s!!.isClosed) _s!!.close()
                _t!!.interrupt()
            } catch (e1: IOException) {
            }
        } finally {
            try {
                _in!!.close()
                _out!!.close()
                if (_compte != null) {
                    SQLManager.UPDATE_ACCOUNT("", false, _compte!!.get_subscriberTime(), _compte!!.get_GUID())
                    _compte!!.realmThread = null
                    Realm.deleteAccount(_compte!!)
                }
                if (!_s!!.isClosed) _s!!.close()
                _t!!.interrupt()
            } catch (e1: IOException) {
            }
        }
    }
}