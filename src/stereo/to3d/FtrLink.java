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
    public MatchedFtr mf2;
    public double e;
    public ArrayList<MatchedFtr> candidates;
    
    public FtrLink(Feature f1, MatchedFtr f2, double e)
    {
        candidates = new ArrayList<>();
        this.f1 = f1;
        this.mf2 = f2;
        this.e = e;
    }
    
    public Feature getFeature(int i) {
        if (i == 0) {
            return f1;
        }
        if (mf2 != null) {
            return mf2.f;
        }
        return null;
    }
    
    public void setF2(MatchedFtr mf) {
        mf2 = mf;
        f2 = mf.f;
    }
}
