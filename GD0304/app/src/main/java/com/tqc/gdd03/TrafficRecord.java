package com.tqc.gdd03;

import android.net.TrafficStats;

/**
 * Created by david.lanz on 2018
 */
class TrafficRecord
{
  long tx = 0;
  long rx = 0;
  String tag = null;

  // 裝置總共下載流量 tx為上行，rx為下行
  TrafficRecord()
  {
    // 1. 修改TrafficRecord.java，利用 TrafficStats 取得裝置的累計總流量。
    // TO DO

  }

  TrafficRecord(int uid, String tag)
  {

    // 2. 修改TrafficRecord.java，利用 TrafficStats 取得已安裝APP(UID)的累計總流量。
    // TO DO


    this.tag = tag;
  }
}
