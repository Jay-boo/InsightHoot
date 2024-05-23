from django.shortcuts import render
from django.views import generic
from django.utils import timezone
from django.contrib.auth.mixins import LoginRequiredMixin
from graphs.models import Messages, Topics

class IndexView(LoginRequiredMixin,generic.ListView):
    template_name = "feedviewer/index.html"
    context_object_name = "latest_message_list"

    def get_queryset(self):
        topic_name = self.kwargs['topic_name']
        return Messages.objects.filter(topic__name=topic_name)

class DetailView(LoginRequiredMixin, generic.DetailView):
    model = Messages
    template_name = "feedviewer/detail.html"

class TopicView(LoginRequiredMixin,generic.ListView):
    template_name = "feedviewer/index_topics.html"
    context_object_name = "topics_list"

    def get_queryset(self):
        return Topics.objects.order_by("name")
