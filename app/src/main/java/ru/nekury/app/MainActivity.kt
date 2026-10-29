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
            var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }
            LaunchedEffect(Unit) {
                while (true) {
                    kotlinx.coroutines.delay(60_000)
                    tick = System.currentTimeMillis()
                }
            }
            val started = prefs?.get(startedKey) ?: tick
            val price = prefs?.get(priceKey) ?: 300.0
            val perDay = prefs?.get(perDayKey) ?: 15
            val pack = prefs?.get(packKey) ?: 20
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
                            Text("Цена пачки: " + formatRubles(price), color = Color.White)
                            Slider(
                                value = price.toFloat(),
                                onValueChange = { value -> scope.launch { dataStore.edit { it[priceKey] = value.toDouble() } } },
                                valueRange = 100f..1500f
                            )
                            Text("Сигарет в день: $perDay", color = Color.White)
                            Slider(
                                value = perDay.toFloat(),
                                onValueChange = { value -> scope.launch { dataStore.edit { it[perDayKey] = value.toInt() } } },
                                valueRange = 1f..60f,
                                steps = 58
                            )
                            Text("Сигарет в пачке: $pack", color = Color.White)
                            Slider(
                                value = pack.toFloat(),
                                onValueChange = { value -> scope.launch { dataStore.edit { it[packKey] = value.toInt() } } },
                                valueRange = 10f..30f,
                                steps = 19
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
