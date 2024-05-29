from django.shortcuts import render
from .serializers import MessageSerializer
from .models import Topics, Messages, MessageTags
from django.shortcuts import render
from django.db.models import Count 
from django.http import JsonResponse
from django.contrib.auth.decorators import login_required
from datetime import datetime, timedelta
# @login_required
def home(request):
    return render(request, 'graphs/home.html')

@login_required
def messages_by_topic(request):
    data = Messages.objects.values('topic__name') \
        .annotate(count_items=Count('id')) \
        .order_by('topic__name')
    return JsonResponse(list(data), safe=False)

# @login_required
def messages_by_tag(request):
    data = MessageTags.objects.values('tagid__label', 'tagid__theme').annotate(count_items=Count('messageid')).order_by('tagid__label')
    return JsonResponse(list(data), safe=False)

# @login_required
def messages_with_tags(request):
    period = request.GET.get('period', 'all')

    if period == 'last_7_days':
        start_date = datetime.now() - timedelta(days=7)
        messages = Messages.objects.filter(date__gte=start_date)
    elif period == 'last_month':
        start_date = datetime.now() - timedelta(days=30)
        messages = Messages.objects.filter(date__gte=start_date)
    elif period == 'last_3_months':
        start_date = datetime.now() - timedelta(days=90)
        messages = Messages.objects.filter(date__gte=start_date)        
    elif period == 'last_6_months':
        start_date = datetime.now() - timedelta(days=180)
        messages = Messages.objects.filter(date__gte=start_date)         
    else:
        messages = Messages.objects.all()
    serializer = MessageSerializer(messages, many=True)
    return JsonResponse(serializer.data, safe=False)  
