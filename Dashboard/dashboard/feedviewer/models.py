from django.db import models

class Topic(models.Model):
    name = models.CharField(max_length=50)

class Message(models.Model):
    topic = models.ForeignKey(Topic, on_delete=models.CASCADE) 
    title = models.CharField(max_length=100)
    message = models.CharField(max_length=40000)
    pub_date = models.DateTimeField("date published")
