package com.br.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.br.todo.ui.theme.ToDoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToDoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// Перечисление экранов приложения
enum class Screen(val title: String) {
    List("Задачи"),
    Create("Создать задачу"),
}

// Содержимое приложения
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    modifier: Modifier = Modifier,
    viewModel: TodoViewModel = viewModel(),
) {
    val initialRoute = Screen.List.name  // Начальный экран
    val navController = rememberNavController()  // Контроллер навигации

    val navControllerState = navController  // Текущее состояние контроллера навигации
        .currentBackStackEntryFlow
        .collectAsState(initial = navController.currentBackStackEntry)

    // Текущий экран
    val currentRoute = navControllerState.value?.destination?.route ?: initialRoute

    Scaffold (
        modifier = modifier,
        topBar = {
            // Шапка
            TopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text(Screen.valueOf(currentRoute).title) },
                navigationIcon = {
                    // Кнопка назад, если есть куда возвращаться
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, "Назад")
                        }
                    }
                },
                actions = {
                    // Кнопка обновить, только на главном экране
                    if (currentRoute == initialRoute) {
                        IconButton(onClick = { viewModel.loadData() }) {
                            Icon(Icons.Default.Refresh, "Обновить")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Кнопка создать задачу, только на главном экране
            if (currentRoute == initialRoute) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.Create.name)
                    },
                    shape = CircleShape,
                ) {
                    Icon(Icons.Default.Add, "Создать задачу")
                }
            }
        },
    ) {paddingValues ->
        NavHost(
            navController = navController,
            startDestination = initialRoute,
            modifier = Modifier.padding(paddingValues)
        ) {
            val innerModifier = Modifier
                .fillMaxSize()
                .padding(8.dp)

            // Экран списка задач
            composable(Screen.List.name) {
                TodoList(
                    modifier = innerModifier,
                    viewModel = viewModel,
                )
            }

            // Экран создания задачи
            composable(Screen.Create.name) {
                TodoCreate(
                    modifier = innerModifier,
                    viewModel = viewModel,
                    afterCreate = {
                        navController.popBackStack(initialRoute, false)
                        viewModel.loadData()
                    }
                )
            }
        }
    }
}

// Список задач
@Composable
fun TodoList(
    modifier: Modifier = Modifier,
    viewModel: TodoViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()  // Текущее состояние модели представления
    var dialogOpen by remember { mutableStateOf(false) }  // Открыт ли диалог удаления
    var idToDelete by remember { mutableIntStateOf(-1) }  // Какую задачу удалить

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = modifier,
        ) {
            // Выбор содержимого в зависимости от статуса
            when (val status = state.status) {
                // Данные готовы
                is Status.Ready -> {
                    if (state.data.isEmpty()) {
                        // Список задач пуст
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Список пуст")
                        }
                    } else {
                        // Отображение списка задач
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.data) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                // Обработка долгого нажатия на карточку
                                                onLongPress = {
                                                    dialogOpen = true
                                                    idToDelete = item.id
                                                }
                                            )
                                        },
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        // Текст задачи
                                        Text(
                                            text = item.name,
                                            modifier = Modifier
                                                .height(40.dp)
                                                .wrapContentHeight(
                                                    align = Alignment.CenterVertically
                                                )
                                        )
                                        // Галочка
                                        Checkbox(
                                            checked = item.done,
                                            onCheckedChange = {
                                                viewModel.toggleTodo(item.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Данные загружаются
                is Status.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(64.dp),
                        )
                    }
                }

                // Произошла ошибка
                is Status.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("An error has occurred: ${status.message}")
                    }
                }
            }
        }

        // Диалог удаления задачи
        if (dialogOpen) {
            Box(
                modifier = Modifier
                    .background(
                        Color(0, 0, 0, 100)
                    ).fillMaxSize(),
            ) {}
            AlertDialog(
                text = { Text("Удалить выбранную задачу?") },
                confirmButton = {
                    // Кнопка подтверждения
                    TextButton(onClick = {
                        dialogOpen = false
                        viewModel.deleteTodo(idToDelete)
                        // viewModel.loadData()
                    }) {
                        Text("Удалить")
                    }
                },
                // Кнопка отмены
                dismissButton = {
                    TextButton(onClick = {
                        dialogOpen = false
                    }) {
                        Text("Отмена")
                    }
                },
                onDismissRequest = { dialogOpen = false }
            )
        }
    }
}

// Создание задачи
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoCreate(
    modifier: Modifier = Modifier,
    viewModel: TodoViewModel = viewModel(),
    afterCreate: () -> Unit = {}
) {
    // Текущий введённый текст
    var text by remember { mutableStateOf("") }

    Column (
        modifier = modifier,
    ) {
        // Поле ввода текста
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            // Кнопка создать
            Button(onClick = {
                viewModel.newTodo(text)
                afterCreate()
            }) {
                Text("Создать")
            }
        }
    }
}