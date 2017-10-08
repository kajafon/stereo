package stereo_praha;

import java.util.ArrayList;

public class Algebra {

    
    /*
        intersection of a polygon and a line
        polygon does not have to be flat
        construction: find smallest distance between the ray [x,v] and poly-edge   
        calc vector product 'result' of the ray and the edge. 
        if the ray intersects with polygon, all scalar products of 'distances' and 'results' 
        will be either greater or smaller than zero
    */
    public static boolean intersects(double[][] polyVertex, double[] x, double[] v)
    {
        double[] perpendi = new double[3];
        double[] edge = new double[3];
        double[] result = new double[3];
        
        double product = 0;
        
        for (int i=0; i<polyVertex.length; i++) {
            
            int j = i + 1;
            if (j == polyVertex.length){
                j = 0;
            }
            
            Algebra.difference(polyVertex[j], polyVertex[i], edge);
            final double[] distance = Algebra.linesDistanceSquare(edge, polyVertex[i], v, x);
            
            perpendi[0] = distance[4] - distance[1];
            perpendi[1] = distance[5] - distance[2];
            perpendi[2] = distance[6] - distance[3];
            
//            System.out.print("edge: "); Algebra.printVec(edge); System.out.println("");
//            System.out.print("perpe: "); Algebra.printVec(perpendi); System.out.println("");            
            
            Algebra.vectorProduct(edge, v, result);
            
//            System.out.print("result: "); Algebra.printVec(result);System.out.println("");
            
            double scalar1 = Algebra.scalarValue(result, perpendi);
            System.out.println("->" + scalar1 + "\n");
            if (product == 0)
            {               
                product = scalar1;
            } else if (product * scalar1 < 0){
                return false;                
            }          
            
        }
        
        return true;
    }
    
    public static void testIntersection()
    {
        double[][] v = new double[][] {
           {-1,-1,4}, {-1,1,4}, {1,1,4}, {1,-1,4}  
        };
        
        System.out.println(":" + Algebra.intersects(v, new double[]{0.89,0.6,0.5}, new double[]{0.1, 0.2, 4}));
    }
    
    public static double[] anglesFromLine(double[] x1, double[] x2)
    {
        double[] v = new double[]{x2[0] - x1[0], x2[1] - x1[1], x2[2] - x1[2]};
        
        
        double cosXZ = v[1] / Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
        
        double angleY = Math.acos(cosXZ);
        double angleX = Math.PI - Math.atan2(v[0], v[2]);
        
        return new double[]{angleY, angleX};
    }
    
    public static void _anglesFromLineTest()
    {
        double[] m = Algebra.unity(null);
        double[] r = Algebra.rotation(null, Algebra.AXIS_X, 0.334);
        Algebra.multiply_4x4(m, r, m);
        Algebra.rotation(r, Algebra.AXIS_Y, 0.223);
        Algebra.multiply_4x4(m, r, m);
        
        double[] x1 = new double[]{0,1,0};
        double[] x2 = new double[]{0,3,0};
        double[] y1 = new double[3];
        double[] y2 = new double[3];
        
        Algebra.multiply4_4x4(m, x1, y1);
        Algebra.multiply4_4x4(m, x2, y2);
        
        double[] res = Algebra.anglesFromLine(y1, y2);
        
        System.out.println("res: " + res[0] + ", " + res[1]);
    }
    
    /*
     * double[] calcNormal(double[] v1, double[] v2, double[] v3) { v2 -= v1; v3
     * -= v1; v3*=v2; v3.jednotkuj(); return v3; }
     */
    public static double[] unity(double[] m) {
        if (m == null) {
            m = new double[16];
        }

        for (int i = 0, j = 0; i < 16; i++) {
            if (i == j) {
                m[i] = 1.0;
                j += 5;
            } else {
                m[i] = 0;
            }
        }

        return m;
    }

    /**
     * m3 = m1 * m2
     *
     * @param m1
     * @param m2
     * @param m3
     * @return m3.
     */
    public static double[] multiply_4x4(double[] m1, double[] m2, double[] m3) {
        int i, j;
        double[] t = new double[16];
        for (j = 0; j < 16; j += 4) {
            for (i = 0; i < 4; i++) {
                t[j + i] = m1[j] * m2[i]
                        + m1[j + 1] * m2[4 + i]
                        + m1[j + 2] * m2[8 + i]
                        + m1[j + 3] * m2[12 + i];
            }
        }
        if (m3 != null)
        {
            System.arraycopy(t, 0, m3, 0, 16);
            return m3;
        }
        
        return t;
        
    }

//    public static void multiply4x4_4(double[] m, double[] v1, double[] v2) {
//		double[] v = new double[3];
//		v[0] = v1[0] * m[0] + v1[1] * m[1] + v1[2] * m[2] + m[3];
//		v[1] = v1[0] * m[4] + v1[1] * m[5] + v1[2] * m[6] + m[7];
//		v[2] = v1[0] * m[8] + v1[1] * m[9] + v1[2] * m[10] + m[11];
//		System.arraycopy(v, 0, v2, 0, 3);
//	}

    /*
     * v1 * m => v2.    v1 == v2 is possible
     */
    public static double[] multiply4_4x4(double[] m, double[] v1, double[] v2) {
        if (v2 == null) {
            v2 = new double[3];
        }

        v2[0] = v1[0] * m[0] + v1[1] * m[4] + v1[2] * m[8] + m[12];
        v2[1] = v1[0] * m[1] + v1[1] * m[5] + v1[2] * m[9] + m[13];
        v2[2] = v1[0] * m[2] + v1[1] * m[6] + v1[2] * m[10] + m[14];

        return v2;

    }

    public static void scale(double[] m, double s) {
        for (int i = 0; i < 12; i++) {
            m[i] *= s;
        }
    }

    public static final int AXIS_X = 1;
    public static final int AXIS_Y = 2;
    public static final int AXIS_Z = 3;

    public static double[] rotation(double[] m, int axis, double angle) {
        if (m == null) {
            m = new double[16];
        }
        double cosa = Math.cos(angle);
        double sina = Math.sin(angle);

        unity(m);

        switch (axis) {
            case AXIS_X: {
                m[6] = -sina;
                m[10] = cosa;
                m[5] = cosa;
                m[9] = sina;
            }
            break;
            case AXIS_Y: {
                m[0] = cosa;
                m[8] = -sina;
                m[10] = cosa;
                m[2] = sina;
            }
            break;
            case AXIS_Z: {
                m[0] = cosa;
                m[4] = sina;
                m[5] = cosa;
                m[1] = -sina;
            }
            break;
        }

        return m;
    }
    
    public static void printVec(double[] v) {
        for (int i = 0; i < v.length; i++) {
            System.out.print(v[i] + ", ");
        }       
    }

    public static void print(double[] m) {
        if (m.length > 3) {
            for (int j = 0; j < m.length; j++) {
                for (int i = 0; i < 4; i++) {
                    System.out.print(m[j] + ", ");
                }
                System.out.println();
            }
        }
    }

    public static double[] difference(double[] v1, double[] v2, double[] dest)
    {
        if (dest == null) { 
            dest = new double[v1.length];
        }
        for (int i=0; i<v1.length; i++){
            dest[i] = v1[i] - v2[i];
        }
        
        return dest;
    }
    
    public static double distance(double[] v1, double[] v2)
    {
        double s = 0;
        for (int i=0; i<v1.length; i++)
        {
            double v = v1[i] - v2[i];
            s += v*v;
        }
        return Math.sqrt(s);
    }
    
    public static double scalarValue(double[] a, double[] b, double[] c) {

        double res = 0;
        
        for (int i=0; i<a.length; i++) {
            res += (b[i]-a[i])*(c[i]-a[i]);
        }

        return res;
    }

    public static double scalarValue(double[] v1, double[] v2) {

        double res = 0;
        
        for (int i=0; i<v1.length; i++) {
            res += v1[i]*v2[i];
        }

        return res;
    }

    public static double[] scalarEvaluation(double[][] vertex, double[] result) {
        if (vertex.length < 3) {
            return result;
        }

        ArrayList<Double> resList = null;

        if (result == null) {
            resList = new ArrayList<>(100);
        }

        int resIndx = 0;
        for (int i = 0; i < vertex.length - 2; i++) {
            for (int j = i + 1; j < vertex.length - 1; j++) {
                for (int k = j + 1; k < vertex.length; k++, resIndx++) {
                    double val = scalarValue(vertex[i], vertex[j], vertex[k]);
                    if (result == null) {
                        resList.add(val);
                    } else {
                        result[resIndx] = val;
                    }
                }
            }
        }

        if (result == null) {
            result = new double[resList.size()];
            for (int i = 0; i < resList.size(); i++) {
                result[i] = resList.get(i);
            }
        }

        return result;
    }

    public static double simpleDistance(double[] x1, double[] x2) {
        double sum = 0;
        for (int i = 0; i < x1.length; i++) {
            sum += Math.abs(x1[i] - x2[i]);
        }
        return sum / x1.length;
    }

    public static double[] vectorProduct(double[] v1, double[] v2, double[] res) {
        if (res == null) {
            res = new double[3];
        }

//      v1 [x,y,z]
//      v2 [a,b,c]
//      result  [yc-zb, za-xc, xb-ya]
        res[0] = v1[1] * v2[2] - v1[2] * v2[1];
        res[1] = v1[2] * v2[0] - v1[0] * v2[2];
        res[2] = v1[0] * v2[1] - v1[1] * v2[0];

        return res;

    }

    public static double[] intersectionPlaneAndLine(double[] a, double[] x1, double[] v2, double[] x2) {

        double d = -a[0] * x1[0] - a[1] * x1[1] - a[2] * x1[2];

//        a[0]*(v2[0]*t + x2[0]) + a[1]*(v2[1]*t + x2[1]) + a[2]*(v2[2]*t + x2[2]) + d == 0;
        double denominator = a[0] * v2[0] + a[1] * v2[1] + a[2] * v2[2];
        if (denominator == 0) {
            return null;
        }

//        t* denominator + a[0]*x2[0] + a[1]*x2[1] + a[2]*x2[2] + d == 0
        double t = -(a[0] * x2[0] + a[1] * x2[1] + a[2] * x2[2] + d) / denominator;

        return new double[]{x2[0] + v2[0] * t, x2[1] + v2[1] * t, x2[2] + v2[2] * t};
    }

    /**
     * 
     * @param v1
     * @param x1
     * @param v2
     * @param x2
     * @return [sqr(distance), p1.x, p1.y, p2.x, p2.y] // distance squared and points on lines 
     */
    public static double[] linesDistanceSquare(double[] v1, double[] x1, double[] v2, double[] x2) {
        // if lines are not parallel. we want to find the smallest distance between them
        double[] product = vectorProduct(v1, v2, null);

        if (Math.abs(product[0]) + Math.abs(product[1]) + Math.abs(product[2]) < 0.0000001) {
            // lines are very close to parallel

            double[] P2 = intersectionPlaneAndLine(v1, x1, v2, x2);
            P2[0] -= x1[0];
            P2[1] -= x1[1];
            P2[2] -= x1[2];

            return new double[]{P2[0] * P2[0] + P2[1] * P2[1] + P2[2] * P2[2], 
                Double.NaN, Double.NaN, Double.NaN, 
                Double.NaN, Double.NaN, Double.NaN} ;
        }

        double[] P1 = intersectionPlaneAndLine(vectorProduct(v2, product, null), x2, v1, x1);
        if (P1 == null) {
            return new double[]{Double.POSITIVE_INFINITY,
                Double.NaN, Double.NaN, Double.NaN, 
                Double.NaN, Double.NaN, Double.NaN}; 
        };

        double[] P2 = intersectionPlaneAndLine(vectorProduct(v1, product, null), x1, v2, x2);
        if (P1 == null) {
             return new double[]{Double.POSITIVE_INFINITY,
                Double.NaN, Double.NaN, Double.NaN, 
                Double.NaN, Double.NaN, Double.NaN}; 
        }

        double vx = P1[0] - P2[0];
        double vy = P1[1] - P2[1];
        double vz = P1[2] - P2[2];

        return new double[]{vx*vx + vy*vy + vz*vz,
             P1[0],P1[1],P1[2],
             P2[0],P2[1],P2[2],
        };
    }

    public static void _lineDistance_test() {
        double[] res = vectorProduct(new double[]{0, 1, 1}, new double[]{0, 1, -1}, null);

        double[] v1_orig = new double[]{1, 1, 1};
        double[] v2_orig = new double[]{1, 1, 0};
        double[] x1_orig = new double[]{0, 0, 0};
        double[] x2_orig = new double[]{0, 0, 1};

        double[] dist = linesDistanceSquare(v1_orig, x1_orig, v2_orig, x2_orig);

        System.out.println("dist = " + dist[0]);

        for (int i = 0; i < 10; i++) {
            double[] m = Algebra.unity(null);

            double[] r = null;

            r = Algebra.rotation(null, AXIS_X, (Math.random() - 0.5) * Math.PI);
            Algebra.multiply_4x4(m, r, m);

            Algebra.rotation(r, AXIS_Y, (Math.random() - 0.5) * Math.PI);
            Algebra.multiply_4x4(m, r, m);

            Algebra.rotation(r, AXIS_Z, (Math.random() - 0.5) * Math.PI);
            Algebra.multiply_4x4(m, r, m);

            double[] v1 = Algebra.multiply4_4x4(m, v1_orig, null);
            double[] v2 = Algebra.multiply4_4x4(m, v2_orig, null);

            // translation
            m[12] = (Math.random() - 0.5) * 100;
            m[13] = (Math.random() - 0.5) * 100;
            m[14] = (Math.random() - 0.5) * 100;

            double[] x1 = Algebra.multiply4_4x4(m, x1_orig, null);
            double[] x2 = Algebra.multiply4_4x4(m, x2_orig, null);

            dist = linesDistanceSquare(v1, x1, v2, x2);

            System.out.println("dist = " + dist);
        }
    }

    public static boolean invertmatrix(double[][] src, double[][] inv) // Spocita inverzni matici k *src a ulozi ji do *inv
    {
        int i, j;
        double det;

        inv[0][0] = src[1][1] * src[2][2] - src[1][2] * src[2][1];
        inv[0][1] = -src[0][1] * src[2][2] + src[0][2] * src[2][1];
        inv[0][2] = src[0][1] * src[1][2] - src[0][2] * src[1][1];

        det = inv[0][0] * src[0][0] + inv[0][1] * src[1][0] + inv[0][2] * src[2][0];

        if (det == 0) {
	   // printf("invertmatrix: nejde spocitat!\n");
            // exit (1);
            return false;

        }

        inv[1][0] = -src[1][0] * src[2][2] + src[1][2] * src[2][0];
        inv[1][1] = src[0][0] * src[2][2] - src[0][2] * src[2][0];
        inv[1][2] = -src[0][0] * src[1][2] + src[0][2] * src[1][0];

        inv[2][0] = src[1][0] * src[2][1] - src[1][1] * src[2][0];
        inv[2][1] = -src[0][0] * src[2][1] + src[0][1] * src[2][0];
        inv[2][2] = src[0][0] * src[1][1] - src[0][1] * src[1][0];

        for (j = 0; j < 3; j++) {
            for (i = 0; i < 3; i++) {
                inv[j][i] /= det;
            }
        }

        inv[3][0] = -src[3][0] * inv[0][0] - src[3][1] * inv[1][0] - src[3][2] * inv[2][0];
        inv[3][1] = -src[3][0] * inv[0][1] - src[3][1] * inv[1][1] - src[3][2] * inv[2][1];
        inv[3][2] = -src[3][0] * inv[0][2] - src[3][1] * inv[1][2] - src[3][2] * inv[2][2];

        inv[0][3] = 0;
        inv[1][3] = 0;
        inv[2][3] = 0;
        inv[3][3] = 1;

        return true;
    }

    public static double[] calcInverse(double[] m, double[] result) {
        double[][] matrix = new double[4][4];

        for (int i = 0; i < 16; i++) {
            matrix[i / 4][i % 4] = m[i];
        }

        double[][] inverse = new double[4][4];

        if (!invertmatrix(matrix, inverse)) {
            return null;
        }

        if (result == null) {
            result = new double[16];
        }
        for (int i = 0; i < 16; i++) {
            result[i] = inverse[i / 4][i % 4];
        }

        return result;
    }

    public static void _inverse_test() {

        double[] r = Algebra.unity(null);
        double[] m = Algebra.unity(null);

        double[] angles = new double[]{Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5};
        double[] translation = new double[]{(Math.random() - 0.5), (Math.random() - 0.5), (Math.random() - 0.5)};

        Algebra.rotation(r, Algebra.AXIS_X, angles[0]);
        Algebra.multiply_4x4(m, r, m);

        Algebra.rotation(r, Algebra.AXIS_Y, angles[1]);
        Algebra.multiply_4x4(m, r, m);

        r = Algebra.rotation(r, Algebra.AXIS_Z, angles[2]);
        Algebra.multiply_4x4(m, r, m);

        System.arraycopy(r, 0, m, 0, r.length);
        m[12] = translation[0] * 9;
        m[13] = translation[1] * 9;
        m[14] = translation[2] * 9;

        double[] im = calcInverse(m, null);

        double[] v1 = new double[3];
        double[] v2 = new double[3];
        double[] v3 = new double[3];

        for (int i = 0; i < 10; i++) {
            v1[0] = Math.random() - 0.5;
            v1[1] = Math.random() - 0.5;
            v1[2] = Math.random() - 0.5;

            Algebra.multiply4_4x4(m, v1, v2);
            Algebra.multiply4_4x4(im, v2, v3);

            double e = Math.abs(v1[0] - v3[0]) + Math.abs(v1[1] - v3[1]) + Math.abs(v1[2] - v3[2]);

            System.out.println(" -e:" + e);

        }

    }

    public static void main(String[] args) {
        testIntersection();
    }

}
