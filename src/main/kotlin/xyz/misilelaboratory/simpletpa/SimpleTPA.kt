package xyz.misilelaboratory.simpletpa

import io.github.monun.kommand.getValue
import io.github.monun.kommand.kommand
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class SimpleTPA: JavaPlugin() {

    private val elistener = TPAHandler()

    override fun onEnable() {
        server.logger.info("Enabled")
        kommand {
            register("back") {
                executes {
                    val p = sender as Player
                    if (elistener.a[p.uniqueId] != null) {
                        p.teleport(elistener.a[p.uniqueId]!!)
                    } else {
                        p.sendMessage("/back을 아직 사용할 수 없습니다.")
                    }
                }
            }
            register("tpa") {
                then("receiver" to player()) {
                    executes { context ->
                        val receiver: Player by context
                        val s = sender as Player

                        if (receiver.uniqueId == s.uniqueId) {
                            s.sendMessage("자기 자신을 대상으로 지정할 수 없습니다.")
                        } else if (!elistener.hasIt(receiver, s)) {
                            if (elistener.b[receiver.uniqueId] == null) {
                                elistener.b[receiver.uniqueId] = mutableListOf()
                            }
                            elistener.b[receiver.uniqueId]!!.add(s.uniqueId)
                            receiver.sendMessage("${s.name}님에게 tpa 요청이 왔습니다. /tpaccept ${s.name} 명령어를 사용해 받거나, /tpadeny ${s.name} 명령어를 사용해 거절하세요.")
                        } else {
                            s.sendMessage("tpa를 이미 보낸 적이 있습니다.")
                        }
                    }
                }
            }
            register("tpaccept") {
                then("s" to player()) {
                    executes {context ->
                        val s: Player by context
                        val receiver = sender as Player

                        if (elistener.hasIt(receiver, s)) {
                            receiver.teleport(s)
                            elistener.b[receiver.uniqueId]?.remove(s.uniqueId)
                        }

                        receiver.sendMessage("${s.name}님을 텔레포트시켰습니다. (안되었을 경우 tpa가 오지 않은 것입니다.)")
                    }
                }
            }
            register("tpadeny") {
                then("s" to player()) {
                    executes { context ->
                        val s: Player by context
                        val receiver = sender as Player

                        if (elistener.hasIt(receiver, s)) {
                            elistener.b[receiver.uniqueId]?.remove(s.uniqueId)
                        }

                        receiver.sendMessage("${receiver.name}님의 tpa 요청을 거절했습니다.")
                    }
                }
            }
            register("sethome") {
                then("n" to string()) {
                    executes { context ->
                        val s = sender as Player
                        val n: String by context

                        if (elistener.homes[s.uniqueId] == null) {
                            elistener.homes[s.uniqueId] = mutableMapOf()
                        }
                        elistener.homes[s.uniqueId]!![n] = s.location
                    }
                }
            }
            register("delhome") {
                then("n" to string()) {
                    executes {context ->
                        val s = sender as Player
                        val n: String by context

                        if (!elistener.hasItHome(s, n)) {
                            s.sendMessage("그런 이름의 home은 없습니다.")
                        } else {
                            elistener.homes[s.uniqueId]!!.remove(n)
                        }
                    }
                }
            }
            register("home") {
                then("n" to string()) {
                    executes { context ->
                        val s = sender as Player
                        val n: String by context

                        if (!elistener.hasItHome(s, n)) {
                            s.sendMessage("그 이름의 home은 존재하지 않습니다.")
                        } else {
                            s.teleport(elistener.homes[s.uniqueId]!![name]!!)
                        }
                    }
                }
            }
        }
        server.pluginManager.registerEvents(elistener, this)
    }

    override fun onDisable() {
        server.logger.info("Disabled")
    }
}