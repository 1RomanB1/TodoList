import json

from django.test import TestCase, Client

from .models import Todo


class ListViewTestCase(TestCase):

    @classmethod
    def setUpTestData(cls):
        Todo.objects.create(name='task1', status=False)
        Todo.objects.create(name='task2', status=True)
        Todo.objects.create(name='task3', status=True)
        Todo.objects.create(name='task4', status=True)
        Todo.objects.create(name='task5', status=False)
        Todo.objects.create(name='task6', status=False)

    def test_get(self):
        c = Client()
        response = c.get('/api/todo/')
        data: dict = json.loads(response.content)
        self.assertIn('todos', data)
        self.assertEqual(len(data['todos']), 6)
        task1 = data['todos'][0]
        self.assertEqual(task1['name'], 'task1')
        self.assertEqual(task1['done'], False)

    def test_post(self):
        c = Client()
        response = c.post(
            '/api/todo/',
            json.dumps({'name': 'task7', 'done': True}),
            content_type='application/json',
        )
        self.assertEqual(response.status_code, 201)
        self.assertEqual(Todo.objects.count(), 7)


class ItemViewTestCase(TestCase):

    @classmethod
    def setUpTestData(cls):
        Todo.objects.create(id=0, name='task1', status=False)
        Todo.objects.create(id=1, name='task2', status=True)
        Todo.objects.create(id=2, name='task3', status=True)
        Todo.objects.create(id=3, name='task4', status=False)
        Todo.objects.create(id=4, name='task5', status=False)

    def test_get(self):
        c = Client()
        response = c.get('/api/todo/1/')
        task2 = json.loads(response.content)
        self.assertEqual(task2['id'], 1)
        self.assertEqual(task2['name'], 'task2')
        self.assertEqual(task2['done'], True)

    def test_put(self):
        c = Client()
        c.put(
            '/api/todo/4/',
            json.dumps({'name': 'updated', 'done': True}),
            content_type='application/json',
        )
        task = Todo.objects.get(id=4)
        self.assertEqual(task.name, 'updated')
        self.assertEqual(task.status, True)

    def test_delete(self):
        c = Client()
        response = c.delete('/api/todo/2/')
        self.assertEqual(Todo.objects.count(), 4)
        task = Todo.objects.filter(id=2).first()
        self.assertIsNone(task)
