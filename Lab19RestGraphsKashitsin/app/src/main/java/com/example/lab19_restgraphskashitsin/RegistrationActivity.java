package com.example.lab19_restgraphskashitsin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import activity_helpers.ErrorActivity;

public class RegistrationActivity extends AppCompatActivity {

    EditText login;
    EditText pwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        login = findViewById(R.id.inputreg_login);
        pwd = findViewById(R.id.inputreg_pwd);
    }

    public void onCancel(View v)
    {
        finish();
    }

    public void onRegister(View v)
    {
        Request r = new Request()
        {
        public void onSuccess(String res) throws Exception {
        runOnUiThread(() -> {
        Toast.makeText(RegistrationActivity.this, "Успешная регистрация", Toast.LENGTH_SHORT).show();
        });
        }
            public void onFail(Exception ex) {
                runOnUiThread(()-> {
                    ErrorActivity.makeDialog(RegistrationActivity.this,ex, "Не удалось зарегистрироваться").show();
                });
            }
        };
        String name = login.getText().toString();
        String secret = pwd.getText().toString();
        r.send(this, "PUT", "/account/create?name=" + name + "&secret="+secret);
    }


}