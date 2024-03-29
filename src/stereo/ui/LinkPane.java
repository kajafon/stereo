/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import stereo.Matcher;
import stereo.poi.Cntx;
import stereo.poi.Cntx2;
import stereo.Greyscale;
import stereo.RelationProcessor;
import stereo.poi.ApproachingPerfection;
import stereo.to3d.FtrLink;
import stereo.poi.Feature;
import stereo.poi.Relation;
import stereo.to3d.Face;
import stereo_praha.Algebra;

/**
 *
 * @author karol presovsky
 */
public class LinkPane extends JPanel
{
    Point poi1;
    Point poi2;
    Point spot;
//    int poiViewSize = 50;
    double errScale;
    double minErr;
    ApproachingPerfection thing1;
    ApproachingPerfection thing2;
    ArrayList<FtrLink> links;
    ArrayList<Face> faceList;
    FtrLink currentLink = null;    
    JLabel view1;
    JLabel view2;
    JLabel view3;
    JLabel view4;
    boolean showLinks = true;
    boolean showFaces = true;
    boolean showMaxims = true;

    FtrLink targettedLink = null;
    int trgt2Index;
    Point target1 = null;
    Point target2 = null;
    int stampViewSize = 100;
    StampView stampView = new StampView(stampViewSize);

    public void setFaceList(ArrayList<Face> faceList)
    {
        this.faceList = faceList;
    }
            
    public LinkPane(ApproachingPerfection thing1, ApproachingPerfection thing2, ArrayList<FtrLink> links)
    {
        setFocusable(true);
        view1 = new JLabel(new ImageIcon(thing1.getImg()));
        view2 = new JLabel(new ImageIcon(thing2.getImg()));
        view3 = new JLabel(new ImageIcon(thing1.getGrad()));
        view4 = new JLabel(new ImageIcon(thing2.getGrad()));
        
        Dimension d = new Dimension(300,300);
        
//        view1.setPreferredSize(d);
//        view2.setPreferredSize(d);
//        view3.setPreferredSize(d);
//        view4.setPreferredSize(d);
        
        this.links = links;
        this.thing1 = thing1;
        this.thing2 = thing2;
      
        add(view1);
        add(view2);
        add(view3);
        add(view4);
        add(stampView);
        
        addMouseMotionListener(new MouseAdapter() 
        {
            @Override
            public void mouseMoved(MouseEvent me)
            {
                if (LinkPane.this.links != null) {
                    spot = me.getPoint();

                    int x = spot.x - view1.getX();
                    int y = spot.y - view1.getY();
                    for (FtrLink l:LinkPane.this.links)
                    {
                        if (Math.abs(l.f1.x - x) + Math.abs(l.f1.y - y) < 10)
                        {
                            currentLink = l;
                            break;
                        }
                    }
                }
                repaint();               
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                Point p = evt.getPoint();
                int x = p.x - view2.getX();
                int y = p.y - view2.getY();
                
//                System.out.println("button: " + evt.getButton());
                if (x >= 0 && x < view2.getWidth() && 
                    y >= 0 && y < view2.getHeight()) {
                    
                    double dist = Double.MAX_VALUE;
                    
                    if (evt.getButton() == 2) {
                        for (Feature f : thing2.features) {
                            double d = (f.x-x)*(f.x-x)+(f.y-y)*(f.y-y);
                            if (d < dist) {
                                target2 = new Point(f.x, f.y);
                                shiftx = 0;
                                shifty = 0;
                                
                                dist = d;
                            }                            
                        }                        
                    } else {
                        target2 = new Point(x,y);       
                    }

                    setStampsToView();                    
                    return;
                }

                x = p.x - view1.getX();
                y = p.y - view1.getY();
                
                if (x >= 0 && x < view1.getWidth() && 
                    y >= 0 && y < view1.getHeight()) {
                    
                    target1 = new Point(x,y);       
                    shiftx = 0;
                    shifty = 0;

                    setStampsToView();
                } 

            }
        });
        
        addKeyListener(new KeyAdapter() 
        {

            @Override
            public void keyPressed(KeyEvent e)
            {
                System.out.println("key pressed");
                switch(e.getKeyCode())
                {
                    case KeyEvent.VK_S:
                        showLinks = !showLinks;
                        repaint();
                        break;
                    case KeyEvent.VK_E:
                        if (currentLink != null)
                            System.out.println("line err.: " + currentLink.e);
                        break;
                    case KeyEvent.VK_F:
                        showFaces = !showFaces;
                        repaint();
                        break;                        
                    case KeyEvent.VK_M:
                        showMaxims = !showMaxims;
                        repaint();
                        break;                      
                    case KeyEvent.VK_T: 
                        if (currentLink != null) {
                             
                            target1 = new Point(currentLink.f1.x, currentLink.f1.y);
                            
                            if (targettedLink != currentLink) {
                                targettedLink = currentLink;
                                trgt2Index = 0;                                
                            } else {
                                trgt2Index = (trgt2Index + 1)%currentLink.candidates.size();                                
                            }
                            
                            Feature f = currentLink.candidates.get(trgt2Index);
                            target2 = new Point(f.x, f.y);
                            shiftx = 0;
                            shifty = 0;
                            
                            setStampsToView();
                            repaint();
                        }
                        break;  
                    case KeyEvent.VK_LEFT:
                        shiftx-=1;
                        setStampsToView();
                        break;
                    case KeyEvent.VK_RIGHT:
                        shiftx+=1;
                        setStampsToView();
                        break;
                    case KeyEvent.VK_UP:
                        shifty-=1;
                        setStampsToView();
                        break;
                    case KeyEvent.VK_DOWN:
                        shifty+=1;
                        setStampsToView();
                        break;
                }
            }
        
        });
    }
    
    int shiftx;
    int shifty;
    
    void setStampsToView() {
        double[][] stamp1 = null;
        int[] mid1 = new int[1];
        int[] mid2 = new int[1];
        
        if (target1 != null) {
            stamp1 = ApproachingPerfection.buildStamp(thing1.gs, target1.x, target1.y, ApproachingPerfection.stampSize, thing1.stampWeights, mid1);
        }
        double[][] stamp2 = null;
        if (target2 != null) {
            stamp2 = ApproachingPerfection.buildStamp(thing2.gs, target2.x + shiftx, target2.y + shifty, ApproachingPerfection.stampSize, thing2.stampWeights, mid2);
        }
        
        stampView.setStamps(stamp1, mid1[0], stamp2, mid2[0], thing1.stampWeights);     
        repaint();
    }
    
   
    void paintLinks(Graphics g)
    {
        if (spot == null)
        {
            System.out.println("spot == null");
            return;
        }
        
        if (links == null || links.isEmpty())
        {
            return;
        }
        int offx1 = view1.getX();
        int offx2 = view2.getX();
        int offy1 = view1.getY();
        int offy2 = view2.getY();

        for (FtrLink r : links)
        {
            if (currentLink == r || showLinks) {
                Feature c1 = r.f1;
                Feature c2 = r.f2;

                if (c2 == null) {
                    continue;
                }

                int x1 = offx1 + c1.x;
                int y1 = offy1 + c1.y;
                int x2 = offx2 + c2.x;
                int y2 = offy2 + c2.y;
                g.drawLine(x1, y1, x2, y2);
            }
        }        
    }

    void markCurrentLink(Graphics g)
    {
        if (currentLink != null && currentLink.f2 != null)
        {
            int x1 = view1.getX() + currentLink.f1.x;
            int y1 = view1.getY() + currentLink.f1.y;
            
            for ( Feature f : currentLink.candidates) {

                if (f == currentLink.f2) {
                    g.setColor(Color.yellow);
                } else {
                    g.setColor(Color.red);
                }

                int x2 = view2.getX() + f.x;
                int y2 = view2.getY() + f.y;
                g.drawRect(x1-2, y1-2, 4, 4);
                g.drawRect(x2-2, y2-2, 4, 4);            
            }            
        }        
    }
    
    void paintMaxims(Graphics g, ArrayList<int[]> maxims, int x0, int y0)
    {
//        System.out.println("maxims:" + maxims.size());
        g.setColor(Color.yellow);
        for (int [] m:maxims)
        {
            g.drawLine(x0 + m[0], y0 + m[1], x0 + m[0], y0 + m[1]);
        }
    }
    
    void paintFaces(Graphics g)
    {
        int offx = view1.getX();
        int offy = view1.getY();
        
        if (faceList == null || !showFaces || links == null) return;

        g.setColor(Color.WHITE);
        
        for (Face face : faceList)
        {
            FtrLink l1 = links.get(face.r1);
            FtrLink l2 = links.get(face.r2);
            FtrLink l3 = links.get(face.r3);
        
            g.drawLine(l1.f1.x + offx, l1.f1.y + offy, l2.f1.x + offx, l2.f1.y + offy);
            g.drawLine(l2.f1.x + offx, l2.f1.y + offy, l3.f1.x + offx, l3.f1.y + offy);
            g.drawLine(l1.f1.x + offx, l1.f1.y + offy, l3.f1.x + offx, l3.f1.y + offy);
            
        }
    }

    
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        paintFaces(g);        
        g.setColor(new Color(255,255,255,50));
        paintLinks(g);
        markCurrentLink(g);
        
        if (showMaxims) {
            paintMaxims(g, thing1.getMaxims(), view1.getX(), view1.getY());
            paintMaxims(g, thing2.getMaxims(), view2.getX(), view2.getY());
        }
        
        g.setColor(new Color(0,0,0,50));
        g.fillRect(view3.getX(), view3.getY(), view3.getWidth(), view3.getHeight());
        g.fillRect(view4.getX(), view4.getY(), view4.getWidth(), view4.getHeight());

        g.setColor(Color.yellow);
        if (showMaxims) {
            paintMaxims(g, thing1.getMaxims(), view3.getX(), view3.getY());
            paintMaxims(g, thing2.getMaxims(), view4.getX(), view4.getY());
        }
        if (target1 != null) {
            g.setColor(Color.BLUE);
            g.drawRect(view1.getX() + target1.x - 5, view1.getY() - 5 + target1.y, 10, 10);
        }      
        if (target2 != null) {
            g.setColor(Color.BLUE);
            g.drawRect(view2.getX() + target2.x - 5, view2.getY() - 5 + target2.y, 10, 10);
        }
        
        if (target1 != null && target2 != null) {
            g.setColor(Color.YELLOW);
            g.drawLine(view1.getX() + target1.x, view1.getY() + target1.y, view2.getX() + target2.x, view2.getY() + target2.y);
        }
    }

        
//    void paintPoi(Graphics g, JComponent view, Greyscale gs, Point poi)
//    {
//        if (poi != null)
//        {
//            int x1 = view.getX();
//            int y1 = view.getY() + view.getHeight()-poiViewSize;
//            int gy = poi.y%(view.getHeight()/2);
//          //  int x2 = view1.getWidth()-1;
//           // int y2 = view1.getHeight()-1;
//            drawSpot(g, gs, x1, y1, poi.x - poiViewSize/2, gy - poiViewSize/2, poiViewSize, poiViewSize);
//            g.setColor(Color.red);
//            g.drawRect(x1, y1, poiViewSize, poiViewSize);
//        }
//    }
//
//    
//    void drawSpot(Graphics g, Greyscale gs, int sx, int sy, int x, int y, int w, int h)
//    {
//       // System.out.println("->" + x + ", " + y);
//        if (x < 0)
//        {
//            sx -= x;
//            w += x;
//            x = 0;
//        }
//        if (x+w > gs.width)
//        {
//            int d = x+w - gs.width;
//            w -= d;
//        }
//        if (y < 0)
//        {
//            sy -= y;
//            h += y;
//            y = 0;
//        }
//        if (y+h > gs.height)
//        {
//            int d = y+h - gs.height;
//            h -= d;
//        }
//        
//        if (h <= 0 || w <= 0 || x >= gs.width || y >= gs.height)
//            return;
//        
//        for (int j=0; j<h; j++)
//        {
//            int adr = (j+y)*gs.width;
//            for (int i=0; i<w; i++)
//            {
//                short c = gs.px[adr + i + x];
//                g.setColor(new Color((int)c,(int)c,(int)c));
//                g.drawLine(sx+i, sy+j,sx+i, sy+j);
//            }
//        }
//    }    
}

class StampView extends JPanel {
    double[][] stamp1 = null;
    double[][] stamp2 = null;
    
    Image stamp1Img;
    Image stamp2Img;
    int imgsize = 100;
    
    double error;
    double error2;
    double errorResult;
    
    public StampView(int size) {
        imgsize = size;                
        setPreferredSize(new Dimension(imgsize *2 + 10, imgsize + 50));
    }
    
    Image createImage(double[][] stamp) {        
        Greyscale gs = new Greyscale(stamp[0].length, stamp.length);
        
        for (int j=0; j<stamp.length; j++) {
            for (int i=0; i<stamp.length; i++) {
                short val = (short)(Math.min(255, stamp[j][i]*15));
                gs.px[j*gs.width + i] = val;
            }            
        }
        
        BufferedImage img = gs.createImage(null);
        return img.getScaledInstance(imgsize, imgsize, 0);        
    }
    
    double midVal1;
    double midVal2;
    int shiftx;
    int shifty;
    
    
    public void setStamps(double[][] s1, int midVal_1, double[][]s2, int midVal_2, double[][] weights) {
        stamp1 = s1;
        stamp2 = s2;
        
        midVal1 = midVal_1;
        midVal2 = midVal_2;        
        
        if (stamp1 != null && stamp2 != null) {
            error = Algebra.compare(stamp1, midVal_1, stamp2, midVal_2, weights, 0, 0);            
//            error2 = Math.abs(midVal_1 - midVal_2) / (double)(midVal_1 + midVal_2) * 2;
//            errorResult = error * Math.pow(Math.E, error2);
//            
            error = Math.floor(error * 10000)/10000;
//            error2 = Math.floor(error2 * 10000)/10000;
//            errorResult = Math.floor(errorResult * 10000)/10000;
            
            System.out.println("e:" + error);            
        }
        
        stamp1Img = null;
        stamp2Img = null;
        
        if (stamp1 != null) {
            stamp1Img = createImage(stamp1);        
        }
        if (stamp2 != null) {
            stamp2Img = createImage(stamp2);
        }
        
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        
        g.drawString("" + error + " | " + error2 + " => " + errorResult + " [ " + shiftx + ", " + shifty + "]", 10, 20);
        if (stamp1Img != null) {
            g.drawImage(stamp1Img,0, 50, null);        
        }
        if (stamp2Img != null) {
            g.drawImage(stamp2Img,imgsize + 10, 50, null);
        }
    }
    
    
    
}