/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import stereo.Greyscale;
import stereo.poi.SiftStamp;
import stereo_praha.Algebra;

class StampView extends JPanel {
    double[][] stamp1 = null;
    double[][] stamp2 = null;
    
    Image stamp1Img;
    Image stamp2Img;
    int imgsize = 100;
    
    double error;
    double error2;
    double errorResult;
    
    GraphPanel siftDesc = new GraphPanel();
    
    public StampView(int size) {
        imgsize = size;                
        setPreferredSize(new Dimension(imgsize *2 + 100, 2 *(imgsize + 50)));        
        
        stampView.setBorder(new LineBorder(Color.BLUE, 2));
        stampView.setPreferredSize(new Dimension(120, 60));
        siftDesc.setPreferredSize(new Dimension(100, 50));
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(stampView, gbc);
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridy++;
        add(siftDesc, gbc);
    }
    
    Image createImage(double[][] stamp) {        
        Greyscale gs = new Greyscale(stamp[0].length, stamp.length);
        
        for (int j=0; j<stamp.length; j++) {
            for (int i=0; i<stamp.length; i++) {
                double v = stamp[j][i] + 120;
                v = Math.min(255, v);
                v = Math.max(0, v);
                gs.px[j*gs.width + i] = (short)v;
            }            
        }
        
        BufferedImage img = gs.createImage(null);
        return img.getScaledInstance(imgsize, imgsize, 0);        
    }
    
    double midVal1;
    double midVal2;
    int shiftx;
    int shifty;
    SiftStamp sift1; 
    SiftStamp sift2;
            
    public void setSifts(SiftStamp sift1, SiftStamp sift2) {
        this.sift1 = sift1;
        siftDesc.clearGraphs();
        if (sift1 != null) {
            siftDesc.addGraph(new int[]{sift1.x, sift1.y}, "sift1");
        }
        this.sift2 = sift2;        
        if (sift2 != null) {
            siftDesc.addGraph(new int[]{sift2.x, sift2.y}, "sift2");
        }
        stampView.repaint();
        repaint();
    }
    
    public void setStamps(double[][] s1, double[][]s2, double[][] weights) {
        stamp1 = s1;
        stamp2 = s2;
                
        if (stamp1 != null && stamp2 != null) {
            error = Algebra.compare(stamp1, stamp2);            
//            error2 = Math.abs(midVal_1 - midVal_2) / (double)(midVal_1 + midVal_2) * 2;
//            errorResult = error * Math.pow(Math.E, error2);
//            
            error = Math.floor(error * 10000)/10000;
//            error2 = Math.floor(error2 * 10000)/10000;
//            errorResult = Math.floor(errorResult * 10000)/10000;
            
//            System.out.println("e:" + error);            
        }
        
        stamp1Img = null;
        stamp2Img = null;
        
        if (stamp1 != null) {
            stamp1Img = createImage(stamp1);        
        }
        if (stamp2 != null) {
            stamp2Img = createImage(stamp2);
        }
        
        repaint();
        stampView.repaint();
    }
    
    JPanel stampView = new JPanel(){ 
        @Override
        protected void paintComponent(Graphics g) {
            System.out.println("---- stamp view repaint");
            super.paintComponent(g); 
            int yoffset = 50;
            int padding = 10;
            g.drawString("" + error + " | " + error2 + " => " + errorResult + " [ " + shiftx + ", " + shifty + "]", 10, 20);
            if (stamp1Img != null) {
                g.drawImage(stamp1Img,0, yoffset, null);        
            }
            if (stamp2Img != null) {
                g.drawImage(stamp2Img,imgsize + padding, yoffset, null);
            }

            if (sift1 != null) {
                int xs = imgsize/2;
                int ys = imgsize/2 + yoffset;
                int x2 = xs + (int)(imgsize/2 * Math.cos(sift1.angle));
                int y2 = ys + (int)(imgsize/2 * Math.sin(sift1.angle));
                g.setColor(Color.yellow);
                g.drawLine(xs, ys, x2, y2);
            }

            if (sift2 != null) {
                int xs = 3*imgsize/2 + padding;
                int ys = imgsize/2 + yoffset;
                int x2 = xs + (int)(imgsize/2 * Math.cos(sift2.angle));
                int y2 = ys + (int)(imgsize/2 * Math.sin(sift2.angle));
                g.setColor(Color.yellow);
                g.drawLine(xs, ys, x2, y2);
            }

        }
    };
    
    public void setFlag(boolean val) {
        if (val) {
            setBorder(new LineBorder(Color.red, 3));
        } else {
            setBorder(null);
        }
        repaint();
    }
}

