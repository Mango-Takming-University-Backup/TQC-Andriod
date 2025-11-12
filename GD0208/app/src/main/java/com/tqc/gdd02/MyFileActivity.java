package com.tqc.gdd02;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.FileInputStream;

public class MyFileActivity extends Activity
{
  public static boolean bIfDebug = false;
  public static String TAG = "HIPPO_DEBUG";
  private ImageView mImageView01;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.my_file);

    init();
  }

  private void init()
  {
    mImageView01 = (ImageView)findViewById(R.id.my_file_imageView1);
    final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyFileActivity.this);
    if(settings.getString(Constants.EXTRA_KEY_DOWNLOAD_FILE_PATH,"").length()>1)
    {
      runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            //  修改MyFileActivity中ImageView的圖檔，當開啟Activity時，置換圖檔為已下載的圖檔路徑。
            // TO DO
            Uri uri=Uri.parse(settings.getString(Constants.EXTRA_KEY_DOWNLOAD_FILE_PATH,""));
            FileInputStream fin=new FileInputStream(uri.getPath());
            Drawable pic=Drawable.createFromStream(fin,"pic");
            mImageView01.setImageDrawable(pic);
           }
          catch(Exception e)
          {
            //java.io.FileNotFoundException: No entry for content://downloads/my_downloads/52
            e.printStackTrace();
            Log.e(TAG, e.toString());
          }

        }
      });
    }
  }



  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.my_file, menu);
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
}
