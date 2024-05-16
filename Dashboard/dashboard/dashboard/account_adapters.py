from allauth.account.adapter import DefaultAccountAdapter
from allauth.socialaccount.adapter import DefaultSocialAccountAdapter


class NoNewUsersAccountAdapter(DefaultAccountAdapter):

    def is_open_for_signup(self, request):
        return False


class MySocialAccountAdapter(DefaultSocialAccountAdapter):

    def is_open_for_signup(self, request, **kwargs):
        return True


