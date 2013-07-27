'''
Created on Jun 13, 2013

@author: sergey
'''
from google.appengine.ext import db


class AnnoSyncEntity(db.Model):
    '''
    classdocs
    '''
    updateTimestamp = db.DateTimeProperty(auto_now = True)
    user  = db.UserProperty()
    userId = db.StringProperty(required = True)   
        