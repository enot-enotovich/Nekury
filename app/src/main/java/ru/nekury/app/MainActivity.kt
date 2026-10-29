package ru.nekury.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val ComponentActivity.dataStore by preferencesDataStore(name = "nekury")
private val startedKey = longPreferencesKey("quit_started")
private val priceKey = doublePreferencesKey("pack_price")
private val perDayKey = intPreferencesKey("cigs_per_day")
private val packKey = intPreferencesKey("pack_size")
private val cravingKey = intPreferencesKey("craving_count")
private val relapseKey = intPreferencesKey("relapse_count")
private val noteKey = stringPreferencesKey("craving_note")
private val green = Color(0xFF43F0A3)
private val bg = Color(0xFF06110D)
private val panel = Color(0xFF0D2119)
private val panel2 = Color(0xFF102A20)
private val muted = Color(0xFFAAC0B5)

private fun formatRubles(value: Double): String = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).apply {
    currency = Currency.getInstance("RUB")
    minimumFractionDigits = 0
    maximumFractionDigits = 2
}.format(value)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            val prefs by dataStore.data.collectAsState(initial = null)
            var tab by remember { mutableIntStateOf(0) }
            var cravingCount by remember { mutableIntStateOf(0) }
            var relapseCount by remember { mutableIntStateOf(0) }
            var note by remember { mutableStateOf("") }
            var quitDateText by remember { mutableStateOf("") }
            var priceText by remember { mutableStateOf("300") }
            var perDayText by remember { mutableStateOf("15") }
            var packText by remember { mutableStateOf("20") }
            var dateError by remember { mutableStateOf(false) }
            var settingsInitialized by remember { mutableStateOf(false) }
            var noteInitialized by remember { mutableStateOf(false) }
            var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }

            LaunchedEffect(Unit) {
                while (true) { delay(60_000); tick = System.currentTimeMillis() }
            }
            val started = prefs?.get(startedKey) ?: tick
            LaunchedEffect(started) {
                quitDateText = SimpleDateFormat("dd.MM.yyyy", Locale("ru", "RU")).format(Date(started))
            }
            val price = (prefs?.get(priceKey) ?: 300.0).coerceAtLeast(1.0)
            val perDay = prefs?.get(perDayKey) ?: 15
            val pack = (prefs?.get(packKey) ?: 20).coerceAtLeast(1)
            LaunchedEffect(prefs) {
                if (prefs != null && !noteInitialized) {
                    cravingCount = prefs?.get(cravingKey) ?: 0
                    relapseCount = prefs?.get(relapseKey) ?: 0
                    note = prefs?.get(noteKey) ?: ""
                    noteInitialized = true
                }
                if (prefs != null && !settingsInitialized) {
                    priceText = price.toString()
                    perDayText = perDay.toString()
                    packText = pack.toString()
                    settingsInitialized = true
                }
            }
            val elapsed = (tick - started).coerceAtLeast(0)
            val days = TimeUnit.MILLISECONDS.toDays(elapsed)
            val hours = TimeUnit.MILLISECONDS.toHours(elapsed) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60
            val avoided = (elapsed.toDouble() / 86_400_000.0 * perDay).toInt()
            val saved = avoided.toDouble() / pack * price

            MaterialTheme(colorScheme = darkColorScheme(
                primary = green, secondary = green, background = bg, surface = panel,
                onSurface = Color(0xFFF0FFF6), onBackground = Color(0xFFF0FFF6)
            )) {
                Scaffold(
                    containerColor = bg,
                    bottomBar = {
                        NavigationBar(containerColor = Color(0xFF081710), contentColor = green) {
                            NavigationBarItem(selected = tab == 0, onClick = { tab = 0 },
                                icon = { Icon(Icons.Default.Home, null) }, label = { Text("Главная") })
                            NavigationBarItem(selected = tab == 1, onClick = { tab = 1 },
                                icon = { Icon(Icons.Default.BarChart, null) }, label = { Text("Статистика") })
                            NavigationBarItem(selected = tab == 2, onClick = { tab = 2 },
                                icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Настройки") })
                        }
                    }
                ) { padding ->
                    Column(
                        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("НеКурю", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text("Твой путь к свободе от курения", color = muted, fontSize = 13.sp)
                            }
                            Surface(shape = RoundedCornerShape(16.dp), color = panel2) {
                                Icon(Icons.Default.Eco, "НеКурю", tint = green, modifier = Modifier.padding(12.dp).size(28.dp))
                            }
                        }
                        when (tab) {
                            0 -> {
                                Card(colors = CardDefaults.cardColors(containerColor = panel2), shape = RoundedCornerShape(28.dp)) {
                                    Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("ТЫ НЕ КУРИШЬ", color = green, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                        Spacer(Modifier.height(8.dp))
                                        Text("$days дней", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        Text("$hours ч $minutes мин свободы", fontSize = 18.sp, color = green)
                                        Spacer(Modifier.height(8.dp))
                                        Text("Каждый день — победа над собой", color = muted)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Metric("Не выкурено", "$avoided", "сигарет", Modifier.weight(1f))
                                    Metric("Сэкономлено", formatRubles(saved), "примерно", Modifier.weight(1f))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Metric("Без сигарет", "$days дн.", "твой прогресс", Modifier.weight(1f))
                                    Metric("Пережил тягу", "$cravingCount", "раз", Modifier.weight(1f))
                                }
                                SectionTitle("Восстановление организма")
                                listOf(
                                    "20 минут" to "Пульс и давление начинают снижаться",
                                    "12 часов" to "Уровень угарного газа в крови снижается",
                                    "2–12 недель" to "Улучшается кровообращение и функция лёгких",
                                    "1 год" to "Снижается риск ишемической болезни сердца"
                                ).forEach { (time, detail) -> InfoCard(time, detail) }
                                SectionTitle("Дневник тяги")
                                Text("Запиши, что чувствуешь — пережить желание станет проще.", color = muted, fontSize = 13.sp)
                                OutlinedTextField(value = note, onValueChange = { value ->
                                    note = value
                                    scope.launch { dataStore.edit { it[noteKey] = value } }
                                }, label = { Text("Что чувствуешь сейчас?") }, modifier = Modifier.fillMaxWidth(),
                                    minLines = 2, shape = RoundedCornerShape(18.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        cravingCount++
                                        note = ""
                                        scope.launch { dataStore.edit { it[cravingKey] = cravingCount; it[noteKey] = "" } }
                                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = green)) {
                                        Text("Пережил тягу", color = bg)
                                    }
                                    OutlinedButton(onClick = {
                                        relapseCount++
                                        scope.launch { dataStore.edit { it[relapseKey] = relapseCount; it[startedKey] = System.currentTimeMillis() } }
                                    }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.History, null); Spacer(Modifier.width(5.dp)); Text("Срыв")
                                    }
                                }
                            }
                            1 -> {
                                SectionTitle("Твоя статистика")
                                Text("Маленькие победы складываются в большие перемены.", color = muted)
                                Metric("Дней без курения", "$days", "дней свободы")
                                Metric("Не выкурено", "$avoided", "сигарет")
                                Metric("Сэкономлено", formatRubles(saved), "денег")
                                Metric("Пережито приступов тяги", "$cravingCount", "ты справился")
                                Metric("Отмечено срывов", "$relapseCount", "это не отменяет прогресс")
                                SectionTitle("Твои достижения")
                                InfoCard(if (days >= 1) "Первый день" else "Первый шаг", if (days >= 1) "Уже целый день без сигарет — отлично!" else "Начни путь и отметь первый день без сигарет.")
                                InfoCard(if (days >= 7) "Неделя свободы" else "Следующая цель: 7 дней", if (days >= 7) "Неделя позади. Продолжай!" else "Каждый час приближает тебя к этой цели.")
                                InfoCard(if (avoided >= 100) "100 сигарет" else "Цель: 100 сигарет", if (avoided >= 100) "Ты уже избежал сотни сигарет." else "Скоро ты избежишь сотни сигарет.")
                            }
                            else -> {
                                SectionTitle("Настройки")
                                Text("Укажи свои привычки — расчёты станут точнее.", color = muted)
                                OutlinedTextField(value = priceText, onValueChange = { value ->
                                    priceText = value
                                    value.replace(',', '.').toDoubleOrNull()?.takeIf { it in 1.0..100000.0 }?.let { v ->
                                        scope.launch { dataStore.edit { it[priceKey] = v } }
                                    }
                                }, label = { Text("Цена пачки, ₽") }, singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                                OutlinedTextField(value = perDayText, onValueChange = { value ->
                                    perDayText = value
                                    value.toIntOrNull()?.takeIf { it in 1..200 }?.let { v ->
                                        scope.launch { dataStore.edit { it[perDayKey] = v } }
                                    }
                                }, label = { Text("Сигарет в день") }, singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                                OutlinedTextField(value = packText, onValueChange = { value ->
                                    packText = value
                                    value.toIntOrNull()?.takeIf { it in 1..100 }?.let { v ->
                                        scope.launch { dataStore.edit { it[packKey] = v } }
                                    }
                                }, label = { Text("Сигарет в пачке") }, singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                                SectionTitle("Дата отказа от курения")
                                OutlinedTextField(value = quitDateText, onValueChange = { quitDateText = it; dateError = false },
                                    label = { Text("Дата (дд.мм.гггг)") }, placeholder = { Text("29.10.2026") },
                                    singleLine = true, isError = dateError,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                                if (dateError) Text("Проверь дату: формат дд.мм.гггг, не позднее сегодня.", color = MaterialTheme.colorScheme.error)
                                Button(onClick = {
                                    try {
                                        val fmt = SimpleDateFormat("dd.MM.yyyy", Locale("ru", "RU")).apply { isLenient = false }
                                        val pos = ParsePosition(0)
                                        val parsed = fmt.parse(quitDateText.trim(), pos)
                                        if (parsed == null || pos.index != quitDateText.trim().length) throw IllegalArgumentException()
                                        val cal = Calendar.getInstance().apply {
                                            time = parsed
                                            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                                        }
                                        val selected = cal.timeInMillis
                                        if (selected > System.currentTimeMillis()) throw IllegalArgumentException()
                                        scope.launch { dataStore.edit { it[startedKey] = selected } }
                                        dateError = false
                                    } catch (_: Exception) { dateError = true }
                                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = green)) {
                                    Text("Сохранить дату отказа", color = bg)
                                }
                                OutlinedButton(onClick = {
                                    scope.launch { dataStore.edit { it[startedKey] = System.currentTimeMillis() } }
                                }, modifier = Modifier.fillMaxWidth()) { Text("Начать отсчёт заново") }
                                SectionTitle("О приложении")
                                InfoCard("Данные на телефоне", "Прогресс и настройки сохраняются локально, без регистрации.")
                                Text("НеКурю — твой выбор. Дыши свободно, живи ярко!", color = green, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Приложение не заменяет медицинскую помощь.", color = Color(0xFF6F8B7D), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
}

@Composable
private fun InfoCard(title: String, detail: String) {
    Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = panel2, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = green, modifier = Modifier.padding(10.dp).size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = green, fontWeight = FontWeight.Bold)
                Text(detail, color = muted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun Metric(title: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, color = muted, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, color = green, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text(detail, color = muted, fontSize = 11.sp)
        }
    }
}
