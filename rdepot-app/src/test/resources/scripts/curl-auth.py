#!/usr/bin/env python3
import sys
import subprocess


import datetime
import json
import os
import sys
import time
from pathlib import Path
from pprint import pprint

import requests


class Auth:

    def __init__(self):
        self.client_id = "device-code"
        self.dirname = os.path.join(Path.home(), ".cache", "crane-auth")
        os.makedirs(self.dirname, exist_ok=True)
        self.device_code_url = "http://192.168.49.21:8080/oauth2/device_authorization"
        self.token_url = "http://192.168.49.21:8080/oauth2/token"
        self.filename = os.path.join(self.dirname, "auth_dev.json")

        self._current_request_device_code = None
        self._current_request_interval = 5
        self._current_refresh_token = None
        self._current_refresh_token_not_after = None
        self._current_access_token = None
        self._current_access_token_not_after = None
        self.session = requests.Session()
        # self.session.auth = ("device-code", "secret2")

    def init(self):
        if self._read_cache():
            # loaded tokens form cache
            return True

        if not self._device_code_request():
            print("Device code request failed")
            sys.exit(1)

        if not self._start_polling():
            print("Polling request failed")
            sys.exit(1)

        self._cache_tokens()
        return True

    def get_access_token(self):
        self._refresh_access_token_if_required()
        # print(self._current_access_token)
        return self._current_access_token

    def _refresh_access_token_if_required(self):
        if self._current_access_token_not_after < datetime.datetime.now():
            self._refresh_access_token()
            self._cache_tokens()

    def _read_cache(self):
        if not os.path.exists(self.filename):
            return False
        with open(self.filename, "r") as fh:
            cache = json.load(fh)
            if "refresh_token" not in cache or "refresh_token_not_after" not in cache:
                return False
            # check refresh token still valid for at least 5 minutes
            not_after = datetime.datetime.now() + datetime.timedelta(minutes=5)
            refresh_token_not_after = datetime.datetime.fromtimestamp(cache['refresh_token_not_after'])
            if refresh_token_not_after < not_after:
                return False

            self._current_refresh_token = cache['refresh_token']
            self._current_refresh_token_not_after = datetime.datetime.fromtimestamp(cache['refresh_token_not_after'])

            # check access token still valid
            if "access_token" in cache and "access_token_not_after" in cache:
                access_token_not_after = datetime.datetime.fromtimestamp(cache['access_token_not_after'])
                if access_token_not_after > datetime.datetime.now():
                    self._current_access_token = cache['access_token']
                    self._current_access_token_not_after = access_token_not_after
                    print("You are now authenticated.")
                    return True

            if not self._refresh_access_token():
                return False
            self._cache_tokens()
            return True

    def _device_code_request(self):
        payload = {
            "client_id": self.client_id,
            "scope": "message.read"
        }

        headers = {
            'Content-Type': 'application/x-www-form-urlencoded'
        }

        response = self.session.request("POST", self.device_code_url, headers=headers, data=payload)

        print(response.json())
        json = response.json()
        print()
        print("Request URL:", response.request.url)
        print("Request Headers:", response.request.headers)
        print("Request Body:", response.request.body)

        if json is None or "error" in json:
            print(json)
            return False

        self._current_request_device_code = json["device_code"]
        if "interval" in json:
            self._current_request_interval = json["interval"]

        print(f"------------------------------")
        print(f"Please authenticate:")
        print(f"\tpoint your browser to: {json['verification_uri']}")
        print(f"\tand enter your user code: {json['user_code']}")
        if "verification_uri_complete" in json:
            print(f"\tor use the direct link: {json['verification_uri_complete']}")
        print(f"------------------------------""")
        return True

    def _start_polling(self):
        print("Waiting for authentication.", sep="", end="", flush=True)
        while True:
            time.sleep(self._current_request_interval)
            print(".", sep="", end="", flush=True)

            payload = {
                "grant_type": "urn:ietf:params:oauth:grant-type:device_code",
                "device_code": self._current_request_device_code,
                "client_id": self.client_id
            }

            headers = {
                'Content-Type': 'application/x-www-form-urlencoded'
            }

            response = self.session.request("POST", self.token_url, headers=headers, data=payload)
            json = response.json()

            if json is None:
                pprint(response)
                return False

            if "error" in json:
                if json["error"] == "authorization_pending":
                    continue
                else:
                    pprint(json)
                    return False

            now = datetime.datetime.now()

            self._current_access_token = json["access_token"]
            self._current_refresh_token = json["refresh_token"]
            self._current_access_token_not_after = now + datetime.timedelta(seconds=json["expires_in"])
            self._current_refresh_token_not_after = now + datetime.timedelta(seconds=60) # TODO
            # self._current_refresh_token_not_after = now + datetime.timedelta(seconds=json["refresh_expires_in"])

            print()
            print("You are now authenticated.")
            return True

    def _refresh_access_token(self):
        print("efresh_access_token")
        payload = {
            "grant_type": "refresh_token",
            "refresh_token": self._current_refresh_token,
            "client_id": self.client_id
        }

        headers = {
            'Content-Type': 'application/x-www-form-urlencoded'
        }

        response = self.session.request("POST", self.token_url, headers=headers, data=payload)
        json = response.json()
        if json is None or "error" in json:
            return False

        now = datetime.datetime.now()

        self._current_access_token = json["access_token"]
        self._current_refresh_token = json["refresh_token"]
        self._current_access_token_not_after = now + datetime.timedelta(seconds=json["expires_in"])
        self._current_refresh_token_not_after = now + datetime.timedelta(seconds=60)  # TODO
        # self._current_refresh_token_not_after = now + datetime.timedelta(seconds=json["refresh_expires_in"])

        print("You are now authenticated.")
        return True

    def _cache_tokens(self):
        cache = {
            "refresh_token": self._current_refresh_token,
            "refresh_token_not_after": self._current_refresh_token_not_after.timestamp(),
            "access_token": self._current_access_token,
            "access_token_not_after": self._current_access_token_not_after.timestamp()
        }
        with open(self.filename, "w") as fh:
            fh.write(json.dumps(cache))
        os.chmod(self.dirname, 0o700)
        os.chmod(self.filename, 0o600)

auth = Auth()
auth.init()
access_token = auth.get_access_token()
command = ["curl", "-H", f"Authorization: Bearer {access_token}" ] + sys.argv[1:]
subprocess.run(command)
