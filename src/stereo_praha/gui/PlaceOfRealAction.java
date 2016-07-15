//ty vole 
package stereo_praha.gui;

import evolve.AbstractAgent;
import evolve.Reactor;
import stereo_praha.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javafx.scene.Scene;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import stereo.to3d.Face;
import stereo.to3d.FtrLink;
import static stereo_praha.gui.FieldsOfError.basicScale;

public class PlaceOfRealAction extends StereoTask {

    double originAx = 0.4;
    double originAy = 0;
    
    /*
     this is also a target focalLength !!!!
    */
    double projectionScale = 10;
    double originZTranslation = 40;
    
    
    double scale = 300;
    
    double[][] sourceProjection = null;
    

    Object3D rays1 = new Object3D();
    Object3D rays2 = new Object3D();
    Object3D plane1 = new Object3D();
    Object3D plane2 = new Object3D();
    Object3D outline1 = new Object3D();
    Object3D outline2 = new Object3D();
    Object3D gold = new Object3D();
    Object3D handle = new Object3D(1,0,0);

    Scene3D scene = new Scene3D("scene");
    Scene3D ray2Subscene = new Scene3D("ray2Subscene");
    Scene3D goldScene = new Scene3D("goldScene");
    double invarianceError;
    double minimalZ;
    double penalty;
    
    ArrayList<Object3D> links;
    ArrayList<Face> faceList;

    final GraphPanel graphPanel = new GraphPanel();

    JPanel gui;   
    JPanel panel;
    JPanel goldPanel;
    JTextField tfAy = new JTextField(5);
    JTextField tfAx = new JTextField(5);

    ArrayList<FtrLink> featureLinks;
    
    double unknownAngle = 0.2;
    int activeObject = 0;
    
    double[][] origin_projection_1;
    double[][] origin_projection_2;
    double[][] gold_projection_1;
    double[][] gold_projection_2;
    
    int [][]   origin_triangles;
    double[] goldMatrix = new double[16];

    double mutationStrength = 0.5; 
    int test;
    
    Thread animationThread = null;
    
    
    public PlaceOfRealAction(Object3D obj, double ax, double ay)
    {
        originAx = ax;
        originAy = ay;
        processObject(obj);
        init();
    }
    
    public PlaceOfRealAction(ArrayList<FtrLink> links, ArrayList<Face> faces) {

        featureLinks = links;
        faceList = faces;
        processFeatures();
        init();
    }
    
    Object3D createOutline(double[][] projected, Object3D obj)
    {
        if (obj == null) {
            obj = new Object3D();
            obj.setColor(Color.black);
        } 
        
        obj.init(projected.length, origin_triangles.length, origin_triangles[0].length);
        for(int i=0; i<projected.length; i++) {
            obj.vertex[i][0] = projected[i][0];
            obj.vertex[i][1] = projected[i][1];
            obj.vertex[i][2] = 0;
        }
        
        for(int i=0; i<origin_triangles.length; i++) {
            for(int j=0; j<origin_triangles[i].length; j++) { 
                obj.triangles[i][j] = origin_triangles[i][j];
            }
        } 
        
        return obj;
    }
    
    Object3D getGold() 
    {
        return gold;
    }
    
    Object3D createPlane()
    {
        double[][] pln = {{10,10,0}, {10,-10,0},{-10,-10,0},{-10,10,0}};
        int[][] t = {{0,1,2,3,0}};
        final Object3D obj = new Object3D(pln, t);
        obj.setName("plane");
        return obj;
    }
    
    void buildTask()
    {
        rays1 = SpringInspiration.objectFromProjection(origin_projection_1, rays1, focalLength, 5);
        outline1 = createOutline(origin_projection_1, outline1);
        
        rays2 = SpringInspiration.objectFromProjection(origin_projection_2, rays2, focalLength, 5);
        outline2 = createOutline(origin_projection_2, outline2);
        
        rays1.project();
        ray2Subscene.project();
        
        final ArrayList<Object3D> newLinks = SpringInspiration.createDistanceObjects(rays1, rays2, links);
        if (links == null) 
        {
            for(Object3D obj: newLinks) 
                scene.add(obj);
        }
        links = newLinks;
        
        goldError = reconstruction();
        
    }
    
    public void moveRays2()
    {
        ray2Subscene.setTranslation(moveX, moveY, moveZ);
    }
    
    public void rotateRays2()
    {
        ray2Subscene.setRotation(angleX, angleY, 0);
    }
    
    @Override
    public double[] getVector()
    {
        return new double[]{moveX, moveY, moveZ, handle.vertex[0][0], handle.vertex[0][1], handle.vertex[0][2]}; 
    }

    @Override
    public void setVector(double[] vec)
    {
        moveX = vec[0]; 
        moveY = vec[1];
        moveZ = vec[2];
        handle.vertex[0][0] = vec[3];
        handle.vertex[0][1] = vec[4];
        handle.vertex[0][2] = vec[5];
        
        moveRays2();
        applyHandle();
    }
    
    void applyHandle()
    {
        double[] x1 = ray2Subscene.translation;
        double[] x2 = handle.vertex[0];
        double[] a = Algebra.anglesFromLine(x1, x2);
        
        angleX = Math.PI*1.5 + a[0];
        angleY = a[1];
        
        focalLength = Algebra.distance(x1, x2);

        applySolution();
        scene.project();

        double aa = a[0];
        double bb = a[1];
        
        
        if (aa > Math.PI) {
            aa = 2*Math.PI - aa;
        } 
        if (bb > Math.PI) {
            bb = 2*Math.PI - bb;
        } 

        if (Double.isNaN(aa)) {
           System.out.println("nan!!!!");
        }
        
        System.out.println(">>>" + aa + ", " + bb + " / " + a[0] + ", " + a[1]);
        
    }
    
    void cheat()
    {
        double bulgarianScale = projectionScale; 
        handle.vertex[0][0] = 0;
        handle.vertex[0][1] = 0;
        handle.vertex[0][2] = -bulgarianScale;
        Object3D anchor = new Object3D(1, 0, 0);
        anchor.vertex[0][0] = 0;
        anchor.vertex[0][1] = 0;
        anchor.vertex[0][2] = 0;
        
        
        Scene3D cheatScene = new Scene3D();
        cheatScene.add(handle);
        cheatScene.add(anchor);
        
        cheatScene.setRotation(-originAx, -originAy, 0);
        cheatScene.project();
        
        setVector(new double[]{
            anchor.transformed[0][0], anchor.transformed[0][1], anchor.transformed[0][2], 
            handle.transformed[0][0], handle.transformed[0][1], handle.transformed[0][2]});   
        
        panel.repaint();
    }
    
    @Override
    public void applySolution()
    {
        rotateRays2();
        moveRays2();
        buildTask();
        reconstruction();
    }

    void processObject(Object3D obj)
    {        
        origin_projection_1 = new double[obj.projected.length][2];
        origin_projection_2 = new double[obj.projected.length][2];
        origin_triangles = new int[obj.triangles.length][obj.triangles[0].length];
        
        for (int i=0; i<obj.triangles.length; i++)
        {
            for (int j=0; j<obj.triangles[0].length; j++)
            {
                origin_triangles[i][j] = obj.triangles[i][j];
            }
        }
        
        obj.setTranslation(0,0,originZTranslation);
        obj.project();
        
        for (int i=0; i<origin_projection_1.length; i++)
        {
            origin_projection_1[i][0] = obj.projected[i][0]*projectionScale;
            origin_projection_1[i][1] = obj.projected[i][1]*projectionScale;
        }
        
        obj.setRotation(originAx, originAy, 0);
        obj.project();
        
        for (int i=0; i<origin_projection_2.length; i++)
        {
            origin_projection_2[i][0] = obj.projected[i][0]*projectionScale;
            origin_projection_2[i][1] = obj.projected[i][1]*projectionScale;
        }
        
    }
    
    void processFeatures()
    {
        origin_projection_1 = new double[featureLinks.size()][2];
        origin_projection_2 = new double[featureLinks.size()][2];
        double maxx = -10000;
        double maxy = -10000;
        double minx = 10000;
        double miny = 10000;
        
        for (int i=0; i<featureLinks.size(); i++)
        {
            FtrLink fl = featureLinks.get(i);
            origin_projection_1[i][0] = fl.f1.x;
            origin_projection_1[i][1] = fl.f1.y;
            origin_projection_2[i][0] = fl.f2.x;
            origin_projection_2[i][1] = fl.f2.y;
            
            maxx = maxx > fl.f1.x ? maxx : fl.f1.x;
            maxx = maxx > fl.f2.x ? maxx : fl.f2.x;
            maxy = maxy > fl.f1.y ? maxy : fl.f1.y;
            maxy = maxy > fl.f2.y ? maxy : fl.f2.y;

            minx = minx < fl.f1.x ? minx : fl.f1.x;
            minx = minx < fl.f2.x ? minx : fl.f2.x;
            miny = miny < fl.f1.y ? miny : fl.f1.y;
            miny = miny < fl.f2.y ? miny : fl.f2.y;
        }
        
        maxx -= minx;
        maxy -= miny;
        
        if (maxx != 0 && maxy != 0) 
        {
        
            double size = 2; 
            for (int i=0; i<origin_projection_1.length; i++)
            {
                origin_projection_1[i][0] = (origin_projection_1[i][0] - minx)/maxx*size - size/2;
                origin_projection_1[i][1] = (origin_projection_1[i][1] - miny)/maxy*size - size/2;
                origin_projection_2[i][0] = (origin_projection_2[i][0] - minx)/maxx*size - size/2;
                origin_projection_2[i][1] = (origin_projection_2[i][1] - miny)/maxy*size - size/2;
            }
        } else
        {
            System.out.println("WARNING: zero size on normalization!");
        }
        origin_triangles = new int[faceList.size()][3];
        for(int i=0; i<faceList.size(); i++) 
        {
            Face f = faceList.get(i);
            origin_triangles[i][0] = f.r1;
            origin_triangles[i][1] = f.r2;
            origin_triangles[i][2] = f.r3;
        }
        
    }
    void init()
    {
        rays1.setName("rays");
        rays2.setName("rays2");
        
        rays1.setColor(new Color(0,0,0, .2f));
        rays2.setColor(new Color(0,0,1.0f, .2f));
        
        plane1 = createPlane();
        plane2 = createPlane();
        
        plane1.setColor(Color.gray);
        plane2.setColor(Color.blue);
        
        gold = new Object3D();

        ray2Subscene.add(rays2);
        ray2Subscene.add(plane2);
        ray2Subscene.add(outline2);
        
        scene.add(rays1);
        scene.add(plane1);
        scene.setTranslation(0, 0, 30);
        scene.add(outline1); 
        scene.add(ray2Subscene);
        scene.add(handle);
        
        handle.vertex[0][2] = -focalLength;
        
        buildTask();
        
        goldScene.add(gold);
        goldScene.setTranslation(0, 0, 60);
        
        System.out.println("..");

    }
    
    double calcVectorError()
    {
        vectorError = 0;
        gold.setTranslation(0, 0, focalLength);
        gold.setRotation(0, 0, 0);
        gold.project();
        if (gold_projection_1 == null) gold_projection_1 = new double[gold.projected.length][2];
        for (int i=0; i<gold.projected.length; i++)
        {
            gold_projection_1[i][0] = gold.projected[i][0];
            gold_projection_1[i][1] = gold.projected[i][1];
            vectorError += Math.abs(gold_projection_1[i][0] - origin_projection_1[i][0]);
            vectorError += Math.abs(gold_projection_1[i][1] - origin_projection_1[i][1]);
        }

        gold.setRotation(-angleX, -angleY, 0);
        gold.setTranslation(moveX, moveY, moveZ + focalLength);
        gold.project();
        
        if (gold_projection_2 == null) gold_projection_2 = new double[gold.projected.length][2];
        for (int i=0; i<gold.projected.length; i++)
        {
            gold_projection_2[i][0] = gold.projected[i][0];
            gold_projection_2[i][1] = gold.projected[i][1];
            double e = Math.abs(gold_projection_2[i][0] - origin_projection_2[i][0]);
            e += Math.abs(gold_projection_2[i][1] - origin_projection_2[i][1]);
            
            if (e == Double.NaN)
            {
                System.out.println("nan!!");
            } else {
                vectorError += e;
            }
        }
        return vectorError;
    }
    
    
    double vectorError;
    double reconstruction()
    {
        if (gold == null)
        {
            gold = new Object3D(links.size(), 1, links.size());
        } else {
            gold.init(links.size(), origin_triangles.length, origin_triangles[0].length);
        }
        double error = 0;
        double maxx = -10000;
        double maxy = -10000;
        double maxz = -10000;
        double minx = 10000;
        double miny = 10000;
        double minz = 10000;
        
        double maxe = 0;
                
        for (int i=0; i<links.size(); i++)
        {
            Object3D link = links.get(i);
            double x = (link.vertex[0][0] + link.vertex[1][0])/2;
            double y = (link.vertex[0][1] + link.vertex[1][1])/2;
            double z = (link.vertex[0][2] + link.vertex[1][2])/2;
            
            gold.vertex[i][0] = x;
            gold.vertex[i][1] = y;
            gold.vertex[i][2] = z;
                        
            maxx = (x > maxx) ? x : maxx;
            maxy = (y > maxy) ? y : maxy;
            maxz = (z > maxz) ? z : maxz;

            minx = (x < minx) ? x : minx;
            miny = (y < miny) ? y : miny;
            minz = (z < minz) ? z : minz;
            
            double e = Math.abs(link.vertex[0][0] - link.vertex[1][0]); 
            e += Math.abs(link.vertex[0][1] - link.vertex[1][1]); 
            e += Math.abs(link.vertex[0][2] - link.vertex[1][2]); 
            
            maxe = (maxe < e) ? e : maxe;
            
            error += e;
            if (Double.isNaN(error))
            {
                System.out.println("nan!");
            }           
        }
        gold.setTranslation(-(maxx+minx)/2, -(maxy+miny)/2, -(maxz+minz)/2 + 60);
        
        minimalZ = minz;
        for (int j=0; j<origin_triangles.length; j++)
        {
            for (int i=0; i<origin_triangles[j].length; i++)
            {
                gold.triangles[j][i] = origin_triangles[j][i];
            }
        }
        
        maxx -= minx;
        maxy -= miny;
        maxz -= minz;
        
        maxx = (maxx < maxy) ? maxy : maxx;
        maxx = (maxx < maxz) ? maxz : maxx;
        
        if (maxx < 0.000001)
            return Double.POSITIVE_INFINITY;
        
        error /= links.size();
        error /= focalLength;

        
        if (minz < -focalLength/2)
        {
            penalty = -focalLength/2 - minz;
            penalty = penalty * penalty + 1;
        } else
        {
            penalty = 1.0;
        }
//        penalty = Math.exp(-minz + focalLength);
            
        error *= penalty;
//        calcVectorError();
//        if (vectorError == Double.NaN)
//            return Double.POSITIVE_INFINITY;
//        
//        error *= Math.abs(vectorError);
        
        
        
        return error;
    }


    double xGuiToObject(double x)
    {
        return ((x - panel.getWidth()/2) + 50)/scale;
    }
    
    double yGuiToObject(double y)
    {
        return (y - panel.getHeight()/2)/scale;
    }    

   
    MouseAdapter createMouseManipulator()
    {
        final MouseTracker sceneTracker = new MouseTracker();
        final MouseTracker taskTracker = new MouseTracker();
        final MouseTracker moveTracker = new MouseTracker();
        final MouseTracker originTracker = new MouseTracker();
        
        return new MouseAdapter() {
            MouseTracker getTracker(MouseEvent e) {
                boolean meta = (e.getModifiersEx()&MouseEvent.META_DOWN_MASK) != 0;
                boolean alt = (e.getModifiersEx()&MouseEvent.ALT_DOWN_MASK) != 0;
                boolean ctrl = (e.getModifiersEx()&MouseEvent.CTRL_DOWN_MASK) != 0;

                MouseTracker trck = null;

                if (!(meta|alt|ctrl)) {
                    activeObject = 0;
                    trck = sceneTracker;
                } else if (meta && !(alt || ctrl)) {
                    activeObject = 1;
                    trck = taskTracker;
                } else if (meta && alt) {
                    activeObject = 4;
                    trck = moveTracker;
                } else if (alt && !(meta || ctrl)){
                    activeObject = 3;
                    trck = originTracker;
                } else {    
                    System.out.println("unknown drag");
                }
                return trck;

            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (activeObject == 5) {  
                    double mx = xGuiToObject(e.getX());
                    double my = yGuiToObject(e.getY());
                    drag(mx, my);
                } else {
                    MouseTracker trck = getTracker(e);
                    trck.mouseDragged(e);
                    drag(trck.mouseY/200.0, trck.mouseX/-200.0);
                }    
            }

            @Override
            public void mousePressed(MouseEvent e) {
                double mx = xGuiToObject(e.getX());
                double my = yGuiToObject(e.getY());
                double boxSize = 10/scale;

                if (mx > handle.projected[0][0]-boxSize && mx < handle.projected[0][0] + boxSize &&
                    my > handle.projected[0][1]-boxSize && my < handle.projected[0][1] + boxSize)
                {
                    activeObject = 5;
                    scene.project();
                    handleInverse = Algebra.calcInverse(handle.tmp_matrix, null);
                } else {
                    MouseTracker trck = getTracker(e);
                    trck.mousePressed(e); 
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (activeObject == 5)
                {
                    applyHandle();
                    panel.repaint();
                }
            }
            
        };
    }
   
    
    void draw(Graphics g) {
        if (animationThread != null)
        {
           if (System.currentTimeMillis()/500 % 2 == 0) {
               g.setColor(Color.red);
               g.fillRect(10, 10, 20, 20);
           }   
        }
        int mx = panel.getWidth()/2;
        int my = panel.getHeight()/2;

        g.drawLine(mx, my-10, mx, my+10);
        g.drawLine(mx-10, my, mx+10, my);
        
        scene.draw(g, scale, -50, 0);
        if (goldPanel != null)
            goldPanel.repaint();
    }

    void reset() {

        panel.repaint();
        moveX = 0;
        moveY = 0;
        moveZ = 0;

    }
    
    double[] handleInverse;
    void drag(double ax, double ay)
    {
        switch (activeObject) {
            case 0 :
                scene.setRotation(ax, ay, 0);
                scene.project();
                break;
            case 1 :
                angleX = ax;
                angleY = ay;
                moveRays2();
                rotateRays2();
                buildTask();
                scene.project();
                break;

            case 4:
                moveX = -ay*5;
                moveY = ax*5;
                moveRays2();
                rotateRays2();
                buildTask();
                scene.project();
                break;
            case 5:
                
                double[] x = new double[]{ax*handle.transformed[0][2], ay*handle.transformed[0][2], handle.transformed[0][2], 1};
                double[] y = new double[3];
                
                Algebra.multiply4_4x4(handleInverse, x, y);
                
                handle.vertex[0][0] = y[0];
                handle.vertex[0][1] = y[1];
                handle.vertex[0][2] = y[2];
                applyHandle();
                scene.project();
                
                break;
        }
        panel.repaint();
    }

    JPanel getGoldPanel(final JFrame frame)
    {
        goldPanel = new JPanel()
        {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Color.GRAY);
                stuff3D.draw(g, scale/4, origin_triangles, origin_projection_1, -10 - 100, -10);
                stuff3D.draw(g, scale/4, origin_triangles, origin_projection_2, +10 + 100, -10);
                if (gold_projection_1 != null)
                {
                    g.setColor(Color.WHITE);
                    stuff3D.draw(g, scale/4, origin_triangles, gold_projection_1, -10 - 100, 0);
                    stuff3D.draw(g, scale/4, origin_triangles, gold_projection_2, +10 + 100, 0);
                }
                
                g.drawString("E:" + goldError, 12, 12);
                g.drawString("minz:" + minimalZ, 12, 24);
                g.drawString("penalty:" + penalty, 12, 36);
                g.drawString("vec E:" + vectorError, 12, 48);
                goldScene.draw(g, 4*scale, 0, 0);
                
            }
        };
        
        goldPanel.setPreferredSize(new Dimension(300, 300));
        goldPanel.setBackground(Color.red);
        
        final boolean[] animate = {true};
        if (frame != null) {
            new Thread(new Runnable(){

                @Override
                public void run() {
                    System.out.println("running animation");
                    int safeCount = 30;
                    while (!frame.isVisible()) 
                    {
                        try{
                           Thread.sleep(1000);
                        } catch(InterruptedException e){}   
                        if (safeCount-- <= 0){
                            break;
                        }
                    }

                    while (frame.isVisible()) 
                    {
                        gold.rotate(0.04, 0.03, 0);
                        goldScene.project();
                        goldPanel.repaint();
                        try{
                           Thread.sleep(50);
                        } catch(InterruptedException e){}   
                    }
                    System.out.println("stopped animation");
                }
            }).start();
        }   
        
        return goldPanel;
        
    }
    
    public JPanel buildGui(JFrame frame)
    {

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };
        
        panel.setLayout(new FlowLayout());
        MouseAdapter ma = createMouseManipulator();
        panel.addMouseMotionListener(ma);
        panel.addMouseListener(ma);
        panel.setPreferredSize(new Dimension(700,500));


        JSlider unknownAngleInput = new JSlider(JSlider.HORIZONTAL, -100, 100, 0);
        unknownAngleInput.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider sldr = (JSlider)e.getSource();
                unknownAngle = (double)sldr.getValue()/100.0 * 2;
                buildTask();
                scene.project();
                goldScene.project();                
                panel.repaint();
            }
        });        
        
        JSlider scaleInput = new JSlider(JSlider.HORIZONTAL, 10, 400, 300);
        scaleInput.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider sldr = (JSlider)e.getSource();
                scale = (double)sldr.getValue();
                buildTask();
                scene.project();
                goldScene.project();                
                panel.repaint();
            }
        }); 
        
        JButton cheatButton = new JButton(new AbstractAction("cheat"){

            @Override
            public void actionPerformed(ActionEvent e) {
                cheat();
            }
        });
        
        JButton relaxButton = new JButton(new AbstractAction("r"){

            @Override
            public void actionPerformed(ActionEvent e) {
                animationThread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        relax();
                        animationThread = null;
                        panel.repaint();
                    }
                });
                animationThread.start();
            }
        });
        JButton evolveButton = new JButton(new AbstractAction("e"){

            @Override
            public void actionPerformed(ActionEvent e) {
                animationThread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        evolve();
                        animationThread = null;
                        panel.repaint();
                    }
                });
                animationThread.start();
            }
        });
        
        final JTextField mutationTf = new JTextField(5);
        mutationTf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ev) {
                if (mutationTf.getText().length() == 0)
                    return;
                try {
                    mutationStrength = new Double(mutationTf.getText());
                    System.out.println("mutation:" + mutationStrength);
                } catch(NumberFormatException e) 
                {
                    System.out.println("invalid double:" + mutationTf.getText());
                }    
            }
        });
        
        panel.setFocusable(true);
        panel.add(unknownAngleInput);
        panel.add(scaleInput);
        panel.add(mutationTf);
        panel.add(cheatButton);
        panel.add(relaxButton);
        panel.add(evolveButton);
        
        gui = new JPanel();
        gui.setLayout(new BorderLayout());
        gui.add(panel, BorderLayout.WEST);
        gui.add(graphPanel, BorderLayout.SOUTH);
        gui.add(getGoldPanel(frame), BorderLayout.CENTER);

        
//        frame = new JFrame("ta co neska?");
//        frame.getContentPane().setLayout(new BorderLayout());
//        frame.getContentPane().add();
//        frame.setSize(300, 300);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setVisible(true);
        
        gui.setPreferredSize(new Dimension(1100,200));
        gui.setBorder(new LineBorder(Color.RED, 1));
        
        return gui;
    }
    
    public void evolve()
    {
        Reactor reactor = new Reactor();

        RealAgent etalon = new RealAgent(PlaceOfRealAction.this);

        
        reactor.initPopulation( etalon, 70, mutationStrength*60, mutationStrength, 0.5);
        System.out.println("mutation:" + reactor.getMutationStrength());
        
        AbstractAgent boss = null;
        for (int j=0; j<3; j++) {
            for (int i=0; i<20; i++) {
                boss = reactor.iterate();
                boss.calcFitness();
                scene.project();
                panel.repaint();
            }
            reactor.setMutationStrength(reactor.getMutationStrength()/50);
        }    
        System.out.println("-" + boss.getFitness());
        System.out.println("    m:" + moveX + ", " + moveY + ", " + moveZ);
        System.out.println("    h:" + handle.vertex[0][0] + ", " + handle.vertex[0][1] + ", " + handle.vertex[0][2]);
    }

    
    public void relax()
    {
        
        AbstractReliever reliever = new AbstractReliever(getVector(), 2) {
            
            @Override
            public double getTension(double[] x) {
                setVector(x);
                return goldError;                
            }
        };
        
        ArrayList<Double> series = new ArrayList<>();
        int i;
        for (i=0; i<50; i++) {
            double error = reliever.relax();
            series.add(error);
            System.out.println("->" + error);
            if (Double.isNaN(error))
                return;
            panel.repaint();
                   
            if (reliever.isZipp()){
                System.out.println("ZIP!");
                reliever.halfStepSize();
            }
            
            try {
                Thread.sleep(100);  
            } catch(InterruptedException ex){}
        }      
        
        double[] data = new double[series.size()];
        for (i=0; i<data.length; i++) {
            data[i] = series.get(i);
        }
 
        
        graphPanel.clearGraphs();
        graphPanel.addGraph(data, "E");
        graphPanel.repaint();
    }
    
    public static void main(String[] args) {
        final JFrame frame = new JFrame("welcome back my friends...");
        JPanel p = new PlaceOfRealAction(SampleObject.platforms(5),  0.1, 0.15).buildGui(frame);
        
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(p, BorderLayout.CENTER);
        frame.setSize(1100, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }

}

