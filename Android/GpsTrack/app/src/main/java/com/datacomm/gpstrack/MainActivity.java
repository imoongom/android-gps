package com.datacomm.gpstrack;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    EditText nameEdit, ipEdit, portEdit;
    TextView errMsg;
    String nameStr, ipStr;
    int portNum;
    Button mapBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }



    public void init(){

        nameEdit = (EditText)findViewById(R.id.eName);
        ipEdit = (EditText)findViewById(R.id.eIP);
        portEdit = (EditText)findViewById(R.id.ePort);
        errMsg = (TextView)findViewById(R.id.errMsg);

        nameEdit.setText("");
        ipEdit.setText("");
        errMsg.setText("");

        mapBtn = (Button)findViewById(R.id.btn_gps);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, LocationActivity.class);
                errMsg.setText("");
                if(!checkInput()){
                    return;
                }
                i.putExtra("name", nameStr);
                i.putExtra("ip", ipStr);
                i.putExtra("port", portNum);
                startActivity(i);
            }
        });
    }

    private boolean checkInput(){
        nameStr = nameEdit.getText().toString();
        ipStr = ipEdit.getText().toString();
        portNum = Integer.parseInt(portEdit.getText().toString());
        Log.e("CHECK INOUT", "check start");
        if(nameStr.equals("")){
            errMsg.setText("Please fill out the form.");
            Log.e("CHECK INOUT", "emptyspace");
            return false;
        }
        if (!ipStr.equals("") && !validate(ipStr)) {
            errMsg.setText("Please check ip address.");
            Log.e("CHECK INOUT", "format check");
            return false;
        }else {
            String[] splits = ipStr.split("\\.");
            for (int i = 0; i < splits.length; i++) {
                if (Integer.valueOf(splits[i]) > 255) {
                    errMsg.setText("Please check ip address.");
                    Log.e("CHECK INOUT", "ip individual num check");
                    return false;
                }
            }
        }
        if(portNum <0 || portNum > 65546){
            errMsg.setText("Please check port number");
            return false;
        }
        return true;
    }

    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }
}
