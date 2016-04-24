package stereo_praha;

/**
 * Created by Karol Presovsky on 8/28/14.
 */
public class ArtefactsZ {
    double[] m;
    double U1;
    double V1;
    double U2;
    double V2;

    double K = 1; // scale factor

    public ArtefactsZ() {
    }

    public ArtefactsZ(double[] m, double u1, double v1, double u2, double v2) {
        init(m, u1, v1, u2, v2);
    }

    public void init(double[] m, double u1, double v1, double u2, double v2) {
        this.m = m;
        U1 = u1;
        V1 = v1;
        U2 = u2;
        V2 = v2;
    }

    public double z1() {
        double denom = a1() + b1() + c1();
        if (denom == 0) {
            System.out.println("z1: division by zero.");
            return Double.NaN;
        }
        double _d1 = d1();
        return _d1 / denom;
    }

    public double z2() {
        double denom = a2() + b2() + c2();
        if (denom == 0) {
            System.out.println("z2: division by zero.");
            return Double.NaN;
        }
        double _d2 = d2();
        return _d2 / denom;
    }

    public double _val1() {
        double denom = a1() + b1() + c1();

        return d2() * denom;
    }

    public double _val2() {
        double denom = a2() + b2() + c2();

        return d1() * denom;
    }


    public double Value1() {return _val1();}
    public double Value2() {return _val2();}

    public double a1() {return U1/K*(U2/K*m[2] - m[0]);}
    public double b1() {return V1/K*(U2/K*m[6] - m[4]);}
    public double c1() {return U2/K*m[10] - m[8];}
    public double d1() {return m[12] - U2/K*m[14];}
    public double a2() {return U1/K*(V2/K*m[2] - m[1]);}
    public double b2() {return V1/K*(V2/K*m[6] - m[5]);}
    public double c2() {return V2/K*m[10] - m[9];}
    public double d2() {return m[13] - V2/K*m[14];}

    public void debug(double x, double y, double z) {

        double U1 = x/z;
        double V1 = y/z;

        double x2 = x*m[0] + y*m[4] + z*m[8] + m[12];
        double y2 = x*m[1] + y*m[5] + z*m[9] + m[13];
        double z2 = x*m[2] + y*m[6] + z*m[10] + m[14];

        double U2 = x2 / z2;
        double V2 = y2 / z2;


        double w1 = x*(U2*m[2] - m[0]) + y*(U2*m[6] - m[4]) + z*(U2*m[10] - m[8]);
        double w2 = m[12] - U2*m[14];

        System.out.println("R1:-----");
        System.out.println("w1:" + w1 + "\nw2:" + w2 + "\n");

        w1 = U1*z*(U2*m[2] - m[0]) + V1*z*(U2*m[6] - m[4]) + z*(U2*m[10] - m[8]);
        w2 = + m[12] - U2*m[14];

        System.out.println("w1:" + w1 + "\nw2:" + w2 + "\n");

        double a1 = U1*(U2*m[2] - m[0]);
        double b1 = V1*(U2*m[6] - m[4]);
        double c1 = (U2*m[10] - m[8]);
        double d1 = m[12] - U2*m[14];

        w1 = z *(a1 + b1 + c1);
        w2 = d1;

        double z_2 = d1/(a1 + b1 + c1);

        System.out.println("w1:" + w1 + "\nw2:" + w2 + "\n");
        System.out.println("z:" + z + "\nz_2:" + z_2 + "\n");

        ///-----

        System.out.println("R2:-----");


        w1 = V2*(x*m[2] + y*m[6] + z*m[10] + m[14]);
        w2 = (x*m[1] + y*m[5] + z*m[9] + m[13]);

        System.out.println("w1:" + w1 + "\nw2:" + w2 + "\n");

        w1 = x * (V2*m[2] - m[1]) + y * (V2*m[6] - m[5]) + z * (V2*m[10] - m[9]);
        w2 = m[13] - V2*m[14];

        System.out.println("w1:" + w1 + "\nw2:" + w2 + "\n");

        w1 = U1*z * (V2*m[2] - m[1]) + V1*z * (V2*m[6] - m[5]) + z * (V2*m[10] - m[9]);
        w2 = m[13] - V2*m[14];

        System.out.println("w1:" + w1 + "\nw2:" + w2 + "\n");

        double a2 = U1*(V2*m[2] - m[1]);
        double b2 = V1*(V2*m[6] - m[5]);
        double c2 = (V2*m[10] - m[9]);
        double d2 = m[13] - V2*m[14];

        w1 = z * (a2 + b2 + c2);
        w2 = d2;

        System.out.println("w1:" + w1 + "\nw2:" + w2 + "\n");

        z_2 = d2/(a2 + b2 + c2);

        System.out.println("z:" + z + ", z_2:" + z_2);

        System.out.println("a1:" + a1 + " --- " + a1());
        System.out.println("b1:" + b1 + " --- " + b1());
        System.out.println("c1:" + c1 + " --- " + c1());
        System.out.println("d2:" + d1 + " --- " + d1());
        System.out.println("a2:" + a2 + " --- " + a2());
        System.out.println("b2:" + b2 + " --- " + b2());
        System.out.println("c2:" + c2 + " --- " + c2());
        System.out.println("d2:" + d2 + " --- " + d2());
        System.out.println("V1:" + V1 + " --- " + this.V1);
        System.out.println("U1:" + U1 + " --- " + this.U1);
        System.out.println("V2:" + V2 + " --- " + this.V2);
        System.out.println("U2:" + U2 + " --- " + this.U2);


    }



}
