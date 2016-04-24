/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.poi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import stereo.Histogram;
import stereo.Matcher;
import stereo.Outline;

/**
 *
 * @author karol presovsky
 */
public class Relation
{

    public Cntx2 c1;
    public Cntx2 c2;
    public double error;
    public double z3d;
    public double x3d;
    public double y3d;
    
    public ArrayList<Double> errors = new ArrayList<Double>();
    boolean cross = false;
    int diff;
    
    public void addError(double e)
    {
        errors.add(e);
    }

    public Relation(Cntx2 c1, Cntx2 c2, double e)
    {
        this.c1 = c1;
        this.c2 = c2;
        this.error = error;
    }
    
    /*
     * creates relation from two vertices and registers new relation to those vertices
     */
    public static Relation create(Cntx2 c1, Cntx2 c2)
    {
        Relation r = new Relation(c1, c2, 0);
        c1.relations.add(r);
        c2.relations.add(r);
        return r;
    }

}
