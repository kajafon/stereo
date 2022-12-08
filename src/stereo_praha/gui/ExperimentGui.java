/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo_praha.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import stereo.ui.GuiUtils;
import stereo_praha.AbstractReliever;
import stereo_praha.Algebra;
import stereo_praha.ProblemInterface;

/**
 *
 * @author macbook
 */
public class ExperimentGui {
    
    NewStereoSolver solver;
    int activeObject = 0;
    Thread animationThread = null;
    JPanel gui;   
    JPanel panel;
    JPanel goldPanel;
    GraphPanel graphPanel = new GraphPanel();
    MouseTracker mouseTracker = new MouseTracker();

    
    double scale = 500;

    public void relax() {
        solver.relaxAndReconstruct();             
    }

    public ExperimentGui(NewStereoSolver solver) {
        this.solver = solver;
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
        return new MouseAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseTracker.mouseDragged(e);
                drag(mouseTracker.mouseX, mouseTracker.mouseY);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseTracker.mousePressed(e); 
                panel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseTracker.mouseReleased(e);
            }            
        };
    }   
    
    void draw(Graphics g) {

        int mx = panel.getWidth()/2;
        int my = panel.getHeight()/2;
       
        solver.scene.draw(g, scale, 0, 0);
        if (goldPanel != null)
            goldPanel.repaint();
    }

    void drag(double mx, double my)
    {
        solver.scene.setRotation(my / 300, -mx / 300, 0);
        solver.scene.project();
        panel.repaint();
    }
    
    public JPanel getMainPanel()
    {
        if (gui != null)
            return gui;
        
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
        
        JButton relaxButton = new JButton(new AbstractAction("relax"){

            @Override
            public void actionPerformed(ActionEvent e) {
                relax();
                panel.repaint();
            }
        });
        JButton randomizeButton = new JButton(new AbstractAction("randomize"){

            @Override
            public void actionPerformed(ActionEvent e) {
                solver.randomize();
                panel.repaint();
            }
        });
        
        panel.setFocusable(true);
        panel.add(relaxButton);
        panel.add(randomizeButton);
        
        gui = new JPanel();
        gui.setLayout(new BorderLayout());
        gui.add(panel, BorderLayout.CENTER);
        gui.setPreferredSize(new Dimension(1100,200));
        gui.setBorder(new LineBorder(Color.RED, 1));
        
        return gui;
    }

    public static void main(String[] args) {
        
        NewStereoSolver solver = new NewStereoSolver(SampleObject.platforms(3),  0.1, 0.15);
        ExperimentGui gui = new ExperimentGui(solver);
        
        JPanel p = gui.getMainPanel();
        
        GuiUtils.frameIt(p, 1100, 500, new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                
            }
         });
    }
}
