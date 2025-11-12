package com.tqc.gdd02;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by david.lanz on 2018.
 */
public class DownloadReceiver extends BroadcastReceiver
{
  public static boolean bIfDebug = false;
  public static String TAG = "HIPPO_DEBUG";
  private long downloadReference;

  @Override
  public void onReceive(Context context, Intent intent)
  {
    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

    downloadReference = settings.getLong(Constants.EXTRA_KEY_DOWNLOAD_REFERENCE, -1);
    if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    {
      long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
      if(downloadReference == referenceId)
      {
        DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        try
        {
          //  於DownloadReceiver.java捕捉下載完成廣播訊息，並利用DownloadManager與下載識別碼(long型別)取得其下載的Content URI路徑。
          // TO DO
          Uri uri = null; // TO DO

          String downloadPath = uri.toString();
          SharedPreferences.Editor editor = settings.edit();
          //  將已下載的Content URI路徑，轉成字串儲存至SharedPreferences變數Constants.EXTRA_KEY_DOWNLOAD_FILE_PATH中
          // TO DO
          editor.commit();
          Intent openIntent = new Intent(context, MyFileActivity.class);
          openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
          context.startActivity(openIntent);
        }
        catch(Exception e){}
      }
    }
  }
}
