/*
 * 
 * 
 */

package stereo.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

/**
 *
 * @author Karol Presovsky
 */
public class InspectionPanel2D extends JPanel
{
    double[] graph1;
    double[] graph2;
    double[] dif1;
    double[] dif2;
    double[] dif3;
    double[] _tmp;

    int fncHeight = 100;

    void drawFnc(Graphics g, double[] fnc, Color c, Double fncMax)
    {
        g.setColor(c);
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        double koef;

        if (fncMax == null)
        {

            for (int i=0; i<fnc.length; i++)
            {
                //if (Math.abs(fnc[i]) > 10) continue;
                if (max < fnc[i]) max = fnc[i];
                if (min > fnc[i]) min = fnc[i];
            }

            if (max == min)
            {
                System.out.println(" max == min");
                return;
            }

            koef = fncHeight/(max-min);
        }
        else
        {
            koef = fncHeight/fncMax;
            min = -fncMax;
        }

        int y1 = (int)((fnc[0]-min)*koef);

        for (int x=1; x<fnc.length; x++)
        {
            int y = (int)((fnc[x]-min)*koef);

            g.drawLine(x-1, y1, x, y);
            y1 = y;
        }
    }

    void calcFirstDiference(double[] src, double[] dst)
    {

        for (int i=0; i<src.length-1; i++)
        {
            _tmp[i] = src[i+1] - src[i];
        }
        
        smooth(_tmp, dst, 20);

    }

    void calcDiference(double[] src1, double[] src2, double[] dst)
    {
        double signedsum = 0;
        double sum = 0;
        for (int i=0; i<src1.length; i++)
        {
            dst[i] = src1[i] - src2[i];
            signedsum += dst[i];
            sum += dst[i]*dst[i];
        }
        System.out.println(" sum:" + sum + " / " + signedsum);
    }

    void calc()
    {
        calcFirstDiference(graph1, dif1);
        calcFirstDiference(graph2, dif2);
        calcDiference(dif1, dif2, dif3);
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        double y1a = graph1[0];
        double y2a = graph2[0];

        double max = Integer.MIN_VALUE;
        for (int x=1; x<graph1.length; x++)
        {
            double y = graph1[x];

            g.setColor(Color.red);
            g.drawLine(x-1, (int)y1a, x, (int)y);
            y1a = y;
            if (y > max) max = y;

            y = graph2[x];
            g.setColor(Color.blue);
            g.drawLine(x-1,(int)y2a, x, (int)y);
            y2a = y;
            if (y > max) max = y;
        }

        drawFnc(g, dif1, Color.LIGHT_GRAY,5.0);
        drawFnc(g, dif2, Color.LIGHT_GRAY,5.0);
        drawFnc(g, dif3, Color.BLACK, 50.0);
    }

    public void smooth(double[] fnc, double[] dest, int range)
    {
        if (range >= fnc.length) return;

        double acum = 0;

        for (int i=0; i<range/2; i++)
        {
            acum += fnc[i];
            acum += fnc[fnc.length-1-i];
        }

        for (int i=0; i<fnc.length; i++)
        {
            dest[i] = acum/range;
            int indx = i - range/2;
            if (indx < 0)
                indx = fnc.length-1+indx;

            acum -= fnc[indx];

            indx = i + range/2;
            if (indx >= fnc.length)
                indx = indx - fnc.length;

            acum += fnc[indx];
        }
    }

    public InspectionPanel2D(int a)
    {
        setFocusable(true);
        setPreferredSize(new Dimension(a,a));
        graph1 = new double[a];
        graph2 = new double[a];
        dif1 = new double[a];
        dif2 = new double[a];
        dif3 = new double[a];
        _tmp = new double[a];

        MouseAdapter ma = new MouseAdapter()
        {

            int prevy;
            int prevx;
            int btn;

            @Override
            public void mouseDragged(MouseEvent e)
            {
                if (e.getX() == prevx) return;
                
                int dx = e.getX()>prevx?1:-1;
                double dy = ((double)e.getY() - prevy)/(e.getX() - prevx);
                double y = prevy;

                for (int x = prevx; x!=e.getX(); x+=dx)
                {

                    if (x < graph1.length && x >= 0)
                    {
                        if (btn == MouseEvent.BUTTON1)
                        {
                            graph1[x] = (int)y;
                            
                        }
                        else
                        {
                            graph2[x] = (int)y;
                            
                        }
                    }
                    y += dy;
                }
                calc();
                repaint();
                prevx = e.getX();
                prevy = e.getY();
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.getX() < graph1.length)
                {
                    if (e.getButton() == MouseEvent.BUTTON1)
                    {
                        graph1[e.getX()] = e.getY();
                    }
                    else
                    {
                        graph2[e.getX()] = e.getY();
                    }
                }
                calc();
                repaint();
                btn = e.getButton();
                prevx = e.getX();
                prevy = e.getY();
            }


        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_LEFT)
                {
                    double tmp = graph1[graph1.length-1];
                    for (int i = graph1.length-2; i>=0; i--)
                    {
                        graph1[i+1] = graph1[i];
                    }
                    graph1[0] = tmp;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
                {
                    double tmp = graph1[0];
                    for (int i = 1; i<graph1.length; i++)
                    {
                        graph1[i-1] = graph1[i];
                    }
                    graph1[graph1.length-1] = tmp;
                }
                calc();
                repaint();
            }
        });
    }





}
