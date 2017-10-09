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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static stereo_praha.gui.FieldsOfError.basicScale;

public class PlaceOfAction_inspiration extends StereoTask {

    double scale = 300;

    double[][] orig_vertex = new double[][] {
            {-10,-10, 0},
            { 10,-10, 0},
            {-10, 10, 0},
            { 10, 10, 0},

            {-10,-10, +20},
            { 10,-10, +20},
            {-10, 10, +20},
            { 10, 10, +20},

            {20,5, +10}

    };

    int [][] orig_triangles = new int[][] {
            {0,1,2,0}, {2,1,3,2}, {4,5,6,4}, {5,6,7,5},
            {5,6,8,5}
    };    
    
    
    double[][] sourceProjection = null;
    
    Object3D origin = null;
    Object3D rays1 = new Object3D();
    Object3D rays2 = new Object3D();
    Object3D plane1 = new Object3D();
    Object3D plane2 = new Object3D();
    Object3D outline1 = new Object3D();
    Object3D outline2 = new Object3D();
    Object3D gold = new Object3D();

    Scene3D scene = new Scene3D("scene");
    Scene3D ray2Subscene = new Scene3D("ray2Subscene");
    Scene3D originScene = new Scene3D("originScene");
    Scene3D goldScene = new Scene3D("goldScene");
    double invarianceError;
    double minimalZ;
    double penalty;
    
    ArrayList<Object3D> links;

    final GraphPanel graphPanel = new GraphPanel();

    JPanel panel;
    JPanel goldPanel;
    JTextField tfAy = new JTextField(5);
    JTextField tfAx = new JTextField(5);
    
    FieldsOfError fieldsOfError;
    

    double unknownAngle = 0.2;
    int activeObject = 0;
    
    double[][] origin_projection_1;
    double[][] origin_projection_2;
    double[] goldMatrix = new double[16];

    
    double mutationStrength = 0.5; 
    int test;

    public PlaceOfAction_inspiration() {

        init();
    }
    
    void createOrigin()
    {
        int stories = 4;
        int size = 5;
        
        orig_vertex = new double[stories*4][3];
        orig_triangles = new int[stories*4][2]; 
        
        int base_y = -stories*size/2;
        
        for (int j=0; j<stories; j++)
        {
            int x = -10;
            int z = -10;
            
            for (int i=0; i<4; i++)
            {
                orig_vertex[j*4 + i][0] = x;
                orig_vertex[j*4 + i][1] = base_y;
                orig_vertex[j*4 + i][2] = z;
                
                int tmp = -z;
                z = x;
                x = tmp;
            }
            
            base_y += size;
            
            for (int i=0; i<4; i++)
            {
               orig_triangles[j*4 + i][0] = j*4 + i;
               orig_triangles[j*4 + i][1] = j*4 + ((i + 1)%4);
            }
        }
       
        origin = new Object3D(orig_vertex, orig_triangles);
        origin.setTranslation(0, 0, 20);
        //origin.setRotation(Math.random()*Math.PI, Math.random()*Math.PI, Math.random()*Math.PI);
        
        originScene.add(origin);
        originScene.setTranslation(0,0,60);
        originScene.project();
    }
    
    
    Object3D createOutline(double[][] projected, Object3D obj, double scale)
    {
        if (obj == null) {
            obj = new Object3D();
            obj.setColor(Color.black);
        } 
        
        obj.init(projected.length, orig_triangles.length, orig_triangles[0].length);
        for(int i=0; i<projected.length; i++) {
            obj.vertex[i][0] = projected[i][0]*scale;
            obj.vertex[i][1] = projected[i][1]*scale;
            obj.vertex[i][2] = 0;
        }
        
        for(int i=0; i<orig_triangles.length; i++) {
            for(int j=0; j<orig_triangles[i].length; j++) { 
                obj.triangles[i][j] = orig_triangles[i][j];
            }
        } 
        
        return obj;
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
        origin.project();
        rays1 = SpringInspiration.objectFromProjection(origin.projected, rays1, focalLength, 5);
        outline1 = createOutline(origin.projected, outline1, 10);
        
        origin.rotate(unknownAngle, 0, 0);
        origin.project();
        
        rays2 = SpringInspiration.objectFromProjection(origin.projected, rays2, focalLength, 5);
        outline2 = createOutline(origin.projected, outline2, 10);
        
        origin.rotate(-unknownAngle, 0, 0);
        
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
        ray2Subscene.setTranslation(moveX, moveY, moveY);
    }
    
    public void rotateRays2()
    {
        ray2Subscene.setRotation(angleX, angleY, 0);
    }
    
    @Override
    public void applySolution()
    {
        rotateRays2();
        moveRays2();
        buildTask();
        reconstruction();
    }
    
    public void recalcFieldsOfError()
    {   
        fieldsOfError = new FieldsOfError(new ProblemInterface() {

            @Override
            public double[] calcError(double angelX, double angelY, double angelZ) {
                PlaceOfAction_inspiration.this.angleX = angelX;
                PlaceOfAction_inspiration.this.angleX = angelY;
                buildTask();
                return new double[]{reconstruction()};
            }
        });
        
        fieldsOfError.recalc(0, 0);
                
    }
            
    
    void init()
    {
        sourceProjection = new double[][] {
           {10, 10},
           {-10, 10},
           {-10, -10},
           {10, -10},
           
        };
        
//        for (int i=0; i<sourceProjection.length; i++) {
//            sourceProjection[i][0] = (Math.random()-0.5)*10;
//            sourceProjection[i][1] = (Math.random()-0.5)*10;
//        }

        createOrigin();
        
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
        
        buildTask();
        
        goldScene.add(gold);
        goldScene.setTranslation(0, 0, 60);
        
        System.out.println("..");

    }
    
    double reconstruction()
    {
        if (gold == null)
        {
            gold = new Object3D(links.size(), 1, links.size());
        } else {
            gold.init(links.size(), origin.triangles.length, origin.triangles[0].length);
        }
        double error = 0;
        double sumx = 0;
        double sumy = 0;
        double sumz = 0;
        double maxx = 0;
        double maxy = 0;
        double maxz = 0;
        double minx = 0;
        double miny = 0;
        double minz = 0;
        
        double maxe = 0;
                
        for (int i=0; i<links.size(); i++)
        {
            Object3D link = links.get(i);
            double x = (link.vertex[0][0] + link.vertex[1][0])/2;
            double y = (link.vertex[0][1] + link.vertex[1][1])/2;
            double z = (link.vertex[0][2] + link.vertex[1][2])/2;
            
            maxx = (x > maxx) ? x : maxx;
            maxy = (y > maxy) ? y : maxy;
            maxz = (z > maxz) ? z : maxz;

            minx = (x < minx) ? x : minx;
            miny = (y < miny) ? y : miny;
            minz = (z < minz) ? z : minz;
            
            sumx += gold.vertex[i][0] = x;
            sumy += gold.vertex[i][1] = y;
            sumz += gold.vertex[i][2] = z;
            
            double e = Math.abs(link.vertex[0][0] - link.vertex[1][0]); 
            e += Math.abs(link.vertex[0][1] - link.vertex[1][1]); 
            e += Math.abs(link.vertex[0][2] - link.vertex[1][2]); 
            
            maxe = (maxe < e) ? e : maxe;
            
            error += e;
            
        }
        minimalZ = minz;
        sumx /= links.size();
        sumy /= links.size();
        sumz /= links.size();
        
        for (int i=0; i<links.size(); i++)
        {
            gold.vertex[i][0] -= sumx;
            gold.vertex[i][1] -= sumy;
            gold.vertex[i][2] -= sumz;
        }
//        System.out.println("error:" + error);
        for (int j=0; j<origin.triangles.length; j++)
        {
            for (int i=0; i<origin.triangles[j].length; i++)
            {
                gold.triangles[j][i] = origin.triangles[j][i];
            }
        }
        
        gold.project();
        
        maxx -= minx;
        maxy -= miny;
        maxz -= minz;
        
        maxx = (maxx < maxy) ? maxy : maxx;
        maxx = (maxx < maxz) ? maxz : maxx;
        
        if (maxx < 0.000001)
            return Double.POSITIVE_INFINITY;
        
        error /= links.size();
        error *= 100;
        error *= maxe*maxe/focalLength;
        
        double w = focalLength + minz;           
        if (w < focalLength/20000)
            penalty = 100;
        else
        {
            penalty = focalLength/w/200;
        }
                
        error *= penalty;
        
        return error;
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

                MouseTracker trck = getTracker(e);
                trck.mouseDragged(e);
                drag(trck.mouseY/200.0, trck.mouseX/-200.0);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                MouseTracker trck = getTracker(e);
                trck.mousePressed(e); 
            }
        };
    }
    
//            double ay = mouseX / -200.0;
//            double ax = mouseY / 200.0;
//            drag(ax, ay);

   
    
    void draw(Graphics g) {
        int mx = panel.getWidth()/2;
        int my = panel.getHeight()/2;

        g.drawLine(mx, my-10, mx, my+10);
        g.drawLine(mx-10, my, mx+10, my);
        
        scene.draw(g, scale, -50, 0);
        if (goldPanel != null)
            goldPanel.repaint();
        originScene.draw(g, 100, 100,100);
    }

    void reset() {

        panel.repaint();
        moveX = 0;
        moveY = 0;
        moveZ = 0;

    }
    
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
            case 3 :
                originScene.setRotation(ax, ay, 0);
                originScene.project();
                break;
            case 4:
                moveX = ax;
                moveY = ay;
                moveRays2();
                rotateRays2();
                buildTask();
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
                g.drawString("E:" + goldError, 12, 12);
                g.drawString("minz:" + minimalZ, 12, 24);
                g.drawString("penalty:" + penalty, 12, 36);
                goldScene.draw(g, scale, 0, 0);
            }
        };
        
        goldPanel.setPreferredSize(new Dimension(300, 300));
        goldPanel.setBackground(Color.red);
        
        final boolean[] animate = {true};
        new Thread(new Runnable(){

            @Override
            public void run() {
                System.out.println("running animation");
                while (!frame.isVisible()) 
                {
                    try{
                       Thread.sleep(1000);
                    } catch(InterruptedException e){}   
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
        
        return goldPanel;
        
    }
    
    void demco()
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
        
        
        JSlider focalLengthInput = new JSlider(JSlider.HORIZONTAL, 1, 200, 10);
        focalLengthInput.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider sldr = (JSlider)e.getSource();
                focalLength = (double)sldr.getValue()/2.0;
                buildTask();
                scene.project();
                goldScene.project();                
                panel.repaint();
            }
        }); 
        
        JButton relaxButton = new JButton(new AbstractAction("r"){

            @Override
            public void actionPerformed(ActionEvent e) {
                relax();
            }
        });
        JButton evolveButton = new JButton(new AbstractAction("e"){

            @Override
            public void actionPerformed(ActionEvent e) {
                evolve();
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
        panel.add(focalLengthInput);
        panel.add(mutationTf);
        panel.add(relaxButton);
        panel.add(evolveButton);

        JFrame frame = new JFrame("welcome back my friends...");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.WEST);
        frame.getContentPane().add(graphPanel, BorderLayout.SOUTH);
        frame.getContentPane().add(fieldsOfError.getPanel(), BorderLayout.CENTER);
        frame.setSize(1100, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        frame = new JFrame("ta co neska?");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(getGoldPanel(frame));
        frame.setSize(300, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void evolve()
    {
        Reactor reactor = new Reactor();

        TaskAgent etalon = new TaskAgent(PlaceOfAction_inspiration.this);
        etalon.movex = moveX;
        etalon.movey = moveY;
        etalon.movez = moveZ;
        etalon.ax = angleX;
        etalon.ay = angleY;
        
        reactor.initPopulation( etalon, 20, mutationStrength*10, mutationStrength, 0.5);
        System.out.println("mutation:" + reactor.getMutationStrength());
        
        AbstractAgent boss = null;
        for (int j=0; j<5; j++) {
            System.out.println(j + ". mutation:" + reactor.getMutationStrength());
            for (int i=0; i<100; i++) {
                boss = reactor.iterate();
                boss.calcFitness();
                scene.project();
                panel.repaint();
            }
            System.out.println("-" + boss.getFitness());
            reactor.setMutationStrength(reactor.getMutationStrength()/50);
        }    
    }

    
    public void relax()
    {
        
        AbstractReliever reliever = new AbstractReliever(getVector(), 0.05) {
            
            @Override
            public double getTension(double[] x) {
                setVector(x);
                rotateRays2();
                moveRays2();
                buildTask();
                reconstruction();

                return goldError;                
            }
        };
        
        int i;
        for (i=0; i<100; i++) {
            double error = reliever.newton();
            System.out.println("->" + error);
        }      
        
        scene.project();
        panel.repaint();
    }
    
    public static void main(String[] args) {
        new PlaceOfAction_inspiration().demco();
    }

}

