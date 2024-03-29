/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.to3d;

import java.util.ArrayList;
import java.util.Collection;
import stereo.poi.Feature;

/**
 *
 * @author karol presovsky
 */
    
public class FtrLink
{
    public Feature f1;
    public Feature f2;
    public double e;
    public ArrayList<Feature> candidates;
    
    public FtrLink(Feature f1, Feature f2, double e)
    {
        this.f1 = f1;
        this.f2 = f2;
        this.e = e;
    }
}
