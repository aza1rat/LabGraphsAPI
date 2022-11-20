package com.example.lab19_restgraphskashitsin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InteractGraphActivity extends AppCompatActivity {
    GraphView gv;
    Button buttonOK;
    Button buttonCancel;
    EditText data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interact_gtaph);
        gv = findViewById(R.id.graphView);
        gv.setCtx(this);
        //локалка
    }

    public void onAddClick(View v)
    {
        gv.addNode();
    }

    public void onRemoveClick(View v)
    {
        gv.removeSelectedNodes();
    }

    public void onRemoveLink(View v)
    {
        gv.removeSelectedLink();
    }

    public void onLinkClick(View v)
    {
        gv.linkSelectedNodes();
    }

    public void onNodeName(View v)
    {
        AlertDialog dlg = makeDialog("Надпись узла", "Имя:");
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gv.setNameOfSelectedNode(data.getText().toString());
                dlg.cancel();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gv.setNameOfSelectedNode(null);
                dlg.cancel();
            }
        });
        dlg.show();
    }

    public void onLinkName(View v)
    {
        AlertDialog dlg = makeDialog("Надпись соединения", "Значение:");
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gv.setValueOfSelectedLink(data.getText().toString());
                dlg.cancel();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gv.setValueOfSelectedLink(null);
                dlg.cancel();
            }
        });
        dlg.show();
    }

    AlertDialog makeDialog (String str, String nameBox)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dlg = builder.create();
        dlg.setTitle(str);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.editgraph_layout, null);
        TextView tv = dialogView.findViewById(R.id.tv_field);
        data = dialogView.findViewById(R.id.input_edit);
        tv.setText(nameBox);
        buttonOK = dialogView.findViewById(R.id.btn_inputOK);
        buttonCancel = dialogView.findViewById(R.id.btn_inputCancel);
        dlg.setView(dialogView);
        return dlg;
    }
}