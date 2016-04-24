package stereo.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import javax.swing.JPanel;
import stereo.AproxyGrid;
import stereo.Greyscale;
import stereo.Node;
import stereo.RelaxationKit;

/*
 * 
 * 
 */
/**
 *
 * @author Karol Presovsky
 */
public class KitDisplay extends JPanel
{

    public RelaxationKit relaxKit;
    int xoffs = 0;
    int yoffs = 0;
    int currentLevel = 0;
    boolean img1Version;
    boolean displayingVectors = true;

    public int getCurrentLevel()
    {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel)
    {
        this.currentLevel = currentLevel;
        relaxKit.processLevel(currentLevel);
    }

    public KitDisplay(RelaxationKit kit)
    {
        this.relaxKit = kit;
        final Dimension dimension = new Dimension(2 * kit.img1.getWidth() + kit.img2.getWidth(), 2*kit.img1.getHeight());
        setPreferredSize(dimension);
        addMouseListener(new MouseAdapter()
        {

            @Override
            public void mousePressed(MouseEvent me)
            {
                img1Version = !img1Version;
                repaint();
            }
        });
    }

    public void xoffset(int off)
    {
        xoffs += off;
        relaxKit.approxy.translate((float)off/relaxKit.img1.getWidth(), 0);
        relaxKit.relation.applyTransform();
        relaxKit.relation.calcErrorForDisplay(currentLevel);
        
        //relaxKit.relation.calcErrorVectors((currentLevel + 1), xoffs, yoffs);
        repaint();
    }

    public void yoffset(int off)
    {
        yoffs += off;
        relaxKit.approxy.translate(0, (float)off/relaxKit.img1.getHeight());
        relaxKit.relation.applyTransform();
        relaxKit.relation.calcErrorForDisplay(currentLevel);
        repaint();
    }

    public static float getScale(int imgWidth, int imgHeight, int scrWidth, int scrHeight)
    {
        float w = imgWidth;
        float h = imgHeight;

        if (w == 0 || h == 0)
        {
            return 0f;
        }

        w = scrWidth / w;
        h = scrHeight / h;

        if (h < w)
        {
            w = h;
        }

        return w;
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);


        if (currentLevel < 0 || currentLevel >= relaxKit.greys1.size())
        {
            System.out.println("kitdiplay current level " + currentLevel + " out of boundaries.");
            return;
        }

        BufferedImage gs1 = relaxKit.relation.getResultImage();
        BufferedImage gs2 = relaxKit.images2.get(currentLevel);

        float w1 = getScale(gs1.getWidth(), gs1.getHeight(), getWidth() / 3, getHeight());
        float w2 = getScale(gs2.getWidth(), gs2.getHeight(), getWidth() / 3, getHeight());

        g.drawImage(gs2, getWidth() / 3, (int) (w2 / 2), (int) (gs2.getWidth() * w2), (int) (gs2.getHeight() * w2), null);

        if (img1Version)
        {
            g.drawImage(gs1, 0, 0, (int) (gs1.getWidth() * w1), (int) (gs1.getHeight() * w1), null);
        } else
        {
            g.drawImage(gs2, 0, 0, (int) (gs2.getWidth() * w2), (int) (gs2.getHeight() * w2), null);
        }

        BufferedImage errorImg = relaxKit.relation.getErrorImg();
        if (errorImg != null)
        {
            g.drawImage(errorImg, getWidth() * 2 / 3, 0,
                    (int) (errorImg.getWidth() * w1), (int) (errorImg.getHeight() * w1), (ImageObserver) this);
        } else
        {
            g.drawString("no error img", 10, (int) (gs1.getHeight() * w2) + 30);
        }

        paintGrid(g, getWidth()*2/3, 0, (int)(gs1.getWidth()*w1), (int)(gs1.getHeight()*w1));
        
        if (displayingVectors)
           paintVectors(g, getWidth()*2/3, 0, (int)(gs1.getWidth()*w1), (int)(gs1.getHeight()*w1));
        
        /////
//        
//        int y = (int) (gs1.getHeight() * w1);
//        gs1 = relaxKit.relation.errorDisplay;
//        g.drawImage(gs1, 0, y, (int) (gs2.getWidth() * w2), (int) (gs2.getHeight() * w2), null);
//
//        gs1 = relaxKit.relation.errGradDisplay;
//        g.drawImage(gs1, (int) (gs2.getWidth() * w2)  , y, (int) (gs2.getWidth() * w2), (int) (gs2.getHeight() * w2), null);
//
//        gs1 = relaxKit.relation.combinedDisplay;
//        g.drawImage(gs1, (int) (2*gs2.getWidth() * w2), y, (int) (gs2.getWidth() * w2), (int) (gs2.getHeight() * w2), null);


    }
    
    private static final int GRID_SIZE = 10;
    
    private void paintVectors(Graphics g, int rectx, int recty, int rectWidth, int rectHeight)
    {
                
        AproxyGrid function = relaxKit.approxy;
        int gridStep = rectWidth / function.getWidth();
        
        if (gridStep == 0)
            gridStep = 1;
        
        gridStep = GRID_SIZE/gridStep;    
        if (gridStep == 0)
            gridStep = 1;
        

        g.setColor(Color.yellow);
        for (int i = 0; i < function.getWidth(); i+=gridStep)
        {
            for (int j = 0; j < function.getWidth(); j+=gridStep)
            {
                Node n = function.getNode(i, j);
                int x = (int) (n.x * rectWidth) + rectx;
                int y = (int) (n.y * rectHeight) + recty;

                float[] errVec = relaxKit.relation.getErrVec(i, j);
                int dx = (int) (errVec[0] * rectWidth);
                int dy = (int) (errVec[1] * rectHeight);

                
                g.drawLine(x, y, x + dx, y + dy);
               // g.drawRect(x + dx - 2, y + dy - 2, 4, 4);
            }
        }        
    }
    
    private void paintGrid(Graphics g, int rectx, int recty, int rectWidth, int rectHeight)
    {
        
        
        AproxyGrid function = relaxKit.approxy;

        int gridStep = rectWidth / function.getWidth();
        
        if (gridStep == 0)
            gridStep = 1;
        
        gridStep = GRID_SIZE/gridStep;
        if (gridStep == 0)
            gridStep = 1;
        
        
        
        g.setColor(Color.red);
        for (int i = 0; i < function.getWidth() - gridStep; i+=gridStep)
        {
            for (int j = 0; j < function.getWidth() - gridStep; j+=gridStep)
            {
                Node n = function.getNode(i, j);
                int x = (int) (n.x * rectWidth) + rectx;
                int y = (int) (n.y * rectHeight) + recty;
                n = function.getNode(i + gridStep, j);
                int x2 = (int) (n.x * rectWidth) + rectx;
                int y2 = (int) (n.y * rectHeight) + recty;
                g.drawLine(x, y, x2, y2);
                n = function.getNode(i, j + gridStep);
                x2 = (int) (n.x * rectWidth) + rectx;
                y2 = (int) (n.y * rectHeight) + recty;
                g.drawLine(x, y, x2, y2);
            }
        }        
    }
}
