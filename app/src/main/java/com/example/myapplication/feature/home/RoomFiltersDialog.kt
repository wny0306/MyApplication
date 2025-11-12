package com.example.myapplication.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items


@Composable
fun RoomFiltersDialog(
    visible: Boolean,
    initial: Filters,
    onApply: (Filters) -> Unit,
    onDismiss: () -> Unit,
    roundsOptions: List<Int> = listOf(8, 16, 32)
) {
    var temp by remember(visible, initial) { mutableStateOf(initial) }
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { temp = Filters() }) { Text("重設") }
                Button(onClick = { onApply(temp); onDismiss() }) { Text("套用") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        title = { Text("篩選條件", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                SectionTitle("將數")
                MultiSelectRow(
                    items = roundsOptions,
                    selected = temp.rounds,
                    itemLabel = { "${it}將" },
                    onToggle = { v -> temp = temp.copy(rounds = temp.rounds.toMutableSet().apply { if (!add(v)) remove(v) }.toSet()) }
                )
                Spacer(Modifier.height(12.dp))
                SectionTitle("有無花")
                TriChoiceRow(
                    options = listOf("不限" to null, "有" to true, "無" to false),
                    value = temp.hasFlower,
                    onSelect = { v -> temp = temp.copy(hasFlower = v) }
                )
                Spacer(Modifier.height(12.dp))
                SectionTitle("骰規")
                TriChoiceRow(
                    options = listOf("不限" to null, "有" to true, "無" to false),
                    value = temp.diceRule,
                    onSelect = { v -> temp = temp.copy(diceRule = v) }
                )
                Spacer(Modifier.height(12.dp))
                SectionTitle("哩咕")
                TriChoiceRow(
                    options = listOf("不限" to null, "有" to true, "無" to false),
                    value = temp.liGu,
                    onSelect = { v -> temp = temp.copy(liGu = v) }
                )
            }
        }
    )
}

@Composable private fun SectionTitle(text: String) { Text(text, fontWeight = FontWeight.Medium, color = Color.Gray) }

@Composable
private fun <T> MultiSelectRow(
    items: List<T>,
    selected: Set<T>,
    itemLabel: (T) -> String,
    onToggle: (T) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(items, key = { it.hashCode() }) { item ->
            FilterChip(
                selected = selected.contains(item),
                onClick = { onToggle(item) },
                label = { Text(itemLabel(item)) }
            )
        }
    }
}

@Composable
private fun <T> TriChoiceRow(
    options: List<Pair<String, T?>>,
    value: T?,
    onSelect: (T?) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (label, v) ->
            FilterChip(
                selected = value == v,
                onClick = { onSelect(v) },
                label = { Text(label) }
            )
        }
    }
}