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
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class ActionGuiOnImpulse {
    
    ImpulseSolver solver;
    int activeObject = 0;
    Thread animationThread = null;
    JPanel gui;   
    JPanel panel;
    JPanel goldPanel;
    GraphPanel graphPanel = new GraphPanel();
    double[] handleInverse;
    
    int _terribleCounter;


    public void relax() {
        
        boolean solved = solver.relax(_terribleCounter++);
        solver.project();
        panel.repaint();
//        if (solved) {
        System.out.println(_terribleCounter + ", relax -> E:" + solver.goldError);
//        }
    }

    public ActionGuiOnImpulse(ImpulseSolver solver) {
        this.solver = solver;
    }
    
    double xGuiToObject(double x)
    {
        return ((x - panel.getWidth()/2) + 50)/solver.scale;
    }
    
    double yGuiToObject(double y)
    {
        return (y - panel.getHeight()/2)/solver.scale;
    }    

    MouseAdapter createMouseManipulator()
    {
        final MouseTracker sceneTracker = new MouseTracker();
        final MouseTracker ray2rotationTracker = new MouseTracker();
        final MouseTracker ray2offsetTracker = new MouseTracker();
//        final MouseTracker taskTracker = new MouseTracker();
        
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
                    activeObject = 5;
                    trck = ray2rotationTracker;
                } else if (alt && !(meta || ctrl)) {
                    activeObject = 4;
                    trck = ray2offsetTracker;
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
        
        solver.scene.draw(g, solver.scale, -50, 0);
        if (goldPanel != null)
            goldPanel.repaint();
    }

    void drag(double ax, double ay)
    {
        switch (activeObject) {
            case 0 :
                solver.scene.setRotation(ax, ay, 0);
                break;
                
            case 4:
                double ox = ax; //xGuiToObject(ax);
                double oy = ay; //yGuiToObject(ay);
                solver.ray2Subscene.setTranslation(ox, oy, 0);
                solver.reconstruction();
                
                System.out.println("trasnl: " + ox + ", " + oy + "   " + solver.ray2Subscene.translation[0] + "," + solver.ray2Subscene.translation[0] );
                break;
                        
            case 5:
                
                solver.ray2Subscene.setRotation(ax, ay, 0);
                solver.reconstruction();
                
                break;
        }
        
        solver.scene.project();
        panel.repaint();
    }

    boolean guiLive = true; 
    
    public void guiKilled()
    {
        guiLive = false;
    }
    
    JPanel getGoldPanel()
    {
        if (goldPanel != null)
            return goldPanel;
        
        goldPanel = new JPanel()
        {
            @Override
            protected void paintComponent(Graphics g) {
                double scale = solver.scale;
                
                g.setColor(Color.GRAY);
                stuff3D.draw(g, scale/4, solver.origin_triangles, solver.origin_projection_1, -10 - 100, -10);
                stuff3D.draw(g, scale/4, solver.origin_triangles, solver.origin_projection_2, +10 + 100, -10);
                if (solver.gold_projection_1 != null)
                {
                    g.setColor(Color.WHITE);
                    stuff3D.draw(g, scale/4, solver.origin_triangles, solver.gold_projection_1, -10 - 100, 0);
                    stuff3D.draw(g, scale/4, solver.origin_triangles, solver.gold_projection_2, +10 + 100, 0);
                }
                
                g.drawString("E:" + solver.goldError, 12, 12);
                solver.goldScene.draw(g, 4*scale, 0, 0);
                
            }
        };
        
        goldPanel.setPreferredSize(new Dimension(300, 300));
        goldPanel.setBackground(Color.red);
        
        final boolean[] animate = {true};
        new Thread(new Runnable(){

            @Override
            public void run() {
                System.out.println("running animation");

                while (guiLive) 
                {
                    solver.gold.rotate(0.04, 0.03, 0);
                    solver.goldScene.project();
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
              
        JSlider scaleInput = new JSlider(JSlider.HORIZONTAL, 10, 400, 300);
        scaleInput.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider sldr = (JSlider)e.getSource();
                solver.scale = (double)sldr.getValue();
                solver.buildTask();
                solver.scene.project();
                solver.goldScene.project();                
                panel.repaint();
            }
        }); 
        
        JButton cheatButton = new JButton(new AbstractAction("cheat"){

            @Override
            public void actionPerformed(ActionEvent e) {
                solver.cheat();
                panel.repaint();
            }
        });

        JButton impulseButton = new JButton(new AbstractAction("I!"){

            @Override
            public void actionPerformed(ActionEvent e) {
                relax();
            }
        });
        
        
        JButton relaxButton = new JButton(new AbstractAction("A"){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (animationThread == null) {
                    animationThread = new Thread(new Runnable(){
                        @Override
                        public void run() {
                            solver.solve(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        solver.project();
                                        panel.repaint();
                                        Thread.sleep(100);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(ActionGuiOnImpulse.class.getName()).log(Level.SEVERE, null, ex);
                                    }

                                }
                            });
//                            while(animationThread != null && guiLive) {
//                               relax();    
//                                try {
//                                    Thread.sleep(100);
//                                } catch (InterruptedException ex) {
//                                    Logger.getLogger(ActionGuiOnImpulse.class.getName()).log(Level.SEVERE, null, ex);
//                                }
//                            }
//                            System.out.println("animation thread stopped ");
//                            panel.repaint();
                        }
                    });
                    animationThread.start();
                } else {
                    animationThread = null;
                }
                    
            }
        });
        
        
        panel.setFocusable(true);
        panel.add(scaleInput);
//        panel.add(mutationTf);
        panel.add(cheatButton);
        panel.add(impulseButton);
        panel.add(relaxButton);
        
        gui = new JPanel();
        gui.setLayout(new BorderLayout());
        gui.add(panel, BorderLayout.CENTER);
        gui.add(graphPanel, BorderLayout.SOUTH);
        gui.add(getGoldPanel(), BorderLayout.EAST);
        gui.setPreferredSize(new Dimension(1100,200));
        gui.setBorder(new LineBorder(Color.RED, 1));
        
        return gui;
    }
    
    

    public static void main(String[] args) {
        
        ImpulseSolver solver = new ImpulseSolver(SampleObject.hill(),  0.1, 0.15);
        ActionGuiOnImpulse gui = new ActionGuiOnImpulse(solver);
        JPanel p = gui.getMainPanel();
        
        GuiUtils.frameIt(p, 1100, 500, new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                gui.guiKilled();                
            }
         });

    }


}
