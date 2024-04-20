from setuptools import setup, find_packages
import sys, os

def read(*paths):
    """Build a file path from *paths* and return the contents."""
    with open(os.path.join(*paths), 'r') as f:
        return f.read()
setup(
  name = 'coconutpy',
  version = '2.2.1',
  py_modules = ['coconut.job', 'coconut.config'],
  packages=find_packages(exclude=['tests*']),
  author='Bruno Celeste',
  author_email='bruno@coconut.co',
  description='A python wrapper around the Coconut API',
  license='MIT License',
  url='http://coconut.co',
  keywords='coconut video encoding api',
  install_requires=[ "httplib2" ],
	long_description="""Client Library for encoding Videos with Coconut

Coconut is a Video Encoding Web Service built for developers.

For more information:

* Coconut: http://coconut.co
* API Documentation: http://coconut.co/docs
* Twitter: @openCoconut

Changelogs

2.2.0
Added a new method #config to generate a full configuration based on the given parameters. It's especially useful to handle dynamic settings like source or variables that can be set directly in code.

2.0.0
New version of the client library which uses the HeyWatch API v2. This library is not compatible with 1.x

1.0.0
First version

"""
)