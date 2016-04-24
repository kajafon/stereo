/*
 * 
 * 
 */

package stereo;

/**
 *
 * @author Karol Presovsky
 */
public class Matrix
{
    float[] m;
    int w;
    int h;

    public Matrix(int w, int h)
    {
        this.w = w;
        this.h = h;
        m = new float[h*w];
    }

    public float get(int x, int y)
    {
        return m[x + w*y];
    }

    public void set(float v, int x, int y)
    {
        m[x + w*y] = v;
    }



}
