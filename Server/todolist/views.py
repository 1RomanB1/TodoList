import json

from django.shortcuts import render
from django.http import JsonResponse
from django.views import View
from django.views.decorators.csrf import csrf_exempt
from django.utils.decorators import method_decorator
from .models import Todo


@method_decorator(csrf_exempt, name='dispatch')
class TodoListView(View):
    def get(self, request):
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
        data: dict = json.loads(request.body)
        todo = Todo.objects.create(name=data['name'], status=data['done'])
        return JsonResponse({
            "id": todo.id,
            "name": todo.name,
            "done": todo.status

        }, status=201)


@method_decorator(csrf_exempt, name='dispatch')
class TodoItemView(View):
    def get(self, request, todo_id):
        todo = Todo.objects.filter(id=todo_id).first()
        if todo is None:
            return JsonResponse({}, status=404)

        return JsonResponse({
            "id": todo.id,
            "name": todo.name,
            "done": todo.status
        })

    def put(self, request, todo_id):
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
        todo = Todo.objects.filter(id=todo_id).first()
        if todo is None:
            return JsonResponse({}, status=404)

        todo.delete()
        return JsonResponse({})


'''Rest API, принимает и отпарвляет данные в формате JSON.
Эндпоинты:
/api/todo
GET - Возвращает список задач в формате  (статус 200):
{
  "todos": [
    {
      "id": 12345,
      "name": "name",
      "done": false
    },
    ...
  ]
}
  
POST - Принимает данные и создаёт новую задачу. На входе:
{
  "name": "name",
  "done": false
}
На выходе  (статус 201):
{
  "id": 12345,
  "name": "name",
  "done": false
}

/api/todo/<id>
GET - возвращает данные задачи в формате (статус 200):
{
  "id": 12345,
  "name": "name",
  "done": false
}

  
PUT - обновляет данные задачи. На входе:
{
  "name": "name",
  "done": false
}

На выходе (статус 200):
{
  "id": 12345,
  "name": "name",
  "done": false
}

  
DELETE - удаляет выбранную задачу. Возвращает пустой ответ со статусом 200
Если задачи не существует, вернуть пустой ответ со статусом 404'''
