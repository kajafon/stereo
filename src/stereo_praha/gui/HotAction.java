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
import javax.swing.border.LineBorder;

/**
 *
 * @author kajo
 */
public class HotAction {
    
    PlaceOfRealAction alfa;
    PlaceOfRealAction beta;
    
    public HotAction() {
        init();
    }
        
    public void init() {
        alfa = new PlaceOfRealAction(SampleObject.platforms(5), 0.1, 0.15);
        beta = new PlaceOfRealAction(SampleObject.platforms(5), 0.13, 0.27);        
    }
    
    public JPanel getPanel(JFrame frame) {
        JPanel p = new JPanel(new GridBagLayout());
        
        JPanel ctrlPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawString("ctrl panel", 15,15);
            }            
        };
        
        ctrlPanel.add(new JButton(new AbstractAction("E?"){
            @Override
            public void actionPerformed(ActionEvent e) {
                double er = compareGolds();
                System.out.println("golden error: " + er);
                
            }
        }));
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
        
        return p;  
    }
    
    public double compareGolds()
    {
        Object3D g1 = alfa.getGold();
        Object3D g2 = beta.getGold();
        
        double e_sum = 0;
        
        for (int i=0; i<g1.vertex.length; i++)
        {
            double vx = g1.vertex[i][0] - g2.vertex[i][0];
            double vy = g1.vertex[i][1] - g2.vertex[i][1];
            double vz = g1.vertex[i][2] - g2.vertex[i][2];
            
            e_sum += vx*vx + vy*vy + vz*vz;
        }
        return e_sum / g1.vertex.length;
    }
    
    public static void main(String[] args) {
        final JFrame frame = new JFrame("welcome back my friends...");
        JPanel p = new HotAction().getPanel(frame);
        
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(p, BorderLayout.CENTER);
        frame.setSize(1100, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }    
    
}
