package model;

public class Link {
    public int id;
    public Node a, b;
    public float value;
    public Link(int id, Node a, Node b, float value)
    {
        this.id = id;
        this.a = a;
        this.b = b;
        this.value = value;
    }
}
