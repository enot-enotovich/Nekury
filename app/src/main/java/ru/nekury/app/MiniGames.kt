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
                Triple("math", "🔢 Быстрый счёт", "Решай примеры на скорость"),
                Triple("tetris", "🧱 Тетрис", "Собирай линии из падающих фигур")
            )
            games.forEach { game ->
                Card(modifier = Modifier.fillMaxWidth().clickable { selected = game.first },
                    colors = CardDefaults.cardColors(containerColor = gamePanel), shape = RoundedCornerShape(20.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            color = Color(0xFF183B2D),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(game.second.substringBefore(" ").take(2), fontSize = 22.sp)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(game.second.substringAfter(" "), color = gameGreen, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(game.third, color = gameMuted, fontSize = 13.sp)
                            bestScores[game.first]?.let { Text("🏆 Рекорд: " + it, color = Color(0xFFFFD166), fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        }
                        Text("Играть  ›", color = gameGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                "tetris" -> TetrisGame { s -> bestScores = bestScores + ("tetris" to maxOf(s, bestScores["tetris"] ?: 0)) }
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
    var blockX by remember { mutableFloatStateOf(0.18f) }
    var direction by remember { mutableIntStateOf(1) }
    var ended by remember { mutableStateOf(false) }

    LaunchedEffect(ended) {
        while (!ended) {
            delay(28)
            blockX += direction * (0.012f + height * 0.0008f)
            if (blockX > 0.72f) { blockX = 0.72f; direction = -1 }
            if (blockX < 0.08f) { blockX = 0.08f; direction = 1 }
        }
    }

    GameCard("🏗️ Башня", "Нажми «Поставить блок», когда движущийся блок окажется над башней.") {
        Canvas(Modifier.fillMaxWidth().height(240.dp)) {
            val w = size.width
            val h = size.height
            drawRoundRect(Color(0xFF183B2D), size = Size(w, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f))
            // Чистый фон без крупных декоративных кругов — поле и блоки читаются лучше.
            val blockW = w * 0.25f
            val blockH = h * 0.085f
            val baseX = w * 0.375f
            val visible = height.coerceAtMost(7)
            for (i in 0 until visible) {
                val y = h * 0.86f - (i + 1) * blockH
                drawRoundRect(Color(0xFF278E67), topLeft = Offset(baseX, y), size = Size(blockW, blockH - 3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f))
            }
            val movingY = h * 0.86f - (visible + 1) * blockH
            drawRoundRect(gameGreen, topLeft = Offset(w * blockX, movingY), size = Size(blockW, blockH - 3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f))
            drawLine(Color(0xFF9CB8AA), start = Offset(w * 0.1f, h * 0.86f), end = Offset(w * 0.9f, h * 0.86f), strokeWidth = 4f)
        }
        Text("Высота: $height", color = gameGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        if (ended) Text("Башня остановилась! Результат: $height", color = Color.White)
        GameButton(if (ended) "Играть снова" else "Поставить блок", {
            if (ended) {
                height = 0; blockX = 0.18f; direction = 1; ended = false
            } else {
                val distance = kotlin.math.abs(blockX - 0.375f)
                if (distance < (0.22f - height.coerceAtMost(10) * 0.012f)) {
                    height++
                    blockX = 0.18f
                    direction = 1
                } else {
                    ended = true
                    onScore(height)
                }
            }
        }, Modifier.fillMaxWidth())
    }
}

@Composable
private fun RaceGame(onScore: (Int) -> Unit) {
    var lane by remember { mutableIntStateOf(1) }
    var obstacleLane by remember { mutableIntStateOf(Random.nextInt(3)) }
    var obstacleY by remember { mutableFloatStateOf(0.12f) }
    var score by remember { mutableIntStateOf(0) }
    var crashed by remember { mutableStateOf(false) }

    LaunchedEffect(crashed) {
        while (!crashed) {
            delay(55)
            obstacleY += 0.025f + (score.coerceAtMost(20) * 0.001f)
            if (obstacleY > 0.78f) {
                if (lane == obstacleLane) {
                    crashed = true
                    onScore(score)
                } else {
                    score++
                    obstacleLane = Random.nextInt(3)
                    obstacleY = 0.12f
                }
            }
        }
    }

    GameCard("🏎️ Мини-гонки", "Управляй машиной на трёх полосах и уворачивайся от конусов.") {
        Canvas(Modifier.fillMaxWidth().height(250.dp)) {
            val w = size.width
            val h = size.height
            drawRoundRect(Color(0xFF26352F), size = Size(w, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f))
            val roadLeft = w * 0.12f
            val roadWidth = w * 0.76f
            drawRect(Color(0xFF37443E), topLeft = Offset(roadLeft, 0f), size = Size(roadWidth, h))
            for (i in 1..2) {
                val x = roadLeft + roadWidth * i / 3f
                for (j in 0..5) {
                    val y = (j * h / 5f + (score * 13f % (h / 5f))) % h
                    drawRoundRect(Color(0xFFB7C8BE), topLeft = Offset(x - 2f, y), size = Size(4f, h * 0.07f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
                }
            }
            val laneCenter = roadLeft + roadWidth * (lane + 0.5f) / 3f
            val oy = h * obstacleY
            val obstacleX = roadLeft + roadWidth * (obstacleLane + 0.5f) / 3f
            drawRoundRect(Color(0xFFFFB547), topLeft = Offset(obstacleX - 15f, oy - 18f), size = Size(30f, 36f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f))
            drawRoundRect(Color(0xFFE7F4EE), topLeft = Offset(laneCenter - 20f, h * 0.72f), size = Size(40f, 62f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
            drawRoundRect(Color(0xFF73BDF2), topLeft = Offset(laneCenter - 13f, h * 0.74f), size = Size(26f, 17f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f))
            drawCircle(Color(0xFF101915), radius = 5f, center = Offset(laneCenter - 15f, h * 0.77f))
            drawCircle(Color(0xFF101915), radius = 5f, center = Offset(laneCenter + 15f, h * 0.77f))
        }
        Text("Очки: $score", color = gameGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        if (crashed) Text("💥 Столкновение! Попробуй ещё раз.", color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GameButton("←", { if (crashed) { score = 0; lane = 1; obstacleLane = Random.nextInt(3); obstacleY = 0.12f; crashed = false } else lane = (lane - 1).coerceAtLeast(0) }, Modifier.weight(1f))
            GameButton(if (crashed) "Заново" else "🚗", { if (crashed) { score = 0; lane = 1; obstacleLane = Random.nextInt(3); obstacleY = 0.12f; crashed = false } }, Modifier.weight(1f))
            GameButton("→", { if (crashed) { score = 0; lane = 1; obstacleLane = Random.nextInt(3); obstacleY = 0.12f; crashed = false } else lane = (lane + 1).coerceAtMost(2) }, Modifier.weight(1f))
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
            // Небольшие декоративные элементы не перекрывают игровую область.
            drawCircle(color = Color(0xFF24533D), radius = w * 0.035f, center = Offset(w * 0.86f, h * 0.16f))
            drawCircle(color = Color(0xFF1D4935), radius = w * 0.045f, center = Offset(w * 0.10f, h * 0.24f))
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
private fun TetrisGame(onScore: (Int) -> Unit) {
    val rows = 18
    val cols = 10
    val palette = listOf(Color(0xFF43F0A3), Color(0xFF66A6FF), Color(0xFFFFC857), Color(0xFFFF6B6B), Color(0xFFC792EA), Color(0xFF4DD0E1), Color(0xFFFF9F68))
    val shapes = listOf(
        listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0),
        listOf(0 to 0, 1 to 0, 0 to 1, 1 to 1),
        listOf(1 to 0, 0 to 1, 1 to 1, 2 to 1),
        listOf(0 to 0, 0 to 1, 1 to 1, 2 to 1),
        listOf(2 to 0, 0 to 1, 1 to 1, 2 to 1),
        listOf(1 to 0, 2 to 0, 0 to 1, 1 to 1),
        listOf(0 to 0, 1 to 0, 1 to 1, 2 to 1)
    )
    var board by remember { mutableStateOf(List(rows) { List(cols) { -1 } }) }
    var piece by remember { mutableStateOf(shapes.random()) }
    var pieceColor by remember { mutableIntStateOf(Random.nextInt(palette.size)) }
    var px by remember { mutableIntStateOf(3) }
    var py by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var ended by remember { mutableStateOf(false) }
    var tick by remember { mutableIntStateOf(0) }

    fun collides(shape: List<Pair<Int, Int>>, x: Int, y: Int, grid: List<List<Int>>): Boolean =
        shape.any { (dx, dy) ->
            val xx = x + dx
            val yy = y + dy
            xx < 0 || xx >= cols || yy >= rows || (yy >= 0 && grid[yy][xx] >= 0)
        }

    fun spawn(grid: List<List<Int>>) {
        piece = shapes.random()
        pieceColor = Random.nextInt(palette.size)
        px = 3
        py = 0
        if (collides(piece, px, py, grid)) {
            ended = true
            onScore(score)
        }
    }

    fun stepDown() {
        if (ended) return
        if (!collides(piece, px, py + 1, board)) {
            py++
        } else {
            val next = board.map { it.toMutableList() }.toMutableList()
            piece.forEach { (dx, dy) ->
                val x = px + dx
                val y = py + dy
                if (y in 0 until rows && x in 0 until cols) next[y][x] = pieceColor
            }
            val kept = next.filter { row -> row.any { it < 0 } }
            val cleared = rows - kept.size
            board = List(cleared) { List(cols) { -1 } } + kept.map { it.toList() }
            score += when (cleared) { 1 -> 100; 2 -> 250; 3 -> 400; 4 -> 700; else -> 0 }
            spawn(board)
        }
        tick++
    }

    LaunchedEffect(ended) {
        while (!ended) {
            delay((480 - (score / 100) * 15).coerceAtLeast(140).toLong())
            stepDown()
        }
    }

    GameCard("🧱 Тетрис", "Сдвигай и поворачивай падающие фигуры. Заполняй линии без пробелов!") {
        Canvas(Modifier.fillMaxWidth().height(330.dp)) {
            val cell = minOf(size.width / cols, size.height / rows)
            val left = (size.width - cell * cols) / 2f
            val top = (size.height - cell * rows) / 2f
            drawRoundRect(Color(0xFF07140F), topLeft = Offset(left, top), size = Size(cell * cols, cell * rows), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
            for (y in 0 until rows) for (x in 0 until cols) {
                val value = board[y][x]
                if (value >= 0) {
                    drawRoundRect(palette[value], topLeft = Offset(left + x * cell + 1f, top + y * cell + 1f), size = Size(cell - 2f, cell - 2f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
                } else {
                    drawRoundRect(Color(0xFF1A3026), topLeft = Offset(left + x * cell + 1f, top + y * cell + 1f), size = Size(cell - 2f, cell - 2f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
                }
            }
            if (!ended) piece.forEach { (dx, dy) ->
                val x = px + dx
                val y = py + dy
                if (x in 0 until cols && y in 0 until rows) drawRoundRect(palette[pieceColor], topLeft = Offset(left + x * cell + 1f, top + y * cell + 1f), size = Size(cell - 2f, cell - 2f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
            }
        }
        Text("Очки: $score", color = gameGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        if (ended) Text("Игра окончена! Результат: $score", color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GameButton("←", { if (ended) { board = List(rows) { List(cols) { -1 } }; score = 0; ended = false; spawn(board) } else if (!collides(piece, px - 1, py, board)) px-- }, Modifier.weight(1f))
            GameButton("↻", {
                if (!ended) {
                    val rotated = piece.map { (x, y) -> (2 - y) to x }
                    if (!collides(rotated, px, py, board)) piece = rotated
                }
            }, Modifier.weight(1f))
            GameButton("↓", { if (!ended) stepDown() }, Modifier.weight(1f))
            GameButton("→", { if (ended) { board = List(rows) { List(cols) { -1 } }; score = 0; ended = false; spawn(board) } else if (!collides(piece, px + 1, py, board)) px++ }, Modifier.weight(1f))
        }
        GameButton("Быстро вниз", { if (!ended) repeat(3) { stepDown() } }, Modifier.fillMaxWidth())
        if (ended) GameButton("Играть снова", { board = List(rows) { List(cols) { -1 } }; score = 0; ended = false; px = 3; py = 0; spawn(board) }, Modifier.fillMaxWidth())
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
