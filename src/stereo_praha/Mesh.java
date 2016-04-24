package stereo_praha;

/**
 * Created by Karol Presovsky on 1/18/15.
 */
public class Mesh {

    public static double[][] vertexes(double radius, double zpivot, int count) {
        double[][] m = new double[count][3];
        for (double[] v : m) {
            double a = Math.random()*Math.PI;
            double z = Math.sin(a)*radius;
            double xy = Math.cos(a)*radius;
            a = Math.random()*Math.PI;
            double x = Math.cos(a)*xy;
            double y = Math.sin(a)*xy;
            v[0] = x;
            v[1] = y;
            v[2] = z + zpivot;
        }
        return m;
    }

    public static int[][] wires(int vertexCount) {
        int[][] w = new int[vertexCount-1][2];

        for (int i=0; i<vertexCount-1; i++) {
           w[i][0] = i;
           w[i][1] = i+1;
        }

        return w;
    }

}
