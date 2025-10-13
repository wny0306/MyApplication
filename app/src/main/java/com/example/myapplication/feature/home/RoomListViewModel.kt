package com.example.myapplication.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.RoomRepository
import com.example.myapplication.domain.model.MahjongRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.example.myapplication.core.DefaultRoomRepository

class RoomListViewModel(
    internal val repo: RoomRepository = DefaultRoomRepository()
) : ViewModel() {

    private val selectedCity = MutableStateFlow("全台")

    val allRooms: StateFlow<List<MahjongRoom>> =
        repo.roomsFlow().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val rooms: StateFlow<List<MahjongRoom>> =
        combine(allRooms, selectedCity) { list, city ->
            if (city == "全台") list else list.filter { it.location == city }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun onCitySelected(city: String) { selectedCity.value = city }

    suspend fun create(room: MahjongRoom) { repo.createRoom(room) }
}
