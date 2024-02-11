from django.urls import path
from . import views

urlpatterns = [
    path('api/todo/', views.TodoListView.as_view()),
    path('api/todo/<int:todo_id>/', views.TodoItemView.as_view()),
]
