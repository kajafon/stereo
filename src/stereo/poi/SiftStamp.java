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
    public SiftStamp(int x, int y, double a, double size) {
        this.x = x;
        this.y = y;
        this.angle = a;
        this.size = size;            
    }

    public SiftStamp(int x, int y, double a) {
        this.x = x;
        this.y = y;
        this.angle = a;
    }
    public int x;
    public int y;
    public double angle;
    public double size;
    public int[] vector;
}