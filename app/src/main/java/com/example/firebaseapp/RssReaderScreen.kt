package com.example.firebaseapp

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.firebaseapp.data.NewsItem
import com.example.firebaseapp.data.RssRepository
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.PopupProperties



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RssReaderScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { RssRepository() }

    var allNews by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var filteredNews by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedSource by remember { mutableStateOf<String?>(null) }
    var selectedTopic by remember { mutableStateOf<String?>(null) }
    var keyword by remember { mutableStateOf("") }

    // Состояние для управления открытым меню
    var openedMenu by remember { mutableStateOf<String?>(null) }

    val sources = repository.getAvailableSources()
    val topics = listOf("Спорт", "Наука", "Политика", "Война")

    LaunchedEffect(Unit) {
        scope.launch {
            allNews = repository.fetchAllFeeds()
            filteredNews = allNews
            isLoading = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Новостной агрегатор") }) }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(8.dp)) {

            // 🔍 Фильтры
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Источник
                DropdownMenuBox(
                    label = "Источник",
                    items = listOf("Все") + sources,
                    selectedItem = selectedSource ?: "Все",
                    onItemSelected = {
                        selectedSource = if (it == "Все") null else it
                        openedMenu = null
                    },
                    isOpen = openedMenu == "source",
                    onOpenRequest = { openedMenu = "source" },
                    onDismiss = { openedMenu = null }
                )

                // Тема
                DropdownMenuBox(
                    label = "Тема",
                    items = listOf("Все") + topics,
                    selectedItem = selectedTopic ?: "Все",
                    onItemSelected = {
                        selectedTopic = if (it == "Все") null else it
                        openedMenu = null
                    },
                    isOpen = openedMenu == "topic",
                    onOpenRequest = { openedMenu = "topic" },
                    onDismiss = { openedMenu = null }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Поиск по ключевым словам
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                label = { Text("Поиск по ключевым словам") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    filteredNews = repository.filterFeeds(allNews, selectedSource, selectedTopic, keyword)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Применить")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(filteredNews) { item ->
                        NewsCard(item = item, onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                            context.startActivity(intent)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    label: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    isOpen: Boolean,
    onOpenRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(modifier = Modifier.wrapContentSize()) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = if (isOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .width(160.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        onOpenRequest()
                    })
                }
        )

        DropdownMenu(
            expanded = isOpen,
            onDismissRequest = { onDismiss() },
            properties = PopupProperties(focusable = true)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item)
                            if (item == selectedItem) {
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Выбрано",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    onClick = {
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}

@Composable
fun NewsCard(item: NewsItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.pubDate, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
