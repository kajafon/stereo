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
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import stereo.poi.SiftStamp;
import stereo.to3d.FtrLink;
import stereo.poi.Feature;
import stereo.poi.Relation;
import stereo.to3d.Face;
import stereo.to3d.MatchedFtr;
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
        
    boolean isInView(int x, int y, JComponent view) {
        return x >= view.getX() && x < view.getX() + view.getWidth() && 
        y >= view.getY() && y < view.getY() + view.getHeight();
    }    
    
    public LinkPane(ApproachingPerfection thing1, ApproachingPerfection thing2, ArrayList<FtrLink> links)
    {
        setFocusable(true);
        view1 = new JLabel(new ImageIcon(thing1.getImg()));
        view2 = new JLabel(new ImageIcon(thing2.getImg()));
        view3 = new JLabel(new ImageIcon(thing1.gsExp.createImage(null)));
        view4 = new JLabel(new ImageIcon(thing2.gsExp.createImage(null)));
        
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
        JButton btn = new JButton(new AbstractAction("settle...") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                LinkPane.this.runAnimatedSettling();
            }            
        });
        add(btn);
        
        addMouseMotionListener(new MouseAdapter() 
        {
            @Override
            public void mouseMoved(MouseEvent me)
            {
                if (LinkPane.this.links != null) {
                    spot = me.getPoint();
                    JComponent view = null;
                    int ftrIndx = -1;

                    if (isInView(spot.x, spot.y, view1)) {
                        view = view1;
                        ftrIndx = 0;
                    } else if (isInView(spot.x, spot.y, view2)) {
                        view = view2;
                        ftrIndx = 1;
                    } else {
                        return;
                    }                          
                        
                    int x = spot.x - view.getX();
                    int y = spot.y - view.getY();
                    double dist = 10;
                    for (FtrLink l:LinkPane.this.links)
                    {
                        Feature f = l.getFeature(ftrIndx);
                        double d = Math.abs(f.x - x) + Math.abs(f.y - y);
                        if (d < dist)
                        {
                            dist = d;
                            currentLink = l;
                        }
                    }
                    
                    if (currentLink != null) {
                        int cCount = 0;
                        if (currentLink.candidates != null) {
                            cCount = currentLink.candidates.size();
                        }
//                        System.out.println("curr link: " + x + ", " + y + " cndts cnt: " + cCount + ", f2 refs: " + currentLink.f2.numRefs);
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
                    target2Angle = 0;
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
                    target2Angle = 0;

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
                        target1 = new Point(currentLink.f1.x, currentLink.f1.y);                            
                        target2 = new Point(currentLink.mf2.f.x, currentLink.mf2.f.y);                            
                        shiftx = 0;
                        shifty = 0;
                        target2Angle = 0;
                        setStampsToView();
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
                    case KeyEvent.VK_PAGE_UP: 
                        target2Angle -= Math.PI/80;
                        setStampsToView();                        
                        break;
                    case KeyEvent.VK_PAGE_DOWN: 
                        target2Angle += Math.PI/80;
                        setStampsToView();
                        break;
                }
            }
        
        });
    }
    
    int shiftx;
    int shifty;
    Greyscale stampGs = new Greyscale(2*ApproachingPerfection.stampSize, 2*ApproachingPerfection.stampSize);
    double target2Angle = 0;
    
    void setStampsToView() {
        double[][] stamp1 = null;
        int[] mid1 = new int[1];
        int[] mid2 = new int[1];
        SiftStamp sift1 = null;
        SiftStamp sift2 = null;        
        
        System.out.println("set stamps");
        if (target1 != null) {
//            System.out.println("stamp 1:");
//            stampGs.drawRotatedStamp(ApproachingPerfection.stampSize/2, ApproachingPerfection.stampSize/2, target1.x, target1.y, ApproachingPerfection.stampSize, 0, thing1.gs);
//            stamp1 = ApproachingPerfection.buildStamp(stampGs, ApproachingPerfection.stampSize/2, ApproachingPerfection.stampSize/2, ApproachingPerfection.stampSize, thing1.stampWeights, mid1);

            stamp1 = ApproachingPerfection.buildStamp(thing1.gs, target1.x, target1.y, ApproachingPerfection.stampSize, thing1.stampWeights, null);
//            sift1 = ApproachingPerfection.buildSiftStamp(thing1.gs, target1.x, target1.y);
        }
        double[][] stamp2 = null;
        if (target2 != null) {
//            stampGs.drawRotatedStamp(ApproachingPerfection.stampSize, ApproachingPerfection.stampSize, target2.x + shiftx, target2.y + shifty, ApproachingPerfection.stampSize, target2Angle, thing2.gs);
            stampGs.copyRotatedStamp(ApproachingPerfection.stampSize, ApproachingPerfection.stampSize, target2.x + shiftx, target2.y + shifty, ApproachingPerfection.stampSize, target2Angle, thing2.gs);
            stamp2 = ApproachingPerfection.buildStamp(stampGs, ApproachingPerfection.stampSize, ApproachingPerfection.stampSize, ApproachingPerfection.stampSize, thing2.stampWeights, null);
        }
        stampView.setStamps(stamp1, stamp2, thing1.stampWeights);     
        repaint();
    }
    
    Thread th = null;
    void runAnimatedSettling() {
        if (target1 == null || target2 == null) {
            System.out.println("target missing");
            return;
        }       
        
        ApproachingPerfection.MinimumCallback callback = new ApproachingPerfection.MinimumCallback(){
            @Override
            public void run(double[][] stamp1, double[][] stamp2, int x2, int y2, double a) {
                stampView.setStamps(stamp1, stamp2, thing1.stampWeights);
                stampView.repaint();
                System.out.println("--- step");
                
                try {
                    th.sleep(1000);
                } catch(InterruptedException e)
                {}
            }            
        };
        
        th = new Thread(new Runnable() {
            @Override
            public void run() {
                stampView.setFlag(true);
                ApproachingPerfection.findMinimum(thing1.gs, target1.x, target1.y, thing2.gs, target2.x, target2.y, thing1.stampWeights, callback);
                System.out.println("thread done.");
                stampView.setFlag(false);
            }
        });
        
        th.start();
    }
   
    void paintLinks(Graphics g)
    {
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
                Feature c2 = r.mf2.f;

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
        if (currentLink != null)
        {
            int x1 = view1.getX() + currentLink.f1.x;
            int y1 = view1.getY() + currentLink.f1.y;

            g.setColor(Color.yellow);
            int x2 = view2.getX() + currentLink.mf2.f.x;
            int y2 = view2.getY() + currentLink.mf2.f.y;
            g.drawRect(x1-2, y1-2, 4, 4);
            g.drawRect(x2-2, y2-2, 4, 4);
            g.setColor(Color.BLACK);
            g.drawRect(x1-3, y1-3, 6, 6);
            g.drawRect(x2-3, y2-3, 6, 6);
            
            for ( MatchedFtr mf : currentLink.candidates) {

                if (mf == currentLink.mf2) {
                    g.setColor(Color.yellow);
                } else {
                    g.setColor(Color.red);
                }

                x2 = view2.getX() + mf.f.x;
                y2 = view2.getY() + mf.f.y;
                g.drawRect(x1-2, y1-2, 4, 4);
                g.drawRect(x2-2, y2-2, 4, 4);            
                g.setColor(Color.BLACK);
                g.drawRect(x1-3, y1-3, 6, 6);
                g.drawRect(x2-3, y2-3, 6, 6);
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
}
