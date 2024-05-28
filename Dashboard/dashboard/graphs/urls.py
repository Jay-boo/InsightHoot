from django.urls import path
from . import views

app_name = "graphs"
urlpatterns = [
    path('', views.home, name='home'),
    path('messages_by_topic/', views.messages_by_topic, name='messages_by_topic'),
    path('messages_by_tag/', views.messages_by_tag, name='messages_by_tag'),
    path('messages_with_tags/', views.messages_with_tags, name='messages_with_tags'),
]
