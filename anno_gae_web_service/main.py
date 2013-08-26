'''
Created on Jun 13, 2013

@author: sergey
'''
import webapp2
from sync import AnnoSync
from ReverseProxyServer import ReverseProxyServer

application = webapp2.WSGIApplication([('/', AnnoSync), ('/sync', AnnoSync)], debug=True)
application = ReverseProxyServer(application)