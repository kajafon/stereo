/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import stereo.Matcher;
import stereo.poi.Cntx;
import stereo.poi.Cntx2;
import stereo.Greyscale;
import stereo.RelationProcessor;
import stereo.poi.ApproachingPerfection;
import stereo.poi.ApproachingPerfection_sift;
import stereo.poi.SiftStamp;
import stereo.to3d.FtrLink;
import stereo.poi.Feature;
import stereo.poi.Relation;
import stereo.to3d.Face;
import stereo_praha.Algebra;

/**
 *
 * @author karol presovsky
 */
public class LinkPane_sift extends JPanel
{
    Point poi1;
    Point poi2;
    Point spot;
//    int poiViewSize = 50;
    double errScale;
    double minErr;
    ApproachingPerfection_sift thing1;
    ApproachingPerfection_sift thing2;
    ArrayList<FtrLink> links;
    ArrayList<Face> faceList;
    FtrLink currentLink = null;    
    JLabel view1;
    JLabel view2;
    JLabel view3;
    JLabel view4;
    JLabel view5;
    JLabel view6;
    boolean showLinks = true;
    boolean showFaces = true;
    boolean showMaxims = true;

    FtrLink targettedLink = null;
    int trgt2Index;
    Point target1 = null;
    Point target2 = null;
    int stampViewSize = 100;
    StampView stampView = new StampView(stampViewSize);
    int currenStep = 0;
    int imgWidth = 300;

    public void setFaceList(ArrayList<Face> faceList)
    {
        this.faceList = faceList;
    }

    ImageIcon createGuiImage(Greyscale gs) {
        Image img = gs.createImage(null);
        double scale = (double)imgWidth / img.getWidth(null);
        img = img.getScaledInstance(imgWidth, (int)(img.getHeight(null)*scale), 0);
        return new ImageIcon(img);
    }
    
    void getStep(int i) {
        System.out.println("getting step " + currenStep);
        Greyscale[] gss = this.thing1.getStep(i);
        if (gss != null) {
            view1.setIcon(createGuiImage(gss[0]));
            view2.setIcon(createGuiImage(gss[1]));
            view3.setIcon(createGuiImage(gss[2]));
        }
        gss = this.thing2.getStep(i);
        if (gss != null) {
            view4.setIcon(createGuiImage(gss[0]));
            view5.setIcon(createGuiImage(gss[1]));
            view6.setIcon(createGuiImage(gss[2]));
        }
        
        revalidate();
        repaint();
    }
    
    public LinkPane_sift(ApproachingPerfection_sift thing1, ApproachingPerfection_sift thing2, ArrayList<FtrLink> links)
    {
        setFocusable(true);
        
        Dimension dim = new Dimension(imgWidth, 200);
        JLabel lab = null;
        lab = view1 = new JLabel(); lab.setPreferredSize(dim); lab.setBorder(new LineBorder(Color.red, 1));
        lab = view2 = new JLabel(); lab.setPreferredSize(dim); lab.setBorder(new LineBorder(Color.red, 1));
        lab = view3 = new JLabel(); lab.setPreferredSize(dim); lab.setBorder(new LineBorder(Color.red, 1));
        lab = view4 = new JLabel(); lab.setPreferredSize(dim); lab.setBorder(new LineBorder(Color.red, 1));
        lab = view5 = new JLabel(); lab.setPreferredSize(dim); lab.setBorder(new LineBorder(Color.red, 1));
        lab = view6 = new JLabel(); lab.setPreferredSize(dim); lab.setBorder(new LineBorder(Color.red, 1));
        
    
        this.links = links;
        this.thing1 = thing1;
        this.thing2 = thing2;
      
        add(view1);
        add(view2);
        add(view3);
        add(view4);
        add(view5);
        add(view6);
        
        getStep(0);
        
        addMouseMotionListener(new MouseAdapter() 
        {
            @Override
            public void mouseMoved(MouseEvent me)
            {
                if (LinkPane_sift.this.links != null) {
                    spot = me.getPoint();

                    int x = spot.x - view1.getX();
                    int y = spot.y - view1.getY();
                    for (FtrLink l:LinkPane_sift.this.links)
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
//                int x = p.x - view2.getX();
//                int y = p.y - view2.getY();
//                
////                System.out.println("button: " + evt.getButton());
//                if (x >= 0 && x < view2.getWidth() && 
//                    y >= 0 && y < view2.getHeight()) {
//                    
//                    double dist = Double.MAX_VALUE;
//                    target2 = new Point(x,y);       
//    
//                    setStampsToView();                    
//                    double h = thing1.gs.getHessianTest(x, y);                    
//                    System.out.println("hessian: " + h);                    
//                    return;
//                }

//                int x = p.x - view1.getX();
//                int y = p.y - view1.getY();
//                
//                if (x >= 0 && x < view1.getWidth() && 
//                    y >= 0 && y < view1.getHeight()) {
//                    
//                    double[] h = thing1.gs.calcSubPeak(x, y);                    
//                    System.out.println("subpeak: " + h[0] + ", " + h[1]);       
//                    
//                    
//
//                    target1 = new Point(x,y);       
//                    shiftx = 0;
//                    shifty = 0;
//                } 

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

                        break;  
                    case KeyEvent.VK_LEFT:
                        currenStep-=1;
                        if (currenStep<0) {
                            currenStep = 0;
                        }
                        getStep(currenStep);
                        break;
                    case KeyEvent.VK_RIGHT:
                        currenStep+=1;
                        if (currenStep >= thing1.stepsCount()) {
                            currenStep = thing1.stepsCount()-1;
                        }
                        getStep(currenStep);
                        break;

                }
            }
        
        });
    }
    
    
    int shiftx;
    int shifty;
    
   
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
        if (maxims == null) {
            return;
        }
//        System.out.println("maxims:" + maxims.size());
        g.setColor(Color.yellow);
        for (int [] m:maxims)
        {
            g.drawLine(x0 + m[0], y0 + m[1], x0 + m[0], y0 + m[1]);
        }
    }
    
    
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
               
        g.setColor(new Color(255,255,255,50));
        markCurrentLink(g);
        
        paintMaxims(g, thing1.getMaxims(), view1.getX(), view1.getY());
        paintMaxims(g, thing2.getMaxims(), view4.getX(), view4.getY());
        g.setColor(Color.yellow);
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
}

    
    