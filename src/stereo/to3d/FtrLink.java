/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.to3d;

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

    public FtrLink(Feature f1, Feature f2, double e)
    {
        this.f1 = f1;
        this.f2 = f2;
        this.e = e;
    }
}
