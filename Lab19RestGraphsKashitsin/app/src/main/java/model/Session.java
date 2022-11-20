package model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Session {
    public String token;
    public Date time;
    public boolean isNow = false;

    public Session(String token, int timestamp)
    {
        long unix = TimeUnit.SECONDS.toMillis(timestamp);
        this.time = new Date(unix);
        this.token = token;
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3"));
        if (isNow)
            return dateFormat.format(time) + " (Текущая)";
        else
            return dateFormat.format(time);
    }
}
