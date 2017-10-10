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
public class ActionGui {
    
    StereoSolver solver;
    int activeObject = 0;
    Thread animationThread = null;
    JPanel gui;   
    JPanel panel;
    JPanel goldPanel;
    GraphPanel graphPanel = new GraphPanel();

    public void relax() {
        AbstractReliever reliever = solver.getReliever(fieldsOfError_pointer.getName());
        reliever.setStepSize(fieldsOfError_pointer.getStep());
        reliever.setIsQuitOnZip(true);
        AbstractReliever.relax_routine(reliever, panel, graphPanel);
        fieldsOfError_pointer.createPath(reliever.getPath(), Color.black);
        panel.repaint();
    }

    public ActionGui(StereoSolver solver) {
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
                double boxSize = 10/solver.scale;

                if (mx > solver.handle.projected[0][0]-boxSize && mx < solver.handle.projected[0][0] + boxSize &&
                    my > solver.handle.projected[0][1]-boxSize && my < solver.handle.projected[0][1] + boxSize)
                {
                    activeObject = 5;
                    solver.scene.project();
                    handleInverse = Algebra.calcInverse(solver.handle.tmp_matrix, null);
                } else {
                    MouseTracker trck = getTracker(e);
                    trck.mousePressed(e); 
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (activeObject == 5)
                {
                    solver.applyHandle();
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
        
        solver.scene.draw(g, solver.scale, -50, 0);
        if (goldPanel != null)
            goldPanel.repaint();
    }

    void reset() {
        panel.repaint();
        solver.moveX = 0;
        solver.moveY = 0;
        solver.moveZ = 0;
    }
    
    double[] handleInverse;
    void drag(double ax, double ay)
    {
        switch (activeObject) {
            case 0 :
                solver.scene.setRotation(ax, ay, 0);
                solver.scene.project();
                break;
            case 1 :
                solver.angleX = ax;
                solver.angleY = ay;
                solver.moveRays2();
                solver.rotateRays2();
                solver.buildTask();
                solver.scene.project();
                break;

            case 4:
                solver.moveX = -ay*5;
                solver.moveY = ax*5;
                solver.moveRays2();
                solver.rotateRays2();
                solver.buildTask();
                solver.scene.project();
                break;
            case 5:
                Object3D handle = solver.handle;
                double[] x = new double[]{ax*handle.transformed[0][2], ay*handle.transformed[0][2], handle.transformed[0][2], 1};
                double[] y = new double[3];
                
                Algebra.multiply4_4x4(handleInverse, x, y);
                
                handle.vertex[0][0] = y[0];
                handle.vertex[0][1] = y[1];
                handle.vertex[0][2] = y[2];
                solver.applyHandle();
                solver.scene.project();
                
                break;
        }
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
                g.drawString("minz:" + solver.minimalZ, 12, 24);
                g.drawString("penalty:" + solver.penalty, 12, 36);
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
    
    FieldsOfError fieldsOfError_pointer;
    FieldsOfError fieldsOfError_xy;
    FieldsOfError fieldsOfError_angles;
    StereoSolver.Adapter2d solve2d;
    
    
    private FieldsOfError buildFieldsOfErrors(String what)
    {
        solve2d = solver.getAdapter2d(what);
        FieldsOfError fieldsOfError = new FieldsOfError((ProblemInterface)solve2d);
        fieldsOfError.setName(what);
        
        fieldsOfError.addMarkListener(new FieldsOfError.MarkListener() {
            public void marked(double x, double y) {
                System.out.println("marked: " + x + "," + y);
                solve2d.setVector(new double[]{x,y});
                panel.repaint();
            }
        });
        
        if (what.equals("xy")){
            fieldsOfError.setLimits(-4, 4, 20);
        } else {
            fieldsOfError.setLimits(-0.08, 0.08, 20);
        }

        recalcFieldsOfErrors(fieldsOfError);
       
        return fieldsOfError;        
    }
    
    private void recalcFieldsOfErrors(FieldsOfError fieldsOfError){
        StereoSolver.Adapter2d adapter = (StereoSolver.Adapter2d)fieldsOfError.getTemporaryProblem();
        double[] vec = adapter.getVector();
        fieldsOfError.recalc(vec[0], vec[1]);
        adapter.setVector(vec);
        if (panel != null) panel.repaint();
    }
    private void recalcFieldsOfErrors_threaded() {
        if (fieldsOfError_pointer.buildListener == null) {
            fieldsOfError_pointer.buildListener = new Runnable() {
                @Override
                public void run() {
                    if (panel != null) {
                        solver.project();
                        panel.repaint();
                        try {
                            Thread.sleep(10);
                        } catch(InterruptedException ex) {

                        }    
                    }
                }
            }; 
        } 
        
        new Thread() {
            @Override
            public void run() {
                recalcFieldsOfErrors(fieldsOfError_pointer);
            }
        }.start();
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
        
        JButton switchFieldsOfErrorsButton = new JButton(new AbstractAction("!FE"){

            @Override
            public void actionPerformed(ActionEvent e) {
                gui.remove(fieldsOfError_pointer.getPanel());
                if (fieldsOfError_pointer == fieldsOfError_angles) {
                    fieldsOfError_pointer = fieldsOfError_xy;
                } else {
                    fieldsOfError_pointer = fieldsOfError_angles;
                }
                recalcFieldsOfErrors(fieldsOfError_pointer);
                gui.add(fieldsOfError_pointer.getPanel(), BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            }
        });
        
        JButton relaxButton = new JButton(new AbstractAction("r"){

            @Override
            public void actionPerformed(ActionEvent e) {
//                animationThread = new Thread(new Runnable(){
//                    @Override
//                    public void run() {
                        relax();
//                        animationThread = null;
//                        panel.repaint();
//                    }
//                });
//                animationThread.start();
            }
        });
//        JButton evolveButton = new JButton(new AbstractAction("e"){
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                animationThread = new Thread(new Runnable(){
//                    @Override
//                    public void run() {
//                        solver.evolve(panel);
//                        animationThread = null;
//                        panel.repaint();
//                    }
//                });
//                animationThread.start();
//            }
//        });
        
//        final JTextField mutationTf = new JTextField(5);
//        mutationTf.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyReleased(KeyEvent ev) {
//                if (mutationTf.getText().length() == 0)
//                    return;
//                try {
//                    solver.mutationStrength = new Double(mutationTf.getText());
//                    System.out.println("mutation:" + solver.mutationStrength);
//                } catch(NumberFormatException e) 
//                {
//                    System.out.println("invalid double:" + mutationTf.getText());
//                }    
//            }
//        });
        
        JButton recalcErrorsBtn = new JButton(new AbstractAction("recalc E"){

            @Override
            public void actionPerformed(ActionEvent e) {
                recalcFieldsOfErrors_threaded();
            }
        });
        
        fieldsOfError_angles = buildFieldsOfErrors("angles");
        fieldsOfError_xy = buildFieldsOfErrors("xy");
        fieldsOfError_pointer = fieldsOfError_xy;
        
        panel.setFocusable(true);
        panel.add(scaleInput);
//        panel.add(mutationTf);
        panel.add(cheatButton);
        panel.add(relaxButton);
        panel.add(recalcErrorsBtn);        
        panel.add(switchFieldsOfErrorsButton);        
//        panel.add(evolveButton);
        
        gui = new JPanel();
        gui.setLayout(new BorderLayout());
        gui.add(panel, BorderLayout.WEST);
        gui.add(graphPanel, BorderLayout.SOUTH);
        gui.add(fieldsOfError_pointer.getPanel(), BorderLayout.CENTER);
        gui.setPreferredSize(new Dimension(1100,200));
        gui.setBorder(new LineBorder(Color.RED, 1));
        
        return gui;
    }
    
    

    public static void main(String[] args) {
        
        StereoSolver solver = new StereoSolver(SampleObject.platforms(3),  0.1, 0.15);
        ActionGui gui = new ActionGui(solver);
        JPanel p = gui.getMainPanel();
        
        GuiUtils.frameIt(p, 1100, 500, new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                gui.guiKilled();                
            }
         });

    }


}
