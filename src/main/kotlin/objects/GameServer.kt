package objects

import communication.ComThread

class GameServer(
    val iD: Int,
    val iP: String,
    val port: Int,
    var state: Int,
    val host: String,
    val name: String,
    val user: String,
    var password: String?,
    KEY: String
) {

    val key: String
    var thread: ComThread? = null
    var blockLevel = 0
    private var PlayerLimit = 0
    private var NumPlayer = 0

    init {
        if (password == null) password = ""
        key = KEY
    }

    fun set_PlayerLimit(num: Int) {
        PlayerLimit = num
    }

    fun get_PlayerLimit(): Int {
        return PlayerLimit
    }

    fun set_NumPlayer(num: Int) {
        NumPlayer = num
    }

    fun get_NumPlayer(): Int {
        return NumPlayer
    }
}