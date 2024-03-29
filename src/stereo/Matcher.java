
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo;

import stereo.poi.CircleRaster;

/**
 *
 * @author karol presovsky
 */
public class Matcher
{
    
    public Greyscale g1;
    public Greyscale g2;
    public int[] b;
    public double[] weight;
    public int stampSize = 40;
    public int eyeSize = 5;
    CircleRaster circleRaster;
  
    Callback callback;
    
    public interface Callback
    {
        void convolve(double value);
        void convolveMax(double value);
        void eyeValue1(int index, int val);
        void eyeValue2(int index, int val);
    }
    
    public Matcher(Greyscale g1, Greyscale g2)
    {
        this.g1 = g1;
        this.g2 = g2;
        b = new int[stampSize*stampSize];
        weight = new double[stampSize*stampSize];
        calcWeights();
        circleRaster = new CircleRaster(eyeSize);
    }
    
    public int getCircleLength()
    {
        return circleRaster.length();
    }

    public void setCallback(Callback callback)
    {
        this.callback = callback;
    }
    
    public double compareEyes(int x1, int y1, int x2, int y2)
    {
        int r = circleRaster.getRadius();
        
        if (x1 < r || y1 < r || x1 + r >= g1.width || y1 + r >= g1.height) 
            return 0;
        if (x2 < r || y2 < r || x2 + r >= g2.width || y2 + r >= g2.height) 
            return 0;
        
        if (callback!=null)
        {
            for (int i=0; i<circleRaster.length(); i++)
            {
                int cx = circleRaster.getX(i);
                int cy = circleRaster.getY(i);
                callback.eyeValue1(i, g1.get(x1+cx, y1+cy));
                callback.eyeValue2(i, g2.get(x2+cx, y2+cy));
            }
        }

            
        int iOffs = circleRaster.length()/8;
        int max=0;
        for (int k = -iOffs/2; k<iOffs/2; k++)
        {
            int sum = 0;
            for (int i=0; i<circleRaster.length(); i++)
            {
                int adr  = (circleRaster.getX(i)+x1) + (circleRaster.getY(i)+y1)*g1.width;
                int i2 = i+k;
                if (i2 < 0)
                    i2 = circleRaster.length() + i2 - 1;
                if (i2 >= circleRaster.length())
                    i2 -= circleRaster.length();
                
                int adr2 = (circleRaster.getX(i2)+x2) + (circleRaster.getY(i2)+y2)*g2.width;

                sum += g1.px[adr] * g2.px[adr2];
            }
            
            if (callback != null)
                callback.convolve(sum);
            if (sum > max)
                max = sum;
        }
        return (double)max/circleRaster.length()/(256*256);
    }
    
    
    void calcWeights()
    {
        int m = stampSize*stampSize/4;
        for (int j=0; j<stampSize; j++)
        {
            double y = j - stampSize/2.0;
            for (int i=0; i<stampSize; i++)
            {
                double x = i - stampSize/2.0;
                x = x*x + y*y;
                x /= m;
                x = 1.0 - x;
                if (x < 0) x = 0;
                
                weight[j*stampSize + i] = x;
            }
        }
    }

    public double match(int x1, int y1, int x2, int y2)
    {
        y1 -= stampSize / 2;
        x1 -= stampSize / 2;
        y2 -= stampSize / 2;
        x2 -= stampSize / 2;
        int w = stampSize;
        int h = stampSize;
        
        if (x1 < 0)
        {
            w += x1;
            x2 -= x1;
            x1 = 0;
        }
        if (x2 < 0)
        {
            w += x2;
            x1 -= x2;
            x2 = 0;
        }
        if (y1 < 0)
        {
            h += y1;
            y2 -= y1;
            y1 = 0;
        }
        if (y2 < 0)
        {
            h += y2;
            y1 -= y2;
            y2 = 0;
        }
        
        if (x1 + stampSize >= g1.width)
            w -= x1+stampSize - g1.width;
        if (x2 + stampSize >= g2.width)
            w -= x2+stampSize - g2.width;
        if (y1 + stampSize >= g1.height)
            h -= y1+stampSize - g1.height;
        if (y2 + stampSize >= g2.height)
            h -= y2+stampSize - g2.height;

        int wshiftx = (stampSize - w)/2;
        int wshifty = (stampSize - h)/2;
        double e = 0;
        int max = 0;
        int min = 10000000;
        for (int j = 0; j < h; j++)
        {
            int adr1 = (y1 + j) * g1.width + x1;
            int adr2 = (y2 + j) * g2.width + x2;
            int adr = (j + wshifty) * stampSize;
            for (int i = 0; i < w; i++)
            {
                int v = Math.abs(g1.px[adr1 + i] - g2.px[adr2 + i]);
                v = (int) (v * weight[adr + i + wshiftx]);
                if (v < min)
                {
                    min = v;
                }
                if (v > max)
                {
                    max = v;
                }
                b[adr + i] = v;
                //e += spotBuff[adr+i] = view1.gs.px[adr1+i];
                e += v;
            }
        }
        e/=h*w*256;

        return e;
    }
    
    
    
}
