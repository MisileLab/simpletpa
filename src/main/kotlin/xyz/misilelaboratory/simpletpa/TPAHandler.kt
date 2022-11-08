@file:Suppress("unused")

package xyz.misilelaboratory.simpletpa

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.UUID

class TPAHandler: Listener {

    var a = mutableMapOf<UUID, Location>()
    var b = mutableMapOf<UUID, MutableList<UUID>>()
    var homes = mutableMapOf<UUID, MutableMap<String, Location>>()

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        a[e.player.uniqueId] = e.player.location
    }

    fun hasIt(receiver: Player, sender: Player): Boolean {
        return b[receiver.uniqueId]?.contains(sender.uniqueId) == true
    }

    fun hasItHome(p: Player, name: String): Boolean {
        return homes[p.uniqueId]?.get(name) != null
    }

}