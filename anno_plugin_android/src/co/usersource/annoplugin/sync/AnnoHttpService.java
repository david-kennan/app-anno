package co.usersource.annoplugin.sync;

public interface AnnoHttpService {

  void getAnnoList(long offset, long limit, ResponseHandler respHandler);

  void getAnnoDetail(String annoId, ResponseHandler respHandler);

  void updateAppName(String annoId, String appName, ResponseHandler respHandler);

  void addFollowup(String annoId, String comment, ResponseHandler respHandler);

  void addFlag(String annoId, ResponseHandler respHandler);

  void addVote(String annoId, ResponseHandler respHandler);

  void countVote(String annoId, ResponseHandler respHandler);

  void countFlag(String annoId, ResponseHandler respHandler);

  void removeFlag(String annoId, ResponseHandler respHandler);

  void removeVote(String annoId, ResponseHandler respHandler);

  void removeFollowup(String followUpId, ResponseHandler respHandler);
}
