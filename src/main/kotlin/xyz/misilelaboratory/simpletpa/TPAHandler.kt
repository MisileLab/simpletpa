package xyz.misilelaboratory.simpletpa

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.UUID

@Suppress("unused")
class TPAHandler: Listener {

    val a = mutableMapOf<UUID, Location>()
    val b = mutableMapOf<UUID, MutableList<UUID>>()

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        a[e.player.uniqueId] = e.player.location
    }

    fun hasIt(p: Player, s: Player): Boolean {
        return b[s.uniqueId]?.contains(p.uniqueId) == true
    }

}