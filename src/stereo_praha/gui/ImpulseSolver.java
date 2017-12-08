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

public class ImpulseSolver {

    double originAx = 0.4;
    double originAy = 0;
    
    double projectionScale = 15;
    double originZTranslation = 40;
    
    double focalLength = 20;
    
    
    double scale = 600;
    
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
    double goldSize;
    double goldError;    
    
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

    int test;
    
    double [][] pulls;
    
    
    public ImpulseSolver(Object3D obj, double ax, double ay)
    {
        origin = obj;
        originAx = ax;
        originAy = ay;
        processObject();
        init();
    }
    
    public ImpulseSolver(ArrayList<FtrLink> links, ArrayList<Face> faces) {

        featureLinks = links;
        faceList = faces;
        processFeatures();
        init();
    }
    
    Object3D getGold() 
    {
        return gold;
    }
    
    
    void buildTask()
    {
        rays1 = SpringInspiration.objectFromProjection(origin_projection_1, rays1, focalLength, 5);
        outline1 = createOutline(origin_projection_1, outline1);
        
        rays2 = SpringInspiration.objectFromProjection(origin_projection_2, rays2, focalLength, 5);
        outline2 = createOutline(origin_projection_2, outline2);

        links = new ArrayList<>();
        for (int i=0; i<origin_projection_1.length; i++){
            Object3D o = new Object3D(2,1,2);
            scene.add(o);
            links.add(o);
        }
    }
    
    public double getError()
    {
        return goldError;
    }
    
    public void project() {
        scene.project();
    }
    
//    void applyHandle()
//    {
//        double[] x1 = ray2Subscene.translation;
//        double[] x2 = handle.vertex[0];
//        double[] a = Algebra.anglesFromLine(x1, x2);
//        
//        double angleX = a[0];
//        double angleY = a[1];
//        
//        ray2Subscene.setRotation(angleX, angleY, 0);        
//        focalLength = Algebra.distance(x1, x2);
//
//        reconstruction();
//        scene.project();
//
//        double aa = a[0];
//        double bb = a[1];
//        
//        
//        if (aa > Math.PI) {
//            aa = 2*Math.PI - aa;
//        } 
//        if (bb > Math.PI) {
//            bb = 2*Math.PI - bb;
//        } 
//
//        if (Double.isNaN(aa)) {
//           System.out.println("nan!!!!");
//        }
////        notifyAdapters();
//    }
    
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
        reconstruction();
        scene.project();
        
        goldScene.add(gold);
        goldScene.setTranslation(0, 0, 60);
        goldScene.project();
        
        System.out.println("..");
        
        cheat(); 

    }
    
    double TARGET_ERROR = 0.002;
    void solve(Runnable callback) {
        
        int safetyCount = 10;
        int resetCount;
        System.out.println("solving");
        
        while (safetyCount > 0 && goldError >= TARGET_ERROR) {
            if (safetyCount <= 0) break;
            safetyCount--;
            
            resetCount = 100;
            
            while(resetCount > 0 && goldError >= TARGET_ERROR) {
                relax(resetCount);                
                resetCount--;   
                if (callback != null) {
                    callback.run();
                }
            }
            
            Algebra.unity(ray2Subscene.matrix);
            stuff3D.rotate(ray2Subscene.matrix, Algebra.rand(0, 2), Algebra.rand(0, 2),Algebra.rand(0, 2), 0);            
        }
        
        System.out.println("solved: " + goldError);
        
    }
    
    boolean relax(int k) {
        Impulse impulse = calcResultImpulse();
        
        Algebra.rotate3D(ray2Subscene.matrix, Algebra.scale(impulse.rotation, 4));  
        
        if (k%20 == 0) {
            System.out.println("translation");
            Algebra.combine(ray2Subscene.translation, impulse.translation, ray2Subscene.translation);
        }
        
        
        reconstruction();
        
        return goldError < TARGET_ERROR;
        
    }
    
    Impulse calcResultImpulse()
    {       
        Impulse impulse = new Impulse();
        impulse.init(links.size());
        
        double[] tmp = new double[3];
        for (Object3D link : links) {
            Algebra.difference(link.vertex[0], link.vertex[1], tmp);
            impulse.add(link.vertex[0], tmp);
        }         
        
        impulse.calc();
        
        return impulse;        
    }
    
    
    double reconstruction()
    {
        rays1.project();
        ray2Subscene.project();
        
        SpringInspiration.createDistanceObjects(rays1, rays2, links);
        
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
        
        goldSize = agr.getSize();
        
        if ( goldSize < 0.000001)
            return Double.POSITIVE_INFINITY;
        
        return goldError = agr_e.getAverage(0) / goldSize;

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
    
    Object3D createPlane()
    {
        double[][] pln = {{10,10,0}, {10,-10,0},{-10,-10,0},{-10,10,0}};
        int[][] t = {{0,1,2,3,0}};
        final Object3D obj = new Object3D(pln, t);
        obj.setName("plane");
        return obj;
    }


   

}

