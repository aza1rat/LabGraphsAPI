package com.example.lab19_restgraphskashitsin;

import java.util.ArrayList;

import model.Graph;
import model.Link;
import model.Node;

public class GraphHelper {
    public static String token;
    public static Graph selected;
    static ArrayList<Node> node = new ArrayList<Node>();
    static ArrayList <Link> link = new ArrayList<Link>();

    public void addNode(int id, float x, float y, String name)
    {
        node.add(new Node(id,x,y,name));
    }

    public void removeNode(int index)
    {
        if (index < 0) return;
        Node rmvNode = null;
        for (Node selnode : this.node)
        {
            if (selnode.id == index)
            {
                rmvNode = selnode;
                break;
            }
        }
        node.remove(rmvNode);
    }

    public Node getNode (int index)
    {
        if (index < 0) return null;
        for (Node selnode : this.node)
        {
            if (selnode.id == index)
                return selnode;
        }
        return null;
    }

    public Link Reverse(Link selected)
    {
        for (Link l : link)
        {
            if (l.b == selected.a)
                if (l.a == selected.b)
                    return l;
        }
        return null;

    }

    public void setNodeText (int index, String name)
    {
        if (index < 0) return;
        for (int i = 0; i < this.node.size(); i++)
        {
            Node n = this.node.get(i);
            if (n.id == index)
            {
                n.name = name;
                this.node.set(i,n);
                break;
            }
        }
    }

    public void setLinkValue(int index, float value)
    {
        if (index < 0) return;
        for (int i = 0; i < this.link.size(); i++)
        {
            Link l = this.link.get(i);
            if (l.id == index)
            {
                l.value = value;
                this.link.set(i,l);
                break;
            }
        }

    }

    public void removeLink(int index)
    {
        if (index < 0) return;
        Link rmvLink = null;
        for (Link sellink : this.link)
        {
            if (sellink.id == index)
            {
                rmvLink = sellink;
            }
        }
        this.link.remove(rmvLink);

    }

    public int getId(Node node)
    {
        if (node == null) return -1;
        for (int i = 0; i < this.node.size(); i++)
        {
            if (node == this.node.get(i))
                return i;
        }
        return -1;
    }

    public void addLink(int id, Node a,Node b, float value)
    {
        link.add(new Link(id,a,b,value));
    }
}
