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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
    MouseTracker mouseTracker_move = new MouseTracker();

    double[] superscene_matrix = new double[16];
    
    double scale = 700;
    
    JTextField errorTextField = null;

    public void relax() {
        solver.relaxAndReconstruct();   
        errorTextField.setText(String.format("%.6f", solver.reconstructionError));        
    }

    public ExperimentGui(NewStereoSolver solver) {
        this.solver = solver;
        Algebra.unity(superscene_matrix);
        Algebra.setPosition(superscene_matrix, new double[]{0,0,60});
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
            int btn;
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (btn == MouseEvent.BUTTON1) {
                    mouseTracker.mouseDragged(e);
                    drag(mouseTracker.mouseX, mouseTracker.mouseY);
                } else {
                    mouseTracker_move.mouseDragged(e);
                    sceneOffsetX += mouseTracker_move.dx / 2;
                    sceneOffsetY += mouseTracker_move.dy / 2;
                }
                panel.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                btn = e.getButton();
                if (btn == MouseEvent.BUTTON1) {
                    mouseTracker.mousePressed(e);   
                } else {
                    mouseTracker_move.mousePressed(e);   
                }
                panel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (btn == MouseEvent.BUTTON1) {
                    mouseTracker.mouseReleased(e);
                } else {
                    mouseTracker_move.mouseReleased(e);
                }
            }            
        };
    }   
    
    int sceneOffsetX = 0;
    int sceneOffsetY = 0;
    
    void draw(Graphics g) {
//        int mx = panel.getWidth()/2;
//        int my = panel.getHeight()/2;
       
        solver.scene.project(superscene_matrix);
        solver.scene.draw(g, scale, sceneOffsetX, sceneOffsetY);
        if (goldPanel != null)
            goldPanel.repaint();
    }

    void drag(double mx, double my)
    {
//        solver.scene.setRotation(my / 300, -mx / 300, 0);
        stuff3D.setRotation(superscene_matrix, my / 300, -mx / 300, 0);
        panel.repaint();
    }
    
    Runnable runningSequence = null;
    
    void runSequence(Runnable runnable) {
        runningSequence = new Runnable() {
            @Override
            public void run() {
                for(int i=0; i<100 && runningSequence == this; i++) {
                    runnable.run();
                    panel.repaint();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {                                
                    }
                }                
                if (runningSequence == this) {
                    runningSequence = null;
                }
                System.out.println("sequence finished");
                panel.repaint();
            }
        };
        new Thread(runningSequence).start();
    }

    public JPanel createMainPanel()
    {
        if (gui != null)
            return gui;
        
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
                
                if (runningSequence != null) {
                    g.setColor(Color.red);
                    g.fillRect(10,10, 30, 30);
                }                
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
        JButton relaxXButton = new JButton(new AbstractAction("relax x"){
            @Override
            public void actionPerformed(ActionEvent e) {
                runSequence(() -> {
                    relax();
                });            
            }
        });
        JButton showGoldButton = new JButton(new AbstractAction("show gold"){
            @Override
            public void actionPerformed(ActionEvent e) {                
                runSequence(new Runnable(){                    
                    int cntr = 0;
                    @Override 
                    public void run() {
                        if (cntr < 50) {
                            solver.rotateGold(0.1, Algebra.AXIS_Y);                
                        } else {
                            solver.rotateGold(0.1, Algebra.AXIS_Z);                
                        }
                        cntr++;
                    }
                });
            }
        });
        JButton randomizeButton = new JButton(new AbstractAction("randomize"){
            @Override
            public void actionPerformed(ActionEvent e) {
                solver.randomize();
                panel.repaint();
            }
        });
        JButton rollButton = new JButton(new AbstractAction("roll"){
            @Override
            public void actionPerformed(ActionEvent e) {
                solver.roll();                
                panel.repaint();
            }
        });        
        
        JButton centerButton = new JButton(new AbstractAction("center"){
            @Override
            public void actionPerformed(ActionEvent e) {
                sceneOffsetX = 0;
                sceneOffsetY = 0;
                panel.repaint();
            }
        }); 
        JButton placeItButton = new JButton(new AbstractAction("place it!"){
            @Override
            public void actionPerformed(ActionEvent e) {
                solver.placeIt();
                panel.repaint();
            }
        }); 
        JButton copyGoldButton = new JButton(new AbstractAction("copy other gold"){
            @Override
            public void actionPerformed(ActionEvent e) {
                solver.copyOtherGold(solver.otherSolvers.iterator().next());
                panel.repaint();
            }
        }); 
        JButton copyRaysButton = new JButton(new AbstractAction("copy other rays 2"){
            @Override
            public void actionPerformed(ActionEvent e) {
                solver.copyOtherRays2(solver.otherSolvers.iterator().next());
                panel.repaint();
            }
        });
        JCheckBox showGoldChbx = new JCheckBox(new AbstractAction("show gold"){
            @Override
            public void actionPerformed(ActionEvent e) {
                 solver.gold.setVisible(!solver.gold.isVisible());
                 panel.repaint();
            }
        });
        JCheckBox showOtherRaysChbx = new JCheckBox(new AbstractAction("show other rays 2"){
            @Override
            public void actionPerformed(ActionEvent e) {
                 solver.otherRays2.setVisible(!solver.otherRays2.isVisible());
                 panel.repaint();
            }
        });
        
        errorTextField = new JTextField(10);
        panel.setFocusable(true);
        panel.add(relaxButton);
        panel.add(relaxXButton);
        panel.add(randomizeButton);
        panel.add(rollButton);
        panel.add(showGoldButton);
        panel.add(centerButton);
        panel.add(placeItButton);
        panel.add(copyGoldButton);
        panel.add(copyRaysButton);
        panel.add(showGoldChbx);
        panel.add(showOtherRaysChbx);
        panel.add(errorTextField);
        
        gui = new JPanel();
        gui.setLayout(new BorderLayout());
        gui.add(panel, BorderLayout.CENTER);
        gui.setPreferredSize(new Dimension(1100,200));
        gui.setBorder(new LineBorder(Color.RED, 1));
        
        return gui;
    }
    
    /** 2 solvers */
    public static void main(String[] args) {
        
        NewStereoSolver solver = new NewStereoSolver(SampleObject.platforms(3),  0.1, 0.15);
        ExperimentGui gui = new ExperimentGui(solver);

        NewStereoSolver solver2 = new NewStereoSolver(SampleObject.platforms(3),  -0.2, -0.25);
        ExperimentGui gui2 = new ExperimentGui(solver2);
        
        NewStereoSolver solver3 = new NewStereoSolver(SampleObject.platforms(3),  0.2, -0.10);
        ExperimentGui gui3 = new ExperimentGui(solver3);

        System.out.println("solvers equal:" + solver.equals(solver2));
        
        try {
            solver.addOtherSolver(solver2);
            solver.addOtherSolver(solver3);
            solver2.addOtherSolver(solver);
            solver2.addOtherSolver(solver3);
            solver3.addOtherSolver(solver);
            solver3.addOtherSolver(solver2);

        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
        
        JPanel p = new JPanel();
        
        p.setLayout(new GridLayout(2,2));
        
                
        p.add(gui.createMainPanel());
        p.add(gui2.createMainPanel());
        p.add(gui3.createMainPanel());
        
        GuiUtils.frameIt(p, 1100, 500, new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                
            }
         });
    }

    /** 1 solver */
    public static void main_1_solver(String[] args) {
        
        NewStereoSolver solver = new NewStereoSolver(SampleObject.platforms(3),  0.1, 0.15);
        ExperimentGui gui = new ExperimentGui(solver);

        JPanel p = gui.createMainPanel();
        
        GuiUtils.frameIt(p, 1100, 500, new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                
            }
         });
    }


}
