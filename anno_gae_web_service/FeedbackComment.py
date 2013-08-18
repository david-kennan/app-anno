'''
Created on Jun 13, 2013

@author: sergey
'''
from AnnoSyncEntity import AnnoSyncEntity
from google.appengine.ext import db
from datetime import datetime

class FeedbackComment(AnnoSyncEntity):
    
    JSON_SCREENSHOT_KEY = "screenshot_key"
    JSON_COMMENT = "comment"
    JSON_COORD_X = "x"
    JSON_COORD_Y = "y"
    JSON_DIRECTION = "direction"
    JSON_OBJECT_KEY = "object_key"
    JSON_UPDATE_TIME = "lastUpdateTimestamp"
    JSON_IMAGE = "image"
    JSON_APP_VERSION = "app_version"
    JSON_APP_NAME = "app_name"
    JSON_IS_MOVED = "isMoved"
    JSON_LEVEL = "level"
    JSON_OS_VERSION = "os_version"
    JSON_ANNO_TYPE = "anno_type"
    JSON_MODEL = "model"
    
    '''
    classdocs
    '''
    screenshot_key = db.StringProperty()
    comment = db.StringProperty()
    x = db.StringProperty()
    y = db.StringProperty()
    direction = db.StringProperty()
    image = db.BlobProperty()
    app_version = db.IntegerProperty()
    app_name = db.StringProperty()
    isMoved = db.IntegerProperty()
    level = db.IntegerProperty()
    os_version = db.IntegerProperty()
    anno_type = db.StringProperty()
    anno_model = db.StringProperty()

    def createComment(self, data):
        KeyForUpdate = db.get(data[self.JSON_OBJECT_KEY])
        if None != KeyForUpdate:
            self.updateExistingComment(KeyForUpdate, data)
        else:
            self.addNewComment(data)
            
    def addNewComment(self, data):
        self._key = db.get(data[self.JSON_OBJECT_KEY])
        for name, value in data.items():
            if name == self.JSON_IMAGE:
                setattr(self, name, str(value))
            else:
                setattr(self, name, value)
        self.put()
        
    def updateExistingComment(self, KeyForUpdate, data):
        if KeyForUpdate.lastUpdateTimestamp < datetime.strptime(data[self.JSON_UPDATE_TIME], "%Y-%m-%d %H:%M:%S"):
            KeyForUpdate.screenshot_key =  data[self.JSON_SCREENSHOT_KEY]
            KeyForUpdate.comment =  data[self.JSON_COMMENT]
            KeyForUpdate.x =  data[self.JSON_COORD_X]
            KeyForUpdate.y =  data[self.JSON_COORD_Y]
            KeyForUpdate.direction =  data[self.JSON_DIRECTION]
            KeyForUpdate.image =  data[self.JSON_IMAGE]
            KeyForUpdate.app_version =  data[self.JSON_APP_VERSION]
            KeyForUpdate.app_name  = data[self.JSON_APP_NAME]
            KeyForUpdate.isMoved = data[self.JSON_IS_MOVED]
            KeyForUpdate.level = data[self.JSON_LEVEL]
            KeyForUpdate.os_version = data[self.JSON_OS_VERSION]
            KeyForUpdate.anno_type = data[self.JSON_ANNO_TYPE]
            KeyForUpdate.anno_model = data[self.JSON_MODEL]
            KeyForUpdate.put()
    
    def generateKeys(self, count):
        result = []
        if(count > 0):
            baseKey = db.Key.from_path('FeedbackComment', 1)
            ids =  db.allocate_ids(baseKey, count)
            idsRange = (ids[0], ids[1] + 1)
        
            for item in range(0, count):
                result.append(str(db.Key.from_path('FeedbackComment', idsRange[item])))
        
        return result
    
    def getCommentsAfterDate(self, date):
        model = self.all()
        model.filter("updateTimestamp > ", datetime.strptime(date, "%Y-%m-%d %H:%M:%S:%f"))
        model.filter("userId = ", self.userId)
        result = []
        item = {}
        
        for datastoreObject in model.run():
            item = db.to_dict(datastoreObject)
            item[self.JSON_OBJECT_KEY] = str(datastoreObject.key())
            result.append(item.copy())
        
        return result
            
