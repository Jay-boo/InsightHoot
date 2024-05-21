from django.shortcuts import render
from .models import Topics, Messages
from django.shortcuts import render
from django.db.models import Count 
from django.http import JsonResponse


def home(request):
    return render(request, 'graphs/home.html')

def messages_by_topic(request):
    data = Messages.objects.using("feeds").values('topic__title') \
        .annotate(count_items=Count('id')) \
        .order_by('topic__title')
    return JsonResponse(list(data), safe=False)
