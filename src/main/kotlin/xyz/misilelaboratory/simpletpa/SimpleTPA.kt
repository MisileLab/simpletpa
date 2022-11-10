package xyz.misilelaboratory.simpletpa

import io.github.monun.kommand.getValue
import io.github.monun.kommand.kommand
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

@Serializable
data class DataClass(
    val homes: MutableMap<String, MutableMap<String, LocationData>>?
)

@Serializable
data class LocationData(val world: String, val locations: MutableList<Double>)

@Suppress("unused")
class SimpleTPA: JavaPlugin() {

    private val elistener = TPAHandler()

    override fun onEnable() {
        if (!File("homes.json").exists()) {
            File("homes.json").createNewFile()
        }
        val st = Json.decodeFromString<DataClass>(File("homes.json").readText())
        val m = mutableMapOf<UUID, MutableMap<String, Location>>()

        if (st.homes != null) {
            for (i in st.homes) {
                val m2 = mutableMapOf<String, Location>()
                for (i2 in i.value) {
                    m2[i2.key] = Location(this.server.getWorld(UUID.fromString(i2.value.world)), i2.value.locations[0], i2.value.locations[1], i2.value.locations[2])
                }
                m[UUID.fromString(i.key)] = m2
            }
        }
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
                            s.teleport(receiver)
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
                @Suppress("DuplicatedCode", "DuplicatedCode")
                then("n" to string().apply {
                    suggests {
                        val s = it.source.sender as Player

                        if (elistener.homes[s.uniqueId]?.keys == null) {
                            suggest(mutableListOf(), tooltip=null)
                        } else {
                            suggest(elistener.homes[s.uniqueId]!!.keys, tooltip=null)
                        }
                    }
                }) {
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
                @Suppress("DuplicatedCode")
                then("n" to string().apply {
                    suggests {
                        val s = it.source.sender as Player

                        if (elistener.homes[s.uniqueId]?.keys == null) {
                            suggest(mutableListOf(), tooltip=null)
                        } else {
                            suggest(elistener.homes[s.uniqueId]!!.keys, tooltip=null)
                        }
                    }
                }) {
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
                @Suppress("DuplicatedCode")
                then("n" to string().apply {
                    suggests {
                        val s = it.source.sender as Player

                        if (elistener.homes[s.uniqueId]?.keys == null) {
                            suggest(mutableListOf(), tooltip=null)
                        } else {
                            suggest(elistener.homes[s.uniqueId]!!.keys, tooltip=null)
                        }
                    }
                }) {
                    executes { context ->
                        val s = sender as Player
                        val n: String by context

                        if (!elistener.hasItHome(s, n)) {
                            s.sendMessage("그 이름의 home은 존재하지 않습니다.")
                        } else {
                            s.teleport(elistener.homes[s.uniqueId]!![n]!!)
                        }
                    }
                }
            }
        }
        server.pluginManager.registerEvents(elistener, this)
    }

    override fun onDisable() {
        val ret = mutableMapOf<String, MutableMap<String, LocationData>>()
        for (i in elistener.homes) {
            val ret2 = mutableMapOf<String, LocationData>()
            for (i2 in i.value) {
                ret2[i2.key] = LocationData(i2.value.world.uid.toString(), mutableListOf(i2.value.x, i2.value.y, i2.value.z))
            }
            ret[i.key.toString()] = ret2
        }
        File("homes.json").writeText(Json.encodeToString(DataClass(ret)))
        server.logger.info("Disabled")
    }
}

