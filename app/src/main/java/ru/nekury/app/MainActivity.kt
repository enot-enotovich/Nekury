package ru.nekury.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.Locale
import java.text.NumberFormat
import java.util.Currency
import java.text.SimpleDateFormat
import java.util.Date

private val ComponentActivity.dataStore by preferencesDataStore(name = "nekury")
private val startedKey = longPreferencesKey("quit_started")
private val priceKey = doublePreferencesKey("pack_price")
private val perDayKey = intPreferencesKey("cigs_per_day")
private val packKey = intPreferencesKey("pack_size")
private val green = Color(0xFF65E6A1)
private val bg = Color(0xFF07110D)
private val panel = Color(0xFF102019)

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
            var cravingCount by remember { mutableIntStateOf(0) }
            var relapseCount by remember { mutableIntStateOf(0) }
            var note by remember { mutableStateOf("") }
            var showSettings by remember { mutableStateOf(false) }
            var quitDateText by remember { mutableStateOf("") }
            var priceText by remember { mutableStateOf("300") }
            var perDayText by remember { mutableStateOf("15") }
            var packText by remember { mutableStateOf("20") }
            var dateError by remember { mutableStateOf(false) }
            var settingsInitialized by remember { mutableStateOf(false) }
            var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }
            LaunchedEffect(Unit) {
                while (true) {
                    kotlinx.coroutines.delay(60_000)
                    tick = System.currentTimeMillis()
                }
            }
            val started = prefs?.get(startedKey) ?: tick
            LaunchedEffect(started) {
                quitDateText = SimpleDateFormat("dd.MM.yyyy", Locale("ru", "RU")).format(Date(started))
            }
            val storedPrice = prefs?.get(priceKey) ?: 300.0
            val price = if (storedPrice < 1.0) 300.0 else storedPrice
            val perDay = prefs?.get(perDayKey) ?: 15
            val pack = prefs?.get(packKey) ?: 20
            LaunchedEffect(prefs) {
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
            MaterialTheme(colorScheme = darkColorScheme(primary = green, background = bg, surface = panel, onSurface = Color(0xFFE8F4EC))) {
                Surface(Modifier.fillMaxSize(), color = bg) {
                    Column(
                        Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("НеКурю", fontSize = 29.sp, fontWeight = FontWeight.Black, color = green)
                                Text("Каждый час — победа", color = Color.LightGray)
                            }
                            Icon(Icons.Default.Favorite, null, tint = green, modifier = Modifier.size(30.dp))
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(24.dp)) {
                            Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("БЕЗ СИГАРЕТ", color = green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${days}д ${hours}ч ${minutes}м", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                                Text("Ты справляешься. Продолжай!", color = Color.LightGray)
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = quitDateText,
                                    onValueChange = { quitDateText = it; dateError = false },
                                    label = { Text("Дата отказа (дд.мм.гггг)") },
                                    placeholder = { Text("01.01.2026") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = dateError,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (dateError) Text("Введите дату в формате дд.мм.гггг, не позднее сегодняшнего дня", color = MaterialTheme.colorScheme.error)
                                OutlinedButton(onClick = {
                                    try {
                                        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale("ru", "RU")).apply { isLenient = false }
                                        val parsed = formatter.parse(quitDateText.trim()) ?: throw IllegalArgumentException()
                                        val calendar = java.util.Calendar.getInstance().apply {
                                            time = parsed
                                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                                            set(java.util.Calendar.MINUTE, 0)
                                            set(java.util.Calendar.SECOND, 0)
                                            set(java.util.Calendar.MILLISECOND, 0)
                                        }
                                        val selected = calendar.timeInMillis
                                        if (selected > System.currentTimeMillis()) throw IllegalArgumentException()
                                        scope.launch { dataStore.edit { it[startedKey] = selected } }
                                        dateError = false
                                    } catch (_: Exception) { dateError = true }
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Установить дату отказа")
                                }
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = {
                                    scope.launch { dataStore.edit { it[startedKey] = System.currentTimeMillis() } }
                                }, colors = ButtonDefaults.buttonColors(containerColor = green)) {
                                    Text("Начать отсчёт заново", color = bg)
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Metric("Сэкономлено", formatRubles(saved), Modifier.weight(1f))
                            Metric("Не выкурено", avoided.toString(), Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Metric("Тяга пережита", cravingCount.toString(), Modifier.weight(1f))
                            Metric("Срывы отмечены", relapseCount.toString(), Modifier.weight(1f))
                        }
                        Text("Восстановление организма", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        listOf(
                            "20 минут" to "Пульс и давление начинают снижаться",
                            "12 часов" to "Уровень угарного газа в крови снижается",
                            "2–12 недель" to "Улучшается кровообращение и функция лёгких",
                            "1 год" to "Риск ишемической болезни сердца существенно ниже"
                        ).forEach { (time, detail) ->
                            Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                    Column {
                                        Text(time, color = green, fontWeight = FontWeight.Bold)
                                        Text(detail, color = Color.LightGray)
                                    }
                                }
                            }
                        }
                        Text("Дневник тяги", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Что чувствуешь сейчас?") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { cravingCount++; note = "" },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = green)
                            ) { Text("Пережил тягу", color = bg) }
                            OutlinedButton(
                                onClick = {
                                    relapseCount++
                                    scope.launch { dataStore.edit { it[startedKey] = System.currentTimeMillis() } }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.History, null)
                                Text("Срыв")
                            }
                        }
                        OutlinedButton(onClick = { showSettings = !showSettings }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Settings, null)
                            Text("Настройки")
                        }
                        if (showSettings) {
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = { value ->
                                    priceText = value
                                    value.replace(',', '.').toDoubleOrNull()?.takeIf { it in 1.0..100000.0 }?.let { entered ->
                                        scope.launch { dataStore.edit { it[priceKey] = entered } }
                                    }
                                },
                                label = { Text("Цена пачки, ₽") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = perDayText,
                                onValueChange = { value ->
                                    perDayText = value
                                    value.toIntOrNull()?.takeIf { it in 1..200 }?.let { entered ->
                                        scope.launch { dataStore.edit { it[perDayKey] = entered } }
                                    }
                                },
                                label = { Text("Сигарет в день") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = packText,
                                onValueChange = { value ->
                                    packText = value
                                    value.toIntOrNull()?.takeIf { it in 1..100 }?.let { entered ->
                                        scope.launch { dataStore.edit { it[packKey] = entered } }
                                    }
                                },
                                label = { Text("Сигарет в пачке") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Text(
                            "Данные хранятся на устройстве. Приложение не заменяет медицинскую помощь.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Metric(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color.LightGray, fontSize = 12.sp)
            Text(value, color = green, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }
    }
}
