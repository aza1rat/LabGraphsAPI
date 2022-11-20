package com.example.lab19_restgraphskashitsin;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import org.json.JSONObject;

import java.util.ArrayList;

import activity_helpers.ErrorActivity;

public class MainActivity extends AppCompatActivity {
    EditText loginValue;
    EditText pwdValue;
    CheckBox remember;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginValue = findViewById(R.id.input_login);
        pwdValue = findViewById(R.id.input_pwd);
        remember = findViewById(R.id.cb_remember);
        DB.helper = new DBHelper(this, "nodegraph.db", null, 1);
        ArrayList<String> loginCreds = new ArrayList<>();
        DB.helper.getCreds(loginCreds);
        if (loginCreds.size() == 0)
        {
            DB.helper.setCredsAddress("http://nodegraph.spbcoit.ru:5000");
        }

        else
        {
            if (loginCreds.get(1) != null)
            {
                loginValue.setText(loginCreds.get(0));
                pwdValue.setText(loginCreds.get(1));
                if (loginCreds.get(2) != null && isTokenExist(loginCreds.get(2)) == true)
                {
                    GraphHelper.token = loginCreds.get(2);
                    Intent intent = new Intent(MainActivity.this, GraphsListActivity.class);
                    startActivityForResult(intent,2);
                }
            }
        }


    }

    public void toRegisterActivity(View v)
    {
        Intent intent = new Intent(MainActivity.this,RegistrationActivity.class);
        startActivityForResult(intent, 1);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 2 && resultCode == 0)
            finish();
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onLogIn(View v)
    {
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                JSONObject obj = new JSONObject(res);
                GraphHelper.token = obj.getString("token");
                if (remember.isChecked())
                {
                    DB.helper.setCredsLoginPass(loginValue.getText().toString(),pwdValue.getText().toString());
                }
                else
                    DB.helper.delCredsLoginPass();
                DB.helper.setCredsToken(obj.getString("token"));
                Intent intent = new Intent(MainActivity.this, GraphsListActivity.class);
                startActivityForResult(intent,2);
            }
            public void onFail(Exception ex) {
                runOnUiThread(() -> {
                    ErrorActivity.makeDialog(MainActivity.this, ex, "Не удалось войти в аккаунт").show();
                });
            }
        };
        String name = loginValue.getText().toString();
        String secret = pwdValue.getText().toString();
        r.send(this, "PUT", "/session/open?name=" + name + "&secret="+secret);
    }

    public void onSettings(View v)
    {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.putExtra("ip",true);
        startActivity(intent);
    }

    private boolean isTokenExist(String token)
    {
        final boolean[] isExist = {true};
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                isExist[0] = true;
            }

            public void onFail(Exception ex) {
                isExist[0] = false;
                DB.helper.delCredsToken();
                runOnUiThread(()->{
                    ErrorActivity.makeDialog(MainActivity.this,ex,"Не удалось войти в аккаунт").show();
                });
            }
        };
        r.send(this,"GET","/session/list?token="+token);
        try {
            Request.thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return isExist[0];
    }
}