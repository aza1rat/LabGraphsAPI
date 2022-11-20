package com.example.lab19_restgraphskashitsin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

import activity_helpers.ErrorActivity;
import model.Link;
import model.Node;

public class GraphView extends SurfaceView {
    GraphHelper helper = new GraphHelper();
    Paint p;
    int selected1 = -1;
    int selected2 = -1;
    int lasthit =-1;
    int selectedLink =-1;
    float rad = 70.0f;
    float halfside = 30.0f;
    float lastX;
    float lastY;
    float prevX;
    float prevY;
    Activity ctx;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        p = new Paint();
        p.setAntiAlias(true);
        setWillNotDraw(false);
    }

    public void setCtx(Activity activity)
    {
        this.ctx = activity;
    }

    public void addNode()
    {
        if (GraphHelper.selected.isOnServer)
            APIaddNode();
        else
            LocalAddNode();
        invalidate();
    }

    public  void LocalAddNode()
    {
        try {
            DB.helper.nodeAdd(
                    helper.selected.id,
                    100.0f, 100.0f, "");
            Node node = new Node(DB.helper.getMaxId("node"),
                    100.0f, 100.0f, "");
            helper.node.add(node);
            invalidate();

        }
        catch (Exception e)
        {
            return;
        }
    }

    public void APIaddNode()
    {
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                JSONObject obj = new JSONObject(res);
                Node node = new Node(obj.getInt("id"),
                        100.0f,
                        100.0f,
                        "");
                helper.node.add(node);
                invalidate();
            }

            public void onFail(Exception ex) {
                ctx.runOnUiThread(()->{
                    ErrorActivity.makeDialog(ctx,ex,"Не удалось добавить точку").show();
                });
            }
        };
        r.send(ctx,"PUT", "/node/create?token="+ GraphHelper.token+"&id="+ GraphHelper.selected.id+
                "&x="+100.0+"&y="+100.0+"&name=");
    }

    public void linkSelectedNodes()
    {
        if (selected1 < 0) return;
        if (selected2 < 0) return;
        for (Link l : helper.link)
        {
            if (l.a == helper.getNode(selected1) && l.b == helper.getNode(selected2))
                return;
        }
        if (GraphHelper.selected.isOnServer)
            APIlinkSelectedNodes();
        else
            LocallinkSelectedNodes();
        invalidate();
        return;
    }

    public void LocallinkSelectedNodes()
    {
        Node source = helper.getNode(selected1);
        Node target = helper.getNode(selected2);
        DB.helper.linkAdd(helper.selected.id,source.id,target.id,0.0f);
        Link link = new Link(DB.helper.getMaxId("link"),source,target,0.0f);
        helper.link.add(link);
        invalidate();

    }

    public void APIlinkSelectedNodes()
    {
        Node source = helper.getNode(selected1);
        Node target = helper.getNode(selected2);
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                JSONObject obj = new JSONObject(res);
                Link link = new Link(obj.getInt("id"),
                        source, target, 0.0f);
                helper.link.add(link);
                invalidate();
            }

            public void onFail(Exception ex) {
                ctx.runOnUiThread(()->{
                    ErrorActivity.makeDialog(ctx,ex,"Не удалось соединить точки").show();
                });
            }
        };


        r.send(ctx, "PUT", "/link/create?token="+ GraphHelper.token+"&source="+source.id+"&target="+target.id+"&value="+0.0);
    }


    public void removeSelectedNodes()
    {
        if (selected1 < 0) return;
        ArrayList<Link> linksDel = new ArrayList<Link>();
        for (Link l : helper.link)
        {
            if (l.a == helper.getNode(selected1) || l.b == helper.getNode(selected1))
            {
                linksDel.add(l);
            }
        }
        for (int i = 0; i < linksDel.size(); i++)
        {
            Link l = linksDel.get(i);
            if (helper.selected.isOnServer)
                APIremoveSelectedLink(l.id);
            else
                LocalremoveSelectedLink(l.id);
        }

        if (helper.selected.isOnServer)
            APIremoveSelectedNode(selected1);
        else
            LocalremoveSelectedNode(selected1);
        selected1 =-1;
        selected2 =-1;
        invalidate();
    }

    public void LocalremoveSelectedLink(int id)
    {
        try {
            DB.helper.linkDelete(id);
            helper.removeLink(id);
            invalidate();
        }
        catch (Exception e)
        {
            return;
        }
    }

    public void LocalremoveSelectedNode(int id)
    {
        try {
            DB.helper.nodeDelete(id);
            helper.removeNode(id);
            invalidate();
        }
        catch (Exception e)
        {
            return;
        }

    }

    public void APIremoveSelectedNode(int id)
    {
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                helper.removeNode(id);
                invalidate();
            }

            public void onFail(Exception ex) {
                ctx.runOnUiThread(()->{
                    ErrorActivity.makeDialog(ctx,ex,"Не удалось удалить точку").show();
                });
            }
        };
        r.send(ctx,"DELETE", "/node/delete?token="+ GraphHelper.token+"&id="+id);
    }


    public void removeSelectedLink()
    {
        if (selectedLink < 0) return;
        if (GraphHelper.selected.isOnServer)
            APIremoveSelectedLink(selectedLink);
        else
        {
            DB.helper.linkDelete(selectedLink);
            helper.removeLink(selectedLink);
            invalidate();
        }

        selectedLink = -1;
        invalidate();
    }

    public void APIremoveSelectedLink(int id)
    {
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                helper.removeLink(id);
                invalidate();
            }

            public void onFail(Exception ex) {
                ctx.runOnUiThread(()->{
                    ErrorActivity.makeDialog(ctx,ex,"Не удалось разорвать соединение").show();
                });
            }
        };
        r.send(ctx,"DELETE", "/link/delete?token="+ GraphHelper.token+"&id="+id);
    }


    public void setNameOfSelectedNode(String name)
    {
        if (selected1 < 0) return;
        if (name == null) return;
        Node node = helper.getNode(selected1);
        if (GraphHelper.selected.isOnServer)
        {
            APIUpdateNode(node,true,name);
        }
        else
        {
            DB.helper.nodeUpdate(node.id,node.x,node.y,name);
            helper.setNodeText(selected1,name);
        }
        invalidate();
    }

    public void LocalUpdateNode(Node node)
    {
        try {
            DB.helper.nodeUpdate(node.id,node.x,node.y,node.name);
            invalidate();
        }
        catch (Exception e)
        {
            int id = helper.getId(node);
            if (id < 0) return;
            node.x = prevX;
            node.y = prevY;
            helper.node.set(id,node);
            invalidate();
        }
    }

    public void APIUpdateNode(Node node, boolean isNameChanged, String name)
    {
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                if (isNameChanged)
                {
                    helper.setNodeText(selected1, name);
                    invalidate();
                }
            }

            public void onFail(Exception ex) {
                int idNode = helper.getId(node);
                if (idNode < 0) return;
                node.x = prevX;
                node.y = prevY;
                helper.node.set(idNode,node);
                invalidate();
            }
        };
        if (name == null)
            r.send(ctx,"POST", "/node/update?token="+GraphHelper.token+"&id="+node.id+
                "&x="+node.x+"&y="+node.y+"&name="+node.name);
        else
            r.send(ctx,"POST", "/node/update?token="+GraphHelper.token+"&id="+node.id+
                    "&x="+node.x+"&y="+node.y+"&name="+name);
    }

    public void setValueOfSelectedLink(String value)
    {
        if (selectedLink < 0) return;
        if (value == null) return;
        float v = 0.0f;
        try {
            v = Float.valueOf(value);
        }
        catch (Exception e)
        {
            return;
        }
        if (GraphHelper.selected.isOnServer)
            APIsetValueOfSelectedLink(v);
        else
        {
            DB.helper.linkUpdate(selectedLink, v);
            helper.setLinkValue(selectedLink, v);
        }
        invalidate();
    }

    public void APIsetValueOfSelectedLink(float value)
    {
        Request r = new Request()
        {
            public void onSuccess(String res) throws Exception {
                helper.setLinkValue(selectedLink,value);
                invalidate();
            }

            public void onFail(Exception ex) {
                ctx.runOnUiThread(()->{
                    ErrorActivity.makeDialog(ctx,ex,"Не удалось задать значение соединению").show();
                });
            }
        };
        r.send(ctx,"POST", "/link/update?token="+GraphHelper.token+"&id="+selectedLink+
                "&value="+value);
    }


    public int getNodeAtXY(float x, float y)
    {
        for (Node n : helper.node)
        {
            float dx = x - n.x;
            float dy = y - n.y;
            if (dx * dx + dy * dy <= rad * rad) return n.id;
        }
        return -1;
    }

    public int getLinkAtXY(float x, float y)
    {
        for (Link l : helper.link)
        {
            Node na = l.a;
            Node nb = l.b;
            float bx = (na.x + nb.x) * 0.5f;
            float by = (na.y + nb.y) * 0.5f;
            if (helper.Reverse(l) != null)
            {
                by += 40f;
                if (x >= bx - halfside && x <=bx + halfside && y>= by - halfside && y <= by + halfside)
                    return l.id;
                by -= 80f;
                if (x >= bx - halfside && x <=bx + halfside && y>= by - halfside && y <= by + halfside)
                {
                    for (Link l1 : helper.link)
                    {
                        if (l1 == helper.Reverse(l1))
                            return l1.id;
                    }
                }
            }
            if (x >= bx - halfside && x <=bx + halfside && y>= by - halfside && y <= by + halfside)
                return l.id;
        }
        return -1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.rgb(255,255,255));
        ArrayList<Link> exceptLinks = new ArrayList<Link>();
        for (Link l : helper.link) {
            Node na = l.a;
            Node nb = l.b;
            if (l.id == selectedLink)
                p.setColor(Color.argb(128, 127, 0, 255));
            else
                p.setColor(Color.argb(127, 0, 0, 0));
            canvas.drawLine(na.x, na.y, nb.x, nb.y, p);
            float bx = (na.x + nb.x) * 0.5f;
            float by = (na.y + nb.y) * 0.5f;
            drawArrow(p,canvas,l.a.x,l.a.y,l.b.x,l.b.y);
            if (helper.Reverse(l) != null)
            {
                if (exceptLinks.contains(l) == false)
                {
                    DrawRect(canvas,bx, by + 40f,l);
                    DrawRect(canvas,bx, by - 40f,helper.Reverse(l));
                    exceptLinks.add(helper.Reverse(l));
                }
            }
            else {
                DrawRect(canvas, bx, by,l);
            }
        }

        for (Node n : helper.node)
        {
            p.setStyle(Paint.Style.FILL);
            if (n.id == selected1)
                p.setColor(Color.argb(50,127,0,255));
            else if (n.id == selected2) p.setColor(Color.argb(50,255,0,50));
            else
                p.setColor(Color.argb(50,0,127,255));
            canvas.drawCircle(n.x,n.y,rad,p);
            p.setStyle(Paint.Style.STROKE);
            if (n.id == selected1)
                p.setColor(Color.rgb(127,0,255));
            else if (n.id == selected2) p.setColor(Color.rgb(255,0,50));
            else
                p.setColor(Color.rgb(0,127,255));

            canvas.drawCircle(n.x,n.y, rad, p);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(48f);
            canvas.drawText(n.name, n.x, n.y + rad + 40, p);
        }
    }

    private void drawArrow(Paint paint, Canvas canvas, float fromX, float fromY, float toX, float toY)
    {
        float angle = 45,anglerad, radius = 30, lineangle;
        anglerad= (float) (Math.PI*angle/180.0f);
        lineangle= (float) (Math.atan2(toY-fromY,toX-fromX));
        canvas.drawLine(fromX,fromY,toX,toY,paint);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(toX, toY);
        path.lineTo((float)(toX-radius*Math.cos(lineangle - (anglerad / 2.0))),
                (float)(toY-radius*Math.sin(lineangle - (anglerad / 2.0))));
        path.lineTo((float)(toX-radius*Math.cos(lineangle + (anglerad / 2.0))),
                (float)(toY-radius*Math.sin(lineangle + (anglerad / 2.0))));
        path.close();

        canvas.drawPath(path, paint);
    }
    public void DrawRect(Canvas canvas, float bx, float by, Link l)
    {
        float x0 = bx - halfside;
        float x1 = bx + halfside;
        float y0 = by - halfside;
        float y1 = by + halfside;
        canvas.drawRect(x0,y0,x1,y1,p);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(32f);
        canvas.drawText(String.valueOf(l.value), bx,by, p);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch(action)
        {
            case MotionEvent.ACTION_DOWN:
                int i = getNodeAtXY(x,y);
                int j = getLinkAtXY(x,y);
                if (j < 0)
                    selectedLink = -1;
                else
                    selectedLink = j;
                lasthit = i;
                if (i < 0)
                {
                    selected1 = -1;
                    selected2 = -1;
                }
                else
                {
                    if (selected1 >= 0) selected2 = i;
                    else selected1 = i;
                }
                lastX = x;
                lastY = y;
                prevX = x;
                prevY = y;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                if (lasthit >= 0)
                {
                    Node n = helper.getNode(lasthit);
                    if (n.x != prevX || n.y != prevY)
                    {
                        if (GraphHelper.selected.isOnServer)
                            APIUpdateNode(n,false,null);
                        else
                            LocalUpdateNode(n);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
            {
                if (lasthit >= 0)
                {
                    Node n = helper.getNode(lasthit);
                    n.x += x - lastX;
                    n.y += y - lastY;
                    invalidate();
                }
                lastX = x;
                lastY = y;
                return true;
            }


        }
        return super.onTouchEvent(event);
    }
}
