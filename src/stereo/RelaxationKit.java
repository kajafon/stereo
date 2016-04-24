/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 * tension comes form difference between the two images of the same thing.
 * by relaxing i mean finding a transform of the first image to resemble the second one
 * as good as possible approxy grid is the tool of transform
 * 
 * @author karol presovsky
 */
public class RelaxationKit
{

    public static final int LEVELS = 5;
    public static final int SMOOTHING = 10;
    
    public ArrayList<BufferedImage> images1 = new ArrayList<BufferedImage>();
    public ArrayList<BufferedImage> images2 = new ArrayList<BufferedImage>();
    public ArrayList<Greyscale> greys1 = new ArrayList<Greyscale>();
    public ArrayList<Greyscale> greys2 = new ArrayList<Greyscale>();
    public BufferedImage img1;
    public BufferedImage img2;    
    public AproxyGrid approxy;
    public Relation relation;
    public int level;

    public RelaxationKit(String name1, String name2, double stepSize) throws Exception
    {
        approxy = new AproxyGrid(100);
        init(name1, name2, stepSize);
    }


    private void init(String name1, String name2, double stepSize) throws Exception
    {
        img1 = ImageIO.read(new File(name1));
        img2 = ImageIO.read(new File(name2));
        images1.add(img1);
        images2.add(img2);

        greys1.add(Greyscale.toGreyscale(img1));
        greys2.add(Greyscale.toGreyscale(img2));
        
        int counter = LEVELS - 1;
        
        for (; counter >= 0; counter--)
        {
            greys1.add(new Greyscale(img1.getWidth(), img1.getHeight()));
            greys2.add(new Greyscale(img2.getWidth(), img2.getHeight()));
        }
        
        for (int i=0; i<greys1.size()-1; i++)
        {
            Greyscale.smoothBetter(greys1.get(0),greys1.get(i+1),(i+1)*SMOOTHING);
            Greyscale.smoothBetter(greys2.get(0),greys2.get(i+1),(i+1)*SMOOTHING);
             
            images1.add(greys1.get(i+1).createImage(null));
            images2.add(greys2.get(i+1).createImage(null));
        }
        
        relation = new Relation(img1.getWidth(), img1.getHeight(), img2.getWidth(), img2.getHeight());
       // processLevel(LEVELS-1);
        
    }
    
    public void processLevel(int i)
    {
        if (i>=0 && i<greys1.size())
        {
            level = i;
            relation.take(greys1.get(level), greys2.get(level), approxy, level, greys1.size()-1, SMOOTHING);
        }
    }

/*
    private BufferedImage getBuffered(Image image)
    {
        BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g = bi.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bi;

    }
*/
}
