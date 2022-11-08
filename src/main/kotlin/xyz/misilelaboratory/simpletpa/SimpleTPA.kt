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
                then("p" to player()) {
                    executes { context ->
                        val p: Player by context
                        val s = sender as Player

                        if (p.uniqueId == s.uniqueId) {
                            s.sendMessage("자기 자신을 대상으로 지정할 수 없습니다.")
                        } else if (!elistener.hasIt(p, s)) {
                            if (elistener.b[s.uniqueId] == null) {
                                elistener.b[s.uniqueId] = mutableListOf()
                            }
                            elistener.b[s.uniqueId]!!.add(p.uniqueId)
                            p.sendMessage("${s.name}님에게 tpa 요청이 왔습니다. /tpaccept를 사용해 받거나, /tpadeny를 사용해 거절하세요.")
                        } else {
                            p.sendMessage("tpa를 이미 보낸 적이 있습니다.")
                        }
                    }
                }
            }
            register("tpaccept") {
                then("players" to players()) {
                    executes {context ->
                        val players: Collection<Player> by context
                        val s = sender as Player

                        for (i in players) {
                            if (elistener.hasIt(i, s)) {
                                i.teleport(s)
                                elistener.b[s.uniqueId]?.remove(i.uniqueId)
                            }
                        }

                        s.sendMessage("모든 사람을 다 텔레포트시켰습니다.")
                    }
                }
            }
            register("tpadeny") {
                then("players" to players()) {
                    executes { context ->
                        val players: Collection<Player> by context
                        val s = sender as Player

                        for (i in players) {
                            if (elistener.hasIt(i, s)) {
                                elistener.b[s.uniqueId]?.remove(i.uniqueId)
                            }
                        }

                        s.sendMessage("모든 사람을 다 거절했습니다.")
                    }
                }
            }
            register("sethome") {
                then("name" to string()) {
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

                        if (elistener.hasItHome(s, n)) {
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

                        if (elistener.hasItHome(s, n)) {
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