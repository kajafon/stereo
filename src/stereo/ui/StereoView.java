/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JPanel;
import stereo.poi.Cntx;
import stereo.poi.Cntx2;
import stereo.poi.Relation;

/**
 *
 * @author karol presovsky
 */
public class StereoView extends JPanel
{

    public StereoView()
    {
    }
    
    JComponent view1;
    JComponent view2;
    ArrayList<Cntx2> cntxList1;
    ArrayList<Cntx2> cntxList2;
    ArrayList<Relation> relations;
    Point spot;
    int spotSize = 8;
    


    Color getRelationColor(Relation r)
    {
        return Color.WHITE;
    }
            
    void paintRelations(Graphics g)
    {
        if (spot == null)
        {
            System.out.println("spot == null");
            return;
        }
        
        if (relations.isEmpty())
        {
            return;
        }
        int offx1 = view1.getX();
        int offx2 = view2.getX();
        int offy1 = view1.getY();
        int offy2 = view2.getY();
        /*
        for (int i=0; i<cntxList1.size() && i<cntxList2.size(); i++)
        {
        final Cntx c1 = cntxList1.get(i);
        final Cntx c2 = cntxList2.get(i);
        final int x1 = offx1 + c1.x;
        final int y1 = offy1 + c1.y;
        final int x2 = offx2 + c2.x;
        final int y2 = offy2 + c2.y;
        g.drawLine(x1, y1, x2, y2);
        }
         */
        
        for (Relation r : relations)
        {
            Cntx c1 = r.c1;
            Cntx c2 = r.c2;
          /*  int d = Math.abs(spot.x - c1.x) + Math.abs(spot.y - c1.y);
            if (d > spotSize)
            {
                continue;
            }
            
           
            g.setColor(getRelationColor(r));
           * 
           */
            int x1 = offx1 + c1.x;
            int y1 = offy1 + c1.y;
            int x2 = offx2 + c2.x;
            int y2 = offy2 + c2.y;
            g.drawLine(x1, y1, x2, y2);
        }
    }

    void paintZet(Graphics g)
    {
        int offx1 = view1.getX();
        int offy1 = view1.getY() + getHeight() / 2;
        g.setColor(Color.yellow);
        for (Relation r : relations)
        {
            //            Color c = new Color((float)r.z, (float)r.z, (float)r.z);
            g.drawLine(offx1 + r.c1.x, offy1 + r.c1.y, offx1 + r.c1.x, offy1 + r.c1.y + (int) (r.z3d / 10));
        }
    }
    
}
