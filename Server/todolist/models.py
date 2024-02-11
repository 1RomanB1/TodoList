from django.db import models


class Todo(models.Model):
    """Модель задачи списка дел"""
    name = models.CharField(max_length=100)
    status = models.BooleanField()
