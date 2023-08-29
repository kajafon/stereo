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
import java.util.ArrayList;
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
import stereo.ui.MultiNavigator;
import stereo.ui.Sequencer;
import stereo.ui.SolutionNavigator;
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
    GraphPanel graphPanel = new GraphPanel();
    MouseTracker mouseTracker = new MouseTracker();
    MouseTracker mouseTracker_move = new MouseTracker();

    double[] superscene_matrix = new double[16];
    
    double scale = 700;
    
    JTextField errorTextField = null;

    public void relax(boolean useOtherSolvers) {
        solver.relaxAndReconstruct(useOtherSolvers);   
        errorTextField.setText(String.format("%.6f", solver.reconstructionError));        
    }

    public ExperimentGui(NewStereoSolver solver) {
        this.solver = solver;
        
        solver.addRepaintListener(new Runnable() {
            @Override
            public void run() {
                if (gui != null) {
                    gui.repaint();
                }
            }            
        });
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
        synchronized(solver) {
            solver.project(superscene_matrix);

            Algebra.copy(solver.scene.tmp_matrix, solver.scene._ctrl_matrix);
            solver.scene.draw(g, scale, sceneOffsetX, sceneOffsetY);

            for (int i=0; i<solver.scene.tmp_matrix.length; i++) {
                if (solver.scene.tmp_matrix[i] != solver.scene._ctrl_matrix[i]) {
                    System.out.println("!!!! KOKOT");
                }
            }
        }
    }

    void drag(double mx, double my)
    {
        stuff3D.setRotation(superscene_matrix, my / 300, -mx / 300, 0);
        panel.repaint();
    }

    boolean useOtherSolvers = false;

    GraphPanel errGrapPanel;
    GraphPanel travelGrapPanel;
    
    Sequencer sequencer = new Sequencer();
    boolean errSeriesUpdated = false;
    
    public JPanel createMainPanel()
    {        
        System.out.println("solver knows " + solver.otherSolvers.size() + " other solvers");
        if (gui != null)
            return gui;
        
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
                
                if (sequencer.isRunning()) {
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
        
//        JButton relaxButton = new JButton(new AbstractAction("relax"){
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                relax(useOtherSolvers);
//                panel.repaint();
//            }
//        });

        JButton relaxXButton = new JButton(new AbstractAction("relax x"){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!sequencer.isRunning()) {
                    sequencer.runSequence(() -> {
                        relax(useOtherSolvers);
                        solver.notifyRepaint();
                    }, null);            
                } else {
                    sequencer.donateSteps(100);
                }                    
            }
        });
        JButton probeButton = new JButton(new AbstractAction("probe!"){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!sequencer.isRunning()) {                    
                    sequencer.runSequence(new SolutionNavigator(solver), gui);
                } else {
                    sequencer.donateSteps(100);                        
                }                    
            }
        });
        JButton showGoldButton = new JButton(new AbstractAction("show gold"){
            @Override
            public void actionPerformed(ActionEvent e) {                
                if (!sequencer.isRunning()) {
                    sequencer.runSequence(new Runnable(){                    
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
                    }, gui);
                }
            }
        });
        JButton randomizeButton = new JButton(new AbstractAction("randomize"){
            @Override
            public void actionPerformed(ActionEvent e) {
                solver.randomize();
                panel.repaint();
            }
        });
//        JButton rollButton = new JButton(new AbstractAction("roll"){
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                solver.roll();                
//                panel.repaint();
//            }
//        });        
        
//        JButton centerButton = new JButton(new AbstractAction("center"){
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                sceneOffsetX = 0;
//                sceneOffsetY = 0;
//                panel.repaint();
//            }
//        }); 
//        JButton placeItButton = new JButton(new AbstractAction("place it!"){
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                solver.placeIt();
//                panel.repaint();
//            }
//        }); 
//        JButton copyRaysButton = new JButton(new AbstractAction("copy other rays 2"){
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                solver.copyOtherRays2(solver.otherSolvers.iterator().next());
//                panel.repaint();
//            }
//        });
        JCheckBox showOtherRaysChbx = new JCheckBox(new AbstractAction("show other rays 2"){
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox cbLog = (JCheckBox) e.getSource();
                solver.otherRays2.setVisible(cbLog.isSelected());
                panel.repaint();
            }
        });
        
        showOtherRaysChbx.setSelected(true);
        
        JCheckBox useOtherSolversChbx = new JCheckBox(new AbstractAction("use Others"){
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox cbLog = (JCheckBox) e.getSource();
                useOtherSolvers = cbLog.isSelected();
                panel.repaint();
            }
        });
        useOtherSolversChbx.setSelected(useOtherSolvers);
        
        errorTextField = new JTextField(10);
        panel.setFocusable(true);
        errGrapPanel = new GraphPanel();
        errGrapPanel.addGraph(solver.errSeries, "");
        travelGrapPanel = new GraphPanel();
        travelGrapPanel.addGraph(solver.travelSeries, "");
        travelGrapPanel.setColor(Color.ORANGE);
        
        panel.add(useOtherSolversChbx);
        panel.add(probeButton);
        panel.add(relaxXButton);
        panel.add(randomizeButton);
//        panel.add(rollButton);
        panel.add(showGoldButton);
//        panel.add(centerButton);
//        panel.add(placeItButton);
//        panel.add(copyRaysButton);
        panel.add(showOtherRaysChbx);
//        panel.add(errorTextField);

        errGrapPanel.setPreferredSize(new Dimension(100, 40));
        panel.add(errGrapPanel);       
        travelGrapPanel.setPreferredSize(new Dimension(100, 40));
        panel.add(travelGrapPanel);       
        
        
        gui = new JPanel();
        gui.setLayout(new BorderLayout());
        gui.add(panel, BorderLayout.CENTER);
        gui.setPreferredSize(new Dimension(1100,200));
        gui.setBorder(new LineBorder(Color.RED, 1));
        
        return gui;
    }
    
    static double rndA() {
        return (Math.random()-0.5)* 2 * 0.3;
    }
    
    public static void main(String[] args) {
        
        int solversCount = 4;
        
        
        double[][] a = new double[][] {
            {rndA(), rndA()},
            {rndA(), rndA()},
            {rndA(), rndA()},
            {rndA(), rndA()},
        };
//        double[][] a = new double[][] {
//            {0.05, 0.30},
//            {-0.1, -0.55},
//            {0.1, -0.30},
//            {0.2, 0.35},
//        };
        
        ArrayList<NewStereoSolver> solvers = new ArrayList();
        ArrayList<ExperimentGui> guis = new ArrayList();
        
        MultiNavigator navigator = new MultiNavigator();
        
        for (int i=0; i<solversCount; i++) {
            NewStereoSolver solver = new NewStereoSolver(SampleObject.hill(), a[i][0], a[i][1]);
            solvers.add(solver);
            navigator.add(solver);
            guis.add(new ExperimentGui(solver));
        }

        try {
            for (int i=0; i<solvers.size(); i++) {
                for (int j=0; j<solvers.size(); j++){
                    if (j == i) {
                        continue;
                    }
                    solvers.get(i).addOtherSolver(solvers.get(j));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
        
        Sequencer sequencer = new Sequencer();
                
        JPanel basePanel = new JPanel();
        
        basePanel.setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel();
        
        topPanel.add(new JButton(new AbstractAction("stop") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                sequencer.stop();             
            }            
        }));

        JPanel p = new JPanel();
        
        topPanel.add(new JButton(new AbstractAction("resume") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                sequencer.runSequence(navigator, null);            
            }            
        }));
        topPanel.setPreferredSize(new Dimension(200, 60));
        
        final GraphPanel travelGraphPanel = new GraphPanel();
        travelGraphPanel.addGraph(navigator.travelSeries, "");
        travelGraphPanel.setPreferredSize(new Dimension(100, 50));
        topPanel.add(travelGraphPanel);
        
//        topPanel.setBorder(new LineBorder(Color.RED, 10));
        basePanel.add(topPanel, BorderLayout.NORTH);
        
        
        basePanel.add(p, BorderLayout.CENTER);
        p.setLayout(new GridLayout(2,2));
        
        for (int i=0; i<guis.size(); i++) {
            p.add(guis.get(i).createMainPanel());
        }

        navigator.multiRelaxCallback = new Runnable() {
            @Override
            public void run() {
                sequencer.stop();
            }            
        };
        
        navigator.addRepaintListener(new Runnable() {
            @Override
            public void run() {
                travelGraphPanel.repaint();
            }
        });
        
        sequencer.runSequence(navigator, null);
        sequencer.donateSteps(100000);
        
        GuiUtils.frameIt(basePanel, 1100, 500, new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                
            }
        });
    }

    
    /** _multi_solvers_manual */
    public static void main_multi_solvers_manual(String[] args) {
        
        int solversCount = 4;
        
        double[][] a = new double[][] {
            {0.05, 0.30},
            {-0.1, -0.55},
            {0.1, -0.30},
            {0.2, 0.35},
        };
        
        ArrayList<NewStereoSolver> solvers = new ArrayList();
        ArrayList<ExperimentGui> guis = new ArrayList();
        
        for (int i=0; i<solversCount; i++) {
            NewStereoSolver solver = new NewStereoSolver(SampleObject.platforms(3), a[i][0], a[i][1]);
            solvers.add(solver);
            guis.add(new ExperimentGui(solver));
        }

        try {
            for (int i=0; i<solvers.size(); i++) {
                for (int j=0; j<solvers.size(); j++){
                    if (j == i) {
                        continue;
                    }
                    solvers.get(i).addOtherSolver(solvers.get(j));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
        
        JPanel p = new JPanel();
        
        p.setLayout(new GridLayout(2,2));
        
        for (int i=0; i<guis.size(); i++) {
            p.add(guis.get(i).createMainPanel());
        }
        
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
