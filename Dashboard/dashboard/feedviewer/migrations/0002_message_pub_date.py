# Generated by Django 5.0.6 on 2024-05-14 13:01

import datetime
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('feedviewer', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='message',
            name='pub_date',
            field=models.DateTimeField(default=datetime.datetime(2024, 5, 14, 13, 1, 12, 620784, tzinfo=datetime.timezone.utc), verbose_name='date published'),
            preserve_default=False,
        ),
    ]