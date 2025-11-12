package com.tqc.gdd01;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class GDD01 extends Activity
{
  private EditText etheight;
  private EditText etweight;
  private RadioButton rb1;
  private RadioButton rb2;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    Button b1 = (Button) findViewById(R.id.button1);
    b1.setOnClickListener(new Button.OnClickListener()
    {
      public void onClick(View v)
      {
        //從輸入介面中取出了的身高、體重值，要將身高、體重值傳送給 child_Activity 後作計算
        etheight=(EditText)findViewById(R.id.etHeight);
        etweight=(EditText)findViewById(R.id.etWeight);
        rb1=(RadioButton) findViewById(R.id.rbMale);
        rb2=(RadioButton) findViewById(R.id.rbFemale);
        double hh=Double.parseDouble(etheight.getText().toString());
        double ww=Double.parseDouble(etweight.getText().toString());
        String ss=rb1.isChecked() ? "M" : "F";
        //這些附加在 Intent 上的訊息都儲存在 Bundle 物件中
        Bundle bu=new Bundle();
        bu.putDouble("height",hh); bu.putDouble("weight",ww);bu.putString("Sex",ss);
        //透過「intent.putExtras(bundle)」敘述，將「bundle」 物件附加在 Intent 上，隨著 Intent 送出而送出
        Intent it=new Intent(GDD01.this,GDD01_child.class);
        it.putExtras(bu); startActivity(it);
      }
    });
  }

}
