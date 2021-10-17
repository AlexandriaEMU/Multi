package common

import objects.Account
import objects.GameServer
import java.util.*

object Realm {

    private val Accounts: MutableMap<Int, Account> = TreeMap() //by GUID
    private val Accounts2: MutableMap<String, Int> = TreeMap() //by Name

    var GameServers: MutableMap<Int, GameServer> = TreeMap()

    var BAN_IP = ""

    val accountsMap: Map<Int, Account>
        get() = Accounts

    fun IPcompareToBanIP(ip: String): Boolean {
        val split = BAN_IP.split(",".toRegex()).toTypedArray()
        for (ipsplit in split) {
            if (ip.compareTo(ipsplit) == 0) return true
        }
        return false
    }

    fun cargandomulti() {
        println("\n")
        println("=== Cargando el multi ===")
        print("Cargando servidores activos: ")
        SQLManager.LOAD_SERVERS()
        println(GameServers.size.toString() + " servidores cargados.")
        print("Cargando las IP baneadas: ")
        val nbr = SQLManager.LOAD_BANIP()
        println("$nbr IP cargadas")
        print("Reestableciendo las IP a 0: ")
        SQLManager.RESET_CUR_IP()
        println("OK")
        Config.isRunning = true
    }

    fun addAccount(acc: Account) {
        if (Accounts.containsKey(acc.get_GUID())) {
            Accounts2.remove(acc.get_name())
            Accounts.remove(acc.get_GUID())
        }
        Accounts[acc.get_GUID()] = acc
        Accounts2[acc.get_name().lowercase(Locale.getDefault())] = acc.get_GUID()
    }

    fun deleteAccount(acc: Account) {
        Accounts.remove(acc.get_GUID())
        Accounts2.remove(acc.get_name().lowercase(Locale.getDefault()))
    }

    fun getCompteByID(guid: Int): Account? {
        return Accounts[guid]
    }

    fun getCompteByName(name: String): Account? {
        var guid = -1
        guid = try {
            Accounts2[name.lowercase(Locale.getDefault())]!!
        } catch (e: Exception) {
            return null
        }
        return Accounts[guid]
    }
}