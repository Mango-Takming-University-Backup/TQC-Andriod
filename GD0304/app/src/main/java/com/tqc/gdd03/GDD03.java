package com.tqc.gdd03;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.Observer;

public class GDD03 extends Activity
{
    public static boolean bIfDebug = false;
    public static String TAG = "HIPPO_DEBUG";
    private TextView mTextView01,mPrevious_rx,mPrevious_tx,mDelta_rx,mDelta_tx;
    private EditText mEditText01;
    private Button mButton01, mButton02;
    private ListView mListView01;
    private ArrayList<String> mList = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    TextView latest_rx=null;
    TextView latest_tx=null;
    TextView previous_rx=null;
    TextView previous_tx=null;
    TextView delta_rx=null;
    TextView delta_tx=null;
    TrafficSnapshot latest=null;
    TrafficSnapshot previous=null;

    // http://data.taipei/
    private String strURI =
            "https://data.taipei/api/v1/dataset/e7c46724-3517-4ce5-844f-5a4404897b7d?scope=resourceAquire";
    //private String strURI = "https://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=e7c46724-3517-4ce5-844f-5a4404897b7d";
    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver receiver = new NetworkReceiver();
    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";
    private static boolean wifiConnected = false;
    private static boolean mobileConnected = false;
    public static boolean refreshDisplay = true;
    public static String sPref = null;
    private static boolean bIfAllowDownload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        init();
        if(checkPermission(GDD03.this))
        {
            bIfAllowDownload = true;
        }
        else
        {
            bIfAllowDownload = false;
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
        mTextView01 = (TextView) findViewById(R.id.main_textView1);
        mEditText01 = (EditText) findViewById(R.id.main_editText1);
        mButton01 = (Button) findViewById(R.id.main_button1);
        mButton02 = (Button) findViewById(R.id.main_button2);
        mListView01 = (ListView) findViewById(R.id.main_listView1);
        mPrevious_rx =  (TextView) findViewById(R.id.previous_rx);
        mPrevious_tx=  (TextView) findViewById(R.id.previous_tx);
        mDelta_rx=  (TextView) findViewById(R.id.delta_rx);
        mDelta_tx=  (TextView) findViewById(R.id.delta_tx);

        mEditText01.setText(strURI);
        mButton01.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (refreshDisplay)
                {
                    if(bIfAllowDownload)
                    {
                        mTextView01.setText(getString(R.string.str_parsing));
                        new DownloadTask().execute(strURI);
                    }
                    else
                    {
                        mTextView01.setText(getString(R.string.err_need_permission));
                    }
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
                mTextView01.setText(getString(R.string.app_name));
                mList = new ArrayList<String>();
                adapter = new ArrayAdapter<String>(GDD03.this, android.R.layout.simple_list_item_1, mList);
                mListView01.setAdapter(adapter);
                //  TO DO 點選「重設」，清除前次紀錄及差異的資料



            }
        });
        mTextView01.setText(getString(R.string.app_name));
        mList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(GDD03.this, android.R.layout.simple_list_item_1, mList);
        mListView01.setAdapter(adapter);
        mListView01.setOnItemClickListener(new ListView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //  TO DO  點選運動中心選項，以Toast顯示被選中的運動中心名稱及地址，並重新進行解析



            }
        });


        latest_rx=(TextView)findViewById(R.id.latest_rx);
        latest_tx=(TextView)findViewById(R.id.latest_tx);
        previous_rx=(TextView)findViewById(R.id.previous_rx);
        previous_tx=(TextView)findViewById(R.id.previous_tx);
        delta_rx=(TextView)findViewById(R.id.delta_rx);
        delta_tx=(TextView)findViewById(R.id.delta_tx);
        takeSnapshot();
    }

    public void takeSnapshot()
    {
        // 修改主程式takeSnapshot()方法，計算 TrafficSnapshot 物件所記錄之最新裝置累計(latest_rx、latest_tx)、前次裝置累計(previous_rx、previous_tx)以及差異(delta_rx、delta_tx)等6個TextView。
        // TO DO




        ArrayList<String> log = new ArrayList<String>();
        HashSet<Integer> intersection = new HashSet<Integer>(latest.apps.keySet());

        if (previous != null)
        {
            intersection.retainAll(previous.apps.keySet());
        }

        for (Integer uid : intersection)
        {
            TrafficRecord latest_rec = latest.apps.get(uid);
            TrafficRecord previous_rec = (previous == null ? null : previous.apps.get(uid));
            emitLog(latest_rec.tag, latest_rec, previous_rec, log);
        }

        Collections.sort(log);

        for (String row : log)
        {
            Log.d("TrafficMonitor", row);
        }
    }

    private void emitLog(CharSequence name, TrafficRecord latest_rec, TrafficRecord previous_rec, ArrayList<String> rows)
    {
        if (latest_rec.rx > -1 || latest_rec.tx > -1)
        {
            StringBuilder buf = new StringBuilder(name);

            buf.append("=");
            buf.append(String.valueOf(latest_rec.rx));
            buf.append(" received");

            if (previous_rec != null)
            {
                buf.append(" (delta=");
                buf.append(String.valueOf(latest_rec.rx - previous_rec.rx));
                buf.append(")");
            }

            buf.append(", ");
            buf.append(String.valueOf(latest_rec.tx));
            buf.append(" sent");

            if (previous_rec != null)
            {
                buf.append(" (delta=");
                buf.append(String.valueOf(latest_rec.tx - previous_rec.tx));
                buf.append(")");
            }

            rows.add(buf.toString());
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
            // 按下F8變更網路狀態改變時，開啟網路以Toast顯示「WiFi已連線」，F8關閉網路時，以Toast顯示「失去網路連線」。
            // TO DO
            // Hint :
      /*
      if(已連線)
      {
        refreshDisplay = true;
      }
      else
      {
        refreshDisplay = false;
      }
      */

            // F8關閉網路後 (當失去連線)，清除ListView內容。
            // TO DO
            //Toast.makeText(GDD03.this, String.valueOf(wifiConnected), Toast.LENGTH_SHORT).show();




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

        public DownloadTask()
        {
            super();
        }

        @Override
        protected String doInBackground(String... urls)
        {
            return downloadUrl(urls[0]);
        }

        @Override
        protected void onPostExecute(String result)
        {
            //mTextView01.setText(getString(R.string.str_parsing_ok)+", result="+result);
            mList = new ArrayList<String>();
            try
            {
                JSONArray jsonArray = new JSONObject(result).getJSONObject("result").getJSONArray("results");
                if (jsonArray.length() > 0)
                {
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        mList.add(jsonArray.getJSONObject(i).getString("name") + "\n" + jsonArray.getJSONObject(i).getString("addr"));
                    }
                }
                adapter = new ArrayAdapter<String>(GDD03.this, android.R.layout.simple_list_item_1, mList);
                mListView01.setAdapter(adapter);
                takeSnapshot();
                mTextView01.setText(R.string.str_parsing_ok);
            }
            catch (Exception e)
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

            if (stream != null)
            {
                int leng = 0;
                byte[] Data = new byte[100];
                byte[] totalData = new byte[0];
                int totalLeg = 0;
                do
                {
                    leng = stream.read(Data);
                    if (leng > 0)
                    {
                        totalLeg += leng;
                        byte[] temp = new byte[totalLeg];
                        System.arraycopy(totalData, 0, temp, 0, totalData.length);
                        System.arraycopy(Data, 0, temp, totalData.length, leng);
                        totalData = temp;
                    }
                }
                while (leng > 0);
                // strReturn = new String(totalData,"UTF-8");
                strHTML = new String(totalData, "UTF-8");
            }
        }
        catch (Exception e)
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
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.STATE_CHANGE");

        //<action android:name="android.net.wifi.STATE_CHANGE" />
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        this.unregisterReceiver(receiver);
    }
}
