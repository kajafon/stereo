/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.to3d;

import java.util.ArrayList;
import stereo.Triangulator;

/**
 *
 * @author karol presovsky
 */
public class FaceMetric
{
    public Face face;
    public int area1;
    public int perimeter1;
    public int area2;
    public int perimeter2;

    public FaceMetric(Face face, int area1, int perimeter1, int area2, int perimeter2)
    {
        this.face = face;
        this.area1 = area1;
        this.perimeter1 = perimeter1;
        this.area2 = area2;
        this.perimeter2 = perimeter2;
    }
        
    public static ArrayList<FaceMetric> calcFaceMetrics(ArrayList<Face> list, ArrayList links, Triangulator.LinkAccess link)
    {
        ArrayList<FaceMetric> metrics = new ArrayList<FaceMetric>();
        
        for (Face f:list)
        {
            Object k1 = links.get(f.r1);
            Object k2 = links.get(f.r2);
            Object k3 = links.get(f.r3);
            //int a1 = Triangulator.area(k1.f1.x, k1.f1.y, k2.f1.x, k2.f1.y, k3.f1.x, k3.f1.y);
            int a1 = Triangulator.area(link.x1(k1), link.y1(k1), link.x1(k2), link.y1(k2), link.x1(k3), link.y1(k3));
            int p1 = Triangulator.perimeter(link.x1(k1), link.y1(k1), link.x1(k2), link.y1(k2), link.x1(k3), link.y1(k3));
            int a2 = Triangulator.area(link.x2(k1), link.y2(k1), link.x2(k2), link.y2(k2), link.x2(k3), link.y2(k3));
            int p2 = Triangulator.perimeter(link.x2(k1), link.y2(k1), link.x2(k2), link.y2(k2), link.x2(k3), link.y2(k3));
            FaceMetric m = new FaceMetric(f, a1, p1, a2, p2);
            metrics.add(m);
        }
        
        return metrics;
    }

    
    
    
    
}
