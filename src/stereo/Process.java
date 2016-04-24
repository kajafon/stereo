/*
 * 
 * 
 */

package stereo;

/**
 *
 * @author Karol Presovsky
 */
public class Process
{
    public Greyscale gs1;
    public Greyscale gs2;


    public Process(Greyscale bmp)
    {
        gs1 = bmp;
        gs2 = new Greyscale(bmp.width, bmp.height);
    }

    public void switchBmp()
    {
        Greyscale tmp = gs1;
        gs1 = gs2;
        gs2 = tmp;
    }

}
