from django.shortcuts import render
from .models import Topics, Messages
from django.shortcuts import render
from django.db.models import Count 
from django.http import JsonResponse
from django.contrib.auth.decorators import login_required

def home(request):
    return render(request, 'graphs/home.html')

def messages_by_topic(request):
    data = Messages.objects.values('topic__name') \
        .annotate(count_items=Count('id')) \
        .order_by('topic__name')
    return JsonResponse(list(data), safe=False)
