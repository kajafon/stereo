/*
 * 
 * 
 */
package stereo;

/**
 *
 * @author Karol Presovsky
 */
public class Probe
{

    int v1;
    int v2;
    int x1;
    int y1;
    int x2;
    int y2;

    public Probe(int v1, int v2)
    {
        this.v1 = v1;
        this.v2 = v2;
    }

    public void setCoords(int x1, int y1, int x2, int y2)
    {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public void setValues(int v1, int v2)
    {
        this.v1 = v1;
        this.v2 = v2;
    }
}
