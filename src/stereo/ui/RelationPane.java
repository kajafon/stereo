/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;
import stereo.Matcher;
import stereo.poi.Cntx;
import stereo.poi.Cntx2;
import stereo.Greyscale;
import stereo.RelationProcessor;
import stereo.poi.Relation;

/**
 *
 * @author karol presovsky
 */
public class RelationPane extends StereoView
{
    VectorView view1;
    VectorView view2;
    Point poi1;
    Point poi2;
    Matcher matcher; 
    Matcher matcher2; 
    int poiViewSize = 50;
    double errScale;
    double minErr;
            
    public RelationPane(final VectorView view1, ArrayList<Cntx2> cntxList1, final VectorView view2,  ArrayList<Cntx2> cntxList2, ArrayList<Relation> relations)
    {
       // view1.setReadEvents(false);
       // view2.setReadEvents(false);
      //  view1.setShowVectors(false);
      //  view2.setShowVectors(false);
        this.view1 = view1;
        this.view2 = view2;
        this.cntxList1 = cntxList1;
        this.cntxList2 = cntxList2;
        this.relations = relations;
        matcher = new Matcher(view1.gs, view2.gs); 
        matcher2 = new Matcher(view2.gs_grad, view2.gs_grad); 
        calcErrScale();
        setFocusable(true);
        
        add(view1);
        add(view2);
        
        view1.addMouseMotionListener(new MouseAdapter() 
        {

            @Override
            public void mouseMoved(MouseEvent me)
            {
               spot = me.getPoint();
               if (view1.contains(spot))
                  RelationPane.this.view1.setSpot(spot);
               else if (view2.contains(spot))
                  RelationPane.this.view2.setSpot(spot);
               repaint();
            }
        });
        
        view1.addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                poi1 = e.getPoint();
                calcError();
                repaint();
                requestFocus();
            }
        });
        
        view2.addMouseListener(new MouseAdapter() 
        {
                  
            @Override
            public void mouseClicked(MouseEvent e)
            {
                poi2 = e.getPoint();
                calcError();
                repaint();
                requestFocus();
            }
        });        
        
        addKeyListener(new KeyAdapter() 
        {

            @Override
            public void keyTyped(KeyEvent e)
            {
                //System.out.println(" key:" + e.getKeyChar());
                switch (e.getKeyChar())
                {
                    case 'r' : relationDump(); break;
                    case 'm' : 
                        RelationPane.this.view1.showMaxims(!RelationPane.this.view1.isShowMaxims());
                        RelationPane.this.view2.showMaxims(!RelationPane.this.view2.isShowMaxims());
                        repaint();
                    break;
                }
                
            }
            
        });
    }
    
    void calcErrScale()
    {
        errScale = 0;
        minErr = 10000;
        for (Relation r:relations)
        {
            if (r.error > errScale) errScale = r.error;
            if (r.error < minErr)   minErr = r.error;
        }
        
        errScale -= minErr;
    }

    @Override
    Color getRelationColor(Relation r)
    {
        float s = 1.0F - (float) ((r.error - minErr) / errScale);
        
        return new Color(s, s, s);
    }
    
    
    
    void relationDump()
    {
        if (spot != null)
        {
            System.out.println("relation dump");
            for (Relation r:relations)
            {
                Cntx c1 = r.c1;

                int d = Math.abs(spot.x - c1.x) + Math.abs(spot.y - c1.y);
                if (d < spotSize)
                {
                   System.out.print("-  [" + r.c1.x + "," + r.c1.y + "]->[" + r.c2.x + "," + r.c2.y + "] e=" + r.error);
                   for (double e:r.errors)
                   {
                       System.out.print(", " + e);
                   }
                   System.out.println("; " + r.toString() + "   " + r.c1.toString() + "   " + r.c2.toString() );
                }
            }
        }
    
    }
    
    void paintPoi(Graphics g, VectorView view, Point poi)
    {
        if (poi != null)
        {
            int x1 = view.getX();
            int y1 = view.getY() + view.getHeight()-poiViewSize;
            int gy = poi.y%(view.getHeight()/2);
          //  int x2 = view1.getWidth()-1;
           // int y2 = view1.getHeight()-1;
            drawSpot(g, view.gs, x1, y1, poi.x - poiViewSize/2, gy - poiViewSize/2, poiViewSize, poiViewSize);
            g.setColor(Color.red);
            g.drawRect(x1, y1, poiViewSize, poiViewSize);
        }
    }
    
    void calcError()
    {
        if (poi1 == null || poi2 == null) return;
  
        double[] err = new double[3];
        double e = RelationProcessor.calcError(poi1.x, poi1.y, poi2.x, poi2.y, matcher, matcher2, err);
        System.out.println("[" + poi1.x + "," + poi1.y + "]->[" + poi2.x + "," + poi2.y + "] e = " + e + ", " + err[0] + ", " + err[1] + ", " + err[2] + 
                ".... " + matcher.g1 + "," + matcher.g2);
        
    }
    
//    void calcBuff()
//    {
//        if (poi1 != null && poi2 != null)
//        {
//            int y1 = poi1.y % (view1.getHeight()/2) - poiViewSize/2;
//            int y2 = poi2.y % (view2.getHeight()/2) - poiViewSize/2;
//            int x1 = poi1.x - poiViewSize/2;
//            int x2 = poi2.x - poiViewSize/2;
//            
//            if (x1 < 0     || x2 < 0 ||
//                x1 + poiViewSize >= view1.gs.width  || x2 + poiViewSize >= view2.gs.width ||
//                y1  < 0    || y2 < 0 ||
//                y1 + poiViewSize >= view1.gs.height || y2 + poiViewSize >= view2.gs.height)
//            {
//                System.out.println(" out of image");
//                return;
//            }
//            
//            int e = 0;
//            int max = 0;
//            int min = 10000000;
//            for (int j=0; j<poiViewSize; j++)
//            {
//                int adr1 = (y1 + j)*view1.gs.width + x1;
//                int adr2 = (y2 + j)*view2.gs.width + x2;
//                int adr = j*poiViewSize;
//                for (int i=0; i<poiViewSize; i++)
//                {
//                    int v = Math.abs(view1.gs.px[adr1+i] - view2.gs.px[adr2+i]);
//                    if (v < min) min = v;
//                    if (v > max) max = v;
//                    spotBuff[adr+i] = v;
//                    //e += spotBuff[adr+i] = view1.gs.px[adr1+i];
//                    e += v;
//                }
//            }
//           /* 
//            max -= min;
//            
//            for (int i=0; i<poiViewSize*poiViewSize; i++)
//            {
//                spotBuff[i] = (int)((double)(spotBuff[i]-min)/max*255);
//            }
//            */
//            System.out.println("e = " + e);
//        }
//    }

    void drawBuff(Graphics g, int x, int y)
    {
        int [] spotBuff = matcher.b;   
        for (int j=0; j<matcher.stampSize; j++)
        {
            int adr = j*matcher.stampSize;
            for (int i=0; i<matcher.stampSize; i++)
            {
                g.setColor(new Color((int)spotBuff[adr+i], (int)spotBuff[adr+i], (int)spotBuff[adr+i]));
                g.drawLine(x+i, y+j, x+i, y+j);
            }
        }        
    }
    
    void drawSpot(Graphics g, Greyscale gs, int sx, int sy, int x, int y, int w, int h)
    {
       // System.out.println("->" + x + ", " + y);
        if (x < 0)
        {
            sx -= x;
            w += x;
            x = 0;
        }
        if (x+w > gs.width)
        {
            int d = x+w - gs.width;
            w -= d;
        }
        if (y < 0)
        {
            sy -= y;
            h += y;
            y = 0;
        }
        if (y+h > gs.height)
        {
            int d = y+h - gs.height;
            h -= d;
        }
        
        if (h <= 0 || w <= 0 || x >= gs.width || y >= gs.height)
            return;
        
        for (int j=0; j<h; j++)
        {
            int adr = (j+y)*gs.width;
            for (int i=0; i<w; i++)
            {
                short c = gs.px[adr + i + x];
                g.setColor(new Color((int)c,(int)c,(int)c));
                g.drawLine(sx+i, sy+j,sx+i, sy+j);
            }
        }
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        paintRelations(g);
      //  paintZet(g);
        paintPoi(g, view1, poi1);
        paintPoi(g, view2, poi2);
        drawBuff(g, view1.getX() + poiViewSize + 5, view1.getY() + view1.getHeight() - poiViewSize);

    }
    
}
