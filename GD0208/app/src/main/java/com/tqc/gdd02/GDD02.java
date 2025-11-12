package com.tqc.gdd02;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class GDD02 extends Activity
{
  public static boolean bIfDebug = false;
  public static String TAG = "HIPPO_DEBUG";

  private TextView mTextView01;
  private Button mButton01,mButton02;
  private EditText mEditText01;
  private HandlerThread handlerThread;
  private Handler mBackgroundHandler;
  private Handler mForegroundHandler;
  private boolean mPaused = false;
  private String strFileName = "https://upload.wikimedia.org/wikipedia/commons/1/1d/Android_P_logo.png";
  private static final int API_MSG_DOWNLOAD_START = 1001;
  private static final int API_MSG_DOWNLOAD_OK = 1002;
  private static final int API_MSG_DOWNLOAD_ERROR = 2001;
  private DownloadManager downloadManager;
  private long downloadReference;
  private static boolean bIfAllowDownload = false;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    mTextView01 = (TextView) findViewById(R.id.main_textView1);
    if(checkPermission(GDD02.this))
    {

    }
    else
    {
      mTextView01.setText(getResources().getString(R.string.err_need_permission));
    }
  }

  public static boolean checkPermission(final Activity activity)
  {
    if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    {
      ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.EXTRA_WRITE_STORAGE);
      bIfAllowDownload = false;
      return false;
    }
    bIfAllowDownload = true;
    return true;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
  {
    if(grantResults[0]== PackageManager.PERMISSION_GRANTED)
    {
      Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
      bIfAllowDownload = true;
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private void init()
  {
    downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
    mTextView01 = (TextView) findViewById(R.id.main_textView1);
    mButton01 = (Button) findViewById(R.id.main_button1);
    mButton02 = (Button) findViewById(R.id.main_button2);
    mEditText01 = (EditText) findViewById(R.id.main_editText1);
    mEditText01.setText(strFileName);
    handlerThread = new HandlerThread("BackgroundThread");
    handlerThread.start();
    MyHandlerCallback callback = new MyHandlerCallback();
    mBackgroundHandler = new Handler(handlerThread.getLooper(), callback);
    mForegroundHandler = new Handler(callback);

    mButton01.setOnClickListener(new Button.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        startDownload(strFileName);
      }
    });
    mButton02.setOnClickListener(new Button.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        if(bIfAllowDownload)
        {
          mTextView01.setText(getString(R.string.app_name));
          mEditText01.setText(strFileName);
        }
        else
        {
          mTextView01.setText(getString(R.string.err_need_permission));
        }
      }
    });
  }

  class MyHandlerCallback implements Handler.Callback
  {
    @Override
    public boolean handleMessage(final Message msg)
    {
      switch(msg.what)
      {
        case API_MSG_DOWNLOAD_START:
          mTextView01.setText(getString(R.string.str_downloading));
          break;
        case API_MSG_DOWNLOAD_OK:
          mTextView01.setText(getString(R.string.str_download_ok));
          break;
        case API_MSG_DOWNLOAD_ERROR:
          if (!mPaused)
          {
            if(msg.obj!=null)
            {
              Log.e(TAG, msg.obj.toString());
            }
          }
          break;
      }
      return false;
    }
  }

  private void startDownload(final String url)
  {
    mForegroundHandler.obtainMessage(API_MSG_DOWNLOAD_START, mEditText01.getText().toString()).sendToTarget();
    try
    {
      //  利用DownloadManager.Request下載傳入的圖片網址
      // TO DO
      DownloadManager.Request request = new
      DownloadManager.Request(Uri.parse(url));
      // TO DO
      request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
      request.setAllowedOverRoaming(false);
      request.setTitle("我的檔案下載管理員");
      request.setDescription("使用DownloadManager下載圖檔");
      request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "my.png"); //Download
      //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/GadgetSaint/"  + "/" + "Sample" + ".png");

      //  利用DownloadManager將建立的DownloadManager.Request加入下載佇列，並取得其下載識別碼(long型別)存於downloadReference變數中
      // TO DO
      downloadReference=downloadManager.enqueue(request);
      SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(GDD02.this);
      SharedPreferences.Editor editor = settings.edit();
      editor.putLong(Constants.EXTRA_KEY_DOWNLOAD_REFERENCE, downloadReference);
      editor.apply();
      finish();
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.e(TAG, e.toString());
    }
  }

  public void viewDownload()
  {
    Intent mView = new Intent();
    mView.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
    startActivity(mView);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings)
    {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    init();
    if(bIfAllowDownload)
    {
      mPaused = false;
      mButton01.setEnabled(true);
    }
    else
    {
      mButton01.setEnabled(false);
    }
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    if(bIfAllowDownload)
    {
      mPaused = true;
      //downloadManager.remove(downloadReference);
      mButton01.setEnabled(false);
    }
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();
    if(bIfAllowDownload)
    {
      mBackgroundHandler.removeMessages(API_MSG_DOWNLOAD_OK);
      mBackgroundHandler.getLooper().quit();
    }
  }
}
