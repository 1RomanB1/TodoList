package com.br.todo

import android.content.res.Resources.NotFoundException
import android.util.Log
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class Todo(
    val id: Int,
    val name: String,
    val done: Boolean = false,
)

data class NewTodo(
    val name: String,
    val done: Boolean = false,
)

data class TodoList(
    val todos: List<Todo>,
)

class Empty

interface TodoApi {
    @GET("/api/todo/")
    suspend fun getTodos(): TodoList

    @POST("/api/todo/")
    suspend fun createTodo(@Body todo: NewTodo): Todo

    @GET("/api/todo/{id}/")
    suspend fun getTodo(@Path(value = "id", encoded = true) id: Int): Todo

    @PUT("/api/todo/{id}/")
    suspend fun updateTodo(
        @Path(value = "id", encoded = true) id: Int,
        @Body todo: NewTodo
    ): Todo

    @DELETE("/api/todo/{id}/")
    suspend fun deleteTodo(@Path(value = "id", encoded = true) id: Int): Empty
}

class FakeApi : TodoApi {
    private val todos = mutableListOf(
        Todo(0, "test"),
        Todo(1, "done", true),
        Todo(2, "not done", false),
    )

    override suspend fun getTodos(): TodoList {
        Log.d("FakeApi", "getTodos")
        delay(2000)

        return TodoList(todos)
    }

    override suspend fun createTodo(todo: NewTodo): Todo {
        Log.d("FakeApi", "createTodo ${todo.name} ${todo.done}")
        delay(500)

        val created = Todo(todos.last().id + 1, todo.name, todo.done)
        todos.add(created)
        return created
    }

    override suspend fun getTodo(id: Int): Todo {
        Log.d("FakeApi", "getTodo #$id")
        delay(500)

        val index = todos.indexOfFirst { item -> item.id == id }

        if (index < 0)
            throw NotFoundException()

        return todos[index]
    }

    override suspend fun updateTodo(id: Int, todo: NewTodo): Todo {
        Log.d("FakeApi", "updateTodo #$id -> ${todo.name} ${todo.done}")
        delay(500)

        val index = todos.indexOfFirst { item -> item.id == id }

        if (index < 0)
            throw NotFoundException()

        val created = Todo(id, todo.name, todo.done)

        todos[index] = created

        return created
    }

    override suspend fun deleteTodo(id: Int): Empty {
        Log.d("FakeApi", "deleteTodo #$id")
        delay(500)

        val index = todos.indexOfFirst { item -> item.id == id }

        if (index < 0)
            throw NotFoundException()

        todos.removeAt(index)

        return Empty()
    }

}

class TodoService private constructor() {
    companion object {
        const val baseUrl = "http://192.168.0.100:8000/"

        val instance: TodoService by lazy { TodoService() }
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: TodoApi = retrofit.create(TodoApi::class.java)
}
