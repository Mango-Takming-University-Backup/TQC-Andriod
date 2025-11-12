package com.tqc.gdd02;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class GDD02 extends Activity
{
  public static boolean bIfDebug = false;
  public static String TAG = "HIPPO_DEBUG";

  private TextView mTextView01;
  private Button mButton01,mButton02;
  private ListView mListView01;
  private ArrayList<String> lst = new ArrayList<String>();
  private ArrayAdapter<String> adapter;
  private static final int API_MSG_PARSE_START = 1001;
  private static final int API_MSG_PARSE_OK = 1002;
  private static final int API_MSG_PARSE_ERROR = 2001;
  private HandlerThread handlerThread;
  private Handler mBackgroundHandler;
  private Handler mForegroundHandler;
  private boolean mPaused = false;
  private String strFileName = "myjson.json";

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
    mButton01 = (Button) findViewById(R.id.main_button1);
    mButton02 = (Button) findViewById(R.id.main_button2);
    mListView01 = (ListView) findViewById(R.id.main_listView1);

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
        startParse(strFileName);
      }
    });
    mButton02.setOnClickListener(new Button.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        // 按下「重設」按鈕，清空下方ListView
        // TO DO
        if (lst != null && adapter != null) {
          lst.clear();
          adapter.notifyDataSetChanged();
        }
        mTextView01.setText(getString(R.string.app_name));
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
        case API_MSG_PARSE_START:
          // 1. mTextView01顯示解析中，請稍候
          // TO DO
          mTextView01.setText(getString(R.string.str_parsing));
          break;
        case API_MSG_PARSE_OK:
          mTextView01.setText(getString(R.string.str_parsing_ok));
          if (!mPaused)
          {
            try
            {
              // 6. 呼叫updateListView()方法，更新ListView內容。
              // TO DO
              updateListView((ArrayList<Station>) msg.obj);
            }
            catch(Exception e)
            {
              e.printStackTrace();
              Log.e(TAG, e.toString());
            }
          }
          break;
        case API_MSG_PARSE_ERROR:
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

  private void startParse(final String strFileName)
  {
    mForegroundHandler.obtainMessage(API_MSG_PARSE_START, strFileName).sendToTarget();
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          ArrayList<Station> stations = parseJSON(strFileName);
          // 5. 解析完畢，利用執行敘mForegroundHandler傳送訊息Message.what為API_MSG_PARSE_OK，並將 stations 傳入MyHandlerCallback處理。
          // TO DO
          mForegroundHandler.obtainMessage(API_MSG_PARSE_OK, stations).sendToTarget();
        }
        catch (Exception e)
        {
          mForegroundHandler.obtainMessage(API_MSG_PARSE_ERROR, e.toString()).sendToTarget();
          e.printStackTrace();
        }
      }
    }).start();
  }

  private ArrayList<Station> parseJSON(String strJsonFileName) throws JSONException
  {
    ArrayList<Station> stations = null;
    Station currentObject = null;
    try
    {
      // 2. 透過getAssets()將/assets/myjson.json檔案載入後，轉型為 InputStream 物件。
      InputStream is = getAssets().open("myjson.json"); // TO DO

      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      String strJSON = new String(buffer, "UTF-8");

      // 4. 解析JSON文件後回傳自訂ArrayList<Station>物件。
      /* Hint:
      currentObject = new Station();
      currentObject.origin = "起站名";
      currentObject.destination = "終站名";
      */
      JSONObject all = new JSONObject(strJSON);
      JSONArray ja = all.getJSONObject("result").getJSONArray("results");
      stations = new ArrayList<>();
      for (int i=0; i<ja.length(); i++) {
        JSONObject st = ja.getJSONObject(i);
        currentObject = new Station();
        currentObject.origin = st.getString("Station");
        currentObject.destination = st.getString("Destination");
        stations.add(currentObject);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.e(TAG, e.toString());
      return null;
    }
    // Delay for testing
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //
    return stations;
  }

  private void updateListView(ArrayList<Station> stations)
  {
    lst = new ArrayList<>();
    if(stations!=null && stations.size()>0)
    {
      for(int i = 0; i < stations.size(); i++)
      {
         // TO DO
        Station st = stations.get(i);
        String ss = String.format("%s->%s", st.origin, st.destination);
        lst.add(ss);
       }
      adapter = new ArrayAdapter<String>(GDD02.this, android.R.layout.simple_list_item_1, lst);
      mListView01.setAdapter(adapter);
      // 8. 為ListView設定點選選項時，將選中的品項以Toast訊息方式顯示於畫面中：「你選擇的是:xxx」
      // TO DO
      mListView01.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          Toast.makeText(GDD02.this, "你選擇的是" + lst.get(position), Toast.LENGTH_SHORT).show();
        }
      });
    }
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
    mPaused = false;
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    mPaused = true;
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();
    mBackgroundHandler.removeMessages(API_MSG_PARSE_OK);
    mBackgroundHandler.getLooper().quit();
  }
}
