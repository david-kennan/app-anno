package co.usersource.annoplugin.sync;

public interface AnnoHttpService {

  void getAnnoList(long offset, long limit, ResponseHandler respHandler);

  void getAnnoDetail(String annoId, ResponseHandler respHandler);

  void updateAppName(String annoId, String appName, ResponseHandler respHandler);

}
