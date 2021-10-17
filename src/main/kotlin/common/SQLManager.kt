package common

import common.Realm.addAccount
import objects.Account
import objects.GameServer
import org.mariadb.jdbc.ClientPreparedStatement
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

object SQLManager {
    private lateinit var othCon: Connection
    private var timerCommit: Timer? = null
    private var needCommit = false

    @Synchronized
    @Throws(SQLException::class)
    fun executeQuery(query: String?, DBNAME: String?): ResultSet? {
        if (!Config.isInit) return null
        val DB = othCon
        val stat = DB.createStatement()
        val RS = stat.executeQuery(query)
        stat.queryTimeout = 300
        return RS
    }

    @Synchronized
    @Throws(SQLException::class)
    fun executeQueryG(query: String?, G: GameServer?): ResultSet? {
        return if (!Config.isInit) null else try {
            val DB = DriverManager.getConnection("jdbc:mariadb://" + G!!.host + ":3306/" + G.name, G.user, G.password)
            DB.autoCommit = false
            if (!DB.isValid(1000)) return null
            val stat = DB.createStatement()
            val RS = stat.executeQuery(query)
            stat.queryTimeout = 300
            RS
        } catch (e: SQLException) {
            println("SQL : " + e.message)
            Config.agregaralogdeerrores("SQL : " + e.message)
            e.printStackTrace()
            null
        }
    }

    @Synchronized
    @Throws(SQLException::class)
    fun newTransact(baseQuery: String?, dbCon: Connection?): ClientPreparedStatement {
        val toReturn = dbCon!!.prepareStatement(baseQuery) as ClientPreparedStatement
        needCommit = true
        return toReturn
    }

    @Synchronized
    fun commitTransacts() {
        try {
            if (othCon.isClosed) {
                closeCons()
                setUpConnexion()
            }
            othCon.commit()
        } catch (e: SQLException) {
            println("SQL : " + e.message)
            Config.agregaralogdeerrores("SQL : " + e.message)
            e.printStackTrace()
            commitTransacts()
        }
    }

    @Synchronized
    fun closeCons() {
        try {
            commitTransacts()
            othCon.close()
        } catch (e: Exception) {
            println("SQL : Erreur � la fermeture des connexions : " + e.message)
            Config.agregaralogdeerrores("SQL : Erreur � la fermeture des connexions : " + e.message)
            e.printStackTrace()
        }
    }


    fun setUpConnexion(): Boolean {
        return try {
            othCon = DriverManager.getConnection(
                "jdbc:mariadb://" + Config.REALM_DB_HOST + ":3306/" + Config.REALM_DB_NAME,
                Config.REALM_DB_USER,
                Config.REALM_DB_PASSWORD
            )
            othCon.autoCommit = false
            if (!othCon.isValid(1000)) {
                Config.agregaralogdeerrores("SQL : Connexion a la BD invalide!")
                return false
            }
            needCommit = false
            TIMER(true)
            true
        } catch (e: SQLException) {
            println("SQL : " + e.message)
            Config.agregaralogdeerrores("SQL : " + e.message)
            e.printStackTrace()
            false
        }
    }

    private fun TIMER(start: Boolean) {
        if (start) {
            timerCommit = Timer()
            timerCommit!!.schedule(object : TimerTask() {
                override fun run() {
                    if (!needCommit) return
                    commitTransacts()
                    needCommit = false
                }
            }, Config.REALM_DB_COMMIT.toLong(), Config.REALM_DB_COMMIT.toLong())
        } else timerCommit!!.cancel()
    }

    private fun closeResultSet(RS: ResultSet?) {
        try {
            RS!!.statement.close()
            RS.close()
        } catch (e: SQLException) {
            println("SQL : " + e.message)
            Config.agregaralogdeerrores("SQL : " + e.message)
            e.printStackTrace()
        }
    }

    private fun closePreparedStatement(p: ClientPreparedStatement) {
        try {
            p.clearParameters()
            p.close()
        } catch (e: SQLException) {
            println("SQL : " + e.message)
            Config.agregaralogdeerrores("SQL : " + e.message)
            e.printStackTrace()
        }
    }

    fun UPDATE_ACCOUNT(ip: String?, UpdateSub: Boolean, Sub: Int, guid: Int) {
        val bquery: String =
            if (UpdateSub) "UPDATE accounts SET `curIP`=?, `subscription`=? WHERE `guid`=? ;" else "UPDATE accounts SET `curIP`=? WHERE `guid`=? ;"
        try {
            val p = newTransact(bquery, othCon)
            p.setString(1, ip)
            if (UpdateSub) p.setInt(2, Sub)
            p.setInt(if (UpdateSub) 3 else 2, guid)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            Config.agregaralogdeerrores("SQL : " + e.message)
            try {
                if (othCon.isClosed) setUpConnexion()
                if (!othCon.isClosed) UPDATE_ACCOUNT(ip, UpdateSub, Sub, guid)
            } catch (e1: SQLException) {
                println("SQL : " + e.message)
                Config.agregaralogdeerrores("SQL : " + e.message)
                e1.printStackTrace()
            }
        }
    }

    fun RESET_CUR_IP() {
        val bquery = "UPDATE accounts SET `curIP`=?;"
        try {
            val p = newTransact(bquery, othCon)
            p.setString(1, "")
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            println("SQL : " + e.message)
            Config.agregaralogdeerrores("SQL : " + e.message)
            e.printStackTrace()
        }
    }


    fun getNumberPersosOnThisServer(guid: Int, ID: Int): Int {
        var a = 0
        val G = Realm.GameServers[ID]
        try {
            val RS = executeQueryG("SELECT COUNT(*) from personnages WHERE account=$guid;", G)
            RS!!.next()
            a = RS.getInt(1)
            closeResultSet(RS)
        } catch (e: SQLException) {
            Config.agregaralogdeerrores("SQL : " + e.message)
            try {
                if (othCon.isClosed) setUpConnexion()
                if (!othCon.isClosed) getNumberPersosOnThisServer(guid, ID)
            } catch (e1: SQLException) {
                println("SQL : " + e.message)
                Config.agregaralogdeerrores("SQL : " + e.message)
                e1.printStackTrace()
            }
        }
        return a
    }

    fun LOAD_ACCOUNT_BY_USER(user: String) {
        try {
            val RS = executeQuery("SELECT * from accounts WHERE `account` LIKE '$user';", Config.REALM_DB_NAME)
            while (RS!!.next()) {
                addAccount(
                    Account(
                        RS.getInt("guid"),
                        RS.getString("account").lowercase(Locale.getDefault()),
                        RS.getString("pass"),
                        RS.getString("pseudo"),
                        RS.getString("question"),
                        RS.getString("reponse"),
                        RS.getInt("level"),
                        RS.getInt("subscription"),
                        RS.getInt("banned") == 1,
                        RS.getString("lastIP"),
                        RS.getString("lastConnectionDate"),
                        RS.getString("giftID")
                    )
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            Config.agregaralogdeerrores("SQL : " + e.message)
            try {
                if (othCon.isClosed) setUpConnexion()
                if (!othCon.isClosed) LOAD_ACCOUNT_BY_USER(user)
            } catch (e1: SQLException) {
                println("SQL : " + e.message)
                Config.agregaralogdeerrores("SQL : " + e.message)
                e1.printStackTrace()
            }
        }
    }

    fun LOAD_SERVERS() {
        try {
            val RS = executeQuery("SELECT * from gameservers;", Config.REALM_DB_NAME)
            while (RS!!.next()) {
                Realm.GameServers[RS.getInt("ID")] = GameServer(
                    RS.getInt("ID"),
                    RS.getString("ServerIP"),
                    RS.getInt("ServerPort"),
                    RS.getInt("State"),
                    RS.getString("ServerBDD"),
                    RS.getString("ServerDBName"),
                    RS.getString("ServerUser"),
                    RS.getString("ServerPassword"),
                    RS.getString("Key")
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            println("SQL : " + e.message)
            Config.agregaralogdeerrores("SQL : " + e.message)
            e.printStackTrace()
        }
    }

    fun LOAD_BANIP(): Int {
        var i = 0
        try {
            val RS = executeQuery("SELECT ip from banip;", Config.REALM_DB_NAME)
            while (RS!!.next()) {
                if (!RS.isLast) Realm.BAN_IP += RS.getString("ip") + "," else Realm.BAN_IP += RS.getString("ip")
                i++
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            println("SQL : " + e.message)
            Config.agregaralogdeerrores("SQL : " + e.message)
            e.printStackTrace()
        }
        return i
    }


    fun ADD_BANIP(ip: String?) {
        val baseQuery = "INSERT INTO `banip` VALUES (?);"
        try {
            val p = newTransact(baseQuery, othCon)
            p.setString(1, ip)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            Config.agregaralogdeerrores("SQL : " + e.message)
            try {
                if (othCon.isClosed) setUpConnexion()
                if (!othCon.isClosed) ADD_BANIP(ip)
            } catch (e1: SQLException) {
                println("SQL : " + e.message)
                Config.agregaralogdeerrores("SQL : " + e.message)
                e1.printStackTrace()
            }
        }
    }
}