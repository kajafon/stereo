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
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import stereo_praha.AbstractReliever;
import stereo_praha.Aggregator;

/**
 *
 * @author kajo
 */
public class HotAction {
    
    PlaceOfRealAction alfa;
    PlaceOfRealAction beta;
    
    JPanel guiPanel;
    
    GraphPanel graphPanel;
    
    Thread workerThread;
    JTextField errorDisplay;
    
    public HotAction() {
        init();
    }
        
    public void init() {
        alfa = new PlaceOfRealAction(SampleObject.platforms(5), Math.random()*0.3 + 0.05, Math.random()*0.3 + 0.05);
        beta = new PlaceOfRealAction(SampleObject.platforms(5), Math.random()*0.3 + 0.05, Math.random()*0.3 + 0.05);        
        
        alfa.cheat();
        beta.cheat();
    }
    
    public JPanel getPanel(JFrame frame) {
        if (guiPanel != null) {
            return guiPanel;
        }
        graphPanel = new GraphPanel();
        JPanel p = new JPanel(new GridBagLayout());
        
        JPanel ctrlPanel = new JPanel(){
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
        
        ctrlPanel.add(errorDisplay = new JTextField(6));
        
        ctrlPanel.setBorder(new LineBorder(Color.yellow, 1));
        ctrlPanel.setPreferredSize(new Dimension(200,50));
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        p.add(ctrlPanel, c);
        
        c.gridy = 2;
        c.weighty = 1;        
        p.add(alfa.buildGui(frame), c);
        
        c.gridy = 3;
        p.add(beta.buildGui(frame), c);
        
        c.gridy = 4;
        c.weighty = 0;
        graphPanel.setPreferredSize(new Dimension(500, 50));
        p.add(graphPanel, c);
        
        return guiPanel = p;  
    }
    
    public double compareGolds()
    {
        Object3D g1 = alfa.getGold();
        Object3D g2 = beta.getGold();
        
        double e_sum = 0;
        
        Aggregator a1 = new Aggregator(3);
        Aggregator a2 = new Aggregator(3);
        
        for (int i=0; i<g1.vertex.length; i++)
        {
            a1.add(g1.vertex[i]);
            a2.add(g2.vertex[i]);
            double vx = g1.vertex[i][0] - g2.vertex[i][0];
            double vy = g1.vertex[i][1] - g2.vertex[i][1];
            double vz = g1.vertex[i][2] - g2.vertex[i][2];
            
            e_sum += vx*vx + vy*vy + vz*vz;
        }
        
        e_sum /= g1.vertex.length;
        e_sum /= a1.getSize() + a2.getSize();
        
        return e_sum;
    }
    
    public double[] getVector()
    {
        double[] vec = new double[12];
        double[] v = alfa.getVector();
        System.arraycopy(v, 0, vec, 0, 6);
        v = beta.getVector();
        System.arraycopy(v, 0, vec, 6, 6);
        return vec;        
    }
    
    public void setVector(double[] vec)
    {
        alfa.setVector(vec);
        double[] v = new double[6];
        System.arraycopy(vec, 6, v, 0, 6);
        beta.setVector(v);
        getPanel(null).repaint();        
    }
  
    public void relax()
    {
        AbstractReliever reliever = new AbstractReliever(getVector(), 1) {            
            @Override
            public double getTension(double[] x) {
                setVector(x);
                double e1 = alfa.getError();
                double e2 = beta.getError();
                double ef = alfa.getFocalLength() - beta.getFocalLength();
                ef *= ef;
                double e = compareGolds();
                
                e = 0.2*e + e1 + e2 + ef*0.3;
                
                errorDisplay.setText("" + e);
                errorDisplay.repaint();
                return e;                
            }
        };
        
        workerThread = new Thread(new Runnable()
        {
            public void run() {        
                PlaceOfRealAction.relax_routine(reliever, guiPanel, graphPanel);
                workerThread = null;
            }
        });
        
        workerThread.start();    
    }    
    
    public static void main(String[] args) {
        final JFrame frame = new JFrame("welcome back my friends...");
        JPanel p = new HotAction().getPanel(frame);
        
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(p, BorderLayout.CENTER);
        frame.setSize(1500, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }    
    
}
