from django.db import models

class Messages(models.Model):
    content = models.TextField()
    topic = models.ForeignKey('Topics', models.DO_NOTHING)
    tag = models.ForeignKey('Tags', models.DO_NOTHING)

    class Meta:
        managed = True 
        db_table = 'messages'


class Tags(models.Model):
    label = models.CharField(max_length=255)
    theme = models.CharField(max_length=255)

    class Meta:
        managed = False
        db_table = 'tags'


class Topics(models.Model):
    title = models.CharField(max_length=255)

    class Meta:
        managed = True
        db_table = 'topics'
