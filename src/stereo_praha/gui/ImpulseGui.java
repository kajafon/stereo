/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo_praha.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import stereo.ui.GuiUtils;
import stereo_praha.Algebra;
import stereo_praha.Impulse;

/**
 *
 * @author macbook
 */
public class ImpulseGui {
    
//    double[][] vertex = SampleObject.platforms(count)new double[][] {
//        { 5, 5,0}, 
//        {-5, 5,0},
//        {-5,-5,0}            
//    };
    
    int activeIndex = 0;
    JPanel panel = null;
    Scene3D scene = new Scene3D();
    Object3D object;
    Object3D previewObject;
    Object3D impulses;
    Object3D attractor;
    
    boolean switcher;
    
    final void updateImpulsesObj() {

        for (int i=0; i<object.transformed.length; i++) {
            Algebra.copy(object.transformed[i], impulses.vertex[i]);
            Algebra.copy(attractor.transformed[i], impulses.vertex[i+object.transformed.length]);
        }        
    }
    
    Impulse calcResultImpulse()
    {       
        Impulse impulse = new Impulse();
        impulse.init(object.transformed.length);
        
        double[] tmp = new double[3];
        for (int j=0; j<object.transformed.length; j++) {
            int i = switcher ? j : object.transformed.length - 1 - j;             
            Algebra.difference(attractor.transformed[i], object.transformed[i], tmp);
            impulse.add2(object.transformed[i], tmp);
        }         
        
        impulse.calc2();
        
        return impulse;        
    }
    
    void calcPreview() {
        
        Impulse impulse = calcResultImpulse();

        previewObject.setTranslation(Algebra.scale(impulse.translation, 0.3, new double[3]));
        Algebra.rotate3D(previewObject.matrix, impulse.rotation);                
    }
    
    void applyPulls()
    {
        object.project();
        attractor.project();
        Impulse impulse = calcResultImpulse();
        double[] rotation = new double[3];
        Algebra.copy(impulse.rotation, rotation);
        double rotationSize = Algebra.size(rotation);
        
        if (rotationSize > 0.00001) {
            rotationSize = 0.0003/Math.pow(object.vertex.length, 1.42);
            Algebra.scale(rotation, rotationSize);
        }
        System.out.println("" + rotationSize);
        Algebra.rotate3D(object.matrix, rotation);  
        
        Algebra.combine(object.translation, impulse.translation, object.translation);
        object.project();
        attractor.project();
        
        updateImpulsesObj();        
    }

    public ImpulseGui() {
//        object = SampleObject.hill();
//        attractor = SampleObject.hill();
        object = SampleObject.platforms(2);
        attractor = SampleObject.platforms(2);
        
        for(double[] v : attractor.vertex){
            Algebra.scale(v, 2);
        }
        
        int[][] lines = new int[object.vertex.length][];
        int[][] impLines = new int[object.vertex.length][];
        
//        double[] m = null;
//        double[] tmp = new double[3];
        
        for (int i=0; i<object.vertex.length; i++) {
            
//            m = Algebra.unity(m);
//            Algebra.scale(object.vertex[i], 0.05, tmp);
//            tmp[1] = 0;
//            tmp[0] = 0;
//                   
//            if (Algebra.size(tmp) > 0.00001) {
//                Algebra.rotate3D(m, tmp);
//            }
//
//            pulls[i] = new double[]{
//                m[0],
//                m[1],
//                m[2],
//            };
            
            lines[i] = new int[]{i, (i+1 == object.vertex.length ? 0 : i+1)};
            impLines[i] = new int[]{i, i + object.vertex.length};
        }
        
        previewObject = new Object3D(object.vertex, lines);
        previewObject.setColor(Color.yellow);
        impulses = new Object3D(object.vertex.length * 2, object.vertex.length, 2);
        impulses.triangles = impLines;
        impulses.setColor(Color.red);
        
        applyPulls();
        
        //////////////
        //   resultObject.setRotation(0.3, 0.2, 0.1);
        ////////////////
        
        scene.add(attractor);
        scene.add(object);
        scene.add(impulses);
//        scene.add(previewObject);
        scene.setTranslation(0, 0, 50);
        
        scene.project();
    }
    
    public JPanel getPanel() {
        
        if (panel == null){            
        
            panel = new JPanel(){
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g); 
                    scene.draw(g, 300, 0, 0);                    
                    stuff3D.draw(g, 300, null, object.projected, 0, 0);
                }
            };
            
            MouseTracker tracker = new MouseTracker() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    super.mouseDragged(e);
                    scene.setRotation(mouseY/200.0, mouseX/200.0, 0);
                    scene.project();
                    panel.repaint();
                }
            };
            
            panel.addMouseListener(tracker);
            panel.addMouseMotionListener(tracker);
            panel.add(new JButton(new AbstractAction("davaj het!") {                
                @Override
                public void actionPerformed(ActionEvent e) {
                    applyPulls();
                    scene.project();
                    panel.repaint();
                }
            }));
            panel.add(new JButton(new AbstractAction("A") {                
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (thread == null) {
                        startRelaxation();
                    } else {
                        thread = null;
                    }
                }
            }));
            panel.add(new JButton(new AbstractAction("!") {                
                @Override
                public void actionPerformed(ActionEvent e) {
                    Algebra.rotate3D(object.matrix, new double[]{
                        Algebra.rand(0, 3), Algebra.rand(0, 3), Algebra.rand(0, 3)
                    });
                    
                    object.setTranslation(new double[]{Algebra.rand(10, 20), Algebra.rand(10, 20), Algebra.rand(10, 20)});
                    
                    applyPulls();
                    scene.project();
                    panel.repaint();
                }
            }));
            panel.add(new JButton(new AbstractAction("><") {                
                @Override
                public void actionPerformed(ActionEvent e) {
                    switcher = !switcher;                    
                }
            }));

        }
        return panel;
    }
    
    Thread thread = null;
    
    void startRelaxation() {
        thread = new Thread(new Runnable(){
            @Override
            public void run() {
                while (thread != null) {
                    applyPulls();
                    scene.project();
                    panel.repaint();

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ImpulseGui.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                System.out.println("anim thread stopped");
            }            
        });
        thread.start();
    }
    
    public static void main(String[] args) {
        ImpulseGui gui = new ImpulseGui();
        GuiUtils.frameIt(gui.getPanel(), 500, 400, null);        
    }
        
        
        

    
}
