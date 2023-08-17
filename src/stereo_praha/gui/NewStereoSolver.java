//ty vole 
package stereo_praha.gui;

import stereo_praha.*;

import java.awt.*;
import java.util.ArrayList;
import stereo.to3d.Face;
import stereo.to3d.FtrLink;

public class NewStereoSolver extends StereoTask {

    NewStereoSolver otherSolver = null;    
    
    double focalLength = 15;
    double originZPosition = 40;
    double problem_anglex;
    double problem_angley;

    double[][] sourceProjection = null;
    
    
    /* */
    Object3D rays1 = new Object3D();
    Object3D rays2 = new Object3D();
    Object3D plane1 = new Object3D();
    Object3D plane2 = new Object3D();
    Object3D outline1 = new Object3D();
    Object3D outline2 = new Object3D();
    Object3D gold = new Object3D();
    Object3D otherGold = new Object3D();
    Object3D otherRays2 = new Object3D();
    Object3D serviceObj = new Object3D();

    Scene3D scene = new Scene3D("scene");
    Scene3D ray2Subscene = new Scene3D("ray2Subscene");
    double minimalZ;
    double goldSize;
    
    ArrayList<Object3D> distanceObject;
    ArrayList<Face> faceList;

    ArrayList<FtrLink> featureLinks;
    
    double unknownAngle = 0.2;
    
    /* input data of the task: 2 projections of unknown 3d geometry */
    double[][] origin_projection_1;
    double[][] origin_projection_2;
    
    /* */
    double[][] gold_projection_1;
    double[][] gold_projection_2;
    
    int [][]   origin_triangles;

    double mutationStrength = 0.5; 
    int test;
    
    public NewStereoSolver(Object3D obj, double ax, double ay)
    {
        processObject(obj, ax, ay);
        init();
    }
    
    public NewStereoSolver(ArrayList<FtrLink> links, ArrayList<Face> faces) {

        featureLinks = links;
        faceList = faces;
        processFeatures();
        init();
    }
    
    public void setOtherSolver(NewStereoSolver other) throws Exception {
        if (other == this) {
            throw new Exception("other solver is the same as this");
        }
        otherSolver = other;        
    }
    
    public void copyOtherGold() {     
        
        if (!otherSolver.gold.isEnabled()) {
            otherGold.setEnabled(false);
            System.out.println("... other gold not ready");
            return;
        }
        
        otherGold.setEnabled(true);
        otherGold.init(otherSolver.gold.vertex.length, otherSolver.gold.polygons.length, otherSolver.gold.polygons[0].length);

        otherGold.color = Color.red;
        
        for(int i=0; i<otherSolver.gold.vertex.length; i++) {
            otherGold.vertex[i][0] = otherSolver.gold.vertex[i][0];
            otherGold.vertex[i][1] = otherSolver.gold.vertex[i][1];
            otherGold.vertex[i][2] = otherSolver.gold.vertex[i][2];
        }
        
        for(int i=0; i<otherSolver.gold.polygons.length; i++) {
            for (int j=0; j<otherSolver.gold.polygons[i].length; j++) {
                otherGold.polygons[i][j] = otherSolver.gold.polygons[i][j];
            }        
        }
        
        otherGold.setEnabled(true);
        
        System.arraycopy(otherSolver.gold.matrix, 0, otherGold.matrix, 0, otherGold.matrix.length);        
    }
    
    void copyOtherRays2() {        
        otherSolver.project();
        otherRays2.init(otherSolver.rays2.transformed.length, otherSolver.rays2.polygons.length, 2);
        otherRays2.setColor(Color.red);
        for (int i=0; i<otherSolver.rays2.transformed.length; i++) {
            otherRays2.vertex[i][0] = otherSolver.rays2.transformed[i][0];
            otherRays2.vertex[i][1] = otherSolver.rays2.transformed[i][1];
            otherRays2.vertex[i][2] = otherSolver.rays2.transformed[i][2];
        }
        
        for (int i=0; i<otherSolver.rays2.polygons.length; i++) {
            otherRays2.polygons[i][0] = otherSolver.rays2.polygons[i][1];
            otherRays2.polygons[i][1] = otherSolver.rays2.polygons[i][0];
        }

        otherRays2.setEnabled(true);
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
                obj.polygons[i][j] = origin_triangles[i][j];
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
        double size = focalLength / 2;
        double[][] pln = {{size,size,0}, {size,-size,0},{-size,-size,0},{-size,size,0}};
        int[][] t = {{0,1,2,3,0}};
        final Object3D obj = new Object3D(pln, t);
        obj.setName("plane");
        return obj;
    }
    
    public double getError()
    {
        return goldError;
    }
    
    @Override
    public double[] getVector()
    {
        double[] out = new double[16];
        
        Algebra.copy(this.rays2.matrix, out); 
        return out;
    }

    @Override
    public void setVector(double[] vec)
    {       
        Algebra.copy(vec, this.rays2.matrix); 
    }
      
    @Override
    public void applySolution()
    {
        __reconstruction();
    }

    private void processObject(Object3D origin, double originAx, double originAy)
    {        
        problem_anglex = originAx;
        problem_angley = originAy;
        
        origin_projection_1 = new double[origin.projected.length][2];
        origin_projection_2 = new double[origin.projected.length][2];
        origin_triangles = new int[origin.polygons.length][origin.polygons[0].length];
        
        for (int i=0; i<origin.polygons.length; i++)
        {
            for (int j=0; j<origin.polygons[0].length; j++)
            {
                origin_triangles[i][j] = origin.polygons[i][j];
            }
        }
        
        Algebra.setPosition(origin.matrix, 0,0, originZPosition);
        origin.project();
                
        for (int i=0; i<origin_projection_1.length; i++)
        {
            origin_projection_1[i][0] = origin.projected[i][0]*this.focalLength;
            origin_projection_1[i][1] = origin.projected[i][1]*this.focalLength;
        }        
        
        stuff3D.setRotation(origin.matrix, originAx, originAy, 0);
//        origin.setRotation(originAx, originAy, 0);
        origin.project();
        
        for (int i=0; i<origin_projection_2.length; i++)
        {
            origin_projection_2[i][0] = origin.projected[i][0]*this.focalLength;
            origin_projection_2[i][1] = origin.projected[i][1]*this.focalLength;
        }
        
        stuff3D.setRotation(origin.matrix, 0, 0, 0);
        origin.project();
    }
    
    private void processFeatures()
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
    
    private void init()
    {
        rays1.setName("rays");
        rays2.setName("rays2");
        
        rays1.setColor(new Color(0,0,0, .2f));
        rays2.setColor(new Color(0,0,1.0f, .2f));
        
        plane1 = createPlane();
        plane2 = createPlane();
        
        plane1.setColor(Color.gray);
        plane2.setColor(Color.blue);
        
        gold.name = "gold";
        otherGold.name = "other gold";
        
        otherGold.setEnabled(false);
        otherRays2.setEnabled(false);
        
        serviceObj.setEnabled(false);

        ray2Subscene.add(rays2);
        ray2Subscene.add(plane2);
        ray2Subscene.add(outline2);
        
        scene.add(rays1);
        scene.add(plane1);
        scene.add(outline1); 
        scene.add(ray2Subscene);
        scene.add(otherRays2);
        scene.add(serviceObj);
                
        rays1 = SpringInspiration.objectFromProjection(origin_projection_1, rays1, this.focalLength, 5);
        outline1 = createOutline(origin_projection_1, outline1);
        
        rays2 = SpringInspiration.objectFromProjection(origin_projection_2, rays2, this.focalLength, 5);

        outline2 = createOutline(origin_projection_2, outline2);
        
        /* projected rays need for solution kreation */
        scene.project();
        
        distanceObject = SpringInspiration.calcDistanceObjects(rays1, rays2, distanceObject);
        
        int i=0;
        for(Object3D obj: distanceObject) {
            obj.name = "line " + i;
            scene.add(obj);
        }        
        
        goldError = __reconstruction();
        scene.add(gold);        
        scene.add(otherGold);
        
        /* create a result object */
        
        Object3D resultPlane = createPlane();
        Object3D rays2Result = SpringInspiration.objectFromProjection(origin_projection_2, null, this.focalLength, 5);

        resultPlane.color = Color.LIGHT_GRAY;
        rays2Result.color = Color.LIGHT_GRAY;
        
        Scene3D resultScene = new Scene3D();
        
        resultScene.add(resultPlane);
        resultScene.add(rays2Result);
        
        stuff3D.rotate(resultScene.matrix, -problem_anglex, -problem_angley, 0, originZPosition);
        
        scene.add(resultScene);

        System.out.println("..");
    }
    
    /**
     * calc two vectors to apply to rays2 scene to move rays intersection to expected position
     * scene is assumed to be projected for reconstruction */
    public void fixRaysPosition(Object3D movingRaysObj, Object3D positionedRaysObj, double[] outTranslation){
        
        double[] ray1Pos = Algebra.getPositionBase(positionedRaysObj.tmp_matrix, null);
        double[] ray2Pos = Algebra.getPositionBase(movingRaysObj.tmp_matrix, null);
        
        double[] ray1vec = Algebra.getZBase(positionedRaysObj.tmp_matrix, null);
        double[] ray2vec = Algebra.getZBase(movingRaysObj.tmp_matrix, null);
        
        double[] intersection = Algebra.linesDistanceSquare(ray1vec, ray1Pos, ray2vec, ray2Pos);
        /** the closes point on ray1 to ray2*/
        double[] p1 = new double[]{intersection[1],intersection[2],intersection[3]};

        /** the closes point on ray2 to ray1*/
        double[] p2 = new double[]{intersection[4],intersection[5],intersection[6]};
        
        /** raypos is the position of projection plane.
         * here we want to make sure that reconstructed object 
         * is on the opposite side of the plane to the focal point
         * 
         */
        double dist1 = Algebra.distance(ray1Pos, p1);
        double dist2 = Algebra.distance(ray2Pos, p2);
        Algebra.clear(outTranslation);
        
        if (dist1 < focalLength){
            double[] tmp = new double[3];
            Algebra.copy(ray1vec, tmp);
            Algebra.scale(tmp, dist1);
            Algebra.copy(tmp, outTranslation);
        }      

        if (dist2 < focalLength){
            double[] tmp = new double[3];
            Algebra.copy(ray2vec, tmp);
            Algebra.scale(tmp, -dist2);
            Algebra.add(tmp, outTranslation, outTranslation);
        }      

        serviceObj.setEnabled(true);
        serviceObj.init(2, 1, 2);
//        
        Algebra.copy(p1, serviceObj.vertex[0]);
        Algebra.copy(p1, serviceObj.vertex[1]);
        Algebra.add(outTranslation, serviceObj.vertex[1], serviceObj.vertex[1]);
//
        serviceObj.polygons[0][0] = 0;
        serviceObj.polygons[0][1] = 1;
//        
        serviceObj.color = Color.red;        
//        
//        for(Object3D o : distanceObject){ o.setEnabled(false); }
    }
    
    public void placeIt() {        
        Algebra.unity(ray2Subscene.matrix);
        stuff3D.rotate(ray2Subscene.matrix, -problem_anglex, -problem_angley, 0, originZPosition);
        reconstruct();
    }
    
    void project() {
        scene.project();
    }
    
    public void randomize() {        
        double dist = Math.random() * focalLength;
        
        double x = Math.random() * focalLength;
        double y = Math.random() * focalLength;
        double z = Math.random() * focalLength/4;

        double r = Math.random() * Math.PI;
        double rx = Math.random() * r;
        double ry = Math.random() * r;
        double rz = r;
        
        Algebra.unity(ray2Subscene.matrix);
        
        Algebra.rotate3D(ray2Subscene.matrix, new double[]{rx, ry, rz});
        scene.project();
    }
    
    public void reconstruct() {
        double[] mtrx_sceneBackup = Algebra.duplicate(scene.matrix);
        Algebra.unity(scene.matrix);
        scene.project();
        
        SpringInspiration.calcDistanceObjects(rays1, rays2, distanceObject, Color.RED);
        
        __reconstruction();
        
        Algebra.copy(mtrx_sceneBackup, scene.matrix);
        scene.project();
    }
    
    public void relaxAndReconstruct() {
        double[] mtrx_sceneBackup = Algebra.duplicate(scene.matrix);
        Algebra.unity(scene.matrix);
        scene.project();
       
        __relax();
        __reconstruction();
        
        Algebra.copy(mtrx_sceneBackup, scene.matrix);
        scene.project();
    }
    
    /** !!! make sure that scene is projected with scene.matrix == unity !!! */
    private double __reconstruction()
    {
        System.out.println("reconstruction");
        
        scene.project();

        if (distanceObject.size() == 0) {
            System.out.println("gold vert count: 0");
        }
        gold.init(distanceObject.size(), origin_triangles.length, origin_triangles[0].length);
        
        /* copy polygon indicies */
        for (int j=0; j<origin_triangles.length; j++)
        {
            for (int i=0; i<origin_triangles[j].length; i++)
            {
                gold.polygons[j][i] = origin_triangles[j][i];
            }
        }
        
        Aggregator agr = new Aggregator(3);
        Aggregator agr_e = new Aggregator(1);
                
        for (int i=0; i<distanceObject.size(); i++)
        {
            Object3D link = distanceObject.get(i);
            double x = (link.transformed[0][0] + link.transformed[1][0])/2;
            double y = (link.transformed[0][1] + link.transformed[1][1])/2;
            double z = (link.transformed[0][2] + link.transformed[1][2])/2;
            
            gold.vertex[i][0] = x;
            gold.vertex[i][1] = y;
            gold.vertex[i][2] = z;
            
            double e = Math.abs(link.transformed[0][0] - link.transformed[1][0]); 
            e += Math.abs(link.transformed[0][1] - link.transformed[1][1]); 
            e += Math.abs(link.transformed[0][2] - link.transformed[1][2]); 
            
            agr.add(gold.vertex[i]);
            agr_e.add(e);            
        }
        
        Algebra.setPosition(gold.matrix, new double[]{agr.getAverage(0), agr.getAverage(1), agr.getAverage(2)});
        
        for (int i=0; i<distanceObject.size(); i++)
        {
            gold.vertex[i][0] -= agr.getAverage(0);
            gold.vertex[i][1] -= agr.getAverage(1);
            gold.vertex[i][2] -= agr.getAverage(2);            
        }
        
        minimalZ = agr.min[2];
        
        goldSize = agr.getSize();
        
        if ( goldSize < 0.000001)
            return Double.POSITIVE_INFINITY;
        
        double error = agr_e.getAverage(0) / goldSize;
        
        return error;
    }
    
    /** rotate ray2 scene randomly around local z axis */
    public void roll() {
        double[] pos = Algebra.getPositionBase(ray2Subscene.matrix, null);
        double[] zet = Algebra.getZBase(ray2Subscene.matrix, null);
        Algebra.scale(zet, Math.random() * 2 - 1);
        Algebra.subtractFromPosition(ray2Subscene.matrix, pos);
        Algebra.rotate3D(ray2Subscene.matrix, zet);
        Algebra.addToPosition(ray2Subscene.matrix, pos);
        scene.project();
    }
    
    public void rotateGold(double a, int axis) {
        double[] pos = Algebra.getPositionBase(gold.matrix, null);
        double[] tmp = null;
        
        switch(axis) {
            case Algebra.AXIS_Y : tmp = Algebra.getYBase(gold.matrix, null); break;
            case Algebra.AXIS_Z : tmp = Algebra.getZBase(gold.matrix, null); break;
        }
        
        Algebra.scale(tmp, a);
        Algebra.subtractFromPosition(gold.matrix, pos);
        Algebra.rotate3D(gold.matrix, tmp);
        Algebra.addToPosition(gold.matrix, pos);
        scene.project();
    }
    
    /** scales the impulse to weaken or strengthen its effect */
    public double impulseScaler = 1;
    
    /**
      apply on projected scene, need valid tmp_matrix of rays2 object
      !!! make sure that scene is projected with scene.matrix == unity !!!! 
    */    
    void __relax()
    {        
//        copyOtherGold();
        copyOtherRays2();

        scene.project();
        SpringInspiration.calcDistanceObjects(rays1, rays2, distanceObject, Color.RED);
        scene.project();

        /* impulse that is a combination of partial impulses where each of them 
           represents an attractive force between corresponding rays. 
        */


        Impulse impulse = new Impulse();
        if (otherRays2.isEnabled()) {
            impulse.init(distanceObject.size()*2);        
        } else {
            impulse.init(distanceObject.size());
        }
        double[] tmp = new double[3];
        
        for(Object3D link : distanceObject) {            
            /* add partial impulse that acts on the object ray2 in the point where the ray from ray2 is closest to 
               corresponding ray in ray1. this partial impulse materializes an idea of attractive force between
               corresponding rays in the two sets of rays
            */
            impulse.add(link.transformed[1], Algebra.difference(link.transformed[0], link.transformed[1], tmp));
        } 
        

        
        
        
        
//        if (otherGold.isEnabled()) {
//            for(int i=0; i<gold.transformed.length; i++) {            
//                
//                /** impulse vector to tmp */
//                Algebra.difference(otherGold.transformed[i], gold.transformed[i], tmp);
//                Object3D link = distanceObject.get(i);
//                Algebra.copy(gold.transformed[i], link.vertex[0]);
//                Algebra.add(tmp, link.vertex[0], link.vertex[1]);
//                impulse.add(gold.transformed[i], tmp);
//            }             
//        }
        
        if (otherRays2.isEnabled()) {
            SpringInspiration.calcDistanceObjects(otherRays2, rays2, distanceObject, Color.RED);
            scene.project();
            for(Object3D link : distanceObject) {            
                /* add partial impulse that acts on the object ray2 in the point where the ray from ray2 is closest to 
                   corresponding ray in ray1. this partial impulse materializes an idea of attractive force between
                   corresponding rays in the two sets of rays
                */
                impulse.add(link.transformed[1], Algebra.difference(link.transformed[0], link.transformed[1], tmp));
            }             
        }        

        double[] rotationVec = impulse.getRotation(null);
        double[] translationVec = impulse.getTranslation(null);
        Algebra.scale(rotationVec, impulseScaler, rotationVec);
        Algebra.scale(translationVec, impulseScaler, translationVec);
        
        /* now apply impulse rotation on ray2 in the impact center stored in tmp */
        impulse.getHitSpot(tmp);
        Algebra.subtractFromPosition(ray2Subscene.matrix, tmp);        
        Algebra.rotate3D(ray2Subscene.matrix, rotationVec);
        Algebra.addToPosition(ray2Subscene.matrix, tmp);
        
        /* apply impulse translation on ray2 matrix */
        Algebra.addToPosition(ray2Subscene.matrix, translationVec);
                
        fixRaysPosition(rays2, rays1, tmp);
        Algebra.addToPosition(ray2Subscene.matrix, tmp);
        
        if (otherRays2.isEnabled()) {
            fixRaysPosition(rays2, otherRays2, tmp);
            Algebra.addToPosition(ray2Subscene.matrix, tmp);            
        }
        
//        System.out.println("fixture: " + Algebra.size(tmp));
        System.out.println(" other gold:" + otherGold.isEnabled());
        System.out.println(" other ray2:" + otherRays2.isEnabled());
        
    }    
}

