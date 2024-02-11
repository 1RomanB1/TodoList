package com.br.todo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Объект задачи
data class Todo(
    val id: Int,
    val name: String,
    val done: Boolean = false,
)

// Объект новой задачи
data class NewTodo(
    val name: String,
    val done: Boolean = false,
)

// Список задач
data class TodoList(
    val todos: List<Todo>,
)

// Пустой ответ
class Empty

// Интерфейс API серврера
interface TodoApi {
    // Получить список задач
    @GET("/api/todo/")
    suspend fun getTodos(): TodoList

    // Создать новую задачу
    @POST("/api/todo/")
    suspend fun createTodo(@Body todo: NewTodo): Todo

    // Получить задачу по id
    @GET("/api/todo/{id}/")
    suspend fun getTodo(@Path(value = "id", encoded = true) id: Int): Todo

    // Обновить задачу по id
    @PUT("/api/todo/{id}/")
    suspend fun updateTodo(
        @Path(value = "id", encoded = true) id: Int,
        @Body todo: NewTodo
    ): Todo

    // Удалить задачу по id
    @DELETE("/api/todo/{id}/")
    suspend fun deleteTodo(@Path(value = "id", encoded = true) id: Int): Empty
}

// Сервис списка задач для взаимодействия с сервером
class TodoService private constructor() {
    companion object {
        // Ссылка на сервер
        const val baseUrl = "http://192.168.0.100:8000/"

        // Экземпляр синглтона
        val instance: TodoService by lazy { TodoService() }
    }

    // Объект ретрофита для создания объекта API
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Объект API для выполнения запросов
    val api: TodoApi = retrofit.create(TodoApi::class.java)
}
