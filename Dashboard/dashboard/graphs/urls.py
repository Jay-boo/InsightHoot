from django.urls import path
from . import views

app_name = "graphs"
urlpatterns = [
    path('', views.home, name='home'),
    path('messages_by_topic/', views.messages_by_topic, name='messages_by_topic'),
]
