package com.example.lab19_restgraphskashitsin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import activity_helpers.ErrorActivity;
import model.Graph;
import model.Session;

public class SettingsActivity extends AppCompatActivity {
    LinearLayout otherSettings;
    LinearLayout inputServ;
    LinearLayout changeServ;
    ListView listSession;
    ArrayList<Session> sessions = new ArrayList<Session>();
    ArrayAdapter<Session> adapter;
    EditText address;
    EditText newpwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        otherSettings = findViewById(R.id.layout_setother);
        inputServ = findViewById(R.id.layout_inputserv);
        changeServ = findViewById(R.id.layout_chserv);
        listSession = findViewById(R.id.lv_sessions);
        address = findViewById(R.id.input_address);
        newpwd = findViewById(R.id.input_chpassword);
        Intent intent = getIntent();
        boolean showIpOnly = intent.getBooleanExtra("ip",true);
        if (showIpOnly)
        {
            otherSettings.setVisibility(View.GONE);
            inputServ.setVisibility(View.VISIBLE);
            changeServ.setVisibility(View.VISIBLE);
        }
        else
        {
            otherSettings.setVisibility(View.VISIBLE);
            inputServ.setVisibility(View.GONE);
            changeServ.setVisibility(View.GONE);
            adapter = new ArrayAdapter<Session>(this, android.R.layout.simple_list_item_1,sessions);
            listSession.setAdapter(adapter);
            updateSessions();
            listSession.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Session delSession = sessions.get(i);
                    String t1 = delSession.token;
                    String t2 = GraphHelper.token;
                    if (t1.equals(t2))
                    {
                        DB.helper.delCredsToken();
                        deleteSession(delSession.token);
                        GraphHelper.token = "";
                        setResult(500);
                        finish();
                    }
                    else
                        deleteSession(delSession.token);
                }
            });

        }
        String savedAddress = DB.helper.getCredsAddress();
        if (savedAddress != "")
            address.setText(savedAddress);


    }

    public void updateAdress(View v)
    {
        DB.helper.setCredsAddress(address.getText().toString());
    }

    public void updateToDefaultAdress(View v)
    {
        DB.helper.setCredsAddress("http://nodegraph.spbcoit.ru:5000");
    }

    public void changePassword(View v)
    {
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                DB.helper.delCredsToken();
                DB.helper.delCredsLoginPass();
                deleteSession(GraphHelper.token);
                GraphHelper.token = "";
                setResult(500);
                finish();
            }

            public void onFail(Exception ex) {
                runOnUiThread(()->{
                    ErrorActivity.makeDialog(SettingsActivity.this,ex,"Не удалось сменить пароль, перезайдите в систему еще раз").show();
                });
            }
        };
        r.send(this,"POST","/account/update?token="+GraphHelper.token+"&secret="+newpwd.getText().toString());
    }

    public void deleteAccount(View v)
    {
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                DB.helper.delCredsToken();
                DB.helper.delCredsLoginPass();
                GraphHelper.token = "";
                setResult(500);
                finish();
            }
            public void onFail(Exception ex) {
                runOnUiThread(()->{
                    ErrorActivity.makeDialog(SettingsActivity.this,ex,"Не удалось удалить аккаунт, перезайдите в систему еще раз").show();
                });
            }
        };
        r.send(this,"DELETE","/account/delete?token="+GraphHelper.token);
    }

    void updateSessions()
    {
        sessions.clear();
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                JSONArray responce = new JSONArray(res);
                for (int i = 0; i < responce.length(); i++)
                {
                    JSONObject object = responce.getJSONObject(i);
                    Session session = new Session(object.getString("token"),
                            object.getInt("timestamp"));
                    String t1 = session.token;
                    String t2 = GraphHelper.token;
                    if (t1.equals(t2))
                    {
                        session.isNow = true;
                    }
                    sessions.add(session);
                }
                adapter.notifyDataSetChanged();
            }

            public void onFail(Exception ex) {
                runOnUiThread(()-> {
                    ErrorActivity.makeDialog(SettingsActivity.this,ex,"Не удалось получить историю входов, попробуйте позже").show();
                });

            }
        };
        r.send(this,"GET", "/session/list?token="+GraphHelper.token);
    }

    void deleteSession(String token)
    {
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                if (token != GraphHelper.token)
                    updateSessions();
            }

            public void onFail(Exception ex) {
                runOnUiThread(()->{
                    ErrorActivity.makeDialog(SettingsActivity.this,ex,"Не удалось удалить, попробуйте позже").show();
                });
            }
        };
        r.send(this, "DELETE", "/session/close?token=" + token);
        try {
            Request.thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}