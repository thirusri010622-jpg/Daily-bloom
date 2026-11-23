package com.dailybloom.tracker

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Task(val time: String, val title: String, val category: String)

val routine = listOf(
    Task("4:00 AM", "Wake up", "Morning"),
    Task("4:05 AM", "Morning gratitude", "Mindfulness"),
    Task("4:30 AM", "Wash face / get ready", "Morning"),
    Task("4:45 AM", "Morning study", "Study"),
    Task("6:00 AM", "Walking / movement", "Fitness"),
    Task("6:30 AM", "Cooking", "Home"),
    Task("7:30 AM", "Get ready for the day", "Home"),
    Task("8:30 AM", "Breakfast", "Health"),
    Task("1:00 PM", "Gratitude time", "Mindfulness"),
    Task("6:00 PM", "Eat coconut", "Health"),
    Task("6:30 PM", "Relax without phone", "Wellbeing"),
    Task("7:00 PM", "Dinner", "Health"),
    Task("7:30 PM", "Read books", "Reading"),
    Task("8:30 PM", "Prepare for business", "Business"),
    Task("8:55 PM", "Evening gratitude", "Mindfulness")
)

val waterTimes = listOf("9:00 AM", "6:30 AM", "10:00 AM", "11:00 AM",
    "12:30 PM", "3:00 PM", "5:00 PM", "6:30 PM")

class MainActivity : ComponentActivity() {
    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        ReminderReceiver.createChannel(this)
        ReminderScheduler.scheduleAll(this)
        setContent { DailyBloomApp(this) }
    }
}

@Composable
fun DailyBloomApp(context: Context) {
    val prefs = remember { context.getSharedPreferences("dailybloom", Context.MODE_PRIVATE) }
    var tab by remember { mutableStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }
    val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    val completed = routine.count { prefs.getBoolean("${today}_${it.title}", false) } + refresh * 0
    val waterDone = (0 until 8).count { prefs.getBoolean("${today}_water_$it", false) }

    val cream = Color(0xFFFFF9F2)
    val pink = Color(0xFFE9B8C5)
    val sage = Color(0xFF8D9F89)
    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF9A7180), background = cream, surface = Color.White)) {
        Scaffold(containerColor = cream, bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf("Today", "Water", "Books", "Analysis").forEachIndexed { i, label ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i },
                        icon = { Text(listOf("☀", "💧", "📚", "✦")[i]) }, label = { Text(label) })
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(18.dp))
                Text("DAILY BLOOM", color = sage, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text(listOf("A gentle day, one step at a time", "Water check-in", "Your reading corner", "Your progress")[tab],
                    fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E4145))
                Spacer(Modifier.height(14.dp))
                when (tab) {
                    0 -> {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF4E5E8)), shape = RoundedCornerShape(20.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("TODAY'S ROUTINE", color = Color(0xFF8A6873), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("$completed / ${routine.size} completed", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                                LinearProgressIndicator(progress = { completed.toFloat() / routine.size }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), color = sage)
                                Text("Morning gratitude 4:05 AM  •  Evening gratitude 8:55 PM", fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 10.dp))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(routine) { task ->
                                val checked = prefs.getBoolean("${today}_${task.title}", false)
                                Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).clickable {
                                    prefs.edit().putBoolean("${today}_${task.title}", !checked).apply(); refresh++
                                }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked, onCheckedChange = {
                                        prefs.edit().putBoolean("${today}_${task.title}", it).apply(); refresh++
                                    }, colors = CheckboxDefaults.colors(checkedColor = sage))
                                    Column(Modifier.weight(1f)) {
                                        Text(task.title, fontWeight = FontWeight.SemiBold, color = Color(0xFF51454A))
                                        Text("${task.time}  •  ${task.category}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    if (checked) Text("Done", color = sage, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    1 -> {
                        Text("Tick each 200 ml glass as you drink it. Daily target: 1.6 litres.", color = Color.Gray)
                        Spacer(Modifier.height(12.dp))
                        (0 until 8).forEach { i ->
                            val done = prefs.getBoolean("${today}_water_$i", false)
                            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp).background(Color.White, RoundedCornerShape(14.dp)).clickable {
                                prefs.edit().putBoolean("${today}_water_$i", !done).apply(); refresh++
                            }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(done, onCheckedChange = { prefs.edit().putBoolean("${today}_water_$i", it).apply(); refresh++ })
                                Text("${waterTimes[i]}  •  200 ml", Modifier.weight(1f))
                                Text(if (done) "Drunk" else "Pending", color = if (done) sage else Color.Gray, fontSize = 12.sp)
                            }
                        }
                        Text("Total: ${waterDone * 200} / 1600 ml", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                    }
                    2 -> {
                        var book by remember { mutableStateOf("") }
                        var books by remember { mutableStateOf(prefs.getStringSet("books", emptySet())!!.toList()) }
                        OutlinedTextField(book, { book = it }, label = { Text("Add a book") }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = { if (book.isNotBlank()) { books = books + book; prefs.edit().putStringSet("books", books.toSet()).apply(); book = "" } },
                            colors = ButtonDefaults.buttonColors(containerColor = sage)) { Text("Add book") }
                        Text("Reading list • Aim for 2 new books each month", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.SemiBold)
                        books.forEach { b -> Text("📖  $b", Modifier.fillMaxWidth().padding(8.dp)) }
                        Text("Keep finished books and notes in your reading journal.", color = Color.Gray, fontSize = 12.sp)
                    }
                    else -> {
                        Text("TODAY", fontWeight = FontWeight.Bold, color = sage)
                        Text("$completed of ${routine.size} routine activities completed")
                        LinearProgressIndicator(progress = { completed.toFloat() / routine.size }, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), color = sage)
                        Text("WATER", fontWeight = FontWeight.Bold, color = sage, modifier = Modifier.padding(top = 18.dp))
                        Text("$waterDone of 8 glasses • ${waterDone * 200} ml")
                        LinearProgressIndicator(progress = { waterDone / 8f }, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), color = Color(0xFF83B7C5))
                        Text("SUPPLEMENTS", fontWeight = FontWeight.Bold, color = sage, modifier = Modifier.padding(top = 18.dp))
                        Text("Protein • Daily Plus • Omega-3\nTick these in your routine after taking them.")
                        Text("Your weekly and monthly trend will build as you mark each day.", color = Color.Gray, modifier = Modifier.padding(top = 18.dp))
                    }
                }
            }
        }
    }
}

object ReminderScheduler {
    fun scheduleAll(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        routine.forEachIndexed { index, task ->
            val parts = task.time.split(" ")
            val hm = parts[0].split(":")
            var hour = hm[0].toInt()
            val minute = hm[1].toInt()
            if (parts[1] == "PM" && hour != 12) hour += 12
            if (parts[1] == "AM" && hour == 12) hour = 0
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            val intent = Intent(context, ReminderReceiver::class.java).putExtra("title", task.title).putExtra("id", index)
            val pi = PendingIntent.getBroadcast(context, index, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            try {
                if (Build.VERSION.SDK_INT >= 23) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
                else am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            } catch (_: SecurityException) {
                am.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            }
        }
    }
}
