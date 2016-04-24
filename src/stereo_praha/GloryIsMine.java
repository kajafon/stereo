package stereo_praha;

import stereo_praha.gui.Object3D;
import stereo_praha.gui.stuff3D;

/**
 * Created by Karol Presovsky on 8/30/14.
 */
public class GloryIsMine implements ProblemInterface{

    double[][] projected_target;
    double[][] projected_zero;
    double[] matrix_test = Algebra.unity(null);

    double zetPivot;
    double angleZ;

    double moveX;
    double moveY;
    double moveZ;

    public static final int sampleCount = 500;
    public static final int rotaryProbeStepCount = 100;
    public static final double diameter = 0.4;
    public static final int lineDetectorStepCount = 400;

    boolean withCorrection = false;

    // -------- statistics -----

    public int corrections;
    public int calculations;
    public double finalError;

    public void setZetPivot(double zetPivot) {
        this.zetPivot = zetPivot;
    }

    public boolean isWithCorrection() {
        return withCorrection;
    }

    public void setWithCorrection(boolean withCorrection) {
        this.withCorrection = withCorrection;
    }

    Object3D testObject;

    int version;

    double[] projectedScalars_zero;
    double[] projectedScalars_target;


    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public GloryIsMine(double[][] prj1, double[][] prj2, double angleZ) {
        projected_zero = prj1;
        projected_target = prj2;
        this.angleZ = angleZ;

        double[][] vertex = new double[projected_zero.length][];
        for (int i=0; i<vertex.length; i++)
            vertex[i] = new double[3];

        testObject = new Object3D(vertex, null);
        testObject.matrix = matrix_test;

        projectedScalars_zero   = Algebra.scalarEvaluation(prj1, null);
        projectedScalars_target = Algebra.scalarEvaluation(prj2, null);

    }

    private void prepareMatrix(double angelX, double angelY, double angelZ) {
        Algebra.unity(matrix_test);

        stuff3D.rotate(matrix_test, angelX, angelY, angelZ, zetPivot);
        matrix_test[12] += moveX;
        matrix_test[13] += moveY;
        matrix_test[14] += moveZ;
    }

    double[] errorstruct = new double[4];

    @Override
    public double[] calcError(double angelX, double angelY, double angelZ) {
        return calcError_invarinace(angelX, angelY, angelZ);
    }

    double[][] t_vrt1;
    double[][] t_vrt2;
    double[] scalarValues1;
    double[] scalarValues2;

    public double[] calcError_scalar(double angelX, double angelY, double angelZ) {

        if (t_vrt1 == null) {
            t_vrt1 = new double[projected_zero.length][3];
            t_vrt2 = new double[projected_zero.length][3];
        }

        calculations++;

        prepareMatrix(angelX, angelY, angelZ);

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

            t_vrt1[i][0] = x1;
            t_vrt1[i][1] = y1;
            t_vrt1[i][2] = z1;

            t_vrt2[i][0] = x2;
            t_vrt2[i][1] = y2;
            t_vrt2[i][2] = z2;
        }

        scalarValues1 = Algebra.scalarEvaluation(t_vrt1, scalarValues1);
        scalarValues2 = Algebra.scalarEvaluation(t_vrt2, scalarValues2);

        errorstruct[0] = 0;
        for (int i=0; i<scalarValues1.length; i++) {
            errorstruct[0] += Math.abs(scalarValues1[i] - scalarValues2[i]);
        }

        errorstruct[0] /= scalarValues1.length;

        return errorstruct;
    }

    public double[] calcError_invarinace(double angelX, double angelY, double angelZ) {

        calculations++;

        prepareMatrix(angelX, angelY, angelZ);

        double[] errorAcumulator = errorstruct;
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


    public double[] calcError_projections(double angelX, double angelY, double angelZ) {
        calculations++;

        prepareMatrix(angelX, angelY, angelZ);

        double errorAcumulator = 0;
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

            testObject.vertex[i][0] = (x1+x2)/2;
            testObject.vertex[i][1] = (y1+y2)/2;
            testObject.vertex[i][2] = (z1+z2)/2;
        }

        testObject.project();

        scalarValues1 = Algebra.scalarEvaluation(testObject.projected, scalarValues1);
        testObject.setRotation(angelX, angelY, angleZ, zetPivot);
        testObject.project();
        scalarValues2 = Algebra.scalarEvaluation(testObject.projected, scalarValues2);

        errorstruct[0] = 0;
        for (int i=0; i<scalarValues1.length; i++) {
            errorstruct[0] += Math.abs(projectedScalars_zero[i] - scalarValues1[i]);
            errorstruct[0] += Math.abs(projectedScalars_target[i] - scalarValues2[i]);
        }

        errorstruct[0] /= scalarValues1.length*2;

        return errorstruct;
    }

    /**
     * returns detected angle
     * @return
     */
    public double rotaryProbe(double az) {


        double[] samples = new double[sampleCount];
        for ( int sampleIndx = 0; sampleIndx < sampleCount; sampleIndx++) {

            double vx = Math.cos(Math.PI/sampleCount * sampleIndx) * diameter / rotaryProbeStepCount;
            double vy = Math.sin(Math.PI/sampleCount * sampleIndx) * diameter / rotaryProbeStepCount;

            double accum = 0;
            for (int i = 0; i < rotaryProbeStepCount; i++) {
                double ax = i*vx;
                double ay = i*vy;
                accum += calcError(ax, ay, az)[0];
                accum += calcError(-ax, -ay, az)[0];
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

    /**
     * returns detected angle
     * @return
     */
    public double rotaryProbe2(double az) {

        double minAngle = -1000000;
        double min = 10000;
        final double diameter = 0.2;

        for (double angle = 0; angle < Math.PI; angle += Math.PI/sampleCount)
        {
            double ax = Math.cos(angle) * diameter;
            double ay = Math.sin(angle) * diameter;

            double e = calcError(ax, ay, az)[0];

            if (min > e) {
                min = e;
                minAngle = angle;
            }
        }

        double angle1 = minAngle - Math.PI/sampleCount;
        double angle2 = minAngle + Math.PI/sampleCount;
        double a = minAngle;
        double e = min;
        double deltaAngle = 0.0001;

        for (int i=0; i<10; i++) {

            double grad = calcError(
                    Math.cos(a+deltaAngle) * diameter,
                    Math.sin(a+deltaAngle) * diameter, 0)[0];

            grad -= e;

            if (grad > 0) {
                angle1 = a;
            } else {
                angle2 = a;
            }

            a = (angle1 + angle2)/2;
            e = calcError(
                    Math.cos(a) * diameter,
                    Math.sin(a) * diameter, 0)[0];

        }

        return a;
    }


    public double[] minimumOnLineCorrected(double startAngle, double az) {

        int correctionOnStep = 0;
        boolean corrected;

        double resX;
        double resY;
        double min;

        do {
            corrected = false;
            double vx = Math.cos(startAngle) * diameter / lineDetectorStepCount;
            double vy = Math.sin(startAngle) * diameter / lineDetectorStepCount;
            resX = 100000;
            resY = 100000;
            min = 100000;

            for (int i = 1; i < lineDetectorStepCount; i++) {

                double ax = i*vx;
                double ay = i*vy;
                double e1 = calcError(ax, ay, az)[0];
                double e2 = calcError(-ax, -ay, az)[0];

                if (e1 < min) {
                    min = e1;
                    resX = ax;
                    resY = ay;
                }
                if (e2 < min) {
                    min = e2;
                    resX = -ax;
                    resY = -ay;
                }

                if (withCorrection && i > correctionOnStep && i % 5 == 0) {

                    // angle correction
                    int corrStepCount = 5;

                    // Math.PI/sampleCount == angle difference between sample lines in rotary probe
                    double scale = diameter / lineDetectorStepCount * i;
                    double h = Math.sin(Math.PI / sampleCount) * scale;

                    // vector perpendicular to line in startAngle
                    double hx = h * Math.cos(startAngle + Math.PI / 2);
                    double hy = h * Math.sin(startAngle + Math.PI / 2);

                    // starting point of correction probes

                    double rx = - hx / 2;
                    double ry = - hy / 2;

                    hx /= corrStepCount;
                    hy /= corrStepCount;

                    double minCorr = e1;
                    double resXCorr = ax;
                    double resYCorr = ay;
                    if (e2 < e1) {
                        resXCorr = -ax;
                        resYCorr = -ay;
                    }

                    for (int j = 0; j < corrStepCount; j++) {
                        double e = calcError(ax + rx, ay + ry, az)[0];
                        if (e < minCorr) {
                            minCorr = e;
                            resXCorr = ax + rx;
                            resYCorr = ay + ry;
                        }
                        e = calcError(-ax + rx, -ay + ry, az)[0];
                        if (e < minCorr) {
                            minCorr = e;
                            resXCorr = -ax + rx;
                            resYCorr = -ay + ry;
                        }
                        rx += hx;
                        ry += hy;
                    }

                    double newAngle = Math.atan2(resYCorr, resXCorr);
                    double difference = Math.abs(newAngle - startAngle) / (Math.abs(newAngle) + Math.abs(startAngle));
                    if (difference > 0.0001) {
                        correctionOnStep = i;
                        startAngle = newAngle;
                        corrected = true;
                        corrections++;
                        break;
                    }
                }
            }

        }while(corrected == true);

        finalError = min;
        return new double[]{resX, resY, startAngle};
    }

    public double[] solve(double moveX, double moveY, double moveZ, double zetPivot) {
        this.moveX = moveX;
        this.moveY = moveY;
        this.moveZ = moveZ;
        this.zetPivot = zetPivot;
        return solve();
    }

    double[] solve(double zPivot) {
        this.zetPivot = zPivot;
        return solve();
    }

    private double[] solve() {
        corrections = -1;
        calculations = 0;
        finalError = -1;

        double angleOfAngles = rotaryProbe2(angleZ);
        return minimumOnLineCorrected(angleOfAngles, angleZ);
    }

    public double[][] reconstruct(double angelX, double angelY, double angelZ, double[][] vertex) {

        prepareMatrix(angelX, angelY, angelZ);

        ArtefactsXY artxy = new ArtefactsXY();
        ArtefactsZ artz = new ArtefactsZ();

        if (vertex == null)
            vertex = new double[projected_zero.length][];

        for (int i=0; i<projected_zero.length; i++) {
            artxy.init(matrix_test, projected_zero[i][0], projected_zero[i][1], projected_target[i][0], projected_target[i][1]);
            artz.init(matrix_test, projected_zero[i][0], projected_zero[i][1], projected_target[i][0], projected_target[i][1]);

            double x,y,z;
            if (version == 0) {
                z = artz.z1();
                y = artxy.y1();
                x = artxy.x1();
            } else {
                z = artz.z2();
                y = artxy.y2();
                x = artxy.x2();
            }
            vertex[i] = new double[]{x,y,z};

        }

        return vertex;
    }

    public double[] lineGraph(double startAngle, double az) {

        double[] g1 = new double[lineDetectorStepCount-1];
        double[] g2 = new double[lineDetectorStepCount-1];


        double vx = Math.cos(startAngle) * diameter / lineDetectorStepCount;
        double vy = Math.sin(startAngle) * diameter / lineDetectorStepCount;


        for (int i = 1; i < lineDetectorStepCount; i++) {
            double ax = i*vx;
            double ay = i*vy;
            double e = calcError(ax, ay, az)[0];


            g2[i-1] = e;

            e = calcError(-ax, -ay, az)[0];


            g1[lineDetectorStepCount - i - 1] = e;
        }

        double[] graph = new double[g1.length + g2.length];

        for (int i=0; i<lineDetectorStepCount-1; i++) {
            graph[i] = g1[i];
        }

        for (int i=0; i<lineDetectorStepCount-1; i++) {
            graph[i+lineDetectorStepCount-1] = g2[i];
        }
        return graph;
    }

    public AbstractReliever relaxer = new AbstractReliever(new double[2], 0.01) {
        @Override
        public double getTension(double[] x) {
            return calcError(x[0], x[1], 0)[0];
        }
    };

    public double[][] relax(double[] start, double stepSize, int stepCount, double[] target) {

        double[][] steps = new double[stepCount][];

        relaxer.init(start, stepSize);
        relaxer.setTarget(target);

        for (int i=0; i<stepCount; i++) {
            relaxer.relax();
            double[] x = relaxer.getX();
            steps[i] = new double[]{x[0], x[1], relaxer.val};
        }

        return steps;
    }

    public static void invariantTest() {

        double zPivot = 20;
        // one cube, 4 triangles
        double[][] vertex = new double[][] {
                {-1,-1, zPivot + 1},
                { 1,-1, zPivot},
                {-1, 1, zPivot},
                { 1, 1, zPivot},

                {-1,-1, zPivot+1},
                { 1,-1, zPivot+2},
                {-1, 1, zPivot+2},
                { 1, 1, zPivot+2},
        };

        int [][] triangles = new int[][] {
                {0,1,2,0}, {2,1,3,2}, {4,5,6,4}, {5,6,7,5}
        };

        double[][] clonedVertex = new double[vertex.length][];
        for (int i=0; i<vertex.length; i++) {

            clonedVertex[i] = new double[]{vertex[i][0], vertex[i][1], vertex[i][2]};
        }

        Object3D o1 = new Object3D(vertex, triangles);
        Object3D o2 = new Object3D(o1.transformed, triangles);


        GloryIsMine problem = new GloryIsMine(o1.projected, o2.projected, 0);
        problem.setZetPivot(zPivot);

        System.out.println("------- test of invariant ------");

        for (int k=0; k<10; k++) {
            double ax = Math.random() * 2 - 1;
            double ay = Math.random() * 2 - 1;

            o1.setRotation(ax, ay, 0, zPivot);
            o1.project();

            ax = Math.random() * 2 - 1;
            ay = Math.random() * 2 - 1;

            o2.setRotation(ax, ay, 0, zPivot);
            o2.project();

            double[] e = problem.calcError(ax, ay, 0);
            System.out.println("e = " + e[0]);
        }
    }

    public static void main(String[] args) {
        invariantTest();
    }

}
