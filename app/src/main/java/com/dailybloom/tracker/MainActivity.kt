package com.dailybloom.tracker

import android.Manifest
import android.content.Context
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
import java.util.Date
import java.util.Locale

private val BloomCream = Color(0xFFFFF9F2)
private val BloomPink = Color(0xFFF4E5E8)
private val BloomSage = Color(0xFF8D9F89)
private val BloomText = Color(0xFF51454A)

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (
            Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        setContent {
            DailyBloomApp(this)
        }
    }
}

@Composable
fun DailyBloomApp(context: Context) {
    val prefs = remember {
        context.getSharedPreferences(
            "dailybloom",
            Context.MODE_PRIVATE
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "Today", "Water", "Books",
        "Healing", "Diary", "Analytics"
    )

    val icons = listOf("☀", "💧", "📚", "🌸", "📔", "✦")

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = BloomSage,
            secondary = Color(0xFFD9AAB8),
            background = BloomCream,
            surface = Color.White
        )
    ) {
        Scaffold(
            containerColor = BloomCream,
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    tabs.forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Text(icons[index])
                            },
                            label = {
                                Text(label, fontSize = 10.sp)
                            }
                        )
                    }
                }
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                Text(
                    "DAILY BLOOM",
                    color = BloomSage,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Text(
                    "A gentle day, one step at a time 🌷",
                    color = BloomText,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        top = 6.dp,
                        bottom = 14.dp
                    )
                )

                when (selectedTab) {
                    0 -> TodayPage(prefs) { refresh++ }
                    1 -> WaterPage(prefs) { refresh++ }
                    2 -> BooksPage(prefs) { refresh++ }
                    3 -> HealingPage()
                    4 -> DiaryPage(prefs)
                    5 -> AnalyticsPage(prefs, refresh)
                }
            }
        }
    }
}

private fun todayKey(): String {
    return SimpleDateFormat(
        "yyyyMMdd",
        Locale.getDefault()
    ).format(Date())
}

@Composable
private fun BloomCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                color = BloomSage,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}
@Composable
private fun TodayPage(
    prefs: android.content.SharedPreferences,
    refresh: () -> Unit
) {
    val today = todayKey()

    val tasks = listOf(
        "Wake up" to "4:00 AM",
        "Morning gratitude" to "4:05 AM",
        "Morning study" to "4:45 AM",
        "Walking / movement" to "6:00 AM",
        "Breakfast" to "8:30 AM",
        "Coconut" to "5:00 PM",
        "Evening gratitude" to "8:55 PM"
    )

    val completed = tasks.count {
        prefs.getBoolean("${today}_${it.first}", false)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            BloomCard("TODAY'S ROUTINE") {
                Text(
                    "$completed / ${tasks.size} completed",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = BloomText
                )
                LinearProgressIndicator(
                    progress = {
                        completed.toFloat() / tasks.size
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = BloomSage
                )
            }
        }

        items(tasks) { task ->
            val key = "${today}_${task.first}"
            val checked = prefs.getBoolean(key, false)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable {
                        prefs.edit()
                            .putBoolean(key, !checked)
                            .apply()
                        refresh()
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = {
                        prefs.edit()
                            .putBoolean(key, it)
                            .apply()
                        refresh()
                    }
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        task.first,
                        color = BloomText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        task.second,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                if (checked) {
                    Text("Done", color = BloomSage)
                }
            }
        }
    }
}

@Composable
private fun WaterPage(
    prefs: android.content.SharedPreferences,
    refresh: () -> Unit
) {
    val today = todayKey()
    val waterCount = (0 until 8).count {
        prefs.getBoolean("${today}_water_$it", false)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BloomCard("💧 WATER TRACKER") {
            Text(
                "${waterCount * 200} / 1600 ml",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = { waterCount / 8f },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF83B7C5)
            )
            Text("Tick one glass whenever you drink 200 ml.")
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items((0 until 8).toList()) { index ->
                val key = "${today}_water_$index"
                val done = prefs.getBoolean(key, false)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            prefs.edit()
                                .putBoolean(key, !done)
                                .apply()
                            refresh()
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = done,
                        onCheckedChange = {
                            prefs.edit()
                                .putBoolean(key, it)
                                .apply()
                            refresh()
                        }
                    )
                    Text(
                        "Glass ${index + 1} • 200 ml",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (done) "Done" else "Pending",
                        color = if (done) BloomSage else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun BooksPage(
    prefs: android.content.SharedPreferences,
    refresh: () -> Unit
) {
    var bookName by remember { mutableStateOf("") }

    var books by remember {
        mutableStateOf(
            prefs.getStringSet(
                "daily_bloom_books",
                emptySet()
            )?.toList() ?: emptyList()
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BloomCard("📚 MY READING CORNER") {
            Text("Monthly goal: 2 books")

            OutlinedTextField(
                value = bookName,
                onValueChange = { bookName = it },
                label = { Text("Enter book name") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val name = bookName.trim()
                    if (name.isNotEmpty() && name !in books) {
                        books = books + name
                        prefs.edit()
                            .putStringSet(
                                "daily_bloom_books",
                                books.toSet()
                            )
                            .apply()
                        bookName = ""
                        refresh()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BloomSage
                )
            ) {
                Text("Add Book")
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(books) { book ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White,
                            RoundedCornerShape(14.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📖 $book",
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            books = books.filterNot { it == book }
                            prefs.edit()
                                .putStringSet(
                                    "daily_bloom_books",
                                    books.toSet()
                                )
                                .apply()
                            refresh()
                        }
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
@Composable
private fun HealingPage() {
    val affirmations = listOf(
        "I am sorry.",
        "Please forgive me.",
        "Thank you.",
        "I love you.",
        "I forgive myself."
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BloomCard("🌸 HEALING CORNER") {
            Text("Take a gentle moment for yourself.")
        }

        affirmations.forEach { affirmation ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = BloomPink
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    affirmation,
                    modifier = Modifier.padding(18.dp),
                    fontSize = 18.sp,
                    color = BloomText
                )
            }
        }

        Text(
            "Repeat these affirmations slowly for 5 minutes.",
            color = Color.Gray
        )

        Text(
            "Voice playback will be added in the next update.",
            color = BloomSage
        )
    }
}

@Composable
private fun DiaryPage(
    prefs: android.content.SharedPreferences
) {
    val today = SimpleDateFormat(
        "dd-MM-yyyy",
        Locale.getDefault()
    ).format(Date())

    var proud by remember {
        mutableStateOf(
            prefs.getString("${today}_proud", "") ?: ""
        )
    }

    var regret by remember {
        mutableStateOf(
            prefs.getString("${today}_regret", "") ?: ""
        )
    }

    var freeWriting by remember {
        mutableStateOf(
            prefs.getString("${today}_free", "") ?: ""
        )
    }

    var saved by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            BloomCard("📔 PROUD & REGRET MOMENTS DIARY") {
                Text("Today's date: $today")
            }
        }

        item {
            Text(
                "✨ Proud Moments",
                fontWeight = FontWeight.Bold,
                color = BloomSage
            )
            OutlinedTextField(
                value = proud,
                onValueChange = {
                    proud = it
                    saved = false
                },
                label = { Text("What did I do well today?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        item {
            Text(
                "🌱 Regret Moments",
                fontWeight = FontWeight.Bold,
                color = BloomSage
            )
            OutlinedTextField(
                value = regret,
                onValueChange = {
                    regret = it
                    saved = false
                },
                label = { Text("What would I do differently?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        item {
            Text(
                "💗 Free Writing",
                fontWeight = FontWeight.Bold,
                color = BloomSage
            )
            OutlinedTextField(
                value = freeWriting,
                onValueChange = {
                    freeWriting = it
                    saved = false
                },
                label = { Text("Write anything on your mind...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
        }

        item {
            Button(
                onClick = {
                    prefs.edit()
                        .putString("${today}_proud", proud)
                        .putString("${today}_regret", regret)
                        .putString("${today}_free", freeWriting)
                        .apply()
                    saved = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BloomSage
                )
            ) {
                Text("Save Today's Diary")
            }

            if (saved) {
                Text(
                    "Your diary entry is saved 💗",
                    color = BloomSage
                )
            }
        }
    }
}

@Composable
private fun AnalyticsPage(
    prefs: android.content.SharedPreferences,
    refresh: Int
) {
    val today = todayKey()

    val routineNames = listOf(
        "Wake up",
        "Morning gratitude",
        "Morning study",
        "Walking / movement",
        "Breakfast",
        "Coconut",
        "Evening gratitude"
    )

    val completed = routineNames.count {
        prefs.getBoolean("${today}_$it", false)
    }

    val waterCount = (0 until 8).count {
        prefs.getBoolean("${today}_water_$it", false)
    }

    val bookCount = prefs.getStringSet(
        "daily_bloom_books",
        emptySet()
    )?.size ?: 0

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BloomCard("✨ YOUR DAILY PROGRESS") {
            Text("Today's routine")
            Text(
                "$completed / ${routineNames.size} completed",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = {
                    completed.toFloat() / routineNames.size
                },
                modifier = Modifier.fillMaxWidth(),
                color = BloomSage
            )
        }

        BloomCard("💧 WATER INTAKE") {
            Text(
                "$waterCount / 8 glasses",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text("${waterCount * 200} ml consumed")
            LinearProgressIndicator(
                progress = { waterCount / 8f },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF83B7C5)
            )
        }

        BloomCard("📚 READING") {
            Text("Books in your reading list: $bookCount")
            Text("Monthly reading goal: 2 books")
        }

        BloomCard("🌷 A LITTLE REMINDER") {
            Text("Small steps are still progress.")
            Text("Keep blooming, one day at a time 💗")
        }
    }
}
