'''
Created on Aug 29, 2013

@author: sergey
'''
import webapp2
import json
from google.appengine.ext import db
from utils.AnnoJsonEncoder import AnnoJsonEncoder
from FeedbackComment import FeedbackComment


class Community(webapp2.RequestHandler):
    
    def get(self):
        self.response.out.write(self.proceedRequest());
        
    def post(self):
        self.response.out.write(self.proceedRequest());
        
    def proceedRequest(self):
        requestData = self.request.get("id");
       
        if requestData != None and requestData != '':
            requestData = self.getItemById(requestData)
        else:
            requestData = self.getAllItems()
        
        return requestData
    
    def getItemById(self, itemID):
        result = {}
        anno = {}
        comments = []
        commentItem = {}
        try:
            item = db.get(itemID)
            if item != None:
                result["success"] = "true"
                anno["screenshot"] = item.image
                anno["circleX"] = item.x
                anno["circleY"] = item.y
                anno["deviceModel"] = item.model 
                anno["OSVersion"] = item.os_version
                commentItem["author"] = item.user.nickname()
                commentItem["comment"] = item.comment
                comments.append(commentItem)
                anno["comments"] = comments
            else:
                result["success"] = "false"
                result["message"] = "Item does not exist"
        except:
            result["success"] = "false"
            result["message"] = "Invalid item key"
       
        result["anno"] = anno
        
        return json.dumps(result, cls=AnnoJsonEncoder)
    
    def getAllItems(self):
        result = {}
        annos = []
        shortItem = {}
        
        model = FeedbackComment.all()
        for item in model.run():
            shortItem["id"] = str(item.key())
            shortItem["annoText"] = item.comment
            shortItem["app"] = item.app_name
            shortItem["author"] = item.user.nickname()
            annos.append(shortItem.copy())
        result["annos"] = annos
        return json.dumps(result, cls=AnnoJsonEncoder)
        