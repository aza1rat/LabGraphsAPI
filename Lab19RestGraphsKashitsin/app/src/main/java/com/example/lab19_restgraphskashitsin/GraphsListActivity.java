package com.example.lab19_restgraphskashitsin;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import activity_helpers.ErrorActivity;
import activity_helpers.GraphAdapter;
import model.Graph;
import model.Link;
import model.Node;

public class GraphsListActivity extends AppCompatActivity {
    ListView graphList;
    ArrayList<Graph> graphs = new ArrayList<>();
    GraphAdapter adapter;
    EditText graphName;
    TextView graphNow;
    Graph selected;
    RadioButton rbLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);
        graphList = findViewById(R.id.list_graphs);
        graphName = findViewById(R.id.input_graphs_name);
        graphNow = findViewById(R.id.tv_graphs_now);
        rbLocal = findViewById(R.id.rb_local);
        adapter = new GraphAdapter(this,graphs);
        graphList.setAdapter(adapter);
        updateList();
        graphList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selected = (Graph) adapter.getItem(i);
                updateView(selected);
            }
        });
    }

    public void onExit(View v)
    {
        setResult(1);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 3 && resultCode == 500) {
            setResult(1);
            finish();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onFullSettings(View v)
    {
        Intent intent = new Intent(GraphsListActivity.this, SettingsActivity.class);
        intent.putExtra("ip",false);
        startActivityForResult(intent,3);
    }

    public void onCreateClick(View v)
    {
        if (rbLocal.isChecked())
        {
            localCreateGraph();
            return;
        }
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                updateList();
            }

            public void onFail(Exception ex) {
                runOnUiThread(()->{
                    ErrorActivity.makeDialog(GraphsListActivity.this,ex,"Не удалось создать новый граф, проверьте название или перезайдите в систему").show();
                });
            }
        };
        r.send(this,"PUT","/graph/create?token="+ GraphHelper.token+"&name="+graphName.getText().toString());
    }

    public void localCreateGraph()
    {
        DB.helper.graphAdd(graphName.getText().toString());
        updateList();
    }

    public void localDeleteGraph()
    {
        DB.helper.graphDelete(selected.id);
        updateList();
    }

    public void onCopyLink(View v)
    {
        if (selected.isOnServer == false)
        {
            LocalCopyGraph();
            return;
        }
        Request create = new Request()
        {
            public void onSuccess(String res) throws Exception {
                JSONObject object = new JSONObject(res);
                CopyNodes(object.getInt("id"));
            }

            public void onFail(Exception ex) {
                runOnUiThread(()->{
                    ErrorActivity.makeDialog(GraphsListActivity.this,ex,"Не удалось полностью скопировать граф, попробуйте позже").show();
                });
            }
        };
        create.send(this, "PUT","/graph/create?token="+GraphHelper.token+"&name="+selected.name);
    }

    public int getIdOfCopyLink(ArrayList<Integer[]> ids, int original)
    {
        for (Integer[] id : ids)
        {
            if (id[0] == original)
                return id[1];
        }
        return 0;
    }

    public void LocalCopyGraph()
    {
        ArrayList<Integer[]>ids = new ArrayList<Integer[]>();
        DB.helper.graphAdd(selected.name);
        int idGraph = DB.helper.getMaxId("graph");
        ArrayList<Node> nodes = DB.helper.nodeList(selected.id);
        for (Node copyNode : nodes)
        {
            Integer[]id = new Integer[2];
            DB.helper.nodeAdd(idGraph,copyNode.x,copyNode.y,copyNode.name);
            id[0] = copyNode.id;
            id[1] = DB.helper.getMaxId("node");
            ids.add(id);
        }
        ArrayList<String[]> links = DB.helper.linkList(selected.id);
        for (String[] copyLink : links)
        {
            int source = getIdOfCopyLink(ids,Integer.parseInt(copyLink[2]));
            int target = getIdOfCopyLink(ids,Integer.parseInt(copyLink[3]));
            DB.helper.linkAdd(idGraph,source,target,Float.valueOf(copyLink[4]));
        }
        updateList();
    }

    public void CopyNodes(int idGraph)
    {
        Request getNodes = new Request()
        {
            public void onSuccess(String res) throws Exception {
                JSONArray jsonArray = new JSONArray(res);
                Node[][] nodes = new Node[jsonArray.length()][2];
                for (int i=0;i<jsonArray.length();i++)
                {
                    JSONObject object = jsonArray.getJSONObject(i);
                    Node node = new Node(object.getInt("id"),
                            (float) object.getDouble("x"),
                            (float) object.getDouble("y"),
                            object.getString("name"));
                    nodes[i][0] = node;

                }
                InsertNodes(idGraph,nodes);
            }

            public void onFail(Exception ex) {
                runOnUiThread(()->{
                    ErrorActivity.makeDialog(GraphsListActivity.this,ex,"Не удалось полностью скопировать граф, попробуйте позже").show();
                });
            }
        };
        getNodes.send(this,"GET", "/node/list?token="+GraphHelper.token+"&id="+selected.id);
    }

    public void InsertNodes(int idGraph, Node[][] nodes)
    {
        for (int i = 0; i < nodes.length; i++)
        {
            insertNode(idGraph,nodes[i][0].x,nodes[i][0].y,nodes[i][0].name,nodes,i);
        }
        updateList();

    }

    public void insertNode(int idGraph,float x,float y, String name, Node[][] nodes, int i)
    {
        final int[] id = new int[1];
        WaitingRequest newNode = new WaitingRequest(this, "PUT",
                "/node/create?token="+GraphHelper.token+"&id="+idGraph+
                "&x="+x+"&y="+y+"&name="+name)
        {
            public void onSuccess(String res) throws Exception {
                JSONObject object = new JSONObject(res);
                id[0] = object.getInt("id");
                nodes[i][1] = new Node(id[0],nodes[i][0].x,nodes[i][0].y,nodes[i][0].name);
                if (i == nodes.length - 1)
                {
                    insertLinks(nodes);
                }
            }

            public void onFail(Exception ex) {
                runOnUiThread(()->{
                    ErrorActivity.makeDialog(GraphsListActivity.this,ex,"Не удалось полностью скопировать граф, попробуйте позже").show();
                });
            }
        };
        synchronized (newNode)
        {
            newNode.start();
            try {
                newNode.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void insertLinks(Node[][] nodes)
    {
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                JSONArray jsonArray = new JSONArray(res);
                for (int i = 0; i < jsonArray.length();i++)
                {
                    int source = 0;
                    int target = 0;
                    JSONObject object = jsonArray.getJSONObject(i);
                    for (int j = 0; j < nodes.length; j++)
                    {
                        if (nodes[j][0].id == object.getInt("source"))
                            source = nodes[j][1].id;
                        if (nodes[j][0].id == object.getInt("target"))
                            target = nodes[j][1].id;
                    }
                    insertLink(source,target,(float)object.getDouble("value"));
                }
            }

            public void onFail(Exception ex) {
                runOnUiThread(()->{
                    ErrorActivity.makeDialog(GraphsListActivity.this,ex,"Не удалось полностью скопировать граф, попробуйте позже").show();
                });
            }
        };
        r.send(this,"GET","/link/list?token="+GraphHelper.token+"&id="+selected.id);
    }

    public void insertLink(int source, int target, float value)
    {
        Request insert = new Request()
        {
            public void onSuccess(String res) throws Exception {

            }

            public void onFail(Exception ex) {
                runOnUiThread(()->{
                    ErrorActivity.makeDialog(GraphsListActivity.this,ex,"Не удалось полностью скопировать граф, попробуйте позже").show();
                });
            }
        };
        insert.send(this,"PUT","/link/create?token="+GraphHelper.token+
                "&source="+source+"&target="+target+"&value="+value);
        try {
            Request.thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void onRenameClick(View v)
    {
        if (selected == null)
            return;
        if (selected.isOnServer == false)
        {
            localRenameGraph();
            return;
        }
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                updateList();
            }

            public void onFail(Exception ex) {
                runOnUiThread(()->{
                    ErrorActivity.makeDialog(GraphsListActivity.this,ex,"Не удалось переименовать граф, проверьте название, или попробуйте позже").show();
                });
            }
        };
        r.send(this, "POST", "/graph/update?token="+ GraphHelper.token+"&id="+selected.id+"&name="+graphName.getText().toString());
        if (GraphHelper.selected == selected)
        {
            GraphHelper.selected = null;
            selected = null;
            updateView(selected);
        }

    }

    public void localRenameGraph()
    {
        DB.helper.graphUpdate(selected.id, graphName.getText().toString());
        updateList();
    }

    public void localLoadGraph()
    {
        ArrayList<Node> nodes = DB.helper.nodeList(GraphHelper.selected.id);
        for (Node node : nodes)
        {
            GraphHelper.node.add(node);
        }
        ArrayList<String[]> listLink = DB.helper.linkList(GraphHelper.selected.id);
        for (String[] linkStr : listLink)
        {
            Node source = null;
            Node target = null;
            for (Node node : nodes)
            {
                if (node.id == Integer.parseInt(linkStr[2]))
                    source = node;
                if (node.id == Integer.parseInt(linkStr[3]))
                    target = node;
            }
            Link link = new Link(
                    Integer.parseInt(linkStr[0]),
                    source, target,
                    Float.valueOf(linkStr[4])
            );
            GraphHelper.link.add(link);
        }
    }

    public void onLoadClick(View v)
    {
        if (selected == null)
            return;
        GraphHelper.selected = selected;
        GraphHelper.node.clear();
        GraphHelper.link.clear();
        if (GraphHelper.selected.isOnServer == false)
        {
            localLoadGraph();
        }
        else {
            Request r = new Request() {
                public void onSuccess(String res) throws Exception {
                    JSONArray responce = new JSONArray(res);
                    for (int i = 0; i < responce.length(); i++) {
                        JSONObject object = responce.getJSONObject(i);
                        Node node = new Node(object.getInt("id"),
                                (float) object.getDouble("x"),
                                (float) object.getDouble("y"),
                                object.getString("name"));
                        GraphHelper.node.add(node);
                    }
                }

                public void onFail(Exception ex) {
                    runOnUiThread(()->{
                        ErrorActivity.makeDialog(GraphsListActivity.this,ex,"Не удалось загрузить граф").show();
                    });
                }
            };

            r.send(this, "GET", "/node/list?token=" + GraphHelper.token + "&id=" + GraphHelper.selected.id);
            try {
                Request.thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Request r2 = new Request() {
                public void onSuccess(String res) throws Exception {
                    JSONArray responce = new JSONArray(res);
                    for (int i = 0; i < responce.length(); i++) {
                        JSONObject object = responce.getJSONObject(i);
                        Node idSource = null;
                        Node idTarget = null;
                        for (Node node : GraphHelper.node) {
                            if (node.id == object.getInt("source"))
                                idSource = node;
                            if (node.id == object.getInt("target"))
                                idTarget = node;
                        }
                        Link link = new Link(object.getInt("id"),
                                idSource, idTarget, (float) object.getDouble("value"));
                        GraphHelper.link.add(link);
                    }
                }

                public void onFail(Exception ex) {
                    runOnUiThread(()->{
                        ErrorActivity.makeDialog(GraphsListActivity.this,ex,"Не удалось загрузить граф").show();
                    });
                }
            };
            r2.send(this, "GET", "/link/list?token=" + GraphHelper.token + "&id=" + GraphHelper.selected.id);
            try {
                Request.thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent(GraphsListActivity.this, InteractGraphActivity.class);
        startActivity(intent);

    }

    public void onDeleteClick(View v)
    {
        if (selected == null)
            return;
        if (selected.isOnServer == false)
        {
            localDeleteGraph();
            return;
        }
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                updateList();
            }

            public void onFail(Exception ex) {
                runOnUiThread(()->{
                    ErrorActivity.makeDialog(GraphsListActivity.this,ex,"Не удалось удалить граф, попробуйте позже").show();
                });
            }
        };
        r.send(this,"DELETE","/graph/delete?token="+ GraphHelper.token+"&id="+selected.id);
        if (GraphHelper.selected == selected)
        {
            GraphHelper.selected = null;
            selected = null;
            updateView(selected);
        }
    }

    void updateList()
    {
        graphs.clear();
        adapter.notifyDataSetChanged();
        updateLocalList();
        getFromAPI();
    }

    void updateLocalList()
    {
        ArrayList<Graph> repeated = new ArrayList<>();
        for (Graph graph : graphs)
        {
            if (graph.isOnServer == false)
                repeated.add(graph);
        }
        graphs.removeAll(repeated);
        ArrayList<Graph> newGraphs = DB.helper.graphList();
        for (Graph graph : newGraphs)
        {
            graphs.add(graph);
        }
        adapter.notifyDataSetChanged();

    }

    void updateView(Graph graph)
    {
        if (graph == null)
            graphNow.setText("");
        else
            graphNow.setText("Выбрано : "+graph.name);
    }

    private void getFromAPI()
    {
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                JSONArray responce = new JSONArray(res);
                for (int i = 0; i < responce.length(); i++)
                {
                    JSONObject object = responce.getJSONObject(i);
                    int tssql = object.getInt("timestamp");
                    Date date = new Date((long) tssql * 1000);
                    Graph graph = new Graph(object.getInt("id"),
                            object.getString("name"),
                            date,
                            object.getInt("nodes"),true);
                    graphs.add(graph);
                }
                adapter.notifyDataSetChanged();

            }

            public void onFail(Exception ex) {
                runOnUiThread(()->{
                    ErrorActivity.makeDialog(GraphsListActivity.this,ex,"Не удалось загрузить все графы").show();
                });
            }
        };
        r.send(this, "GET", "/graph/list?token=" + GraphHelper.token);
    }
}