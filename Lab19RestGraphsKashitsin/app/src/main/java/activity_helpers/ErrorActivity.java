package activity_helpers;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.lab19_restgraphskashitsin.R;

public class ErrorActivity extends AppCompatActivity {
    public static TextView message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        message = findViewById(R.id.tv_error);
    }



    public static AlertDialog makeDialog(Activity ctx, Exception ex, String text)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        AlertDialog dlg = builder.create();
        dlg.setTitle("Возникла ошибка");
        LayoutInflater inflater =  ctx.getLayoutInflater();
        View errorView = inflater.inflate(R.layout.activity_error, null);
        message = errorView.findViewById(R.id.tv_error);
        message.setText(text);
        String errorText = ex.getMessage();
        if (errorText.contains("no protocol"))
            message.setText("В адресе неправильно указан протокол");
        if (errorText.contains("No address associated with hostname"))
            message.setText("Неправильно указан адрес или отсутствует подключение к сети");
        if (errorText.contains("Failed to connect"))
            message.setText("Не удается подключиться к серверу");
        dlg.setView(errorView);
        dlg.setCancelable(true);
        dlg.setCanceledOnTouchOutside(true);
        return dlg;
    }
}