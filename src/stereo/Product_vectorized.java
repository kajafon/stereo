/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo;

import stereo.poi.Cntx2;
import stereo.ui.OutlineView;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.imageio.ImageIO;
import stereo.ui.SimpleVectorView;
import stereo.ui.VectorView;

/**
 *
 * @author karol presovsky
 */
public class Product_vectorized
{

    ArrayList<Outline> vcs;
    BufferedImage img;
    BufferedImage grad;
    ArrayList<Cntx2> maxims;
    ArrayList<Cntx2> points;
    Greyscale gs;
    Greyscale gs2;
    double thrsh1;
    double thrsh2;
    
    
    public Product_vectorized(BufferedImage image)
    {
        thrsh1 = image.getWidth()/10.0;
        thrsh1 *= thrsh1;
        
        thrsh2 = image.getWidth()/25.0;
        thrsh2 *= thrsh2;
        img = image;
        ArrayList<Edge> vectorsx = null;
        ArrayList<Edge> vectorsy = null;
        
       // gs = Greyscale.toGreyscale(img);
      //  grad = proces.gs1.createImage(grad);
        
        Process proces = new Process(Greyscale.toGreyscale(img));
        
    //    Greyscale.fillShapes(proces.gs1, proces.gs2);
     //   proces.switchBmp();
      //  Greyscale.smooth(proces.gs1, proces.gs2);
      //  proces.switchBmp();
      
        Greyscale.edgesFilter(proces.gs1, proces.gs2);
        proces.switchBmp();

        proces.gs1.contrast(0, 30);
        grad = proces.gs1.createImage(grad);
        
        gs2 = proces.gs2;
        //img = proces.gs1.createImage(null);
        gs = Greyscale.toGreyscale(img);

        //--------
        
        int valueTreshold = 10;
        int lengthTreshold = 5;

        vectorsx = Numeric.vectorize(proces.gs1, false, valueTreshold, lengthTreshold);
        vectorsy = Numeric.vectorize(proces.gs1, true, valueTreshold, lengthTreshold);
        
        Numeric.diagonalElimination(vectorsy, true);
        Numeric.diagonalElimination(vectorsx, false);
        
        ArrayList<Outline> youtlines = Numeric.toOutlines(null, vectorsy, true);
        ArrayList<Outline> xoutlines = Numeric.toOutlines(vcs, vectorsx, false);
        
        int s1 = youtlines.size();
        int s2 = xoutlines.size();        
        
        Outline.reconnect(youtlines, xoutlines);
        
        System.out.println("merge:\n " + s1 + "->" + youtlines.size() + "  " + s2 + "->" + xoutlines.size());
        
        vcs = youtlines;
        vcs.addAll(xoutlines);
        
        Outline.dilute(vcs, 5);

        maxims = new ArrayList<Cntx2>();
        /*
        for (int i = 0; i < vcs.size(); i++)
        {
            //maxims.addAll(vcs.get(i).buildCntxs());
            vcs.get(i).collectMaxims(maxims);
        }
        
        
        Collections.sort(maxims, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                Cntx2 c1 = (Cntx2)o1;
                Cntx2 c2 = (Cntx2)o2;
                
                return c1.y - c2.y;
            }
        });
        
        Cntx2.connectNeighbours(maxims, img.getWidth()/5, 20);

        
        // ------------------
        /*
        ArrayList<Outline.Delegate> del = new ArrayList<Outline.Delegate>();
        for (Outline o:vcs)
        {
            del.add(o.getDelegate());
        }
        
        Collections.sort(del, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((Outline.Delegate)o1).getTop().y - ((Outline.Delegate)o2).getTop().y;
            }
        });
        
        Outline.connectNeighbours(del, img.getWidth()/10);
        points = new ArrayList<Cntx2>();
        for (Outline.Delegate d:del)
        {
            points.addAll(d.cntx);
        }
        
        */
        
        /*
        points = Outline.collectPoints(vcs, null);
        Collections.sort(points, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                Cntx2 c1 = (Cntx2)o1;
                Cntx2 c2 = (Cntx2)o2;
                
                return c1.y - c2.y;
            }
        });
        
         
         */
        
    }

    public Greyscale getGs()
    {
        return gs;
    }
    public Greyscale getGs2()
    {
        return gs2;
    }
    
    public BufferedImage getImage()
    {
        return grad;
    }
    
    public ArrayList<Cntx2> getCntxList()
    {
        return maxims;
    }

    public ArrayList<Outline> getOutlines()
    {
        return vcs;
    }
    
    

    VectorView vv = null;
    public VectorView getView()
    {
        if (vv == null)
        {
            vv = new VectorView(getImage(), vcs, gs, gs2);
            vv.setMaxims(maxims);
        }
        vv.setNeighbours(maxims);
        return vv;
    }
    
}
