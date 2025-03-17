/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo;

import stereo.to3d.FtrLink;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import static java.awt.Image.SCALE_SMOOTH;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import stereo.poi.ApproachingPerfection;
import stereo.poi.ApproachingPerfection_sift;
import stereo.to3d.Face;
import stereo.to3d.FaceMetric;
import stereo.ui.GuiUtils;
import stereo.ui.LinkPane;
import stereo.ui.LinkPane_sift;
import stereo.ui.Manual;
import stereo_praha.Algebra;
import stereo_praha.gui.GraphPanel;
import stereo_praha.gui.PlaceOfAction_inspiration;
import stereo_praha.gui.StereoSolver;

/**
 *
 * @author Karol Presovsky
 */
public class Main
{     
        
    public static void main(String[] args)
    {        
//        sift();
//        gauss();
        links();
//        manual();
        
        // -----------------------------------------

    }
    
    public static void gauss() {
        GraphPanel gpanel = new GraphPanel();
        
        int size = 10;
        int sigma = 10;
        double[] g = new double[size];
        for (int i=0; i<size; i++) {
            g[i] = Algebra.gaussValue(i, sigma, 4);            
        }
        
        gpanel.addGraph(g, "gauss");
        double[][] gaussian = Algebra.createGausian(sigma);
        Greyscale gs = new Greyscale(gaussian.length, gaussian.length);
        
        for (int j=0; j<gs.height; j++) {
            for (int i=0; i<gs.width; i++) {
                double w = gaussian[j][i];
                w *= 100000;
                gs.px[j*gs.width + i] = (short)(w);                
            }            
        }
        BufferedImage img = gs.createImage(null);
        
        GuiUtils.frameIt(new JLabel(new ImageIcon(img)), 700, 300, null);
    }
    
    static BufferedImage loadImage(String filePath, int width) throws IOException  {
        Image img = ImageIO.read(new File(filePath));
        int height = (int)((double)width/img.getWidth(null)*img.getHeight(null));
        img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bi.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bi;
    }
    
    public static void links()
    {
        System.out.println("={links}=");
        
        BufferedImage img1;
        BufferedImage img2;
        try
        {
           img1 = loadImage("./imgs/cavango1.jpg", 400);
           img2 = loadImage("./imgs/cavango2.jpg", 400);
        } catch(IOException e)
        {
            e.printStackTrace();
            return;
        }              
       
        ApproachingPerfection perfect1 = new ApproachingPerfection(img1);
        ApproachingPerfection perfect2 = new ApproachingPerfection(img2);
//        ArrayList<Link> links = perfect1.findLinks(perfect2);
        final ArrayList<FtrLink> links = perfect1.findLinks(perfect2);
//        Histogram angleHisto = ApproachingPerfection.calcLinkAngleHistogram(links);
        
//        final ArrayList<Face> faces = ApproachingPerfection.triangulate(links);
        
//        ApproachingPerfection.pickWinners(links, faces);
        
        
        //----
//        int maxi = -1;
//        int max = -1;
//        for (int i=0; i<angleHisto.size(); i++)
//        {
//            if (angleHisto.graph[i] > max)
//            {
//                maxi = i;
//                max = angleHisto.graph[i];
//            }
//        }
//        
//        double angle = (double)maxi/angleHisto.size() * (angleHisto.maximum - angleHisto.minimum) + angleHisto.minimum;
//        angle = angle/Math.PI*180;
//        
//        System.out.println("detected angle:" + angle);
        
        //---
        
        
        LinkPane linkPane = new LinkPane(perfect1, perfect2, links);
//        linkPane.setFaceList(faces);
//        GraphPanel histoPanel = new GraphPanel();
//        histoPanel.addGraph(angleHisto.getGraph(), "angle histo");
//        histoPanel.addMark(angleHisto.size()/2);
        
//        histoPanel.add(new JButton(new AbstractAction("demco..."){
        
//           @Override
//           public void actionPerformed(ActionEvent e)
//           {
////               new PlaceOfRealAction(links, faces).demco();
//               System.out.println("not implemented");
//           }
//        
//        }));
        
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout());
//        frame.getContentPane().add(new JLabel(new ImageIcon(img)));
        frame.getContentPane().add(linkPane, BorderLayout.CENTER);
//        frame.getContentPane().add(histoPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setSize(1600, 600);
        frame.setLocation(10, 10);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);        
    }

         
    public static void sift()
    {
        System.out.println("={links}=");
        
        final BufferedImage img1;
        final BufferedImage img2;
        try
        {
           img1 = ImageIO.read(new File("./tehly1.jpg"));
           img2 = ImageIO.read(new File("./tehly2.jpg"));
        } catch(IOException e)
        {
            e.printStackTrace();
            return;
        }        
        
       
        ApproachingPerfection_sift perfect1 = new ApproachingPerfection_sift(img1);
        ApproachingPerfection_sift perfect2 = new ApproachingPerfection_sift(img2);
        
        LinkPane_sift linkPane = new LinkPane_sift(perfect1, perfect2, null);
        
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout());
//        frame.getContentPane().add(new JLabel(new ImageIcon(img)));
        frame.getContentPane().add(linkPane, BorderLayout.CENTER);
//        frame.getContentPane().add(histoPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setSize(1100, 600);
        frame.setLocation(100, 100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);        
    }

   
    public static void manual()
    {
        System.out.println("={manual}=");
        
        final BufferedImage img1;
        final BufferedImage img2;
        
        try
        {
           img1 = ImageIO.read(new File("./tehly1.jpg"));
           img2 = ImageIO.read(new File("./tehly2.jpg"));
        } catch(IOException e)
        {
            e.printStackTrace();
            return;
        }          
        
        Manual manual = new Manual(img1, img2);
        
            
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout());
//        frame.getContentPane().add(new JLabel(new ImageIcon(img)));
        frame.getContentPane().add(manual, BorderLayout.CENTER);
        frame.pack();
        frame.setLocation(100, 100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
    }
    
    
    

}

