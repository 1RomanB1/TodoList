import json

from django.http import JsonResponse
from django.views import View
from django.views.decorators.csrf import csrf_exempt
from django.utils.decorators import method_decorator
from .models import Todo


@method_decorator(csrf_exempt, name='dispatch')  # Отключение проверки csrf-токена
class TodoListView(View):
    """Представление списка задач"""

    def get(self, request):
        """Получение списка задач"""
        todo_list = Todo.objects.all()
        return JsonResponse({
            "todos": [
                {
                    "id": todo.id,
                    "name": todo.name,
                    "done": todo.status
                } for todo in todo_list
            ],
        })

    def post(self, request):
        """Добавление новой задачи в список задач"""
        data: dict = json.loads(request.body)   # Чтение тела запроса в формате json
        todo = Todo.objects.create(name=data['name'], status=data['done'])
        return JsonResponse({
            "id": todo.id,
            "name": todo.name,
            "done": todo.status

        }, status=201)


@method_decorator(csrf_exempt, name='dispatch')
class TodoItemView(View):
    """Представление конкретной задачи"""

    def get(self, request, todo_id):
        """Получение задачи"""
        todo = Todo.objects.filter(id=todo_id).first()  # Получение первого элемента списка по id, при наличии
        if todo is None:
            return JsonResponse({}, status=404)

        return JsonResponse({
            "id": todo.id,
            "name": todo.name,
            "done": todo.status
        })

    def put(self, request, todo_id):
        """Обновление задачи"""
        data: dict = json.loads(request.body)
        todo = Todo.objects.filter(id=todo_id).first()
        if todo is None:
            return JsonResponse({}, status=404)

        todo.name = data['name']
        todo.status = data['done']
        todo.save()

        return JsonResponse({
            "id": todo.id,
            "name": todo.name,
            "done": todo.status
        })

    def delete(self, request, todo_id):
        """Удаление задачи"""
        todo = Todo.objects.filter(id=todo_id).first()
        if todo is None:
            return JsonResponse({}, status=404)

        todo.delete()
        return JsonResponse({})
