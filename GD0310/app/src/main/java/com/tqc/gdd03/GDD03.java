package com.tqc.gdd03;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GDD03 extends Activity
{
  public static boolean bIfDebug = false;
  public static String TAG = "HIPPO_DEBUG";
  private TextView mTextView01;
  private EditText mEditText01;
  private Button mButton01,mButton02;
  private ListView mListView01;
  private ArrayList<String> mList = new ArrayList<String>();
  private ArrayAdapter<String> adapter;

  // http://data.taipei/
  private String strURI =
          "https://data.taipei/api/v1/dataset/e7c46724-3517-4ce5-844f-5a4404897b7d?scope=resourceAquire";
 // private String strURI = "https://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=e7c46724-3517-4ce5-844f-5a4404897b7d";
  // The BroadcastReceiver that tracks network connectivity changes.
  private NetworkReceiver receiver = new NetworkReceiver();
  public static final String WIFI = "Wi-Fi";
  public static final String ANY = "Any";
  private static boolean wifiConnected = false;
  private static boolean mobileConnected = false;
  public static boolean refreshDisplay = true;
  public static String sPref = null;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    init();
  }

  private void init()
  {
    mTextView01 = (TextView) findViewById(R.id.main_textView1);
    mEditText01 = (EditText) findViewById(R.id.main_editText1);
    mButton01 = (Button) findViewById(R.id.main_button1);
    mButton02 = (Button) findViewById(R.id.main_button2);
    mListView01 = (ListView) findViewById(R.id.main_listView1);

    mEditText01.setText(strURI);
    mButton01.setOnClickListener(new Button.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        if(refreshDisplay)
        {
          mTextView01.setText(getString(R.string.str_parsing));
          new DownloadTask().execute(strURI);
        }
        else
        {
          mTextView01.setText(getString(R.string.connection_error));
        }
      }
    });

    mButton02.setOnClickListener(new Button.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        // 6. 按下「重設」按鈕，清除 ListView 與 上方TextView顯示為「台北市運動中心」文字。
       // TO DO
        mTextView01.setText(getString(R.string.app_name));
        if (mList != null && adapter != null) {
            mList.clear();
            adapter.notifyDataSetChanged();
        }
      }
    });
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
    if(id == R.id.action_settings)
    {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public class NetworkReceiver extends BroadcastReceiver
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      if (connMgr != null)
      {
        NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
        if(activeNetwork != null && activeNetwork.isConnected())
        {
          wifiConnected = activeNetwork.isConnected();
          mobileConnected = activeNetwork.isConnected();
        }
        else
        {
          wifiConnected = false;
          mobileConnected = false;
        }
      }
      else
      {
        wifiConnected = false;
        mobileConnected = false;
      }

      // 3. 變更網路狀態改變時，開啟網路以Toast顯示「WiFi已連線」，關閉網路時，以Toast顯示「失去網路連線」。
      // TO DO
        if (wifiConnected || mobileConnected) {
            refreshDisplay = true;
            Toast.makeText(GDD03.this, getString(R.string.wifi_connected), Toast.LENGTH_SHORT).show();
        }

      // 4. 關閉網路後 (當失去連線)，清除ListView內容
      // TO DO
        else {
            refreshDisplay = false;
            Toast.makeText(GDD03.this, getString(R.string.lost_connection), Toast.LENGTH_SHORT).show();
            if (mList != null && adapter != null) {
                mList.clear();
                adapter.notifyDataSetChanged();
            }
        }

    }
  }

  // Checks the network connection and sets the wifiConnected and mobileConnected
  // variables accordingly.
  private void updateConnectedFlags()
  {
    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connMgr != null)
    {
      NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
      if(activeNetwork != null && activeNetwork.isConnected())
      {
        wifiConnected = activeNetwork.isConnected();
        mobileConnected = activeNetwork.isConnected();
      }
      else
      {
        wifiConnected = false;
        mobileConnected = false;
      }
    }
    else
    {
      wifiConnected = false;
      mobileConnected = false;
    }
  }

  private class DownloadTask extends AsyncTask<String, Void, String>
  {
    @Override
    protected String doInBackground(String... urls)
    {
      return downloadUrl(urls[0]);
    }

    @Override
    protected void onPostExecute(String result)
    {
      mTextView01.setText(getString(R.string.str_parsing_ok));
      mList = new ArrayList<String>();
      try
      {
        //5. 處理Taipei Open Data台北市運動中心API JSON物件，顯示於ListView當中。
        // Hint: JSONArray與mList.add("")
        // TO DO
        JSONArray ja = new JSONObject(result).getJSONObject("result").getJSONArray("results");
        for (int i=0; i<ja.length(); i++) {
              JSONObject st = ja.getJSONObject(i);
              String str = st.getString("名稱") + "\n" + st.getString("地址");
              mList.add(str);
          }

        adapter = new ArrayAdapter<String>(GDD03.this, android.R.layout.simple_list_item_1, mList);
        mListView01.setAdapter(adapter);
        mListView01.setOnItemClickListener(new ListView.OnItemClickListener()
        {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id)
          {
            Toast.makeText(GDD03.this, getString(R.string.str_user_choose)+mList.get(position),Toast.LENGTH_SHORT).show();
          }
        });
      }catch(Exception e)
      {
        e.printStackTrace();
        Log.e(TAG, e.toString());
      }
    }
  }

  private String downloadUrl(String urlString)
  {
    String strHTML = "";
    try
    {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setReadTimeout(10000); // milliseconds
      conn.setConnectTimeout(15000);
      conn.setRequestMethod("GET");
      conn.setDoInput(true);
      conn.connect();
      InputStream stream = conn.getInputStream();

      if(stream != null)
      {
        int leng = 0;
        byte[] Data = new byte[100];
        byte[] totalData = new byte[0];
        int totalLeg = 0;
        do
        {
          leng = stream.read(Data);
          if(leng > 0)
          {
            totalLeg += leng;
            byte[] temp = new byte[totalLeg];
            System.arraycopy(totalData, 0, temp, 0, totalData.length);
            System.arraycopy(Data, 0, temp, totalData.length, leng);
            totalData = temp;
          }
        }
        while(leng > 0);
        // strReturn = new String(totalData,"UTF-8");
        strHTML = new String(totalData, "UTF-8");
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.e(TAG, e.toString());
    }
    return strHTML;
  }

  @Override
  protected void onStart()
  {
    super.onStart();
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    // Retrieves a string value for the preferences. The second parameter
    // is the default value to use if a preference value is not found.
    sPref = sharedPrefs.getString("listPref", "Wi-Fi");
    updateConnectedFlags();
  }

  @Override
  protected void onResume()
  {
    super.onResume();
      // 1. 主程式onResume()時，註冊一BroadcastReceiver (receiver物件)，捕捉一個事件："android.net.conn.CONNECTIVITY_CHANGE"，當發生網路連線事件或網路連線改變事件時，被自訂的NetworkReceiver()接收廣播訊息。
      IntentFilter itf = new IntentFilter();
      itf.addAction("android.net.conn.CONNECTIVITY_CHANGE");
      registerReceiver(receiver, itf);
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    // 2. 承1.離開程式呼叫onPause()，反註冊receiver物件。
    unregisterReceiver(receiver);
  }
}
