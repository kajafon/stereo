/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.poi;

/**
 *
 * @author sdsdf
 */
public class SiftStamp {
    public SiftStamp(int[] v, double a) {
        this.vector = v;
        this.angle = a;
    }
    public int[] vector;
    public double angle;
}