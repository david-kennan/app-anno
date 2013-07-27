'''
Created on Jun 13, 2013

@author: sergey
'''
import webapp2
from sync import AnnoSync

application = webapp2.WSGIApplication([('/', AnnoSync), ('/sync', AnnoSync)], debug=True)