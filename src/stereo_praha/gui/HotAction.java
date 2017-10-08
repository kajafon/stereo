/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo_praha.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import stereo_praha.AbstractReliever;
import stereo_praha.Aggregator;
import stereo_praha.Algebra;

/**
 *
 * @author kajo
 */
public class HotAction {
    
    ArrayList<StereoSolver> solvers = new ArrayList<>();
    
    JPanel guiPanel;    
    GraphPanel graphPanel;
    JPanel ctrlPanel;
    
    Thread workerThread;
    JTextField errorDisplay;
    JTextField errorDisplay1;
    JTextField errorDisplay2;
    JTextField errorDisplay3;
    JTextField errorDisplay4;
    
    public HotAction(int solversCount) {
        init(solversCount);
    }
    
    public StereoSolver getSolver(int i) {
        return solvers.get(i);
    }
        
    public void init(int solversCount) {
        for (int i=0; i<solversCount; i++){
            StereoSolver solver = new StereoSolver(SampleObject.platforms(20), Math.random()*0.3 + 0.05, Math.random()*0.3 + 0.05);
            solvers.add(solver);
            solver.cheat();
        }
    }
    
    public JPanel getPanel(JFrame frame) {
        if (guiPanel != null) {
            return guiPanel;
        }
        graphPanel = new GraphPanel();
        JPanel p = new JPanel(new GridBagLayout());
        
        ctrlPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawString("ctrl panel", 15,15);
            }            

            @Override
            public void paint(Graphics g) {
                super.paint(g); 
                if (workerThread != null)
                {
                   if (System.currentTimeMillis()/200 % 2 == 0) {
                       g.setColor(Color.red);
                       
                       g.fillRect(getWidth()/2, 10, 20, 20);
                   }   
                }                
            }
        };
        
        ctrlPanel.add(new JButton(new AbstractAction("E?"){
            @Override
            public void actionPerformed(ActionEvent e) {
                double er = compareGolds();
                System.out.println("golden error: " + er);
                
            }
        }));
               
        ctrlPanel.add(new JButton(new AbstractAction("T"){
            @Override
            public void actionPerformed(ActionEvent e) {
                double[] v = getVector();
                setVector(v);
                
            }
        }));        

        ctrlPanel.add(new JButton(new AbstractAction("Relax!"){
            @Override
            public void actionPerformed(ActionEvent e) {
                relax();                
            }
        }));     
        
        ctrlPanel.add(errorDisplay = new JTextField(7));
        ctrlPanel.add(errorDisplay1 = new JTextField(7));
        ctrlPanel.add(errorDisplay2 = new JTextField(7));
        ctrlPanel.add(errorDisplay3 = new JTextField(7));
        ctrlPanel.add(errorDisplay4 = new JTextField(7));
        
        ctrlPanel.setBorder(new LineBorder(Color.yellow, 1));
        ctrlPanel.setPreferredSize(new Dimension(200,50));
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        p.add(ctrlPanel, c);
        c.weighty = 1;        

        for (StereoSolver solver : solvers) {            
            c.gridy++;
            p.add(new ActionGui(solver).getMainPanel(), c);
        }
        
        c.gridy++;
        c.weighty = 0;
        graphPanel.setPreferredSize(new Dimension(500, 50));
        p.add(graphPanel, c);
        
        return guiPanel = p;  
    }
    
    public double compareGolds()
    {
        ArrayList<Aggregator> verticeAggr = new ArrayList<>();
        
        // init aggregator for every vertice in gold object
        for (int i=0; i<solvers.get(0).getGold().vertex.length; i++)
        {
            verticeAggr.add(new Aggregator(3));
        }
        
        // for all vertices accross all versions of gold aggregate found coordinates
        for (int i=0; i<solvers.size(); i++)
        {  
            Object3D gold = solvers.get(i).getGold();
            for (int j=0; j<gold.vertex.length; j++)
            {
               verticeAggr.get(j).add(gold.vertex[j]);
            }   
        }
        
        // 1 store average gold vertices
        // 2 aggregate vertices for estimation of size of gold object
        Aggregator sizeAgg = new Aggregator(3);
        ArrayList<double[]> verticeAvg = new ArrayList<>();
        for (Aggregator ag : verticeAggr)
        {
            double[] vertice = ag.getAverage();
            verticeAvg.add(vertice);
            sizeAgg.add(vertice);
        }
        
        // aggregate deviations from average vertice position in average gold object
        Aggregator errorAgg = new Aggregator(3);
        double[] tmp = new double[3];
        for (int i=0; i<solvers.size(); i++)
        {  
            Object3D gold = solvers.get(i).getGold();
            for (int j=0; j<gold.vertex.length; j++)
            {
               Algebra.difference(verticeAvg.get(j), gold.vertex[j], tmp);
               tmp[0] = Math.abs(tmp[0]);
               tmp[1] = Math.abs(tmp[1]);
               tmp[2] = Math.abs(tmp[2]);
               errorAgg.add(tmp);
            }   
        }
        
        // calc symbolic scalar deviation from average gold object
        double[] _e = errorAgg.getAverage();
        double e = _e[0]*_e[0] + _e[1]*_e[1] + _e[2]*_e[2];
        
        e /= sizeAgg.getSize();
        
        return e;
    }
    
    public double[] getVector()
    {
        double[] vec = new double[6*solvers.size()];
        
        for (int i=0; i<solvers.size(); i++) {
            double[] v = solvers.get(i).getVector();
            System.arraycopy(v, 0, vec, i*6, 6);
        }
        return vec;        
    }
    
    public void setVector(double[] vec)
    {
        double[] v = new double[6];
        
        for (int i=0; i<solvers.size(); i++) {            
            System.arraycopy(vec, i*6, v, 0, 6);
            solvers.get(i).setVector(v);            
        }

        getPanel(null).repaint();        
    }
  
    public void relax()
    {
        AbstractReliever reliever = new AbstractReliever(getVector(), 1) {            
            @Override
            public double getTension(double[] x) {
                setVector(x);
                double es = 0;
                for (StereoSolver solver : solvers)
                {
                    es += solver.goldError * solver.goldError;
                }

                double eg = 4*compareGolds();
                
                eg *= eg;
                
                double e = eg + es;
                
                errorDisplay1.setText("dG: " + String.format( "%.6f", eg ));
                errorDisplay2.setText("es: " + String.format( "%.6f", es));
                
                errorDisplay.setText("" + String.format( "%.6f", e));
                ctrlPanel.repaint();
                return e;                
            }
        };
        
        reliever.setErrorToStepFnc(new double[][]{  
           {0.000001, 0.1},
           {0.0001, 0.5},
           {0.001, 1},
           {2.0, 1.5}
        });
        
        workerThread = new Thread(new Runnable()
        {
            public void run() {        
                AbstractReliever.relax_routine(reliever, guiPanel, graphPanel);
                workerThread = null;
            }
        });
        
        workerThread.start();    
    }    
    
    public static void main(String[] args) {
        final JFrame frame = new JFrame("welcome back my friends...");
        JPanel p = new HotAction(4).getPanel(frame);
        
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(p, BorderLayout.CENTER);
        frame.setSize(1500, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }    
    
}
