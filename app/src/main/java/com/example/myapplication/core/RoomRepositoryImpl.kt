package com.example.myapplication.core

import android.content.Context
import android.util.Log
import com.example.myapplication.data.datasource.local.UserPreferences
import com.example.myapplication.data.repository.RoomRepository
import com.example.myapplication.domain.model.MahjongRoom
import com.example.myapplication.domain.model.Member
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

class RoomRepositoryImpl(
    private val context: Context,
    private val client: HttpClient = HttpClient(OkHttp) {
        expectSuccess = false
    }
) : RoomRepository {

    private val baseUrl = "http://59.127.30.235:85/api"

    /** ----------------------------------------------------------
     * 房間清單（用於首頁列表）
     * ---------------------------------------------------------- */
    override fun roomsFlow(): Flow<List<MahjongRoom>> = flow {
        emit(getRooms())
    }

    override suspend fun getRooms(): List<MahjongRoom> = withContext(Dispatchers.IO) {
        try {
            val res: HttpResponse = client.get("$baseUrl/get_rooms.php") {
                // GET 不需要 contentType，但保留不影響
                contentType(ContentType.Application.Json)
            }
            val text = res.bodyAsText()
            Log.d("RoomRepo", "get_rooms.php -> $text")

            val root = Json.parseToJsonElement(text).jsonObject
            if (root["success"]?.jsonPrimitive?.booleanOrNull != true) return@withContext emptyList()

            val arr = root["rooms"]?.jsonArray ?: return@withContext emptyList()
            arr.map { el ->
                val o = el.jsonObject
                MahjongRoom(
                    id = o["id"]?.jsonPrimitive?.intOrNull ?: 0,
                    ownerId = o["owner_id"]?.jsonPrimitive?.intOrNull ?: 0,
                    ownerName = o["owner_name"]?.jsonPrimitive?.content,
                    avatarUrl = o["avatar_url"]?.jsonPrimitive?.content,
                    people = o["people"]?.jsonPrimitive?.intOrNull ?: 4,
                    flower = (o["flower"]?.jsonPrimitive?.intOrNull ?: 0) == 1,
                    date = o["date"]?.jsonPrimitive?.content ?: "",
                    time = o["time"]?.jsonPrimitive?.content ?: "",
                    city = o["city"]?.jsonPrimitive?.content ?: "",
                    location = o["location"]?.jsonPrimitive?.content ?: "",
                    rounds = o["rounds"]?.jsonPrimitive?.intOrNull ?: 4,
                    diceRule = (o["dice_rule"]?.jsonPrimitive?.intOrNull ?: 0) == 1,
                    ligu = (o["ligu"]?.jsonPrimitive?.intOrNull ?: 0) == 1,
                    basePoint = o["base_point"]?.jsonPrimitive?.intOrNull ?: 30,
                    taiPoint = o["tai_point"]?.jsonPrimitive?.intOrNull ?: 10,
                    note = o["note"]?.jsonPrimitive?.content ?: "",
                    createdAt = o["created_at"]?.jsonPrimitive?.content,
                    updatedAt = o["updated_at"]?.jsonPrimitive?.content,
                    members = emptyList(),
                    memberCount = o["member_count"]?.jsonPrimitive?.intOrNull ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e("RoomRepo", "getRooms error: ${e.message}", e)
            emptyList()
        }
    }

    /** ----------------------------------------------------------
     * 取得單一房間（含成員）
     * ---------------------------------------------------------- */
    override suspend fun getRoom(roomId: Int): MahjongRoom? = withContext(Dispatchers.IO) {
        try {
            val res: HttpResponse = client.get("$baseUrl/get_room_detail.php") {
                parameter("room_id", roomId)
                parameter("_ts", System.currentTimeMillis())
            }
            val text = res.bodyAsText()
            Log.d("RoomRepo", "get_room_detail.php -> $text")

            val root = Json.parseToJsonElement(text).jsonObject
            if (root["success"]?.jsonPrimitive?.booleanOrNull != true) return@withContext null

            val o = root["room"]?.jsonObject ?: return@withContext null

            val membersArray = o["members"]?.jsonArray ?: JsonArray(emptyList())
            val members = membersArray.map { m ->
                val u = m.jsonObject
                Member(
                    id = u["id"]?.jsonPrimitive?.intOrNull ?: 0,
                    name = u["name"]?.jsonPrimitive?.content ?: "未知玩家",
                    intro = u["intro"]?.jsonPrimitive?.content ?: ""
                )
            }

            MahjongRoom(
                id = o["id"]?.jsonPrimitive?.intOrNull ?: 0,
                ownerId = o["owner_id"]?.jsonPrimitive?.intOrNull ?: 0,
                ownerName = o["owner_name"]?.jsonPrimitive?.content,
                people = o["people"]?.jsonPrimitive?.intOrNull ?: 4,
                flower = (o["flower"]?.jsonPrimitive?.intOrNull ?: 0) == 1,
                date = o["date"]?.jsonPrimitive?.content ?: "",
                time = o["time"]?.jsonPrimitive?.content ?: "",
                city = o["city"]?.jsonPrimitive?.content ?: "",
                location = o["location"]?.jsonPrimitive?.content ?: "",
                rounds = o["rounds"]?.jsonPrimitive?.intOrNull ?: 4,
                diceRule = (o["dice_rule"]?.jsonPrimitive?.intOrNull ?: 0) == 1,
                ligu = (o["ligu"]?.jsonPrimitive?.intOrNull ?: 0) == 1,
                basePoint = o["base_point"]?.jsonPrimitive?.intOrNull ?: 30,
                taiPoint = o["tai_point"]?.jsonPrimitive?.intOrNull ?: 10,
                note = o["note"]?.jsonPrimitive?.content,
                createdAt = o["created_at"]?.jsonPrimitive?.content,
                updatedAt = o["updated_at"]?.jsonPrimitive?.content,
                members = members
            )
        } catch (e: Exception) {
            Log.e("RoomRepo", "getRoom error: ${e.message}", e)
            null
        }
    }

    /** ----------------------------------------------------------
     * 房間成員（單獨呼叫時用）
     * ---------------------------------------------------------- */
    override suspend fun getRoomMembers(roomId: Int): List<Member> = withContext(Dispatchers.IO) {
        try {
            val res: HttpResponse = client.get("$baseUrl/get_members.php") {
                parameter("room_id", roomId)
                parameter("_ts", System.currentTimeMillis())
            }
            val text = res.bodyAsText()
            Log.d("RoomRepo", "get_members.php -> $text")

            val root = Json.parseToJsonElement(text).jsonObject
            if (root["success"]?.jsonPrimitive?.booleanOrNull != true) return@withContext emptyList()

            val arr = root["members"]?.jsonArray ?: return@withContext emptyList()
            arr.map { m ->
                val o = m.jsonObject
                Member(
                    id = o["id"]?.jsonPrimitive?.intOrNull ?: 0,
                    name = o["name"]?.jsonPrimitive?.content ?: "未知玩家",
                    intro = o["intro"]?.jsonPrimitive?.content ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("RoomRepo", "getRoomMembers error: ${e.message}", e)
            emptyList()
        }
    }

    /** ----------------------------------------------------------
     * 其他 CRUD
     * ---------------------------------------------------------- */
    override suspend fun createRoom(room: MahjongRoom): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildJsonObject {
                put("owner_id", room.ownerId)
                put("people", room.people)
                put("flower", if (room.flower) 1 else 0)

                // 空字串→null（後端會接受）
                if (room.date.isNotBlank()) put("date", room.date) else put("date", JsonNull)
                if (room.time.isNotBlank()) put("time", room.time) else put("time", JsonNull)
                if (room.city.isNotBlank()) put("city", room.city) else put("city", JsonNull)
                if (room.location.isNotBlank()) put("location", room.location) else put("location", JsonNull)

                put("rounds", room.rounds)
                put("dice_rule", if (room.diceRule) 1 else 0)
                put("ligu", if (room.ligu) 1 else 0)
                put("base_point", room.basePoint)
                put("tai_point", room.taiPoint)
                put("note", room.note ?: "")
            }

            Log.d("RoomRepo", "create_room payload = $payload")
            val res: HttpResponse = client.post("$baseUrl/create_room.php") {
                contentType(ContentType.Application.Json)
                setBody(payload.toString()) // 純字串 JSON
            }

            val text = res.bodyAsText()
            Log.d("RoomRepo", "create_room.php -> $text")
            val root = Json.parseToJsonElement(text).jsonObject
            root["success"]?.jsonPrimitive?.booleanOrNull == true
        } catch (e: Exception) {
            Log.e("RoomRepo", "createRoom error: ${e.message}", e)
            false
        }
    }

    override suspend fun deleteRoom(roomId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildJsonObject { put("room_id", roomId) }
            val res: HttpResponse = client.post("$baseUrl/delete_room.php") {
                contentType(ContentType.Application.Json)
                setBody(payload.toString()) // 純字串 JSON
            }
            val text = res.bodyAsText()
            Log.d("RoomRepo", "delete_room.php -> $text")
            val json = Json.parseToJsonElement(text).jsonObject
            json["success"]?.jsonPrimitive?.booleanOrNull ?: false
        } catch (e: Exception) {
            Log.e("RoomRepo", "deleteRoom error: ${e.message}", e)
            false
        }
    }

    override suspend fun leaveRoom(roomId: Int, userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildJsonObject {
                put("room_id", roomId)
                put("user_id", userId)
            }
            val res: HttpResponse = client.post("$baseUrl/leave_room.php") {
                contentType(ContentType.Application.Json)
                setBody(payload.toString()) // 純字串 JSON
            }
            val text = res.bodyAsText()
            Log.d("RoomRepo", "leave_room.php -> $text")
            val json = Json.parseToJsonElement(text).jsonObject
            json["success"]?.jsonPrimitive?.booleanOrNull ?: false
        } catch (e: Exception) {
            Log.e("RoomRepo", "leaveRoom error: ${e.message}", e)
            false
        }
    }

    override suspend fun isJoined(roomId: Int, userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildJsonObject {
                put("room_id", roomId)
                put("user_id", userId)
            }
            // ⬇⬇ 這邊要改成 is_joined.php
            val res: HttpResponse = client.post("$baseUrl/is_joined.php") {
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            val text = res.bodyAsText()
            Log.d("RoomRepo", "is_joined.php -> $text")

            val json = Json.parseToJsonElement(text).jsonObject
            json["success"]?.jsonPrimitive?.booleanOrNull == true &&
                    json["is_joined"]?.jsonPrimitive?.booleanOrNull == true
        } catch (e: Exception) {
            Log.e("RoomRepo", "isJoined error: ${e.message}", e)
            false
        }
    }


    override suspend fun joinRoom(roomId: Int, userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildJsonObject {
                put("room_id", roomId)
                put("user_id", userId)
            }
            val res: HttpResponse = client.post("$baseUrl/join_room.php") {
                contentType(ContentType.Application.Json)
                setBody(payload.toString()) // 純字串 JSON
            }
            val text = res.bodyAsText()
            Log.d("RoomRepo", "join_room.php -> $text")
            val json = Json.parseToJsonElement(text).jsonObject
            json["success"]?.jsonPrimitive?.booleanOrNull ?: false
        } catch (e: Exception) {
            Log.e("RoomRepo", "joinRoom error: ${e.message}", e)
            false
        }
    }

    override fun currentUserId(): Int? = try {
        UserPreferences(context).getUserSync()?.id
    } catch (e: Exception) {
        Log.e("RoomRepo", "currentUserId error: ${e.message}", e)
        null
    }
}
