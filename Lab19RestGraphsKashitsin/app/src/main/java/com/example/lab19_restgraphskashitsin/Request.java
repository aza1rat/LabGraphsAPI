package com.example.lab19_restgraphskashitsin;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Request {
    public static String base;
    public static Thread thread;
    public void onSuccess(String res) throws Exception {

    }
    public void onFail(Exception ex)
    {
    }

    public void send(Activity ctx, String method, String request)
    {
        base = DB.helper.getCredsAddress();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(base + request);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod(method);
                    connection.setConnectTimeout(5000);
                    InputStream is = connection.getInputStream();
                    BufferedInputStream input = new BufferedInputStream(is);
                    byte[] buf = new byte[512];
                    String str = "";
                    while (true)
                    {
                        int len = input.read(buf);
                        if (len < 0) break;
                        str += new String(buf,0,len);
                    }
                    connection.disconnect();
                    final String res = str;
                    ctx.runOnUiThread(() -> {
                        try { onSuccess(res); }
                        catch (Exception e) {Toast.makeText(ctx,e.getMessage(),Toast.LENGTH_LONG).show();}
                    });
                }
                catch (Exception ex)
                {
                    ctx.runOnUiThread(() -> {
                        Toast.makeText(ctx, "Request failed!", Toast.LENGTH_SHORT).show();
                    });
                    onFail(ex);
                }

            }
        };

        thread = new Thread(r);
        thread.start();
    }
}
