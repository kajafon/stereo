/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.poi;

import java.util.ArrayList;
import stereo.Outline;

/**
 *
 * @author karol presovsky
 */
public class Cntx
{

    public int x, y;
    public int p;
    public double[][] w = new double[3][3];

    public Cntx(int x, int y, int p, Outline outline)
    {
        this.x = x;
        this.y = y;
        this.p = p;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    
    public void add(Cntx other, double thrsh1, double thrsh2)
    {
        double d = (other.x - x) * (other.x - x) + (other.y - y) * (other.y - y);
        add(0, other.x, other.y);

        if (d < thrsh1)
        {
            add(1, other.x, other.y);
        }
        if (d < thrsh2)
        {
            add(2, other.x, other.y);
        }
    }

    public void add(int level, int x, int y)
    {
        w[level][0] += (x - this.x);
        w[level][1] += (y - this.y);
        w[level][2] += 1;
    }

    public void finish()
    {
        for (int i = 0; i < w.length; i++)
        {
            if (w[i][2] == 0)
            {
                continue;
            }
            w[i][0] /= w[i][2];
            w[i][1] /= w[i][2];
            w[i][2] = Math.sqrt(w[i][0] * w[i][0] + w[i][1] * w[i][1]);
        }
    }


    public double error(int level, Cntx other)
    {
        double s = w[level][2]; //size of vector 
        if (other.w[level][2] > s)
        {
            s = other.w[level][2];
        }

        if (s == 0)
        {
            return 0;
        }

        double u = w[level][0] - other.w[level][0];
        double v = w[level][1] - other.w[level][1];
        return Math.sqrt(u * u + v * v) / s;
    }
       
}
