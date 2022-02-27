package it.pureorigins.purefriends

import it.pureorigins.common.file
import it.pureorigins.common.json
import it.pureorigins.common.readFileAs
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.serialization.Serializable
import java.util.*

class PureFriends : JavaPlugin() {
    fun isBlocked(blockedUniqueId: UUID, blockerUniqueId: UUID)= transaction(database) { BlockedPlayersTable.has(blockerUniqueId, blockedUniqueId) }
    fun isBlocked(blocked: OfflinePlayer, blocker: OfflinePlayer) = isBlocked(blocked.uniqueId, blocker.uniqueId)
    
    fun isFriend(playerUniqueId: UUID, otherUniqueId: UUID) = transaction(database) { FriendsTable.has(otherUniqueId, playerUniqueId) }
    fun isFriend(player: OfflinePlayer, other: OfflinePlayer) = isFriend(player.uniqueId, other.uniqueId)
    
    fun getFriends(playerUniqueId: UUID) = transaction(database) { FriendsTable.get(playerUniqueId) }
    fun getFriends(player: OfflinePlayer) = getFriends(player.uniqueId)
    
    fun getBlockedPlayers(playerUniqueId: UUID) = transaction(database) { BlockedPlayersTable.get(playerUniqueId) }
    fun getBlockedPlayers(player: OfflinePlayer) = getBlockedPlayers(player.uniqueId)
    
    fun getWhoBlockedPlayer(playerUniqueId: UUID) = transaction(database) { BlockedPlayersTable.inverseGet(playerUniqueId) }
    fun getWhoBlockedPlayer(player: OfflinePlayer) = getWhoBlockedPlayer(player.uniqueId)
    
    fun getFriendRequests(playerUniqueId: UUID) = transaction(database) { FriendRequestsTable.get(playerUniqueId) }
    fun getFriendRequests(player: OfflinePlayer) = getFriendRequests(player.uniqueId)
    
    fun getWhoFriendRequest(playerUniqueId: UUID) = transaction(database) { FriendRequestsTable.inverseGet(playerUniqueId) }
    fun getWhoFriendRequest(player: OfflinePlayer) = getWhoFriendRequest(player.uniqueId)
    
    lateinit var database: Database
    
    override fun onEnable() {
        val (db) = json.readFileAs(file("friends.json"), Config())
        require(db.url.isNotEmpty()) { "database url should not be empty" }
        database = Database.connect(db.url, user = db.username, password = db.password)
        transaction(database) {
            createMissingTablesAndColumns(FriendsTable, FriendRequestsTable, NewsTable, BlockedPlayersTable)
        }
    }
    
    @Serializable
    data class Config(
        val database: Database = Database()
    ) {
        @Serializable
        data class Database(
            val url: String = "",
            val username: String = "",
            val password: String = ""
        )
    }
}
