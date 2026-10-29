package ru.nekury.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val gameGreen = Color(0xFF43F0A3)
private val gamePanel = Color(0xFF0D2119)
private val gamePanel2 = Color(0xFF102A20)
private val gameMuted = Color(0xFFAAC0B5)

@Composable
fun MiniGamesSection() {
    var selected by remember { mutableStateOf<String?>(null) }
    var bestScores by remember { mutableStateOf(mapOf<String, Int>()) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (selected == null) {
            Text("Игровая пауза 🎮", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text("Выбери игру на пару минут — пережди волну тяги. Все игры работают офлайн.", color = gameMuted, fontSize = 13.sp)
            val games = listOf(
                Triple("tower", "🏗️ Башня", "Лови момент и строй башню выше"),
                Triple("race", "🏎️ Мини-гонки", "Уворачивайся от препятствий"),
                Triple("jump", "🪂 Прыгун", "Прыгай по платформам и набирай очки"),
                Triple("reflex", "⚡ Молниеносная реакция", "Проверь скорость пальцев"),
                Triple("memory", "🧠 Тренировка памяти", "Повтори последовательность цветов"),
                Triple("math", "🔢 Быстрый счёт", "Решай примеры на скорость")
            )
            games.forEach { game ->
                Card(modifier = Modifier.fillMaxWidth().clickable { selected = game.first },
                    colors = CardDefaults.cardColors(containerColor = gamePanel), shape = RoundedCornerShape(20.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(game.second, color = gameGreen, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(game.third, color = gameMuted, fontSize = 13.sp)
                            bestScores[game.first]?.let { Text("Рекорд: " + it, color = Color.White, fontSize = 12.sp) }
                        }
                        Text("Играть  ›", color = gameGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            OutlinedButton(onClick = { selected = null }, modifier = Modifier.fillMaxWidth()) { Text("← Все игры") }
            when (selected) {
                "tower" -> TowerGame { s -> bestScores = bestScores + ("tower" to maxOf(s, bestScores["tower"] ?: 0)) }
                "race" -> RaceGame { s -> bestScores = bestScores + ("race" to maxOf(s, bestScores["race"] ?: 0)) }
                "jump" -> JumpGame { s -> bestScores = bestScores + ("jump" to maxOf(s, bestScores["jump"] ?: 0)) }
                "reflex" -> ReflexGame { s -> bestScores = bestScores + ("reflex" to maxOf(s, bestScores["reflex"] ?: 0)) }
                "memory" -> MemoryGame { s -> bestScores = bestScores + ("memory" to maxOf(s, bestScores["memory"] ?: 0)) }
                "math" -> MathGame { s -> bestScores = bestScores + ("math" to maxOf(s, bestScores["math"] ?: 0)) }
            }
        }
    }
}

@Composable
private fun GameCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = gamePanel), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = {
            Text(title, color = gameGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = gameMuted, fontSize = 13.sp)
            content()
        })
    }
}

@Composable
private fun GameButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier, colors = ButtonDefaults.buttonColors(containerColor = gameGreen)) {
        Text(label, color = Color(0xFF06110D), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TowerGame(onScore: (Int) -> Unit) {
    var height by remember { mutableIntStateOf(0) }
    var ended by remember { mutableStateOf(false) }
    GameCard("🏗️ Башня", "Нажимай, чтобы поставить блок. Чем выше башня, тем сложнее!") {
        Text("▰".repeat(height.coerceAtMost(12) + 1), fontSize = 28.sp, color = gameGreen)
        Text("Высота: " + height, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        if (ended) Text("Башня закончилась! Результат: " + height, color = gameMuted)
        GameButton(if (ended) "Построить заново" else "Поставить блок", {
            if (ended) { height = 0; ended = false }
            else if (Random.nextInt(100) < (10 + height * 5).coerceAtMost(85)) { ended = true; onScore(height) }
            else height++
        }, Modifier.fillMaxWidth())
    }
}

@Composable
private fun RaceGame(onScore: (Int) -> Unit) {
    var lane by remember { mutableIntStateOf(1) }
    var obstacle by remember { mutableIntStateOf(Random.nextInt(3)) }
    var score by remember { mutableIntStateOf(0) }
    var crashed by remember { mutableStateOf(false) }
    GameCard("🏎️ Мини-гонки", "Перестраивайся между тремя полосами и избегай препятствий.") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (0..2).forEach { i ->
                Surface(modifier = Modifier.weight(1f).height(110.dp), color = if (lane == i) gamePanel2 else Color(0xFF17221D), shape = RoundedCornerShape(12.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text(if (obstacle == i && !crashed) "🚧" else if (lane == i) "🏎️" else "·", fontSize = 30.sp) }
                }
            }
        }
        Text("Очки: " + score, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        if (crashed) Text("Столкновение! Попробуй ещё раз.", color = gameMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GameButton("←", { if (crashed) { score = 0; crashed = false; lane = 1; obstacle = Random.nextInt(3) } else lane = (lane - 1).coerceAtLeast(0) }, Modifier.weight(1f))
            GameButton(if (crashed) "Заново" else "Вперёд", {
                if (crashed) { score = 0; crashed = false; lane = 1; obstacle = Random.nextInt(3) }
                else if (lane == obstacle) { crashed = true; onScore(score) }
                else { score++; obstacle = Random.nextInt(3) }
            }, Modifier.weight(2f))
            GameButton("→", { if (!crashed) lane = (lane + 1).coerceAtMost(2) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun JumpGame(onScore: (Int) -> Unit) {
    var score by remember { mutableIntStateOf(0) }
    var obstacleX by remember { mutableFloatStateOf(1.0f) }
    var jumpTicks by remember { mutableIntStateOf(0) }
    var ended by remember { mutableStateOf(false) }

    LaunchedEffect(ended) {
        while (!ended) {
            delay(70)
            obstacleX -= 0.035f
            if (jumpTicks > 0) jumpTicks--
            if (obstacleX <= 0.12f) {
                if (jumpTicks == 0) {
                    ended = true
                    onScore(score)
                } else {
                    score++
                    obstacleX = 1.0f
                }
            }
        }
    }

    GameCard("🪂 Прыгун", "Нажимай на игровое поле, чтобы перепрыгивать препятствия. Не дай герою столкнуться!") {
        Canvas(
            modifier = Modifier.fillMaxWidth().height(220.dp)
                .clickable { if (!ended) jumpTicks = 7 }
        ) {
            val w = size.width
            val h = size.height
            drawRoundRect(color = Color(0xFF183B2D), size = Size(w, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(22f, 22f))
            // sky and distant hills
            drawCircle(color = Color(0xFF24533D), radius = w * 0.22f, center = Offset(w * 0.82f, h * 0.22f))
            drawCircle(color = Color(0xFF1D4935), radius = w * 0.28f, center = Offset(w * 0.12f, h * 0.84f))
            val groundY = h * 0.82f
            drawLine(color = gameGreen, start = Offset(0f, groundY), end = Offset(w, groundY), strokeWidth = 5f)
            val playerX = w * 0.20f
            val playerY = if (jumpTicks > 0) h * 0.36f else h * 0.65f
            // little astronaut hero
            drawCircle(color = Color(0xFFE7F4EE), radius = 23f, center = Offset(playerX, playerY))
            drawCircle(color = Color(0xFF73BDF2), radius = 15f, center = Offset(playerX, playerY))
            drawRoundRect(color = Color(0xFFE7F4EE), topLeft = Offset(playerX - 18f, playerY + 17f), size = Size(36f, 30f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
            val ox = w * obstacleX
            drawRoundRect(color = Color(0xFFFFB547), topLeft = Offset(ox - 18f, groundY - 42f), size = Size(36f, 42f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
            drawRect(color = Color(0xFF9B5B24), topLeft = Offset(ox - 18f, groundY - 16f), size = Size(36f, 7f))
        }
        Text("Очки: $score", color = gameGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(if (ended) "Столкновение! Попробуй ещё раз." else "Коснись игрового поля для прыжка", color = Color.White)
        GameButton(if (ended) "Играть снова" else "Прыжок!", {
            if (ended) { score = 0; obstacleX = 1.0f; jumpTicks = 0; ended = false }
            else jumpTicks = 7
        }, Modifier.fillMaxWidth())
    }
}

@Composable
private fun ReflexGame(onScore: (Int) -> Unit) {
    var phase by remember { mutableStateOf("ready") }
    var startedAt by remember { mutableLongStateOf(0L) }
    var result by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(0) }
    LaunchedEffect(phase, round) {
        if (phase == "wait") { delay(Random.nextLong(1300, 3500)); phase = "go"; startedAt = System.currentTimeMillis() }
    }
    GameCard("⚡ Молниеносная реакция", "Дождись зелёного сигнала и нажми как можно быстрее.") {
        Text(when (phase) { "wait" -> "Не нажимай…"; "go" -> "ЖМИ!"; "early" -> "Слишком рано!"; "result" -> "Твоя реакция: " + result + " мс"; else -> "Готов проверить реакцию?" },
            color = if (phase == "go") gameGreen else Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        GameButton(when (phase) { "ready", "early", "result" -> "Начать"; "wait" -> "Жду сигнал…"; else -> "Нажать!" }, {
            when (phase) {
                "ready", "early", "result" -> { phase = "wait"; round++ }
                "wait" -> { phase = "early"; round++ }
                "go" -> { result = (System.currentTimeMillis() - startedAt).toInt(); phase = "result"; onScore(10000 / result.coerceAtLeast(1)) }
            }
        }, Modifier.fillMaxWidth())
    }
}

@Composable
private fun MemoryGame(onScore: (Int) -> Unit) {
    val colors = listOf(Color(0xFF43F0A3), Color(0xFF66A6FF), Color(0xFFFFC857), Color(0xFFFF6B6B))
    var sequence by remember { mutableStateOf(listOf(Random.nextInt(4))) }
    var input by remember { mutableStateOf(listOf<Int>()) }
    var showing by remember { mutableStateOf(true) }
    var wrong by remember { mutableStateOf(false) }
    LaunchedEffect(sequence, showing) {
        if (showing) { delay(700L + sequence.size * 140L); showing = false }
    }
    GameCard("🧠 Тренировка памяти", "Запомни порядок цветов, затем повтори его.") {
        Text(if (showing) "Запоминай последовательность…" else if (wrong) "Не страшно — попробуй снова!" else "Повтори " + input.size + " из " + sequence.size, color = Color.White)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colors.forEachIndexed { i, color ->
                Surface(modifier = Modifier.weight(1f).height(58.dp).clickable {
                    if (!showing && !wrong) {
                        val next = input + i
                        input = next
                        if (sequence.getOrNull(next.lastIndex) != i) { wrong = true; onScore(sequence.size - 1) }
                        else if (next.size == sequence.size) { sequence = sequence + Random.nextInt(4); input = emptyList(); showing = true }
                    }
                }, color = if (showing && sequence.lastOrNull() == i) color else color.copy(alpha = 0.65f), shape = RoundedCornerShape(12.dp)) {}
            }
        }
        GameButton(if (wrong) "Начать заново" else "Сбросить", {
            sequence = listOf(Random.nextInt(4)); input = emptyList(); wrong = false; showing = true
        }, Modifier.fillMaxWidth())
        Text("Уровень: " + sequence.size, color = gameGreen, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MathGame(onScore: (Int) -> Unit) {
    var a by remember { mutableIntStateOf(Random.nextInt(2, 20)) }
    var b by remember { mutableIntStateOf(Random.nextInt(2, 20)) }
    var op by remember { mutableIntStateOf(Random.nextInt(3)) }
    var score by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("Выбери ответ") }
    val answer = when (op) { 0 -> a + b; 1 -> a - b; else -> a * b }
    val options = remember(a, b, op) { (setOf(answer, answer + 3, answer - 2, answer + 7)).shuffled() }
    GameCard("🔢 Быстрый счёт", "Решай примеры. Каждая верная задача приносит очки.") {
        Text(a.toString() + " " + listOf("+", "−", "×")[op] + " " + b + " = ?", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Очки: " + score, color = gameGreen, fontSize = 20.sp)
        options.forEach { option ->
            OutlinedButton(onClick = {
                if (option == answer) { score++; message = "Верно! Так держать 🎉"; onScore(score) }
                else message = "Почти! Попробуй следующий пример."
                a = Random.nextInt(2, 20); b = Random.nextInt(2, 20); op = Random.nextInt(3)
            }, modifier = Modifier.fillMaxWidth()) { Text(option.toString()) }
        }
        Text(message, color = gameMuted)
        OutlinedButton(onClick = { score = 0; message = "Новый раунд"; a = Random.nextInt(2, 20); b = Random.nextInt(2, 20); op = Random.nextInt(3) }, modifier = Modifier.fillMaxWidth()) { Text("Начать сначала") }
    }
}
