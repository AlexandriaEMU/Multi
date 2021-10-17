package common

import common.CryptManager.toUtf
import common.Realm.getCompteByID
import common.SQLManager.getNumberPersosOnThisServer
import java.io.PrintWriter
import java.util.*

object SocketManager {

    fun SEND_ALREADY_CONNECTED(out: PrintWriter?) {
        val packet = StringBuilder()
        packet.append("AlEc")
        send(out, packet.toString())
    }

    fun SEND_Ad_Ac_AH_AlK_AQ_PACKETS(
        out: PrintWriter?,
        pseudo: String?, level: Int, question: String, gmlevel: Int
    ) {
        val packet = StringBuilder()
        packet.append("Ad").append(pseudo).append(0x00.toChar())
        packet.append("Ac0").append(0x00.toChar())
        val list = ArrayList(Realm.GameServers.values)
        var isFirst = true
        for (G in list) {
            if (isFirst) {
                packet.append("AH").append(G.iD).append(";")
                if (G.blockLevel > gmlevel) {
                    packet.append("0")
                } else {
                    packet.append(G.state)
                }
                packet.append(";110;1")
            } else {
                packet.append("|").append(G.iD).append(";")
                if (G.blockLevel > gmlevel) {
                    packet.append("0")
                } else {
                    packet.append(G.state)
                }
                packet.append(";110;1")
            }
            isFirst = false
        }
        packet.append(0x00.toChar())
        packet.append("AlK").append(level).append(0x00.toChar())
        packet.append("AQ").append(question.replace(" ", "+"))
        send(out, packet.toString())
    }

    fun SEND_Af_PACKET(
        out: PrintWriter?, position: Int,
        totalAbo: Int, totalNonAbo: Int, subscribe: Int, queueID: Int
    ) {
        val packet = StringBuilder()
        packet.append("Af").append(position).append("|").append(totalAbo).append("|")
        packet.append(totalNonAbo).append("|").append(subscribe).append("|").append(queueID)
        send(out, packet.toString())
    }

    fun SEND_BANNED(out: PrintWriter?) {
        val packet = StringBuilder()
        packet.append("AlEb")
        send(out, packet.toString())
    }

    fun SEND_GAME_SERVER_IP(out: PrintWriter?, guid: Int, server: Int) {
        val packet = StringBuilder()
        packet.append("A")
        val G = Realm.GameServers[server] ?: return
        val acc = getCompteByID(guid)
        val str = (acc!!.get_GUID().toString() + "|" + acc.get_name() + "|" + acc.get_pass() + "|"
                + acc.get_pseudo() + "|" + acc.get_question() + "|" + acc.get_reponse() + "|"
                + acc.get_gmLvl() + "|" + acc.get_subscriber() + "|" + (if (acc.isBanned) 1 else 0) + "|"
                + acc.lastIP + "|" + acc.lastConnectionDate + "|" + acc.get_curIP() + "|" + acc.get_giftID())
        G.thread!!.sendAddWaiting(str)
        packet.append("YK").append(G.iP).append(":").append(G.port).append(";").append(guid)
        send(out, packet.toString())
    }

    fun SEND_HC_PACKET(out: PrintWriter?): String {
        val alphabet = "abcdefghijklmnopqrstuvwxyz"
        val rand = Random()
        val hashkey = StringBuilder()
        val packet = StringBuilder()
        for (i in 0..31) {
            hashkey.append(alphabet[rand.nextInt(alphabet.length)])
        }
        packet.append("HC").append(hashkey)
        send(out, packet.toString())
        return hashkey.toString()
    }

    fun SEND_LOGIN_ERROR(out: PrintWriter?) {
        val packet = StringBuilder()
        packet.append("AlEf")
        send(out, packet.toString())
    }

    fun SEND_PERSO_LIST(out: PrintWriter?, subscriber: Int, number: Int) {
        val packet = StringBuilder()
        packet.append("AxK").append(subscriber * 60).append("000") //Conversion en millisecondes
        val list = ArrayList(Realm.GameServers.values)
        for (G in list) {
            packet.append("|").append(G.iD).append(",").append(getNumberPersosOnThisServer(number, G.iD))
        }
        send(out, packet.toString())
    }

    fun SEND_POLICY_FILE(out: PrintWriter?) {
        val packet = StringBuilder()
        packet.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        packet.append("<cross-domain-policy>")
        packet.append("<allow-access-from domain=\"*\" to-ports=\"*\" secure=\"false\" />")
        packet.append("<site-control permitted-cross-domain-policies=\"master-only\" />")
        packet.append("</cross-domain-policy>")
        send(out, packet.toString())
    }

    fun SEND_REQUIRED_VERSION(out: PrintWriter?) {
        val packet = StringBuilder()
        packet.append("AlEv").append(Config.CLIENT_VERSION)
        send(out, packet.toString())
    }

    fun SEND_TOO_MANY_PLAYER_ERROR(out: PrintWriter?) {
        val packet = StringBuilder()
        packet.append("AlEw")
        send(out, packet.toString())
    }

    fun refresh(out: PrintWriter?) {
        val list = ArrayList(Realm.GameServers.values)
        val packet = StringBuilder()
        var isFirst = true
        for (G in list) {
            if (isFirst) packet.append("AH").append(G.iD).append(";").append(G.state)
                .append(";110;1") else packet.append("|").append(G.iD).append(";").append(G.state).append(";110;1")
            isFirst = false
        }
        send(out, packet.toString())
    }

    private fun send(out: PrintWriter?, packet: String) {
        var packet = packet
        if (out != null && packet != "" && packet != "" + 0x00.toChar()) {
            packet = toUtf(packet)
            out.print(packet + 0x00.toChar())
            out.flush()
            if (Config.REALM_DEBUG) {
                Config.agregaralogdemulti("REALM: Envia>>$packet")
                println("REALM: Envia>>$packet")
            }
        }
    }
}