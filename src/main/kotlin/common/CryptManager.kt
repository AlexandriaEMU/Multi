package common

import java.nio.charset.StandardCharsets
import kotlin.math.pow

object CryptManager {
    private val HASH = charArrayOf(
        'a',
        'b',
        'c',
        'd',
        'e',
        'f',
        'g',
        'h',
        'i',
        'j',
        'k',
        'l',
        'm',
        'n',
        'o',
        'p',
        'q',
        'r',
        's',
        't',
        'u',
        'v',
        'w',
        'x',
        'y',
        'z',
        'A',
        'B',
        'C',
        'D',
        'E',
        'F',
        'G',
        'H',
        'I',
        'J',
        'K',
        'L',
        'M',
        'N',
        'O',
        'P',
        'Q',
        'R',
        'S',
        'T',
        'U',
        'V',
        'W',
        'X',
        'Y',
        'Z',
        '0',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9',
        '-',
        '_'
    )

    fun CryptPassword(Key: String, Password: String): String {
        val _Crypted = StringBuilder("#1")
        for (i in Password.indices) {
            val PPass = Password[i]
            val PKey = Key[i]
            val APass = PPass.code / 16
            val AKey = PPass.code % 16
            val ANB = (APass + PKey.code) % HASH.size
            val ANB2 = (AKey + PKey.code) % HASH.size
            _Crypted.append(HASH[ANB])
            _Crypted.append(HASH[ANB2])
        }
        return _Crypted.toString()
    }

    fun decryptpass(pass: String, key: String): String {
        var l2: Int
        var l3: Int
        var l4: Int
        var l5: Int
        val l7 = StringBuilder()
        val Chaine = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
        var l1: Int = 0
        while (l1 <= pass.length - 1) {
            l3 = key[l1 / 2].code
            l2 = Chaine.indexOf(pass[l1])
            l4 = 64 + l2 - l3
            val l11 = l1 + 1
            l2 = Chaine.indexOf(pass[l11])
            l5 = 64 + l2 - l3
            if (l5 < 0) l5 += 64
            l7.append((16 * l4 + l5).toChar())
            l1 += 2
        }
        return l7.toString()
    }

    fun CryptIP(IP: String): String {
        val Splitted = IP.split("\\.".toRegex()).toTypedArray()
        val Encrypted = StringBuilder()
        var Count = 0
        var i = 0
        while (i < 50) {
            var o = 0
            while (o < 50) {
                if (i and 15 shl 4 or o and 15 == Splitted[Count].toInt()) {
                    val A = (i + 48).toChar()
                    val B = (o + 48).toChar()
                    Encrypted.append(A.toString()).append(B.toString())
                    i = 0
                    o = 0
                    Count++
                    if (Count == 4) return Encrypted.toString()
                }
                o++
            }
            i++
        }
        return "DD"
    }

    fun CryptPort(config_game_port: Int): String {
        var P = config_game_port
        val nbr64 = StringBuilder()
        for (a in 2 downTo 0) {
            nbr64.append(HASH[(P / 64.0.pow(a.toDouble())).toInt()])
            P = (P % 64.0.pow(a.toDouble()).toInt())
        }
        return nbr64.toString()
    }

    fun getIntByHashedValue(c: Char): Int {
        for (a in HASH.indices) {
            if (HASH[a] == c) {
                return a
            }
        }
        return -1
    }

    fun getHashedValueByInt(c: Int): Char {
        return HASH[c]
    }


    fun toUtf(_in: String): String {
        var _out = ""
        _out = try {
            String(_in.toByteArray(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            println("CryptManager : Conversion en UTF-8 echoue : " + e.message)
            Config.agregaralogdeerrores("CryptManager : Conversion en UTF-8 echoue : " + e.message)
            _in
        }
        return _out
    }
}