//ty vole 
package stereo_praha.gui;

import evolve.AbstractAgent;
import evolve.Reactor;
import stereo_praha.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import stereo.to3d.Face;
import stereo.to3d.FtrLink;

public class StereoSolver extends StereoTask {

    double originAx = 0.4;
    double originAy = 0;
    
    double projectionScale = 15;
    double originZTranslation = 40;
    
    
    double scale = 300;
    
    double[][] sourceProjection = null;
    
    Object3D origin;
    Object3D rays1 = new Object3D();
    Object3D rays2 = new Object3D();
    Object3D plane1 = new Object3D();
    Object3D plane2 = new Object3D();
    Object3D outline1 = new Object3D();
    Object3D outline2 = new Object3D();
    Object3D gold = new Object3D();
    Object3D handle = new Object3D(1,0,0);

    Scene3D scene = new Scene3D("scene");
    Scene3D ray2Subscene = new Scene3D("ray2Subscene");
    Scene3D goldScene = new Scene3D("goldScene");
    double invarianceError;
    double minimalZ;
    double goldSize;
    double penalty;
    
    ArrayList<Object3D> links;
    ArrayList<Face> faceList;

    ArrayList<FtrLink> featureLinks;
    
    double unknownAngle = 0.2;
    
    double[][] origin_projection_1;
    double[][] origin_projection_2;
    double[][] gold_projection_1;
    double[][] gold_projection_2;
    
    int [][]   origin_triangles;
    double[] goldMatrix = new double[16];

    double mutationStrength = 0.5; 
    int test;
    
//    ArrayList<Adapter2d> adapters = new ArrayList<>();
//
//    public boolean addAdapter(Adapter2d e) {
//        return adapters.add(e);
//    }
//
//    public boolean removeAdapter(Object o) {
//        return adapters.remove(o);
//    }
//
//    public void clearAdapter() {
//        adapters.clear();
//    }
//    
//    public void notifyAdapters() {
//        for (Adapter2d a:adapters) a.updateFromSolver();
//    }
    
    
    public StereoSolver(Object3D obj, double ax, double ay)
    {
        origin = obj;
        originAx = ax;
        originAy = ay;
        processObject();
        init();
    }
    
    public StereoSolver(ArrayList<FtrLink> links, ArrayList<Face> faces) {

        featureLinks = links;
        faceList = faces;
        processFeatures();
        init();
    }
    
    Object3D createOutline(double[][] projected, Object3D obj)
    {
        if (obj == null) {
            obj = new Object3D();
            obj.setColor(Color.black);
        } 
        
        obj.init(projected.length, origin_triangles.length, origin_triangles[0].length);
        for(int i=0; i<projected.length; i++) {
            obj.vertex[i][0] = projected[i][0];
            obj.vertex[i][1] = projected[i][1];
            obj.vertex[i][2] = 0;
        }
        
        for(int i=0; i<origin_triangles.length; i++) {
            for(int j=0; j<origin_triangles[i].length; j++) { 
                obj.triangles[i][j] = origin_triangles[i][j];
            }
        } 
        
        return obj;
    }
    
    Object3D getGold() 
    {
        return gold;
    }
    
    Object3D createPlane()
    {
        double[][] pln = {{10,10,0}, {10,-10,0},{-10,-10,0},{-10,10,0}};
        int[][] t = {{0,1,2,3,0}};
        final Object3D obj = new Object3D(pln, t);
        obj.setName("plane");
        return obj;
    }
    
    void buildTask()
    {
        rays1 = SpringInspiration.objectFromProjection(origin_projection_1, rays1, focalLength, 5);
        outline1 = createOutline(origin_projection_1, outline1);
        
        rays2 = SpringInspiration.objectFromProjection(origin_projection_2, rays2, focalLength, 5);
        outline2 = createOutline(origin_projection_2, outline2);
        
        rays1.project();
        ray2Subscene.project();
        
        final ArrayList<Object3D> newLinks = SpringInspiration.createDistanceObjects(rays1, rays2, links);
        if (links == null) 
        {
            for(Object3D obj: newLinks) 
                scene.add(obj);
        }
        links = newLinks;
        
        goldError = reconstruction();
        
    }
    
    public void moveRays2()
    {
        ray2Subscene.setTranslation(moveX, moveY, moveZ);
    }
    
    public void rotateRays2()
    {
        ray2Subscene.setRotation(angleX, angleY, 0);
    }
    
    public double getError()
    {
        return goldError;
    }
    
    public void project() {
        scene.project();
    }
    
    @Override
    public double[] getVector()
    {
        return new double[]{moveX, moveY, moveZ, handle.vertex[0][0], handle.vertex[0][1], handle.vertex[0][2]}; 
    }

    @Override
    public void setVector(double[] vec)
    {
        moveX = vec[0]; 
        moveY = vec[1];
        moveZ = vec[2];
        handle.vertex[0][0] = vec[3];
        handle.vertex[0][1] = vec[4];
        handle.vertex[0][2] = vec[5];
        
        moveRays2();
        applyHandle();
        
    }
    
    void applyHandle()
    {
        double[] x1 = ray2Subscene.translation;
        double[] x2 = handle.vertex[0];
        double[] a = Algebra.anglesFromLine(x1, x2);
        
        angleX = Math.PI*1.5 + a[0];
        angleY = a[1];
        
        focalLength = Algebra.distance(x1, x2);

        applySolution();
        scene.project();

        double aa = a[0];
        double bb = a[1];
        
        
        if (aa > Math.PI) {
            aa = 2*Math.PI - aa;
        } 
        if (bb > Math.PI) {
            bb = 2*Math.PI - bb;
        } 

        if (Double.isNaN(aa)) {
           System.out.println("nan!!!!");
        }
//        notifyAdapters();
    }
    
    void cheat()
    {
        System.out.println("\n CHEATING \n");
        double originFromZero = originZTranslation - projectionScale;
         
        handle.vertex[0][0] = 0;
        handle.vertex[0][1] = 0;
        handle.vertex[0][2] = -originZTranslation;
        Object3D anchor = new Object3D(1, 0, 0);
        anchor.vertex[0][0] = 0;
        anchor.vertex[0][1] = 0;
        anchor.vertex[0][2] = -originFromZero; // projectionScale is a focal length of the problem
        
        
        Scene3D cheatScene = new Scene3D();
        cheatScene.add(handle);
        cheatScene.add(anchor);
        
        cheatScene.setRotation(-originAx, -originAy, 0);
        cheatScene.setTranslation(0, 0, originFromZero);
        cheatScene.project();
        
        setVector(new double[]{
            anchor.transformed[0][0], anchor.transformed[0][1], anchor.transformed[0][2], 
            handle.transformed[0][0], handle.transformed[0][1], handle.transformed[0][2]});   
        
    }
    
    @Override
    public void applySolution()
    {
        rotateRays2();
        moveRays2();
        buildTask();
        reconstruction();
    }

    void processObject()
    {        
        origin_projection_1 = new double[origin.projected.length][2];
        origin_projection_2 = new double[origin.projected.length][2];
        origin_triangles = new int[origin.triangles.length][origin.triangles[0].length];
        
        for (int i=0; i<origin.triangles.length; i++)
        {
            for (int j=0; j<origin.triangles[0].length; j++)
            {
                origin_triangles[i][j] = origin.triangles[i][j];
            }
        }
        
        origin.setTranslation(0,0,originZTranslation);
        origin.project();
        

        focalLength = projectionScale;
        
        for (int i=0; i<origin_projection_1.length; i++)
        {
            origin_projection_1[i][0] = origin.projected[i][0]*focalLength;
            origin_projection_1[i][1] = origin.projected[i][1]*focalLength;
        }
        
        
        origin.setRotation(originAx, originAy, 0);
        origin.project();
        
        for (int i=0; i<origin_projection_2.length; i++)
        {
            origin_projection_2[i][0] = origin.projected[i][0]*focalLength;
            origin_projection_2[i][1] = origin.projected[i][1]*focalLength;
        }
        
        origin.setRotation(0, 0, 0);
        origin.setTranslation(0,0,originZTranslation - focalLength);
        origin.project();

    }
    
    void processFeatures()
    {
        origin_projection_1 = new double[featureLinks.size()][2];
        origin_projection_2 = new double[featureLinks.size()][2];
        double maxx = -10000;
        double maxy = -10000;
        double minx = 10000;
        double miny = 10000;
        
        for (int i=0; i<featureLinks.size(); i++)
        {
            FtrLink fl = featureLinks.get(i);
            origin_projection_1[i][0] = fl.f1.x;
            origin_projection_1[i][1] = fl.f1.y;
            origin_projection_2[i][0] = fl.f2.x;
            origin_projection_2[i][1] = fl.f2.y;
            
            maxx = maxx > fl.f1.x ? maxx : fl.f1.x;
            maxx = maxx > fl.f2.x ? maxx : fl.f2.x;
            maxy = maxy > fl.f1.y ? maxy : fl.f1.y;
            maxy = maxy > fl.f2.y ? maxy : fl.f2.y;

            minx = minx < fl.f1.x ? minx : fl.f1.x;
            minx = minx < fl.f2.x ? minx : fl.f2.x;
            miny = miny < fl.f1.y ? miny : fl.f1.y;
            miny = miny < fl.f2.y ? miny : fl.f2.y;
        }
        
        maxx -= minx;
        maxy -= miny;
        
        if (maxx != 0 && maxy != 0) 
        {
        
            double size = 2; 
            for (int i=0; i<origin_projection_1.length; i++)
            {
                origin_projection_1[i][0] = (origin_projection_1[i][0] - minx)/maxx*size - size/2;
                origin_projection_1[i][1] = (origin_projection_1[i][1] - miny)/maxy*size - size/2;
                origin_projection_2[i][0] = (origin_projection_2[i][0] - minx)/maxx*size - size/2;
                origin_projection_2[i][1] = (origin_projection_2[i][1] - miny)/maxy*size - size/2;
            }
        } else
        {
            System.out.println("WARNING: zero size on normalization!");
        }
        origin_triangles = new int[faceList.size()][3];
        for(int i=0; i<faceList.size(); i++) 
        {
            Face f = faceList.get(i);
            origin_triangles[i][0] = f.r1;
            origin_triangles[i][1] = f.r2;
            origin_triangles[i][2] = f.r3;
        }
        
    }
    void init()
    {
        rays1.setName("rays");
        rays2.setName("rays2");
        
        rays1.setColor(new Color(0,0,0, .2f));
        rays2.setColor(new Color(0,0,1.0f, .2f));
        
        plane1 = createPlane();
        plane2 = createPlane();
        
        plane1.setColor(Color.gray);
        plane2.setColor(Color.blue);
        
        gold = new Object3D();

        ray2Subscene.add(rays2);
        ray2Subscene.add(plane2);
        ray2Subscene.add(outline2);
        
        scene.add(rays1);
        scene.add(plane1);
        scene.setTranslation(0, 0, 60);
        scene.add(outline1); 
        scene.add(ray2Subscene);
        scene.add(handle);
        scene.add(origin);
        
        handle.vertex[0][2] = -focalLength;
        
        buildTask();
        
        goldScene.add(gold);
        goldScene.setTranslation(0, 0, 60);
        
        System.out.println("..");
        
        cheat(); 

    }
    
    double reconstruction()
    {
        if (gold == null)
        {
            gold = new Object3D(links.size(), 1, links.size());
        } else {
            gold.init(links.size(), origin_triangles.length, origin_triangles[0].length);
        }
        
        for (int j=0; j<origin_triangles.length; j++)
        {
            for (int i=0; i<origin_triangles[j].length; i++)
            {
                gold.triangles[j][i] = origin_triangles[j][i];
            }
        }
        
        Aggregator agr = new Aggregator(3);
        Aggregator agr_e = new Aggregator(1);
                
        for (int i=0; i<links.size(); i++)
        {
            Object3D link = links.get(i);
            double x = (link.vertex[0][0] + link.vertex[1][0])/2;
            double y = (link.vertex[0][1] + link.vertex[1][1])/2;
            double z = (link.vertex[0][2] + link.vertex[1][2])/2;
            
            gold.vertex[i][0] = x;
            gold.vertex[i][1] = y;
            gold.vertex[i][2] = z;
            
            double e = Math.abs(link.vertex[0][0] - link.vertex[1][0]); 
            e += Math.abs(link.vertex[0][1] - link.vertex[1][1]); 
            e += Math.abs(link.vertex[0][2] - link.vertex[1][2]); 
            
            agr.add(gold.vertex[i]);
            agr_e.add(e);
            
        }
        
        double[] av = agr.getAverage();
        gold.setTranslation(-av[0], -av[1], -av[2] + 60);
        
        minimalZ = agr.min[2];
        
        goldSize = agr.getSize();
        
        if ( goldSize < 0.000001)
            return Double.POSITIVE_INFINITY;
        
        double error = agr_e.getAverage()[0] / goldSize;

        
        if (minimalZ < -focalLength/2)
        {
            penalty = -focalLength/2 - minimalZ;
            penalty = penalty * penalty + 1;
        } else
        {
            penalty = 1.0;
        }
            
        error *= penalty;
        
        return error;
    }


    
    public void evolve(JPanel panel)
    {
        Reactor reactor = new Reactor();

        RealAgent etalon = new RealAgent(StereoSolver.this);

        
        reactor.initPopulation( etalon, 70, mutationStrength*60, mutationStrength, 0.5);
        System.out.println("mutation:" + reactor.getMutationStrength());
        
        AbstractAgent boss = null;
        for (int j=0; j<3; j++) {
            for (int i=0; i<20; i++) {
                boss = reactor.iterate();
                boss.calcFitness();
                scene.project();
                panel.repaint();
            }
            reactor.setMutationStrength(reactor.getMutationStrength()/50);
        }    
        System.out.println("-" + boss.getFitness());
        System.out.println("    m:" + moveX + ", " + moveY + ", " + moveZ);
        System.out.println("    h:" + handle.vertex[0][0] + ", " + handle.vertex[0][1] + ", " + handle.vertex[0][2]);
    }
    
    public interface Adapter2d {
        public void setVector(double[] vec);
        public double[] getVector();
        public void updateFromSolver();
    }
    
    public class Adapter2d_xy implements Adapter2d, ProblemInterface {
        StereoSolver solver;  
        double[] handleRef = new double[2];

        @Override
        public double[] calcError(double x, double y, double angelZ) {
            setVector(new double[]{x, y});
            return new double[]{solver.goldError};
        }
        
        public Adapter2d_xy(StereoSolver solver) {
            this.solver = solver;
        }

        @Override
        public void updateFromSolver() {
            double[] vec = solver.getVector();
            handleRef[0] = vec[3] - vec[0];
            handleRef[1] = vec[4] - vec[1];    
        }

        public void setVector(double[] vec) {
            double[] theVec = solver.getVector();
            theVec[0] = vec[0];
            theVec[1] = vec[1];
            theVec[3] = vec[0] + handleRef[0];
            theVec[4] = vec[1] + handleRef[1];
            solver.setVector(theVec); //To change body of generated methods, choose Tools | Templates.
        }

        public double[] getVector() {
            double[] theVec = solver.getVector(); //To change body of generated methods, choose Tools | Templates.
            return new double[]{theVec[0], theVec[1]};
        }
    }
    
    public class Adapter2d_Angles implements Adapter2d, ProblemInterface{
        StereoSolver solver;  
        double[] handleRef;
        Object3D angleObject = new Object3D(2,0,0);
        
        @Override
        public double[] calcError(double x, double y, double angelZ) {
            setVector(new double[]{x, y});
            return new double[]{solver.goldError};
        }

        public Adapter2d_Angles(StereoSolver solver) {
            this.solver = solver;
        }

        @Override
        public void updateFromSolver() {
            double[] vec = solver.getVector();
            
            Object3D gold = solver.getGold();
            
            Aggregator agr = new Aggregator(3);
            for (double[] v:gold.vertex) {
                agr.add(v);
            }
            
            double[] pivot = agr.getAverage();
            
            angleObject.vertex[0][0] = vec[0] - pivot[0];
            angleObject.vertex[0][1] = vec[1] - pivot[1];
            angleObject.vertex[0][2] = vec[2] - pivot[2];
            angleObject.vertex[1][0] = vec[3] - pivot[0];
            angleObject.vertex[1][1] = vec[4] - pivot[1];
            angleObject.vertex[1][2] = vec[5] - pivot[2];
            angleObject.setTranslation(pivot[0], pivot[1], pivot[2]);
            angleObject.project();            
        }

        public void setVector(double[] vec) {
            angleObject.setRotation(vec[0], vec[1], 0);
            angleObject.project();
            double[] theVec = new double[6];
            theVec[0] = angleObject.transformed[0][0];
            theVec[1] = angleObject.transformed[0][1];
            theVec[2] = angleObject.transformed[0][2];
            theVec[3] = angleObject.transformed[1][0];
            theVec[4] = angleObject.transformed[1][1];
            theVec[5] = angleObject.transformed[1][2];
            solver.setVector(theVec); //To change body of generated methods, choose Tools | Templates.
        }

        public double[] getVector() {
            return new double[]{angleObject.getAngleX(), angleObject.getAngleY()};
        }
    }
    
    public Adapter2d getAdapter2d(String what) {
        Adapter2d adapter = null;
        if (what.equals("angles")) {
            adapter = new Adapter2d_Angles(this);
        } 
        
        if (what.equals("xy")) {
            adapter = new Adapter2d_xy(this);
        }
        
        if (adapter != null){
//            addAdapter(adapter);
            adapter.updateFromSolver();            
        }
        
        return adapter;
    }
    
    public AbstractReliever getReliever(String what) {
        Adapter2d adapter = getAdapter2d(what);
        return new AbstractReliever(adapter.getVector(), 2) {
            
            @Override
            public double getTension(double[] x) {
                adapter.setVector(x);
                return goldError;                
            }
        };
        
    } 
    

}

