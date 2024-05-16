from django.contrib import admin

from .models import Message, Topic

class MessageInline(admin.StackedInline):
    model = Message 

class TopicAdmin(admin.ModelAdmin):
    inlines = [MessageInline]

admin.site.register(Topic, TopicAdmin)
