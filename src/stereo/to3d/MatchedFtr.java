/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.to3d;

import stereo.poi.Feature;

/**
 *
 * @author sdsdf
 */
public class MatchedFtr {
    public Feature f;
    public double e;
    public double angle;

    public MatchedFtr() {}
    public MatchedFtr(Feature f, double e, double angle) {
        this.f = f;
        this.e = e;
        this.angle = angle;
    }        
}
