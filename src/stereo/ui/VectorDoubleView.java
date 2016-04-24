/*
 * 
 * 
 */

package stereo.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JPanel;
import stereo.AproxyGrid;
import stereo.Node;
import stereo.Outline;

/**
 *
 * @author Karol Presovsky
 */
public class VectorDoubleView extends JPanel
{
    ArrayList<Outline> vecs;
    ArrayList<Outline> vecs2;

    ArrayList<Point> maxims;

    AproxyGrid function;

    int w;
    int h;

    boolean showVectors = true;

    public void setMaxims(ArrayList<Point> maxims)
    {
        this.maxims = maxims;
    }

    public VectorDoubleView(ArrayList<Outline> vecs, ArrayList<Outline> vecs2, AproxyGrid fu, int w, int h)
    {
        this.vecs = vecs;
        this.vecs2 = vecs2;
        function = fu;
        this.w = w;
        this.h = h;

        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(w,h));
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1)
                    showVectors = !showVectors;
                repaint();
            }
        });
    }


    void paintVectors(Graphics g)
    {
        if (vecs == null) return;
        for (int k=0;k<vecs.size();k++)
        {
            Outline v = vecs.get(k);
            float w = (float)Math.pow(v.weigth,0.2);
//            float w = (float)Math.sqrt(v.weigth);
            Point p1 = v.getP(0);
            int offx = 0;
            int offy = 0;

//            if (showVectors)
//            {
//                offx = (int)(p1.x*z - getWidth()/2*z);
//                offy = (int)(p1.y*z - getHeight()/2*z);
//            }

            for (int i=1;i<v.size(); i++)
            {
                Point p2 = v.getP(i);
                g.drawLine(p1.x+offx, p1.y+offy, p2.x+offx, p2.y+offy);
                p1 = p2;
            }
        }
    }

    void paintVectorsFNC(Graphics g)
    {
        if (vecs2 == null) return;
        Point p1 = new Point();
        Point p2 = new Point();

        for (int k=0;k<vecs2.size();k++)
        {
            Outline v = vecs2.get(k);
            float w = (float)Math.pow(v.weigth,0.2);
//            float w = (float)Math.sqrt(v.weigth);
            
            transform(v.getP(0), p1);
            int offx = 0;
            int offy = 0;

//            if (showVectors)
//            {
//                offx = (int)(p1.x*z - getWidth()/2*z);
//                offy = (int)(p1.y*z - getHeight()/2*z);
//            }

            for (int i=1;i<v.size(); i++)
            {
                transform(v.getP(i), p2);
                g.drawLine(p1.x+offx, p1.y+offy, p2.x+offx, p2.y+offy);
                p1 = p2;
            }
        }
    }

    void transform(Point p, Point p2)
    {
        Node n = new Node((float)p.x/w, (float)p.y/h);
        function.fnc(n);
        p2.x = (int)(n.x * w);
        p2.y = (int)(n.y * h);
    }

    void drawMaxims(Graphics g)
    {
        if (maxims != null)
        {
            g.setColor(Color.red);
            for (int i=0;i<maxims.size();i++)
            {
                Point p = maxims.get(i);
                g.drawOval(p.x-5, p.y-5, 10, 10);
            }
        }
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);

        g.setColor(Color.BLUE);
        paintVectors(g);
        g.setColor(Color.RED);
        paintVectorsFNC(g);
    //    drawMaxims(g);
    }
}
