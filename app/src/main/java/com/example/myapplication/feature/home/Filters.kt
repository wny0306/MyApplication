package com.example.myapplication.feature.home


data class Filters(
    val rounds: Set<Int> = emptySet(), // 例如 8、16、32；空集合=不限
    val hasFlower: Boolean? = null, // null=不限, true=有, false=無
    val diceRule: Boolean? = null, // null=不限, true=有, false=無
    val liGu: Boolean? = null // null=不限, true=有, false=無
)


internal fun applyFilters(source: List<com.example.myapplication.domain.model.MahjongRoom>, f: Filters): List<com.example.myapplication.domain.model.MahjongRoom> =
    source.filter { room ->
        val roundsOk = f.rounds.isEmpty() || room.rounds in f.rounds
        val flowerOk = f.hasFlower == null || room.flower == f.hasFlower
        val diceOk = f.diceRule == null || room.diceRule == f.diceRule
        val liguOk = f.liGu == null || room.ligu == f.liGu
        roundsOk && flowerOk && diceOk && liguOk
    }