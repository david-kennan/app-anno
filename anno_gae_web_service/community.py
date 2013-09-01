'''
Created on Aug 29, 2013

@author: sergey
'''
import webapp2
import json
from google.appengine.ext import db
from utils.AnnoJsonEncoder import AnnoJsonEncoder
from model.FeedbackComment import FeedbackComment
from google.appengine.api import users
from model.FollowUp import FollowUp
from model.Flags import Flags
from model.Votes import Votes
import logging


class Community(webapp2.RequestHandler):
    
    ANNO_ID = "anno_id"
    JSON_REQUEST = "jsonRequest"
    SET_APP_NAME = "setName"
    OFFSET = "offset"
    LIMIT = "limit"
    FEEDBACK_KEY = "feedback_key"
    
    ACTION = "action"
    ADD_ACTION = "add"
    DELETE_ACTION = "delete"
    
    TYPE = "type"
    FOLLOWUP_TYPE = "followup"
    FOLLOWUP_COUNT_TYPE = "getFollowUpCount"
    
    FLAGS_COUNT_TYPE = "getFlagsCount"
    VOTES_COUNT_TYPE = "getVotesCount"
    VOTE_TYPE = "Vote"
    FLAG_TYPE = "Flag"
    
    def get(self):
        self.response.out.write(self.proceedRequest());
        
    def post(self):
        self.response.out.write(self.proceedRequest());
        
    def proceedRequest(self):
        response = {}
        jsonRequest =  self.request.get(Community.JSON_REQUEST)
        if jsonRequest != None and jsonRequest != '':
            logging.info("Request = " + jsonRequest)
            jsonData = json.loads(jsonRequest)
            response = self.proceedJson(jsonData)   
        else:       
            annoId = self.request.get(Community.ANNO_ID);
            if annoId != None and annoId != '':
                name = self.request.get(Community.SET_APP_NAME)
                if name != None and name != '':
                    response = self.updateAppName(annoId, name)
                else:
                    response = self.getItemById(annoId)
            else:
                offset = self.request.get(Community.OFFSET)
                limit = self.request.get(Community.LIMIT) 
                
                if offset.isdigit() and  limit.isdigit():      
                    response = self.getAllItems(int(offset), int(limit))
                else:
                    response = self.getResult(False, "Request is not supported")
                    
        logging.info("Response = " + json.dumps(response, cls=AnnoJsonEncoder))
        return json.dumps(response, cls=AnnoJsonEncoder)
    
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
                result = self.getResult(False, "Item does not exist")
                
        except:
            result = self.getResult(False, "Invalid item key")
       
        result["anno"] = anno
        
        return result
    
    def getAllItems(self, recOffset, recLimit):
        result = {}
        annos = []
        shortItem = {}
        
        model = FeedbackComment.all()
        for item in model.run(offset=recOffset, limit=recLimit):
            shortItem["id"] = str(item.key())
            shortItem["annoText"] = item.comment
            shortItem["app"] = item.app_name
            shortItem["author"] = item.user.nickname()
            annos.append(shortItem.copy())
        result["annos"] = annos
        
        return result
    
    def updateAppName(self, anno, name):
        com = FeedbackComment(user = users.get_current_user(), userId = users.get_current_user().user_id())
        return com.setAppName(anno, name)
    
    def proceedJson(self, data):
        result = {}
        
        if data[Community.TYPE] == Community.FOLLOWUP_TYPE:
            if data[Community.ACTION] == Community.DELETE_ACTION:
                feedback = db.get(data[Community.FEEDBACK_KEY])
                feedback.followups.filter("user = ", users.get_current_user())
                db.delete(feedback.flags.get())
                result = self.getResult(True, "")
            else:
                followUp = FollowUp(user = users.get_current_user(), 
                                    userId = users.get_current_user().user_id())
                result = followUp.AddNewFollowUp(data)
        
        elif data[Community.TYPE] == Community.FLAG_TYPE:
            if data[Community.ACTION] == Community.DELETE_ACTION:
                feedback = db.get(data[Community.FEEDBACK_KEY])
                feedback.flags.filter("user = ", users.get_current_user())
                db.delete(feedback.flags.get())
            else:    
                flag = Flags(user = users.get_current_user(), 
                             userId = users.get_current_user().user_id())
                result = flag.AddNewFlag(data)
        
        elif data[Community.TYPE] == Community.VOTE_TYPE:
            if data[Community.ACTION] == Community.DELETE_ACTION:
                feedback = db.get(data[Community.FEEDBACK_KEY])
                feedback.votes.filter("user = ", users.get_current_user())
                db.delete(feedback.flags.get())
                result = self.getResult(True, "")
            else:
                vote = Votes(user = users.get_current_user(), 
                         userId = users.get_current_user().user_id())
                result = vote.AddNewVote(data)
        else:
            result = self.getCountByType(data[Community.TYPE], data[Community.FEEDBACK_KEY])
            
        return result
    
    def getResult(self, flag, message):
        result = {}
        
        if flag == True:
            result["success"] = "true"
        else:
            result["success"] = "false"
            result["message"] = message
        
        return result
    
    def getCountByType(self, CountType, key):
        result = {}
        try:
            feedback = db.get(key)
            if feedback != None:
                result = self.getResult(True, "")
                if CountType == Community.FOLLOWUP_COUNT_TYPE:
                    result["followupCount"] = feedback.followups.count()
                elif CountType == Community.FLAGS_COUNT_TYPE:
                    result["flagsCount"] = feedback.flags.count()
                elif CountType == Community.VOTES_COUNT_TYPE:
                    result["votesCount"] = feedback.votes.count()
            else:
                result = self.getResult(False, "Feedback does not exists")
        except:
            result = self.getResult(False, "Unknown exception in getCountByType")
            
        return result
        