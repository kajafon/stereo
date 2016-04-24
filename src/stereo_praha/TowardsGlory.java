package stereo_praha;

import evolve.AbstractAgent;
import evolve.Reactor;
import stereo_praha.gui.VectorView;
import stereo_praha.gui.stuff3D;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by Karol Presovsky on 8/30/14.
 */
public class TowardsGlory implements ProblemInterface {
    public double[][] vertex;
    public int[][] triangles;

    public double[][] transformed_test;
    public double[][] projected_test;

    public double[][] transformed_zero;
    public double[][] projected_zero;

    public double[][] transformed_target;
    public double[][] projected_target;

    public double[] matrix_target = Algebra.unity(null);
    public double[] matrix_test = Algebra.unity(null);

    public int test;


    public TowardsGlory() {
        // one cube, 4 triangles
        vertex = new double[][] {
                {-1,-1, 20},
                { 1,-1, 20},
                {-1, 1, 20},
                { 1, 1, 20},

                {-1,-1, 22},
                { 1,-1, 22},
                {-1, 1, 22},
                { 1, 1, 22},
        };

        triangles = new int[][] {
                {0,1,2,0},{2,1,3,2}, {4,5,6,4}, {5,6,7,5}
        };

        init();
    }

    public TowardsGlory(double[][] vertex, int[][] triangles) {
        this.vertex = vertex;
        this.triangles = triangles;
        init();
    }

    void init() {

        transformed_test = new double[vertex.length][];
        projected_test = new double[vertex.length][];
        transformed_zero = new double[vertex.length][];
        projected_zero = new double[vertex.length][];
        transformed_target = new double[vertex.length][];
        projected_target = new double[vertex.length][];

        /// init projected
        for (int i=0; i<vertex.length; i++) {
            projected_test[i] = new double[2];
            projected_zero[i] = new double[2];
            projected_target[i] = new double[2];
            transformed_test[i] = new double[3];
            transformed_zero[i] = new double[3];
            transformed_target[i] = new double[3];
        }

        stuff3D.project(vertex, transformed_zero, projected_zero, matrix_target); // just uniti matrix
        stuff3D.project(vertex, transformed_test, projected_test, matrix_test);
        stuff3D.project(vertex, transformed_target, projected_target, matrix_target);
    }

    public void rotateTest(double angelX, double angelY, double angelZ) {

        matrix_test = Algebra.unity(matrix_test);
        stuff3D.rotate(matrix_test, angelX, angelY, angelZ, getDefaultZetPivot());

        // -------- project ---------

        stuff3D.project(vertex, transformed_test, projected_test, matrix_test);

    }

    public void rotateTarget(double angelX, double angelY, double angelZ) {

        matrix_target = Algebra.unity(matrix_target);
        stuff3D.rotate(matrix_target, angelX, angelY, angelZ, getDefaultZetPivot());

        // -------- project ---------

        stuff3D.project(vertex, transformed_target, projected_target, matrix_target);

    }


    @Override
    public double[] calcError(double angelX, double angelY, double angelZ) {

        Algebra.unity(matrix_test);
        stuff3D.rotate(matrix_test, angelX, angelY, angelZ, getDefaultZetPivot());
        // ------- test ---------------

        double[] errorAcumulator = new double[4];
        ArtefactsXY artxy = new ArtefactsXY();
        ArtefactsZ artz = new ArtefactsZ();

        for (int i=0; i<projected_zero.length; i++) {
            artxy.init(matrix_test, projected_zero[i][0], projected_zero[i][1], projected_target[i][0], projected_target[i][1]);
            artz.init(matrix_test, projected_zero[i][0], projected_zero[i][1], projected_target[i][0], projected_target[i][1]);
            double y1 = artxy.y1();
            double y2 = artxy.y2();
            double x1 = artxy.x1();
            double x2 = artxy.x2();
            double z1 = artz.z1();
            double z2 = artz.z2();

            double dif = Math.abs(y1 - y2);
            double sum = Math.abs(y1) + Math.abs(y2);
            double ey = 0;
            if (dif != 0) {
                ey = (dif/ sum);
            }

            dif = Math.abs(x1 - x2);
            sum = Math.abs(x1) + Math.abs(x2);
            double ex = 0;
            if (dif != 0) {
                ex = (dif/ sum);
            }

            dif = Math.abs(z1 - z2);
            sum = Math.abs(z1) + Math.abs(z2);
            double ez = 0;
            if (dif != 0) {
                ez = (dif/ sum);
            }

            errorAcumulator[0] += Math.sqrt(ex*ex + ey*ey + ez*ez);
            errorAcumulator[1] += ex;
            errorAcumulator[2] += ey;
            errorAcumulator[3] += ez;

        }
        errorAcumulator[0] /= projected_zero.length;
        errorAcumulator[1] /= projected_zero.length;
        errorAcumulator[2] /= projected_zero.length;
        errorAcumulator[3] /= projected_zero.length;
        return errorAcumulator;
    }

    public static final int sampleCount = 500;

    /**
     *
     * @return samples of error accumulation from rotated line probes
     */
    public double[] rotaryProbe_devel(double taz) {

        double diameter = 0.3;
        double stepCount = 30;

        double[] samples = new double[sampleCount];
        for ( int sampleIndx = 0; sampleIndx < sampleCount; sampleIndx++) {

            double vx = Math.cos(Math.PI/sampleCount * sampleIndx) * diameter / stepCount;
            double vy = Math.sin(Math.PI/sampleCount * sampleIndx) * diameter / stepCount;

            double accum = 0;
            for (int i = 0; i < stepCount; i++) {
                double ax = i*vx;
                double ay = i*vy;
                accum += calcError(ax, ay, taz)[0];
                accum += calcError(-ax, -ay, taz)[0];
            }

            samples[sampleIndx] = accum;
        }

        return samples;
    }

    public double[] minimumOnLine(int sampleIndx) {
        double diameter = 0.3;
        int stepCount = 50;

        double vx = Math.cos(Math.PI/sampleCount * sampleIndx) * diameter / stepCount;
        double vy = Math.sin(Math.PI/sampleCount * sampleIndx) * diameter / stepCount;


        double minimum = 100000;
        double resultX = 100000;
        double resultY = 100000;

        for (int i = 1; i < sampleCount; i++) {
            double ax = i*vx;
            double ay = i*vy;
            double e = calcError(ax, ay, 0)[0];
            if (e < minimum) {
                minimum = e;
                resultX = ax;
                resultY = ay;
            }
            e = calcError(-ax, -ay, 0)[0];
            if (e < minimum) {
                minimum = e;
                resultX = -ax;
                resultY = -ay;
            }
        }

        return new double[]{resultX, resultY};
    }

    /**
     * returns detected angle
     * @return
     */
    public double rotaryProbe() {

        double diameter = 0.3;
        double stepCount = 30;

        double[] samples = new double[sampleCount];
        for ( int sampleIndx = 0; sampleIndx < sampleCount; sampleIndx++) {

            double vx = Math.cos(Math.PI/sampleCount * sampleIndx) * diameter / stepCount;
            double vy = Math.sin(Math.PI/sampleCount * sampleIndx) * diameter / stepCount;

            double accum = 0;
            for (int i = 0; i < stepCount; i++) {
                double ax = i*vx;
                double ay = i*vy;
                accum += calcError(ax, ay, 0)[0];
                accum += calcError(-ax, -ay, 0)[0];
            }

            samples[sampleIndx] = accum;
        }

        int minIndx = -1;
        double min = 10000;
        for (int i=0; i<samples.length; i++) {
            if (samples[i] < min) {
                min = samples[i];
                minIndx = i;
            }
        }


        return Math.PI*minIndx/sampleCount;
    }


    public double[] minimumOnLineCorrected(double startAngle) {

        double diameter = 0.3;

        int stepCount = 50;
        int correctionOnStep = 0;
        boolean corrected;

        double resX;
        double resY;

        do {
            corrected = false;
            double vx = Math.cos(startAngle) * diameter / stepCount;
            double vy = Math.sin(startAngle) * diameter / stepCount;
            double min = 100000;
            resX = 100000;
            resY = 100000;

            for (int i = 1; i < stepCount; i++) {

                if (i > correctionOnStep && i % 5 == 0) {

                    // angle correction
                    int corrStepCount = 5;

                    // Math.PI/sampleCount == angle difference between sample lines in rotary probe
                    double scale = diameter / stepCount * i;
                    double h = Math.sin(Math.PI / sampleCount) * scale;

                    // vector perpendicular to line in startAngle
                    double hx = h * Math.cos(startAngle + Math.PI / 2);
                    double hy = h * Math.sin(startAngle + Math.PI / 2);

                    // starting point of correction probes

                    double rx = - hx / 2;
                    double ry = - hy / 2;
                    double ax = Math.cos(startAngle) * scale;
                    double ay = Math.sin(startAngle) * scale;

                    hx /= corrStepCount;
                    hy /= corrStepCount;

                    double minimum = 100000;
                    double resultX = 100000;
                    double resultY = 100000;

                    for (int j = 0; j < corrStepCount; j++) {
                        double e = calcError(ax + rx, ay + ry, 0)[0];
                        if (e < minimum) {
                            minimum = e;
                            resultX = ax + rx;
                            resultY = ay + ry;
                        }
                        e = calcError(-ax + rx, -ay + ry, 0)[0];
                        if (e < minimum) {
                            minimum = e;
                            resultX = -ax + rx;
                            resultY = -ay + ry;
                        }
                        rx += hx;
                        ry += hy;
                    }

                    double newAngle = Math.atan2(resultY, resultX);
                    if (Math.abs(newAngle - startAngle) / (Math.abs(newAngle) + Math.abs(startAngle)) > 0.001) {
                        System.out.println("corrected on step " + i);
                        correctionOnStep = i;
                        startAngle = newAngle;
                        corrected = true;
                        break;
                    }
                }

                double ax = i*vx;
                double ay = i*vy;
                double e = calcError(ax, ay, 0)[0];
                if (e < min) {
                    min = e;
                    resX = ax;
                    resY = ay;
                }
                e = calcError(-ax, -ay, 0)[0];
                if (e < min) {
                    min = e;
                    resX = -ax;
                    resY = -ay;
                }
            }

        }while(corrected == true);


        return new double[]{resX, resY};
    }

    public static ArrayList<double[]> correctionTest(double startAngle) {

        double diameter = 0.3;

        ArrayList<double[]> vectors = new ArrayList<>();

        int stepCount = 20;
        boolean corrected;


        do {
            corrected = false;
            double vx = Math.cos(startAngle) * diameter / stepCount;
            double vy = Math.sin(startAngle) * diameter / stepCount;

            for (int i = 1; i < stepCount; i++) {

                if (i % 5 == 0) {

                    // angle correction
                    int corrStepCount = 5;

                    // Math.PI/sampleCount == angle difference between sample lines in rotary probe
                    double scale = diameter / stepCount * i;
                    double h = Math.sin(Math.PI / sampleCount) * scale;

                    // vector perpendicular to line in startAngle
                    double hx = h * Math.cos(startAngle + Math.PI / 2);
                    double hy = h * Math.sin(startAngle + Math.PI / 2);

                    // starting point of correction probes
                    double ax = Math.cos(startAngle) * scale - hx / 2;
                    double ay = Math.sin(startAngle) * scale - hy / 2;

                    hx /= corrStepCount;
                    hy /= corrStepCount;

                    for (int j = 0; j < corrStepCount+1; j++) {

                        vectors.add(new double[]{ax, ay});

                        ax += hx;
                        ay += hy;
                    }
                }

                double ax = i*vx;
                double ay = i*vy;

                vectors.add(new double[]{ax, ay});
                vectors.add(new double[]{-ax, -ay});
            }

        }while(corrected == true);


        return vectors;
    }


    public double getDefaultZetPivot()
    {
        return 23;
    }

    public static void main(String[] args) {
        //totalBasics();
       // invariantTest();
      // evolution(new TowardsGlory());

        testVectors();
    }

    public static void testVectors() {

        VectorView view = new VectorView();

        ArrayList<double[]> vcs = correctionTest(0.2);

        for (double[] v : vcs) {
            view.addVector(v);
        }

        vcs = correctionTest(0.2 + Math.PI/sampleCount);

        for (double[] v : vcs) {
            view.addVector(v);
        }

        JFrame frm = new JFrame();
        frm.getContentPane().add(view);

        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setSize(500,500);

        frm.setVisible(true);
    }
    /**
     * basic check of concept and implementation of invariant
     */
    public static void totalBasics() {

        TowardsGlory problem = new TowardsGlory();

        for (int k=0; k<10; k++) {
            double ax = Math.random()*2 - 1;
            double ay = Math.random()*2 - 1;
            double az = Math.random()*2 - 1;


            problem.rotateTest(ax, ay, az);

            double errorAcumulator = 0;
            double errorAcumulatorX = 0;
            double errorAcumulatorY = 0;
            double errorAcumulatorZ = 0;
            ArtefactsXY artxy = new ArtefactsXY();
            ArtefactsZ artz = new ArtefactsZ();

            for (int i=0; i<problem.projected_zero.length; i++) {
                artxy.init(problem.matrix_test, problem.projected_zero[i][0], problem.projected_zero[i][1], problem.projected_test[i][0], problem.projected_test[i][1]);
                artz.init(problem.matrix_test, problem.projected_zero[i][0], problem.projected_zero[i][1], problem.projected_test[i][0], problem.projected_test[i][1]);
                double y1 = artxy.y1();
                double y2 = artxy.y2();
                double x1 = artxy.x1();
                double x2 = artxy.x2();
                double z1 = artz.z1();
                double z2 = artz.z2();
                double dif = Math.abs(y1 - y2);
                double sum = Math.abs(y1) + Math.abs(y2);
                double ey = 0;
                if (sum != 0) {
                    ey = (dif / sum);
                }
                dif = Math.abs(x1 - x2);
                sum = Math.abs(x1) + Math.abs(x2);
                double ex = 0;
                if (sum != 0) {
                    ex = (dif / sum);
                }
                dif = Math.abs(z1 - z2);
                sum = Math.abs(z1) + Math.abs(z2);
                double ez = 0;
                if (sum != 0) {
                    ez = (dif / sum);
                }

                errorAcumulatorX += ex;
                errorAcumulatorY += ey;
                errorAcumulatorZ += ez;

                errorAcumulator += ex + ey + ez;
            }

            errorAcumulator /= 3*problem.projected_test.length;
            errorAcumulatorX /= problem.projected_test.length;
            errorAcumulatorY /= problem.projected_test.length;
            errorAcumulatorZ /= problem.projected_test.length;

            System.out.println("-----\n error: " + errorAcumulator);
            System.out.println(" errorx: " + errorAcumulatorX);
            System.out.println(" errory: " + errorAcumulatorY);
            System.out.println(" errorz: " + errorAcumulatorZ);
        }

    }

    public static void invariantTest() {
        TowardsGlory problem = new TowardsGlory();

        System.out.println("------- test of invariant ------");

        for (int k=0; k<10; k++) {
            double ax = Math.random() * 2 - 1;
            double ay = Math.random() * 2 - 1;
            double az = Math.random() * 2 - 1;

            problem.rotateTarget(ax, ay, az);

            double[] e = problem.calcError(ax, ay, az);
            System.out.println("e = " + e[0]);

        }

        System.out.println("-------- test of error on -ax, -ay");

        for (int k=0; k<10; k++) {
            double ax = Math.random() * 2 - 1;
            double ay = Math.random() * 2 - 1;

            problem.rotateTarget(ax, ay, 0);

            double[] e  = problem.calcError( ax,  ay, 0);
            double[] e2 = problem.calcError(-ax, -ay, 0);
            System.out.println("e = " + e[0] + ", e2 = " + e2[0]);

        }

    }

    public static StereoAgent evolution(TowardsGlory problem) {
        if (problem == null)
            problem = new TowardsGlory();

        Reactor reactor = new Reactor();
        StereoAgent adam = new StereoAgent(problem);

        AbstractAgent boss = null;
        int trials = 0;
        do {
            System.out.println("trial :" + trials);
            reactor.initPopulation(adam, 100, Math.PI/2, Math.PI/80, 0.3);
            int i;
            for (i = 0; i < 400; i++) {
                boss = reactor.iterate();
                if (boss.getFitness() < 0.002) {
                    break;
                }
            }
            System.out.println("fitness:" + boss.getFitness());
            System.out.println("iterations:" + i);
            trials ++;

        }while (trials < 4 && boss.getFitness() > 0.0012);


        StereoAgent winner = (StereoAgent)boss;

        // test coordinates and test matrixFOE are setup_basic_error_graph to values from winner
        winner.calcFitness();

        return winner;


    }



/*
    void nechapacky()
    {


        for (int i=0; i<transformed.length; i++) {
            Artefacts2v art = new Artefacts2v(matrixFOE, projected_zero[i][0], projected_zero[i][1], projected[i][0], projected[i][1]);
            double val1 = art.Value1();
            double val2 = art.Value2();
            //System.out.println(i + ".)" + val1 + "      " + val2);

            double dif = Math.abs(val1 - val2);
            double sum = Math.abs(val1) + Math.abs(val2);
            if (sum == 0)
                System.out.println(i + ".) zero ");
            else {
                dif = (dif/ sum);
                if (dif < 0.00001)
                    System.out.println(i + ".) 0");
                else
                    System.out.println(i + ".) " + dif);
            }
        }
    }

*/

}
