/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import stereo.RelaxationKit;

/**
 *
 * @author karol presovsky
 */
public class RelaxPane extends JPanel
{

    JLabel title;
    KitDisplay kitDisplay;
    int kitIndex = 0;
    RelaxationKit kit;

    public RelaxPane(RelaxationKit kit)
    {
        this.kit = kit;

        kitDisplay = new KitDisplay(kit);

        setLayout(new BorderLayout());
        add(kitDisplay, BorderLayout.CENTER);
        title = new JLabel();
        add(title, BorderLayout.NORTH);
        JPanel bottom = new JPanel();
        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        JButton automationBtn = new JButton();

        bottom.add(automationBtn);
        bottom.add(prevBtn);
        bottom.add(nextBtn);
        add(bottom, BorderLayout.SOUTH);
        
        previousKit();
        
        automationBtn.setAction(new AbstractAction("Relax") 
        {
            public void actionPerformed(ActionEvent ae)
            {
                RelaxPane.this.kit.relation.relaxation();
                kitDisplay.repaint();
            }
        });
        
        bottom.add(new JButton(new AbstractAction("Iteration") 
        {
            public void actionPerformed(ActionEvent ae)
            {
                RelaxPane.this.kit.relation.iteration();
                kitDisplay.repaint();
            }
        }));
        
        prevBtn.setAction(new AbstractAction("prev.")
        {

            public void actionPerformed(ActionEvent ae)
            {
                previousKit();

            }
        });


        nextBtn.setAction(new AbstractAction("next")
        {

            public void actionPerformed(ActionEvent ae)
            {
                nextKit();
            }
        });
        
        bottom.add(new JButton(new AbstractAction("<")
        {
            public void actionPerformed(ActionEvent ae)
            {
                if (kitDisplay != null)
                    kitDisplay.xoffset(-1);
            }
        }));

        bottom.add(new JButton(new AbstractAction(">")
        {
            public void actionPerformed(ActionEvent ae)
            {
                if (kitDisplay != null)
                    kitDisplay.xoffset(1);
            }
        }));
        bottom.add(new JButton(new AbstractAction("^")
        {
            public void actionPerformed(ActionEvent ae)
            {
                if (kitDisplay != null)
                    kitDisplay.yoffset(-1);
            }
        }));
        bottom.add(new JButton(new AbstractAction("v")
        {
            public void actionPerformed(ActionEvent ae)
            {
                if (kitDisplay != null)
                    kitDisplay.yoffset(1);
            }
        }));   
        final JCheckBox check = new JCheckBox();
        check.setSelected(true);
        bottom.add(check);
        check.setAction(new AbstractAction("vectors on") 
        {
            public void actionPerformed(ActionEvent ae)
            {
                kitDisplay.displayingVectors = check.isSelected();
                kitDisplay.repaint();
            }
        });
        
        
    }

    private boolean previousKit()
    {

        kitIndex--;
        if (kitIndex < 0)
        {
            kitIndex = kitDisplay.relaxKit.greys1.size() - 1;
        }
        if (kitIndex < 0)
        {
            System.out.println("error: no kit display in relaxpane");
            return true;
        }
 
        setKit();
        return false;
    }

    private boolean nextKit()
    {
        if (kitDisplay.relaxKit.greys1.size() == 0)
        {
            System.out.println("error: no kit display in relaxpane");
            return true;
        }

        kitIndex++;
        if (kitIndex >= kitDisplay.relaxKit.greys1.size())
        {
            kitIndex = 0;
        }

        setKit();
        return false;
    }
    
    private boolean setKit()
    {
        //BufferedImage ei = kitDisplay.relaxKit.relation.errorImg;
//        add(kitDisplay, BorderLayout.CENTER);

        title.setText("kit : " + kitIndex);
        kitDisplay.setCurrentLevel(kitIndex);
        revalidate();
        repaint();        
        return false;
    }    
}
