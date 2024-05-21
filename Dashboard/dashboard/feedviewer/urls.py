from django.urls import path

from . import views

app_name = "feedviewer"
urlpatterns = [
    path("", views.TopicView.as_view(), name="topics"),
    # ex: /polls/
    path("<str:topic_title>/", views.IndexView.as_view(), name="index"),
    # ex: /polls/5/
    path("messages/<pk>/", views.DetailView.as_view(), name="detail")
]
