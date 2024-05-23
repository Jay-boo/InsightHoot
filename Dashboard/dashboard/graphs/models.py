from django.db import models

class Messages(models.Model):
    date = models.DateTimeField()
    content = models.TextField(blank=True, null=True)
    title = models.CharField(max_length=400)
    link = models.CharField(max_length=400)
    topic = models.ForeignKey('Topics', models.DO_NOTHING)

    class Meta:
        managed = True 
        db_table = 'messages'


class Tags(models.Model):
    label = models.CharField(max_length=255)
    theme = models.CharField(max_length=255)

    class Meta:
        managed = True
        db_table = 'tags'
        unique_together = (('label', 'theme'),)


class Topics(models.Model):
    name = models.CharField(max_length=255)
    url = models.CharField(unique=True, max_length=255)

    class Meta:
        managed = True
        db_table = 'topics'

class MessageTags(models.Model):
    messageid = models.OneToOneField('Messages', models.DO_NOTHING, db_column='messageid', primary_key=True)      
    tagid = models.ForeignKey('Tags', models.DO_NOTHING, db_column='tagid')

    class Meta:
        managed = True 
        db_table = 'message_tags'
        unique_together = (('messageid', 'tagid'),)
