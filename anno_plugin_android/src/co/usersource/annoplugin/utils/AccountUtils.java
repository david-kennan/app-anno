package co.usersource.annoplugin.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * Utilies to process account information.
 * 
 * @author leo
 * 
 */
public final class AccountUtils {

  /**
   * Get account whose name is the given one.
   * 
   * @param accountName
   * @param context
   * @return
   */
  public static Account getAccountByName(String accountName, Context context) {
    Account[] accounts = getAllAccounts(context, null);
    for (Account account : accounts) {
      if (account.name.equals(accountName)) {
        return account;
      }
    }
    return null;
  }

  /**
   * Get all accounts of a specified account type. by default, the account type
   * is com.google
   * 
   * @param context
   * @param accountType
   * @return
   */
  public static Account[] getAllAccounts(Context context, String accountType) {
    AccountManager accountManager = AccountManager.get(context);
    String type = accountType;
    if (type == null || type.isEmpty()) {
      type = Constants.ACCOUNT_TYPE_GOOGLE;
    }
    return accountManager.getAccountsByType(Constants.ACCOUNT_TYPE_GOOGLE);
  }
}
