/*
 * 
 * 
 */
package stereo;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author Karol Presovsky
 */
public class Greyscale
{

    public short[] px;
    public int width;
    public int height;

    public int getHeight()
    {
        return height;
    }

    public int getWidth()
    {
        return width;
    }

    public Greyscale(int w, int h)
    {
        width = w;
        height = h;
        px = new short[width * height];
    }

    public Greyscale(short[] px, int width, int height)
    {
        this.px = px;
        this.width = width;
        this.height = height;
    }

    public short get(int x, int y)
    {
        return px[x + width * y];
    }

    /**
     * sets pixel value. coordinates are not checked!
     * @param v
     * @param x
     * @param y 
     */
    public void set(short v, int x, int y)
    {
        px[x + width * y] = v;
    }

    public Greyscale getCopy(Greyscale copy)
    {
        if (copy == null)
        {
            copy = new Greyscale(width, height);
        }

        System.arraycopy(px, 0, copy.px, 0, height * width);
        return copy;
    }

    public static Greyscale toGreyscale(BufferedImage image)
    {
        short[] gs = new short[image.getWidth(null) * image.getHeight(null)];
        for (int i = 0; i < image.getWidth(); i++)
        {
            for (int j = 0; j < image.getHeight(); j++)
            {
                int rgb = image.getRGB(i, j);
                int r = rgb & 255;
                int g = (rgb >> 8) & 255;
                int b = (rgb >> 16) & 255;

                gs[i + j * image.getWidth()] = (short) ((r + g + b) / 3);
            }
        }
        return new Greyscale(gs, image.getWidth(), image.getHeight());
    }
    
    public void inversion()
    {
        for (int i=0; i<px.length; i++)
        {
            px[i] = (short)(255 - px[i]);
        }
    }
   
    Greyscale rotateImage(double angle)
    {
        Greyscale gs = this;
        Greyscale gs2 = new Greyscale(gs.width, gs.height);
        
        int x = gs.width/2;
        int y = gs.height/2;
        double sinx = Math.sin(-angle);
        double cosx = Math.cos(-angle);
        int x1 = (int)Numeric._rotateX(0,0,cosx, sinx);
        int y1 = (int)Numeric._rotateY(0,0,cosx, sinx);
        int x2 = (int)Numeric._rotateX(gs.width,0,cosx, sinx);
        int y2 = (int)Numeric._rotateY(gs.width,0,cosx, sinx);
        int x3 = (int)Numeric._rotateX(gs.width,gs.height,cosx, sinx);
        int y3 = (int)Numeric._rotateY(gs.width,gs.height,cosx, sinx);
        int x4 = (int)Numeric._rotateX(0,gs.height,cosx, sinx);
        int y4 = (int)Numeric._rotateY(0,gs.height,cosx, sinx);
         
        
        Triangle.triangleShort(x1,y1, x2, y2, x3, y3, 
                               0, 0, gs.width-1, 0, gs.width-1, gs.height-1, gs.px, gs.width, gs2.px, gs.width, gs.height);
        Triangle.triangleShort(x1,y1, x3, y3, x4, y4,
                               0, 0, gs.width-1, gs.height-1, 0, gs.height-1, gs.px, gs.width, gs2.px, gs.width, gs.height);  
        
        return gs2;
        
    }
        
    public Histogram calcHistogram()
    {
        Histogram histo = new Histogram(0, 256, 256);
        for (short v:px)
        {
            histo.add(v);
        }
        
        return histo;
    }

    public static void smooth(Greyscale src, Greyscale dest)
    {
        int adr = src.width + 1;
        for (; adr < src.px.length - 2 * src.width - 1; adr++)
        {
            int a = (src.px[adr] + src.px[adr + 1] + src.px[adr + src.width] + src.px[adr + src.width + 1]
                    + src.px[adr - 1 + src.width] + src.px[adr - 1] + src.px[adr - 1 - src.width] + src.px[adr - src.width]
                    + +src.px[adr - src.width + 1]) / 9;
            dest.px[adr] = (short) a;
        }

        adr = src.width * (src.height - 1);
        for (int i = 0; i < src.width; i++)
        {
            dest.px[i] = src.px[i];
            dest.px[adr + i] = 0xfff;//src.px[adr + i];
        }

        adr = 0;
        int stop = src.width * src.height;
        while (adr < stop)
        {
            dest.px[adr] = src.px[adr];
            dest.px[adr + (src.width - 1)] = src.px[adr + (src.width - 1)];
            adr += src.width;
        }
    }

    public static void smoothBetter(Greyscale src, Greyscale dest, int range)
    {

        int startSum = 0;
        int startCount = 0;
        int halfRange = range / 2;

        int adr = 0;
        for (int j = 0; j < halfRange; j++)
        {
            for (int i = 0; i < halfRange; i++)
            {
                startSum += src.px[adr + i];
                startCount++;
            }
            adr += src.width;
        }

        if (startCount == 0)
        {
            System.out.println("error with better smoothing, count == 0");
            return;
        }

        adr = 0;
        for (int j = 0; j < src.height; j++)
        {
            int sum = startSum;
            int count = startCount;

            for (int i = 0; i < src.width;)
            {
                
                dest.px[adr + i] = (short) (sum / count);
                i++;

                // recalc sum in x- direction

                if (i > halfRange)
                {
                    int y1 = j > halfRange ? j - halfRange : 0;
                    int y2 = j + halfRange >= src.height ? src.height - 1 : j + halfRange;
                    int x = i - halfRange;

                    int _adr = y1 * src.width + x;
                    while (y1 <= y2)
                    {
                        sum -= src.px[_adr];
                        count--;
                        _adr += src.width;
                        y1 += 1;
                    }
                }
                
                if (i + halfRange < src.width)
                {
                    int y1 = j > halfRange ? j - halfRange : 0;
                    int y2 = j + halfRange >= src.height ? src.height - 1 : j + halfRange;
                    int x = i + halfRange;

                    int _adr = y1 * src.width + x;
                    while (y1 <= y2)
                    {
                        sum += src.px[_adr];
                        _adr += src.width;
                        count++;
                        y1 += 1;

                    }

                }

            }
            

            // recalc sum in y-direction

            if (j > halfRange)
            {
                int _adr = (j-halfRange) * src.width;
                for (int x=0; x<halfRange; x++)
                {
                    startSum -= src.px[_adr + x];
                    startCount--;
                }
            }
            if (j + halfRange < src.height)
            {
                int _adr = (j+halfRange) * src.width;
                for (int x=0; x<halfRange; x++)
                {
                    startSum += src.px[_adr + x];
                    startCount++;
                }
            }

            adr += src.width;
        }



    }

    public void sqrt()
    {
        double exp = 0.4;
        double max = Math.pow(255, exp);

        for (int i = 0; i < px.length; i++)
        {
            double v = Math.pow(px[i], exp) / max * 255;
            px[i] = (short) v;
        }
    }

    public static Greyscale edgesFilter(Greyscale src, Greyscale dest)
    {
        System.out.println("edges");

        if (dest == null)
        {
            dest = new Greyscale(src.getWidth(), src.getHeight());
        }
        dest.clear();
        int adr = src.width;
        for (; adr < src.px.length - src.width; adr++)
        {
            // (a-b) - (b-c) = a+c-2b
            int a = Math.abs(src.px[adr] - src.px[adr + 1]);
            int b = Math.abs(src.px[adr] - src.px[adr + src.width]);
            int c = Math.abs(src.px[adr] - src.px[adr - 1]);
            int d = Math.abs(src.px[adr] - src.px[adr - src.width]);

            dest.px[adr] = (short) ((a + b + c + d) / 3);
        }

        return dest;
    }
    
    public static Greyscale fillShapes(Greyscale src, Greyscale dest)
    {
        System.out.println("edges");

        if (dest == null)
        {
            dest = new Greyscale(src.getWidth(), src.getHeight());
        }
        dest.clear();
        int adr = src.width;
        for (; adr < src.px.length - src.width; adr++)
        {
            // (a-b) - (b-c) = a+c-2b
            int a = Math.abs(src.px[adr] - src.px[adr + 1]);
            int b = Math.abs(src.px[adr] - src.px[adr + src.width]);
            int c = Math.abs(src.px[adr] - src.px[adr - 1]);
            int d = Math.abs(src.px[adr] - src.px[adr - src.width]);

            a = ((a + b + c + d) / 4);
            dest.px[adr] = (short)((a + src.px[adr])/2);
        }

        return dest;
    }
        
    

    public static void edgesFilter_(Greyscale src, Greyscale dest)
    {
     //   System.out.println("edges");

        int adr = 0;
        for (; adr < src.px.length - src.width - 1; adr++)
        {
            int a = Math.abs(src.px[adr] - src.px[adr + 1]);
            int b = Math.abs(src.px[adr] - src.px[adr + src.width]);
            int c = Math.abs(src.px[adr] - src.px[adr + 1 + src.width]);

            dest.px[adr] = (short) ((a + b + c) / 3);
        }
    }

    public static void localContrast(Greyscale src, Greyscale dest, Matrix m)
    {
     //   System.out.println("local contrast");
        int radius = m.w / 2;
        for (int j = 0; j < src.height; j++)
        {
            if (j % radius == 0)
            {
                System.out.println("." + (100 * j / src.height));
            }

            for (int i = 0; i < src.width; i++)
            {
                int j1 = j - radius;
                if (j1 < 0)
                {
                    j1 = 0;
                }
                int j2 = j + radius;
                if (j2 > src.height)
                {
                    j2 = src.height;
                }
                int i1 = i - radius;
                if (i1 < 0)
                {
                    i1 = 0;
                }
                int i2 = i + radius;
                if (i2 > src.width)
                {
                    i2 = src.width;
                }

                short min = 255;
                short max = 0;

                if (i == 100 && j == 100)
                {
                    System.out.println(" sto");
                }
                for (int _j = j1; _j < j2; _j++)
                {
                    for (int _i = i1; _i < i2; _i++)
                    {
                        short v = src.get(_i, _j);
                        if (v > max)
                        {
                            float w = m.get(_i - i + radius, _j - j + radius);
                            w = w * (v - max);
                            max = (short) (max + w);
                        }
                        if (v < min)
                        {
                            float w = m.get(_i - i + radius, _j - j + radius);
                            w = w * (v - min);
                            min = (short) (min + w);
                        }
                    }
                }

                max -= min;
                if (max > 0)
                {
                    //  System.out.println(" max > 0");
                    for (int _j = j1; _j < j2; _j++)
                    {
                        for (int _i = i1; _i < i2; _i++)
                        {
                            short v = src.get(_i, _j);
                            float fnv = (float) 255 * (v - min) / max;
                            short nv = (short) (fnv);
                            dest.set(nv, _i, _j);
                        }
                    }
                }


            }
        }
    }

    public void bw(int t)
    {
        for (int i = 0; i < px.length; i++)
        {
            if (px[i] > t)
            {
                px[i] = 255;
            } else
            {
                px[i] = 0;
            }
        }
    }

    public int[] contrast()
    {
     //   System.out.println("contrast");

        int min = 255;
        int max = 0;

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                int adr = i + j * width;
                if (min > px[adr])
                {
                    min = px[adr];
                }
                if (max < px[adr])
                {
                    max = px[adr];
                }
            }
        }

        int[] limits = new int[]
        {
            min, max
        };

        max -= min;

        if (max == 0)
        {
            return limits;
        }

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                int adr = i + j * width;

                px[adr] = (short) ((double) (px[adr] - min) / max * 255);
            }
        }

        return limits;

    }

    public void clear()
    {
        for (int i = 0; i < px.length; i++)
        {
            px[i] = 0;
        }
    }

    public void contrast(int low, int high)
    {
        System.out.println("contrast");

        high -= low;

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                int adr = i + j * width;
                int val = 255*(px[adr] - low) / high;
                if (val > 255)
                    val = 255;
                if (val < 0)
                    val = 0;

                px[adr] = (short) val;
            }
        }

    }

    public void corners()
    {
        corners(this);
    }
    
    public void corners(Greyscale target)
    {
        short[] accu = new short[px.length];
        
        int shiftx = 0;
        int shifty = 0;
        
        for (shiftx = -1; shiftx <= 1; shiftx++)
        {       
            for (shifty = -1; shifty <= 1; shifty++)
            {
                if (shifty == 0 && shiftx == 0)
                    continue;

                for (int j = 0; j<height; j++)
                {
                    int jj = j + shifty;
                    if (jj < 0) continue;
                    if (jj >= height) break;
                    int adr = j*width;
                    int adr2 = jj*width;
                    for (int i = 0; i<width; i++)
                    {
                        int ii = i + shiftx;
                        if (ii < 0) continue;
                        if (ii >= width) break;
                        int dif = px[adr+i] - px[adr2+ii];

                        if (dif>=0)
                            accu[adr + i] += (short)(dif/8);
                        else
                            accu[adr + i] += (short)-(dif/8);
                    }            
                }
            }
        }
        
        if (target == null)
            target = this;
        
        for (int i=0; i<px.length; i++)
        {
            px[i] = accu[i];
        }
    }
    
    public ArrayList<int[]> localMaxims(ArrayList<int[]> list, int thrsh)
    {
        if (list == null)
            list = new ArrayList<int[]>();

        int adr = width+1;
        for (int j = 1; j < height-1; j++)
        {
            for (int i = 1; i < width-1; i++,adr++)
            {
                if (px[adr] < thrsh) continue;
                if (px[adr] <= px[adr-1      ]) continue;
                if (px[adr] <= px[adr-1-width]) continue;
                if (px[adr] <= px[adr  -width]) continue;
                if (px[adr] <= px[adr+1-width]) continue;
                if (px[adr] <= px[adr+1      ]) continue;
                if (px[adr] <= px[adr+1+width]) continue;
                if (px[adr] <= px[adr  +width]) continue;
                if (px[adr] <= px[adr-1+width]) continue;
               
                list.add(new int[]{i,j,px[adr]});
                
            }
            adr+=2;
        }
        return list;
    }

    public BufferedImage createImage(BufferedImage img)
    {
        if (img == null)
        {
            img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                short b = px[i + j * width];
                img.setRGB(i, j, (int) b + ((int) b << 8) + ((int) b << 16));
            }
        }
        return img;
    }

    void paint(BufferedImage img, int x, int y)
    {
        if (x > img.getWidth()
                || y > img.getHeight())
        {
            return;
        }
        if (x + width <= 0 || y + height <= 0)
        {
            return;
        }

        int endx = x + width;
        int endy = y + height;

        if (endx > img.getWidth())
        {
            endx = img.getWidth();
        }

        if (endy > img.getHeight())
        {
            endy = img.getHeight();
        }

        int pxi = 0;
        int pxj = 0;
        if (x < 0)
        {
            pxi = -x;
            x = 0;
        }

        if (y < 0)
        {
            pxj = -y;
            y = 0;
        }

        int adr = pxj * width + pxi;

        for (; y < endy; y++, adr += width)
        {
            for (int i = 0; x + i < endx; i++)
            {
                int v = px[adr + i];
                img.setRGB(x + i, y, (int) v + ((int) v << 8) + ((int) v << 16));
            }
        }

    }

    void paint(Greyscale output, int x, int y)
    {
        if (x > output.getWidth()
                || y > output.getHeight())
        {
            return;
        }
        if (x + width <= 0 || y + height <= 0)
        {
            return;
        }

        int endx = x + width;
        int endy = y + height;

        if (endx > output.getWidth())
        {
            endx = output.getWidth();
        }

        if (endy > output.getHeight())
        {
            endy = output.getHeight();
        }

        int pxi = 0;
        int pxj = 0;
        if (x < 0)
        {
            pxi = -x;
            x = 0;
        }

        if (y < 0)
        {
            pxj = -y;
            y = 0;
        }

        int adr = pxj * width + pxi;

        for (; y < endy; y++, adr += width)
        {
            for (int i = 0; x + i < endx; i++)
            {
                output.px[x + i + y * output.width] = px[adr + i];
            }
        }

    }
}
