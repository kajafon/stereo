/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo_praha.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JPanel;
import stereo.ui.GuiUtils;
import stereo_praha.Algebra;
import stereo_praha.Impulse;

/**
 *
 * @author macbook
 */
public class ImpulseGui {
    
    double[][] vertex = new double[][] {
        { 5, 5,0}, 
        {-5, 5,0},
        {-5,-5,0}            
    };
    
    double[][] pulls;
    int activeIndex = 0;
    JPanel panel = null;
    Scene3D scene = new Scene3D();
    Object3D object;
    Object3D resultObject;
    Object3D impulses;
    
    final void updateImpulsesObj() {
        for (int i=0; i<vertex.length; i++) {
            Algebra.copy(object.vertex[i], impulses.vertex[i]);
            Algebra.combine(impulses.vertex[i], pulls[i], impulses.vertex[i+vertex.length]);
        }        
    }
    
    void applyImpulses() {
        
        Impulse impulse = new Impulse();
        
        for (int i=0; i<vertex.length; i++) {
            impulse.add(object.vertex[i], pulls[i]);
            Algebra.copy(object.vertex[i], resultObject.vertex[i]); 
        }                
        
        resultObject.setTranslation(Algebra.scale(impulse.translation, 0.3, new double[3]));
        Algebra.rotate3D(resultObject.matrix, impulse.rotation);
        
        
    }

    public ImpulseGui() {
        pulls = new double[vertex.length][];
        int[][] lines = new int[vertex.length][];
        int[][] impLines = new int[vertex.length][];
        
        for (int i=0; i<vertex.length; i++) {
            pulls[i] = new double[]{
                Math.random() - 0.5,
                Math.random() - 0.5,
                Math.random() - 0.5,
            };
            
            lines[i] = new int[]{i, (i+1 == vertex.length ? 0 : i+1)};
            impLines[i] = new int[]{i, i + vertex.length};
        }
        
        object = new Object3D(vertex, lines);
        resultObject = new Object3D(vertex.length, lines.length, 2);
        resultObject.triangles = lines;
        resultObject.setColor(Color.yellow);
        impulses = new Object3D(vertex.length * 2, vertex.length, 2);
        impulses.triangles = impLines;
        
        updateImpulsesObj();
        applyImpulses();
        
        //////////////
        //   resultObject.setRotation(0.3, 0.2, 0.1);
        ////////////////
        
        scene.add(object);
        scene.add(impulses);
        scene.add(resultObject);
        scene.setTranslation(0, 0, 30);
        scene.project();
    }
    
    public JPanel getPanel() {
        
        if (panel == null){            
        
            panel = new JPanel(){
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g); 
                    scene.draw(g, 300, 0, 0);
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
        }
        return panel;
    }
    
    public static void main(String[] args) {
        ImpulseGui gui = new ImpulseGui();
        GuiUtils.frameIt(gui.getPanel(), 500, 400, null);        
    }
        
        
        

    
}
