package com.example.lab19_restgraphskashitsin;

import android.app.Activity;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WaitingRequest extends Thread{
    public static String base;
    public Activity ctx;
    public String method;
    public String request;

    public WaitingRequest(Activity ctx, String method, String request)
    {
        this.ctx = ctx;
        this.method = method;
        this.request = request;
    }

    public void onSuccess(String res) throws Exception {

    }
    public void onFail(Exception ex)
    {
    }


    @Override
    public void run() {
        synchronized (this) {
            try {
                base = DB.helper.getCredsAddress();
                URL url = new URL(base + request);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(method);
                InputStream is = connection.getInputStream();
                BufferedInputStream input = new BufferedInputStream(is);
                byte[] buf = new byte[512];
                String str = "";
                while (true) {
                    int len = input.read(buf);
                    if (len < 0) break;
                    str += new String(buf, 0, len);
                }
                connection.disconnect();
                final String res = str;
                ctx.runOnUiThread(() -> {
                    try {
                        onSuccess(res);
                    } catch (Exception e) {
                        Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception ex) {
                ctx.runOnUiThread(() -> {
                    Toast.makeText(ctx, "Request failed!", Toast.LENGTH_SHORT).show();
                });
                onFail(ex);
            }
            notify();
        }
    }


}


