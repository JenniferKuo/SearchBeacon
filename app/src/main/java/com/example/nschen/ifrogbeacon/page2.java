package com.example.nschen.ifrogbeacon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class page2 extends AppCompatActivity {
    private Button button1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page2);

        Bundle bundle= this.getIntent().getExtras();    // 取得第一頁傳遞過來的資料。
        int t_value=bundle.getInt("intvlaue");
        String t_web=bundle.getString("Web");
        String t_data=bundle.getString("data");
        TextView textView1=(TextView) findViewById(R.id.textView1 );   //取得TextView
        textView1.setText(t_data);
        button1=(Button) findViewById(R.id.button1  );          //取得button1
        button1.setOnClickListener(new View.OnClickListener(){       // 監聽按鍵動作
            @Override
            public void onClick(View v) {
                FunBack();
            }}
        );
    }

    @Override                                              //用戶按下android 機器上的back 按鍵。
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {   //查用戶按下back 按鍵 ?
            FunBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);     // 如果其他按鍵交給android
    }

    private void FunBack(){
        CheckBox Apple = (CheckBox) findViewById(R.id.checkBox1);  //取得選項元件
        CheckBox Orange = (CheckBox) findViewById(R.id.checkBox2);
        CheckBox Watermelon = (CheckBox) findViewById(R.id.checkBox3);

        Intent intent=new Intent();              //準備將資料傳遞回去
        Bundle bundle=new Bundle();
        bundle.putBoolean("Apple", Apple.isChecked());
        bundle.putBoolean("Orange", Orange.isChecked());
        bundle.putBoolean("Watermelon", Watermelon.isChecked());
        bundle.putString("MESSAGE", "hi!");
        intent.putExtras(bundle);
        setResult(RESULT_OK,intent);                // 傳遞資料回上一頁
        finish();
    }

}
