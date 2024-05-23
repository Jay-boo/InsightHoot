from django.test import TestCase
from django.core.management import call_command
from .models import Messages, Topics, Tags, MessageTags
from django.urls import reverse
from django.utils import timezone
from django.test import Client
from django.contrib.auth.models import User

class MessagesTestCase(TestCase):
    
    databases = {'default', 'feeds'}


    def setUp(self):
        self.client = Client()
        self.user = User.objects.create_user("dio")
        Topics.objects.create(name="testtopic", url="dummyurl")
        Tags.objects.create(label="testlabel", theme="testtheme")
        
    def test_create_message(self):
        topic_test = Topics.objects.get(name="testtopic")
        tag_test = Tags.objects.get(label="testlabel", theme="testtheme")
        Messages.objects.create(date=timezone.now() ,content="This is a test content", title="Test Title", link="https://example.com", topic=topic_test)
        message_test = Messages.objects.get(content="This is a test content", topic=topic_test)
        self.assertEqual(message_test.content, "This is a test content")
        self.assertEqual(message_test.topic, topic_test)
        self.assertIsInstance(message_test, Messages)

    def test_messages_by_topic_logged_in(self):
        self.client.force_login(self.user)
        topic_test = Topics.objects.get(name="testtopic")
        tag_test = Tags.objects.get(label="testlabel", theme="testtheme")
        Messages.objects.create(date=timezone.now() ,content="This is a test content", title="Test Title", link="https://example.com", topic=topic_test)
        response =  self.client.get(reverse("graphs:messages_by_topic"))
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), [{'topic__name': topic_test.name, 'count_items': 1}])
