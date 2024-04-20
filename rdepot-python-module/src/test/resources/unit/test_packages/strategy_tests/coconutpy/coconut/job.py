import json
import os
import httplib2
import config

USER_AGENT = 'Coconut/2.2.0 (Python)'

def submit(config_content, **kwargs):
  heywatch_url = os.getenv('COCONUT_URL', 'https://api.coconut.co')
  api_key = os.getenv('COCONUT_API_KEY')

  if 'api_key' in kwargs:
    api_key = kwargs['api_key']

  h = httplib2.Http()
  h.add_credentials(api_key, '')

  headers = {'User-Agent': USER_AGENT, 'Content-Type': 'text/plain', 'Accept': 'application/json'}

  response, content = h.request(heywatch_url + '/v1/job', 'POST', body=config_content, headers=headers)

  return json.loads(content.decode('utf-8'))

def create(**kwargs):
  return submit(config.new(**kwargs), **kwargs)
