package com.example.lab19_restgraphskashitsin;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;

import model.Graph;
import model.Node;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqlDB) {
        String sqlCreate = "CREATE TABLE creds (" +
                "id INTEGER NOT NULL," +
                "name VARCHAR (30) NULL," +
                "password VARCHAR(30) NULL," +
                "token VARCHAR(10) NULL," +
                "address VARCHAR(60) NULL );";
        sqlDB.execSQL(sqlCreate);

        sqlCreate =
                "CREATE TABLE graph (" +
                "id INTEGER NOT NULL," +
                "name VARCHAR(30) NOT NULL," +
                "timestamp timestamp DEFAULT (strftime('%s', 'now')) NOT NULL," +
                "PRIMARY KEY (id)" +
                ");";
        sqlDB.execSQL(sqlCreate);
        sqlCreate =
                "CREATE TABLE node (" +
                "id INTEGER NOT NULL," +
                "graph INTEGER NOT NULL," +
                "x FLOAT NOT NULL," +
                "y FLOAT NOT NULL," +
                "name VARCHAR(30) NOT NULL," +
                "PRIMARY KEY (id)," +
                "FOREIGN KEY (graph) REFERENCES graph (id) ON DELETE CASCADE" +
                ");";
        sqlDB.execSQL(sqlCreate);
        sqlCreate =
                "CREATE TABLE link (" +
                "id INTEGER NOT NULL," +
                "graph INTEGER NOT NULL," +
                "source INTEGER NOT NULL," +
                "target INTEGER NOT NULL," +
                "value FLOAT NOT NULL," +
                "PRIMARY KEY (id)," +
                "UNIQUE (source, target)," +
                "FOREIGN KEY (graph) REFERENCES graph (id) ON DELETE CASCADE," +
                "FOREIGN KEY (source) REFERENCES node (id) ON DELETE CASCADE," +
                "FOREIGN KEY (target) REFERENCES node (id) ON DELETE CASCADE);";
        sqlDB.execSQL(sqlCreate);
        sqlCreate = "PRAGMA foreign_keys = ON";
        sqlDB.execSQL(sqlCreate);
    }

    public int getMaxId(String table)
    {
        String sql = "SELECT MAX(id) FROM " + table + ";";
        SQLiteDatabase sqlDB = getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql,null);
        if (cursor.moveToFirst())
            return cursor.getInt(0);
        return 0;
    }

    public void setCredsLoginPass(String name, String password)
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        int n = getMaxId("creds");
        String sqlCom = "";
        if (n == 0)
            sqlCom = "INSERT INTO creds (id,name,password) VALUES (1,'"+name+"','"+password+"');";
        else
            sqlCom = "UPDATE creds SET name = '"+name+"',password ='"+password+"' WHERE id = 1;";
        sqlDB.execSQL(sqlCom);
    }

    public void delCredsLoginPass()
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        int n = getMaxId("creds");
        String sqlCom = "";
        if (n == 0)
            return;
        else
            sqlCom = "UPDATE creds SET name = NULL,password = NULL WHERE id = 1;";
        sqlDB.execSQL(sqlCom);
    }

    public void setCredsToken(String token)
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        int n = getMaxId("creds");
        String sqlCom = "";
        if (n == 0)
            sqlCom = "INSERT INTO creds (id,token) VALUES (1,'"+token+"');";
        else
            sqlCom = "UPDATE creds SET token = '"+token+"' WHERE id = 1;";
        sqlDB.execSQL(sqlCom);
    }

    public void delCredsToken()
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        int n = getMaxId("creds");
        String sqlCom = "";
        if (n == 0)
            return;
        else
            sqlCom = "UPDATE creds SET token = NULL WHERE id = 1;";
        sqlDB.execSQL(sqlCom);
    }

    public void setCredsAddress(String address)
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        int n = getMaxId("creds");
        String sqlCom = "";
        if (n == 0)
            sqlCom = "INSERT INTO creds (id,address) VALUES (1,'"+address+"');";
        else
            sqlCom = "UPDATE creds SET address = '"+address+"' WHERE id = 1;";
        sqlDB.execSQL(sqlCom);
    }

    public void delCredsAddress()
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        int n = getMaxId("creds");
        String sqlCom = "";
        if (n == 0)
            return;
        else
            sqlCom = "UPDATE creds SET address = NULL WHERE id = 1;";
        sqlDB.execSQL(sqlCom);
    }

    public void getCreds(ArrayList<String> list)
    {
        SQLiteDatabase sqlDB = getReadableDatabase();
        String sql = "SELECT * FROM Creds WHERE id = 1;";
        Cursor cursor = sqlDB.rawQuery(sql, null);
        if (cursor.moveToFirst())
        {
            do {
                list.add(cursor.getString(1));
                list.add(cursor.getString(2));
                list.add(cursor.getString(3));
                list.add(cursor.getString(4));
            }
            while (cursor.moveToNext());
        }

    }

    public String getCredsAddress()
    {
        SQLiteDatabase sqlDB = getReadableDatabase();
        String sql = "SELECT address FROM Creds WHERE id = 1;";
        Cursor cursor = sqlDB.rawQuery(sql, null);
        if (cursor.moveToFirst())
        {
            return cursor.getString(0);
        }
        return "";
    }

    public void graphAdd(String name)
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        int id = getMaxId("graph") + 1;
        String sqlCom = "INSERT INTO graph (id,name) VALUES ("+id+",'"+name+"');";
        sqlDB.execSQL(sqlCom);
    }

    public void nodeAdd(int graph, float x, float y, String name)
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        int id = getMaxId("node") + 1;
        String sqlCom = "INSERT INTO node VALUES ("+id+","+graph+","+x+","+y+",'"+name+"');";
        sqlDB.execSQL(sqlCom);
    }

    public void linkAdd(int graph, int source, int target, float value)
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        int id = getMaxId("link") + 1;
        String sqlCom = "INSERT INTO link VALUES ("+id+","+graph+","+source+","+target+","+value+");";
        sqlDB.execSQL(sqlCom);
    }

    public void graphDelete(int id)
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sqlCom = "DELETE FROM graph WHERE id = "+id + ";";
        sqlDB.execSQL(sqlCom);
        sqlCom = "DELETE FROM node WHERE graph = "+id+";";
        sqlDB.execSQL(sqlCom);
        sqlCom = "DELETE FROM link WHERE graph = "+id+";";
        sqlDB.execSQL(sqlCom);
    }

    public void nodeDelete(int id)
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sqlCom = "DELETE FROM node WHERE id = "+id+";";
        sqlDB.execSQL(sqlCom);
    }

    public void linkDelete(int id)
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sqlCom = "DELETE FROM link WHERE id = "+id+";";
        sqlDB.execSQL(sqlCom);
    }

    public ArrayList<Graph> graphList()
    {
        ArrayList<Graph> graphs = new ArrayList<Graph>();
        SQLiteDatabase sqlDB = getReadableDatabase();
        String sqlCom = "SELECT * FROM graph;";
        Cursor cursor = sqlDB.rawQuery(sqlCom, null);
        if (cursor.moveToFirst())
        {
            do {
                Date date = new Date((long) cursor.getInt(2) * 1000);
                Graph graph = new Graph(
                        cursor.getInt(0),
                        cursor.getString(1),
                        date,
                        0, false
                );
                graphs.add(graph);

            }
            while (cursor.moveToNext());
        }
        return graphs;

    }

    public ArrayList<Node> nodeList(int graph)
    {
        ArrayList<Node> nodes = new ArrayList<Node>();
        SQLiteDatabase sqlDB = getReadableDatabase();
        String sqlCom = "SELECT * FROM node WHERE graph = "+graph + ";";
        Cursor cursor = sqlDB.rawQuery(sqlCom,null);
        if (cursor.moveToFirst())
        {
            do {
                Node node = new Node(cursor.getInt(0),
                        cursor.getFloat(2),
                        cursor.getFloat(3),
                        cursor.getString(4));
                nodes.add(node);
            }
            while (cursor.moveToNext());
        }
        return nodes;
    }

    public ArrayList<String[]> linkList(int graph)
    {
        ArrayList<String[]> links = new ArrayList<String[]>();
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sqlCom = "SELECT * FROM link WHERE graph = "+graph+";";
        Cursor cursor = sqlDB.rawQuery(sqlCom,null);
        if (cursor.moveToFirst())
        {
            do {
                String[] link = new String[5];
                link[0] = String.valueOf(cursor.getInt(0));
                link[1] = String.valueOf(cursor.getInt(1));
                link[2] = String.valueOf(cursor.getInt(2));
                link[3] = String.valueOf(cursor.getInt(3));
                link[4] = String.valueOf(cursor.getFloat(4));
                links.add(link);
            }
            while (cursor.moveToNext());
        }
        return links;
    }


    public void graphUpdate(int id, String name)
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sqlCom = "UPDATE graph SET name = '"+name+"' WHERE id = "+id + ";";
        sqlDB.execSQL(sqlCom);
    }

    public void nodeUpdate(int id, float x, float y, String name)
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sqlCom = "UPDATE node SET id = "+id+",x = "+x+",y = "+y+",name = '"+name+"' WHERE id = "+id+";";
        sqlDB.execSQL(sqlCom);
    }

    public void linkUpdate(int id, float value)
    {
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sqlCom = "UPDATE link SET value = "+value+" WHERE id = "+id+";";
        sqlDB.execSQL(sqlCom);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
