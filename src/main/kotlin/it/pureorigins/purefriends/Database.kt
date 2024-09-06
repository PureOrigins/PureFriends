package it.pureorigins.purefriends


import it.pureorigins.common.Text
import it.pureorigins.common.textFromJson
import it.pureorigins.common.toJson
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.time.temporal.TemporalAmount
import java.util.*

object FriendsTable : Table("friends") {
    val playerUniqueId = uuid("player_uuid")
    val friendUniqueId = uuid("friend_uuid")
    
    override val primaryKey = PrimaryKey(playerUniqueId, friendUniqueId)
    
    fun add(playerUniqueId: UUID, friendUniqueId: UUID) = insertIgnore {
        it[this.playerUniqueId] = playerUniqueId
        it[this.friendUniqueId] = friendUniqueId
    }.insertedCount > 0
    
    fun has(playerUniqueId: UUID, friendUniqueId: UUID) = select(this.playerUniqueId, this.friendUniqueId).where {
        ((FriendsTable.playerUniqueId eq playerUniqueId) and (FriendsTable.friendUniqueId eq friendUniqueId)) or
                ((FriendsTable.playerUniqueId eq friendUniqueId) and (FriendsTable.friendUniqueId eq playerUniqueId))
    }.count() > 0
    
   fun remove(playerUniqueId: UUID, friendUniqueId: UUID) = deleteWhere {
        ((FriendsTable.playerUniqueId eq playerUniqueId) and (FriendsTable.friendUniqueId eq friendUniqueId)) or
                ((FriendsTable.playerUniqueId eq friendUniqueId) and (FriendsTable.friendUniqueId eq playerUniqueId))
    } > 0
    
    fun get(playerUniqueId: UUID): Set<UUID> = select(this.playerUniqueId).where {
        (FriendsTable.playerUniqueId eq playerUniqueId) or (FriendsTable.friendUniqueId eq playerUniqueId)
    }.mapTo(hashSetOf()) { if (it[this.playerUniqueId] == playerUniqueId) it[this.friendUniqueId] else it[this.playerUniqueId] }
}

object FriendRequestsTable : Table("friend_requests") {
    val playerUniqueId = uuid("player_uuid")
    val friendUniqueId = uuid("friend_uuid")
    
    override val primaryKey = PrimaryKey(playerUniqueId, friendUniqueId)
    
    fun add(playerUniqueId: UUID, friendUniqueId: UUID) = insertIgnore {
        it[this.playerUniqueId] = playerUniqueId
        it[this.friendUniqueId] = friendUniqueId
    }.insertedCount > 0
    
    fun has(playerUniqueId: UUID, friendUniqueId: UUID) =
        select(this.playerUniqueId, FriendRequestsTable.friendUniqueId).where { (FriendRequestsTable.playerUniqueId eq playerUniqueId) and (FriendRequestsTable.friendUniqueId eq friendUniqueId) }.count() > 0
    
    fun remove(playerUniqueId: UUID, friendUniqueId: UUID) = deleteWhere {
        (FriendRequestsTable.playerUniqueId eq playerUniqueId) and (FriendRequestsTable.friendUniqueId eq friendUniqueId)
    } > 0
    
    fun get(playerUniqueId: UUID): Set<UUID> =
        select(this.playerUniqueId).where { FriendRequestsTable.playerUniqueId eq playerUniqueId }.mapTo(hashSetOf()) { it[this.friendUniqueId] }
    
    fun inverseGet(playerUniqueId: UUID): Set<UUID> =
        select(this.playerUniqueId).where { FriendRequestsTable.friendUniqueId eq playerUniqueId }.mapTo(hashSetOf()) { it[this.playerUniqueId] }
}

object NewsTable : LongIdTable("friend_notifications") {
    val playerUniqueId = uuid("player_uuid")
    val text = text("text")
    val date = long("date")
    val expirationDate = long("expiration_date").nullable()
    
    fun add(playerUniqueId: UUID, text: Text, expirationTime: TemporalAmount? = null) = insertIgnore {
        it[NewsTable.playerUniqueId] = playerUniqueId
        it[NewsTable.text] = text.toJson()
        val now = Instant.now()
        it[date] = now.toEpochMilli()
        it[expirationDate] = now?.plus(expirationTime)?.toEpochMilli()
    }
    
    fun get(playerUniqueId: UUID) =
        select(this.playerUniqueId).where { (NewsTable.playerUniqueId eq playerUniqueId) and (expirationDate greater Instant.now().toEpochMilli()) }.orderBy(date).map { textFromJson(it[text]) }
    
    fun remove(playerUniqueId: UUID) = deleteWhere { NewsTable.playerUniqueId eq playerUniqueId }
    
    fun getAndRemove(playerUniqueId: UUID) = get(playerUniqueId).also { remove(playerUniqueId) }
}

object BlockedPlayersTable : Table("blocked_players") {
    val playerUniqueId = uuid("player_uuid")
    val blockedUniqueId = uuid("blocked_uuid")
    
    override val primaryKey = PrimaryKey(playerUniqueId, blockedUniqueId)
    
    fun add(playerUniqueId: UUID, blockedUniqueId: UUID) = insertIgnore {
        it[this.playerUniqueId] = playerUniqueId
        it[this.blockedUniqueId] = blockedUniqueId
    }.insertedCount > 0
    
    fun has(playerUniqueId: UUID, blockedUniqueId: UUID) =
        select(this.playerUniqueId, this.blockedUniqueId).where { (BlockedPlayersTable.playerUniqueId eq playerUniqueId) and (BlockedPlayersTable.blockedUniqueId eq blockedUniqueId) }.count() > 0
    
    fun remove(playerUniqueId: UUID, blockedUniqueId: UUID) = deleteWhere {
        (BlockedPlayersTable.playerUniqueId eq playerUniqueId) and (BlockedPlayersTable.blockedUniqueId eq blockedUniqueId)
    } > 0
    
    fun get(playerUniqueId: UUID): Set<UUID> =
        select(this.playerUniqueId).where { BlockedPlayersTable.playerUniqueId eq playerUniqueId }.mapTo(hashSetOf()) { it[this.blockedUniqueId] }
    
    fun inverseGet(playerUniqueId: UUID): Set<UUID> =
        select(this.playerUniqueId).where { BlockedPlayersTable.blockedUniqueId eq playerUniqueId }.mapTo(hashSetOf()) { it[this.playerUniqueId] }
    
}