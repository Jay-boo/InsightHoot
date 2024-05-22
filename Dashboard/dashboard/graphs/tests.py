from django.test import TestCase
from django.core.management import call_command
from .models import Messages, Topics, Tags


class MessagesTestCase(TestCase):
    
    databases = {'feeds'}


    def setUp(self):
        call_command('migrate', database='feeds', run_syncdb=True)
        Topics.objects.using("feeds").create(title="testtopic")
        Tags.objects.using("feeds").create(label="testlabel", theme="testtheme")
        
    def test_create_message(self):
        topic_test = Topics.objects.using("feeds").get(title="testtopic")
        tag_test = Tags.objects.using("feeds").get(label="testlabel", theme="testtheme")
        Messages.objects.using("feeds").create(content="This is a test content", topic=topic_test, tag=tag_test)
        message_test = Messages.objects.using("feeds").get(content="This is a test content", topic=topic_test, tag=tag_test)
        self.assertEqual(message_test.content, "This is a test content")
        self.assertEqual(message_test.topic, topic_test)
        self.assertEqual(message_test.tag, tag_test)
        self.assertIsInstance(message_test, Messages)
