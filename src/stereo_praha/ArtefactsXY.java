package stereo_praha;

/**
 * Created by Karol Presovsky on 8/10/14.
 */
public class ArtefactsXY {

    double[] m;
    double U1;
    double V1;
    double U2;
    double V2;

    double K = 1; // scale factor

    public ArtefactsXY() {

    }

    public void init(double[] m, double u1, double v1, double u2, double v2) {
        this.m = m;
        U1 = u1;
        V1 = v1;
        U2 = u2;
        V2 = v2;
    }

    public double A() {return m[0];}
    public double B() {return m[4];}
    public double C() {return m[8];}
    public double D() {return m[12];}
    public double E() {return m[1];}
    public double F() {return m[5];}
    public double G() {return m[9];}
    public double H() {return m[13];}
    public double I() {return m[2];}
    public double J() {return m[6];}
    public double L() {return m[10];}
    public double M() {return m[14];}

    public double a1()
    {
        return U2 * I() - K * A();
    }

    public double b1()
    {
        return U2 * J() - K * B();
    }

    public double c1()
    {
        return U2 * L() - K * C();
    }

    public double d1()
    {
        return U2 * M() - K * D();
    }

    public double a2() {
        return V2 * I() - K * E();
    }

    public double b2() {
        return V2 * J() - K * F();
    }

    public double c2() {
        return V2 * L() - K * G();
    }

    public double d2() {
        return V2 * M() - K * H();
    }

    public double x2() {
        double denom = a2() + b2() * V1 / U1 + c2() * K / U1;
        if (denom != 0) {
            double _d2 = -d2();
            return _d2 / denom;
        }

        return Double.NaN;
    }

    public double x1() {
        double denom = a1() + b1() * V1 / U1 + c1() * K / U1;
        if (denom != 0) {
            double _d1 = -d1();
            return _d1 / denom;
        }

        return Double.NaN;
    }

    public double Value1() {
        if (U1 == 0) {
            System.out.println("U1 == 0, Vaule1 undefined");
            return Double.NaN;
        }
        return d1() * (a2() + b2()*V1/U1 + c2()*K/U1);


        //R1.) x = -d1 / (a1 + b1*V1/U1 + c1*K/U1)
        //R2.) x = -d2 / (a2 + b2*V1/U1 + c2*K/U1)
    }

    public double Value2() {
        if (U1 == 0) {
            System.out.println("U1 == 0, Vaule2 undefined");
            return Double.NaN;
        }
        return d2() * (a1() + b1()*V1/U1 + c1()*K/U1);
    }


    public double y1() {
        double denom = (a1()*U1/V1 + b1() + c1()*K/V1);
        if (denom != 0) {
            double _d1 = -d1();
            return _d1 /denom;
        }
        return Double.NaN;
    }
    public double y2() {
        double denom = ((a2()*U1/V1 + b2() + c2()*K/V1));
        if (denom != 0) {
            double _d2 = -d2();
            return _d2 /denom;
        }
        return Double.NaN;
    }

}
