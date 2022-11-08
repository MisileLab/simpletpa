package xyz.misilelaboratory.simpletpa

import io.github.monun.kommand.getValue
import io.github.monun.kommand.kommand
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class SimpleTPA: JavaPlugin() {
    override fun onEnable() {
        server.logger.info("Enabled")
        val elistener = TPAHandler()
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
                then("player" to player()) {
                    executes { context ->
                        val p: Player by context
                        val s = sender as Player

                        if (p.uniqueId == s.uniqueId) {
                            s.sendMessage("자기 자신을 대상으로 지정할 수 없습니다.")
                        } else if (!elistener.hasIt(p, s)) {
                            p.sendMessage("${s.name}님에게 tpa 요청이 왔습니다. /tpaccept를 사용해 받거나, /tpadeny를 사용해 거절하세요.")
                        } else {
                            p.sendMessage("tpa를 이미 보낸 적이 있습니다.")
                        }
                    }
                }
            }
            register("tpaccept") {
                then("player" to players()) {
                    executes {context ->
                        val player: List<Player> by context
                        val s = sender as Player

                        for (i in player) {
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
                then("player" to players()) {
                    executes { context ->
                        val player: List<Player> by context
                        val s = sender as Player

                        for (i in player) {
                            if (elistener.hasIt(i, s)) {
                                elistener.b[s.uniqueId]?.remove(i.uniqueId)
                            }
                        }

                        s.sendMessage("모든 사람을 다 거절했습니다.")
                    }
                }
            }
        }
    }

    override fun onDisable() {
        server.logger.info("Disabled")
    }
}