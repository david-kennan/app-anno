package co.usersource.annoplugin.sync;

public interface AnnoHttpService {

  void getAnnoList(long offset, long limit, ResponseHandler respHandler);

  void getAnnoDetail(long annoId, ResponseHandler respHandler);

  void updateAppName(long annoId, String appName, ResponseHandler respHandler);

}
