/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.poi;

/**
 *
 * @author karol presovsky
 */
public class Feature
{
    public int x;
    public int y;
    
    public double[][] stamp;
    public double[][] stamp2;
    public int midValue;
    public int midValue2;

    public Feature(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
}
