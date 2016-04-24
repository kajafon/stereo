
package stereo;

/**
 *
 * @author Karol Presovsky
 */
public class Node
{

    public float x;
    public float y;
    public float acux = 0;
    public float acuy = 0;
    public int acu = 0;
    int ctrl = 0;

    public Node()
    {
    }
   


    public Node(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public boolean isOut()
    {
        return x < 0 || y < 0 || x > 1.0f || y > 1.0f;
    }
    
    public void acumulate(float x, float y)
    {
        acux += x;
        acuy += y;
        acu++;

     //   if (acu > 1000)
       //     System.out.println("--- co to je???!!!!");
    }

    public void applyAcumulated(float koef)
    {
        ctrl ++;
        if (acu == 0) return;
        x += koef*acux/acu;
        y += koef*acuy/acu;
    }
    
    public void addVectors(float koef)
    {
        x += koef*acux;
        y += koef*acuy;
    }

    public void clearAcu()
    {
        acux = acuy = 0;
        acu = 0;
        ctrl = 0;
    }
}
