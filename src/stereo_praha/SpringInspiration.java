package stereo_praha;

import java.awt.Color;
import java.util.ArrayList;
import stereo_praha.gui.Object3D;

/**
 * Created by Karol Presovsky on 4/2/15.
 */
public class SpringInspiration {

    public static Object3D objectFromProjection(double[][] projected, Object3D obj, double focalLength, double rayLength) {
        if (obj == null) {
            obj = new Object3D();
        }
        
        int sidesNum = projected.length;
        if (sidesNum > 10) {
            sidesNum = 10;
        }
        
        obj.init(projected.length + 1, sidesNum, 2);
        
        double[][] vectors = obj.vertex;

        for (int i = 0; i < projected.length; i++) {

            vectors[i][0] = projected[i][0] * rayLength;
            vectors[i][1] = projected[i][1] * rayLength;
            vectors[i][2] = (rayLength - 1) * focalLength;
        }
        vectors[vectors.length - 1][0] = 0;
        vectors[vectors.length - 1][1] = 0;
        vectors[vectors.length - 1][2] = -focalLength;

        int[][] tri = obj.polygons;
        
        int di = projected.length / tri.length;

        for (int i=0; i<tri.length; i++)
        {
            int vi = i * di;
            if (vi > obj.vertex.length) {
                System.out.println("pica matka");
                return null;
            }
            tri[i][0] = vectors.length - 1;
            tri[i][1] = vi;
        }
        return obj;
    }

    /**
     *
     * @param obj1 object created by objectFromProjection
     * @param obj2 object created by objectFromProjection
     * @param links list of links/objects between respective edges of objects
     * @return
     */
    public static ArrayList<Object3D> calcDistanceObjects(Object3D obj1, Object3D obj2, ArrayList<Object3D> links) {
        return calcDistanceObjects(obj1, obj2, links, Color.yellow);
    }
    
    public static ArrayList<Object3D> calcDistanceObjects(Object3D obj1, Object3D obj2, ArrayList<Object3D> links, Color color) {
        if (links == null) {
            links = new ArrayList<>();
        }

        double[] v1 = null;
        double[] v2 = null;
        int i = 0;

        for (; i < obj1.vertex.length - 1; i++) {
            v1 = Algebra.difference(obj1.transformed[i], obj1.transformed[obj1.transformed.length - 1], v1);
            v2 = Algebra.difference(obj2.transformed[i], obj2.transformed[obj2.transformed.length - 1], v2);

            double[] res = Algebra.linesDistanceSquare(v1, obj1.transformed[i], v2, obj2.transformed[i]);

            if (Double.isNaN(res[1])) {
                System.out.println("invalid distance: " + res);
            }

            Object3D link;

            if (links.size() <= i) {
                links.add(link = new Object3D(2, 1, 2));
            } else {
                link = links.get(i);
            }
            link.setColor(color);

            link.vertex[0][0] = res[1];
            link.vertex[0][1] = res[2];
            link.vertex[0][2] = res[3];

            link.vertex[1][0] = res[4];
            link.vertex[1][1] = res[5];
            link.vertex[1][2] = res[6];

            link.polygons[0][0] = 0;
            link.polygons[0][1] = 1;

        }

        while (links.size() > i) {
            links.get(i).setColor(Color.red);
            i++;
        }

        return links;
    }

    public static void main(String[] args) {
        System.out.println("nic tu nie je");
    }
}
