package objects

import common.Config
import common.CryptManager
import common.SQLManager
import realm.RealmThread
import kotlin.math.floor

class Account(
    private val _GUID: Int,
    private val _name: String,
    private val _pass: String,
    private val _pseudo: String,
    private val _question: String,
    private val _reponse: String,
    aGmLvl: Int,
    asubscriber: Int,
    aBanned: Boolean,
    aLastIp: String,
    aLastConnectionDate: String,
    agiftID: String
) {

    var lastIP = ""
    var isBanned = false
    private var _gmLvl = 0
    private var _subscriber = 0 //Timestamp en secondes
    private var _curIP = ""
    var lastConnectionDate = ""
    private var _giftID = ""
    var realmThread: RealmThread? = null

    init {
        _gmLvl = aGmLvl
        _subscriber = asubscriber
        isBanned = aBanned
        lastIP = aLastIp
        lastConnectionDate = aLastConnectionDate
        _giftID = agiftID
    }

    fun setCurIP(ip: String) {
        _curIP = ip
    }

    fun isValidPass(pass: String, hash: String?): Boolean {
        return pass == hash?.let { CryptManager.CryptPassword(it, _pass) }
    }

    fun get_GUID(): Int {
        return _GUID
    }

    fun get_name(): String {
        return _name
    }

    fun get_pass(): String {
        return _pass
    }

    fun get_pseudo(): String {
        return _pseudo
    }

    fun get_subscriberTime(): Int //Renvoi le temps restant
    {
        if (!Config.USE_SUBSCRIBE) return 525600
        return if (_subscriber == 0) {
            //Si non abo ou abo d�passer
            0
        } else if (System.currentTimeMillis() / 1000 > _subscriber) {
            //Il faut d�sabonner le compte
            _subscriber = 0
            SQLManager.UPDATE_ACCOUNT(_curIP, true, 0, get_GUID())
            0
        } else {
            //Temps restant
            val TimeRemaining = (_subscriber - System.currentTimeMillis() / 1000).toInt()
            //Conversion en minute
            floor((TimeRemaining / 60).toDouble()).toInt()
        }
    }

    fun get_subscriber(): Int //Renvoi la date limite d'abonnement TimeStamp
    {
        return _subscriber
    }

    fun get_question(): String {
        return _question
    }

    fun get_reponse(): String {
        return _reponse
    }

    fun get_gmLvl(): Int {
        return _gmLvl
    }

    fun get_curIP(): String {
        return _curIP
    }

    fun setGmLvl(gmLvl: Int) {
        _gmLvl = gmLvl
    }

    fun get_giftID(): String {
        return _giftID
    }

    fun set_giftID(_giftID: String) {
        this._giftID = _giftID
    }
}