/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.ui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author macbook
 */
public class GuiUtils {
    
    
    public static JFrame frameIt(JComponent p, int width, int height, WindowAdapter windowAdapter) {
        final JFrame frame = new JFrame("welcome back my friends...");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(p, BorderLayout.CENTER);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(windowAdapter);
        frame.setVisible(true);
        return frame;
    }

    
}
