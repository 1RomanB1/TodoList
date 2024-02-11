package com.br.todo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Статус состояния модели представления
sealed class Status {
    object Ready : Status()  // Данные готовы
    object Loading : Status()  // Данные загружаются
    data class Error(val message: String) : Status()  // Произошла ошибка
}

// Состояние модели представления
data class State(
    val data: List<Todo> = listOf(),  // Список задач
    val status: Status = Status.Ready,  // Статус
    val nonce: Int = 0, // Костыль, mutableStateFlow не засекает изменения списка
)

// Модель представления
class TodoViewModel : ViewModel() {
    // Состояние модели
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        loadData()  // Загрузить данные при запуске
    }

    // Загрузка данных с сервера
    fun loadData() {
        _state.update { state -> state.copy(status = Status.Loading) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = TodoService.instance.api.getTodos()

                withContext(Dispatchers.Main) {
                    _state.update { State(data.todos) }
                }

            } catch (e: Exception) {
                Log.e("ViewModel", "An error has occurred: ${e.message}")
                withContext(Dispatchers.Main) {
                    _state.update { state ->
                        state.copy(
                            status = Status.Error(e.toString())
                        )
                    }
                }
            }
        }
    }

    // Поставить/убрать галочку у задачи
    fun toggleTodo(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val index = _state.value.data.indexOfFirst { todo -> todo.id == id }
            val todo = _state.value.data[index]
            val newTodo = NewTodo(todo.name, !todo.done)

            try {
                val updatedTodo = TodoService.instance.api.updateTodo(id, newTodo)

                withContext(Dispatchers.Main) {
                    _state.update { state ->
                        state.copy(
                            data = state.data
                                .toMutableList()
                                .apply { this[index] = updatedTodo },
                            nonce = state.nonce + 1,
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e("ViewModel", "An error has occurred: ${e.message}")
            }
        }
    }

    // Создать новую задачу
    fun newTodo(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val todo = NewTodo(name)

            try {
                val createdTodo = TodoService.instance.api.createTodo(todo)

                withContext(Dispatchers.Main) {
                    _state.update { state ->
                        state.copy(
                            data = state.data
                                .toMutableList()
                                .apply { add(createdTodo) },
                            nonce = state.nonce + 1,
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e("ViewModel", "An error has occurred: ${e.message}")
            }
        }
    }

    // Удалить задачу
    fun deleteTodo(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                TodoService.instance.api.deleteTodo(id)

                withContext(Dispatchers.Main) {
                    _state.update { state ->
                        state.copy(
                            data = state.data
                                .toMutableList()
                                .apply {
                                    removeIf { it.id == id }
                                },
                            nonce = state.nonce + 1,
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "An error has occurred: ${e.message}")
            }
        }
    }
}