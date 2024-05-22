import unittest

from django.test import TestCase
from django.test import Client
from django.contrib.auth.models import User

class SimpleTest(TestCase):
    databases = {'default'}

    def setUp(self) -> None:
        self.client = Client()
        self.user = User.objects.create_user("dio")

    def test_not_logged_in(self):
        response = self.client.get("/feedviewer/")
        self.assertEqual(response.status_code, 302)

    def test_logged_in(self):
        self.client.force_login(self.user)
        response = self.client.get("/feedviewer/")
        self.assertEqual(response.status_code, 200)


        
