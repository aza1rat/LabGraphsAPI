package model;

import java.util.Date;

public class Graph {
    public int id;
    public String name;
    public Date date;
    public int nodes;
    public boolean isOnServer;

    public Graph(int id, String name, Date date, int nodes, boolean isOnServer)
    {
        this.id = id;
        this.name = name;
        this.date = date;
        this.nodes = nodes;
        this.isOnServer = isOnServer;
    }
}
