/*
 * 
 * 
 */

package stereo.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JPanel;
import stereo.Outline;

/**
 *
 * @author Karol Presovsky
 */
public class SimpleVectorView extends JPanel
{
    ArrayList<int[]> vecs;

    ArrayList<int[]> maxims;

    Image img;
    boolean showVectors = true;
    boolean showImg = true;
    
    int imgWidth;
    int imgHeight;
    int maxmax;
    int maxmin;

    public void setMaxims(ArrayList<int[]> maxims)
    {
        this.maxims = maxims;
        maxmax = 0;
        maxmin = Integer.MAX_VALUE;
        for (int[] p:maxims)
        {
            if (maxmax < p[2])
                maxmax = p[2];
            if (maxmin > p[2])
                maxmin = p[2];
        }
        
        maxmax -= maxmin;
    }

    public SimpleVectorView(Image img, ArrayList<int[]> vecs, int w, int h)
    {
        this.vecs = vecs;

        this.imgWidth = w;
        this.imgHeight = h;
        
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(w,2*h));
        this.img = img;
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1)
                    showVectors = !showVectors;
                else
                    showImg = !showImg;
                repaint();
            }
        });
    }


    void paintVectors(Graphics g)
    {
        if (vecs == null)
            return;
        if (vecs == null) return;

        g.setColor(Color.yellow);
        for (int[] v:vecs)
        {
            g.drawLine(v[0], v[1], v[0] + v[2], v[1] + v[3]);
        }
    }

    void drawMaxims(Graphics g)
    {
        if (maxims != null)
        {
            g.setColor(Color.yellow);
            for (int i=0;i<maxims.size();i++)
            {
                int[] p = maxims.get(i);
                
                int r = (int)(20*(p[2]-maxmin)/maxmax);
                if (r < 2)
                    r = 2;
                
                g.drawOval(p[0]-r/2, p[1]-r/2, r, r);
            }
        }
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        
        g.drawImage(img, 0, imgHeight, null);

        if (showVectors)
        {
            paintVectors(g);

        }
        drawMaxims(g);
    }
}
