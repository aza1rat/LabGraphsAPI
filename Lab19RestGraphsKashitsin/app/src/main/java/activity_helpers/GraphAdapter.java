package activity_helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lab19_restgraphskashitsin.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import model.Graph;

public class GraphAdapter extends BaseAdapter {
    Context ctx;
    ArrayList<Graph> graphs;
    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    public GraphAdapter(Context context, ArrayList<Graph> graphs)
    {
        this.ctx = context;
        this.graphs = graphs;
    }

    @Override
    public int getCount() {
        return graphs.size();
    }

    @Override
    public Object getItem(int i) {
        return graphs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Graph graph = graphs.get(i);
        view = LayoutInflater.from(ctx).inflate(R.layout.graphitem_layout, viewGroup, false);
        TextView name = view.findViewById(R.id.tv_name);
        TextView other = view.findViewById(R.id.tv_other);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3"));
        name.setText(graph.name);
        other.setText("Дата создания: " + dateFormat.format(graph.date) + "\nКол-во узлов: " + graph.nodes);
        if (graph.isOnServer)
        {
            ImageView image = view.findViewById(R.id.image_server);
            image.setImageResource(R.drawable.servericon);
        }
        return view;
    }


}
