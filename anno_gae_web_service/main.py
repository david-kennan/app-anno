'''
Created on Jun 13, 2013

@author: sergey
'''
import webapp2
from sync import AnnoSync
from ReverseProxyServer import ReverseProxyServer
from community import Community

application = webapp2.WSGIApplication([('/', AnnoSync),
                                       ('/community', Community), 
                                       ('/sync', AnnoSync)], debug=True)
application = ReverseProxyServer(application)