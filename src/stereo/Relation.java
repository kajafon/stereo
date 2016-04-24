package stereo;

import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * finding a relation between images is to find a transform of the first image 
 * to resemble the second one as good as possible. approxy grid defines the transform
 * 
 * @author Karol Presovsky
 */
public class Relation
{

    Greyscale img1 = null;
    Greyscale img2 = null;
    Greyscale img1_original = null;
    Greyscale grd1 = null;
    Greyscale grd2 = null;
    Greyscale errorMap = null;
    Greyscale errorGrad = null;
    Greyscale combinedMap = null;
    public BufferedImage errorDisplay;
    public BufferedImage transDisplay;
    public BufferedImage errGradDisplay;
    public BufferedImage combinedDisplay;
    public AproxyGrid aproxy = null;
    float[][] errorVec;
    int[] errorLimits;
    int globalError;
    float[][][] errVecBackup;
    public double stepSize = 0.5;
    double endNum;
    int levelNum;
    int level;
    int smoothing;
    int img1OffsetX = 0;
    int img1OffsetY = 0;

    /* src1 has original size, src2 is 2^x times smaller
     */
    public Relation(int w, int h, int w2, int h2)
    {
        img1_original = new Greyscale(w, h);
        img1 = new Greyscale(w, h);
        grd1 = new Greyscale(w, h);
        grd2 = new Greyscale(w2, h2);
        errorMap = new Greyscale(w, h);
        errorGrad = new Greyscale(w, h);
        combinedMap = new Greyscale(w, h);

        errorDisplay = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        transDisplay = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        errGradDisplay = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        combinedDisplay = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        errorVec = new float[w * h][2];
    }

    public int getGlobalError()
    {
        return globalError;
    }

    public void take(Greyscale img1, Greyscale img2, AproxyGrid aproxy, int level, int levelNum, int smoothing)
    {
        endNum = Math.log(1.0f / aproxy.getWidth());
        this.levelNum = levelNum;
        this.level = level;
        this.smoothing = smoothing;

        this.img1_original = img1;
        this.img2 = img2;

        this.aproxy = aproxy;

        if (errVecBackup == null
                || errVecBackup.length != aproxy.getWidth()
                || errVecBackup[0].length != aproxy.getWidth())
        {
            errVecBackup = new float[aproxy.getWidth()][aproxy.getWidth()][2];
        }
        applyTransform();


    }

    public float[] getErrVec(int i, int j)
    {
        return errVecBackup[i][j];
    }

    public void applyTransform()
    {
        System.arraycopy(img2.px, 0, img1.px, 0, img1.px.length);
        transformImg(img1_original, img1, aproxy);
        transDisplay = img1.createImage(transDisplay);

        Greyscale.edgesFilter(img1, grd1);
        grd1.contrast();
        Greyscale.edgesFilter(img2, grd2);
        grd2.contrast();
    }

    /**
     * transforms "original" by means of approximation grid "a" and writes to 
     * "img"
     * @param original
     * @param img
     * @param a
     * @return 
     */
    public static void transformImg(Greyscale original, Greyscale img, AproxyGrid a)
    {
        int w = img.getWidth() - 1;
        int h = img.getHeight() - 1;

        double widthCoef = (double) (original.getWidth() - 1) / (a.getWidth() - 1);
        double heightCoef = (double) (original.getHeight() - 1) / (a.getWidth() - 1);

        for (int j = 0; j < a.getWidth() - 1; j++)
        {
            int v1 = (int) (j * heightCoef);
            int v2 = (int) ((j + 1) * heightCoef);

            for (int i = 0; i < a.getWidth() - 1; i++)
            {
                int u1 = (int) (i * widthCoef);
                int u2 = (int) ((i + 1) * widthCoef);

                int x1 = (int) (a.grid[i][j].x * w);
                int y1 = (int) (a.grid[i][j].y * h);
                int x2 = (int) (a.grid[i + 1][j].x * w);
                int y2 = (int) (a.grid[i + 1][j].y * h);
                int x3 = (int) (a.grid[i + 1][j + 1].x * w);
                int y3 = (int) (a.grid[i + 1][j + 1].y * h);

                Triangle.triangleShort(x1, y1, x2, y2, x3, y3, u1, v1, u2, v1, u2, v2, original.px, original.getWidth(), img.px, img.getWidth(), img.getHeight());

                x1 = (int) (a.grid[i][j].x * w);
                y1 = (int) (a.grid[i][j].y * h);
                x2 = (int) (a.grid[i][j + 1].x * w);
                y2 = (int) (a.grid[i][j + 1].y * h);
                x3 = (int) (a.grid[i + 1][j + 1].x * w);
                y3 = (int) (a.grid[i + 1][j + 1].y * h);

                Triangle.triangleShort(x1, y1, x2, y2, x3, y3, u1, v1, u1, v2, u2, v2, original.px, original.getWidth(), img.px, img.getWidth(), img.getHeight());
            }
        }
    }

    public short getImg2Value(int x, int y)
    {

        return 0;
    }

    public Greyscale getImg1()
    {
        return img1;
    }

    public Greyscale getImg2()
    {
        return img2;
    }

    public BufferedImage getErrorImg()
    {
        return errorDisplay;
    }

    public BufferedImage getResultImage()
    {
        return transDisplay;
    }

    double calcPixelError(int i, int j, int x2, int y2)
    {
        double a = Math.abs(img2.get(x2, y2) - img1.get(i, j));
        //double b = Math.abs(grd2.get(x2, y2) - grd1.get(i, j));
        //a = (a*b)/256;  
        return a;
    }

    public void calcErrorForDisplay(int level)
    {
        int probeSize = getProbeSize(level, levelNum);
        //int contrast = getGradientContrast(level);

        System.out.println("-calc: level:" + level + ", probeSize:" + probeSize);

        calcVectorMap(probeSize);
        errorMap.createImage(errorDisplay);

    }
    // calcs error of images. to errorImg writes normalized map of error

    public void calcErrorVectors(int probesize)
    {
        System.out.println("calc error map, probe size = " + probesize);

        //clear
        for (int i = 0; i < errorMap.px.length; i++)
        {
            errorMap.px[i] = 0;
        }

        globalError = 0;


        //int adr = 0;
        for (int j = probesize; j < img1.height - probesize; j++)//, adr+=img1.width)
        {
            int y2 = j;
            if (y2 >= img2.getHeight() - probesize)
            {
                break;
            }
            if (y2 < probesize)
            {
                continue;
            }

            for (int i = probesize; i < img1.width - probesize; i++)
            {
                int x2 = i;
                if (x2 < probesize)
                {
                    continue;
                }
                if (x2 >= img2.getWidth() - probesize)
                {
                    break;
                }

                double e = calcPixelError(i, j, x2, y2);

                globalError += e;

                double e1 = calcPixelError(i, j, x2 + probesize, y2);
                double e2 = calcPixelError(i, j, x2, y2 + probesize);
                double e3 = calcPixelError(i, j, x2 - probesize, y2);
                double e4 = calcPixelError(i, j, x2, y2 - probesize);

                // gradient of error * (-error) is the distance to go to 
                // place with zero error - linear interpolation

                e1 = -(e1 - e) * e / probesize;
                e2 = -(e2 - e) * e / probesize;
                e3 = -(e3 - e) * e / probesize;
                e4 = -(e4 - e) * e / probesize;

                // next we calc weighted average of 4 vectors
                double vx = (e1 - e3) / 4;
                double vy = (e2 - e4) / 4;

                // [vx,vy] is desired offset to eliminate error
                // in space of img1. it has to get proportional to whole img (normalized)

                vx /= img1.getWidth();
                vy /= img1.getHeight();

                errorVec[i + j * img1.getWidth()][0] = (float) vx;
                errorVec[i + j * img1.getWidth()][1] = (float) vy;

                errorMap.set((short) e, i, j);
                // errorMap.set((short)(i+j), i, j);
            }
        }

        accumulateVectors();
    }

    class VectorCollector implements Triangle.PixelTask
    {

        Node n1;
        Node n2;
        Node n3;
        int x1, x2, x3, y1, y2, y3;

        public void prepare(Node n1, Node n2, Node n3)
        {
            this.n1 = n1;
            this.n2 = n2;
            this.n3 = n3;

            x1 = (int) (n1.x * img1.width);
            x2 = (int) (n2.x * img1.width);
            x3 = (int) (n3.x * img1.width);
            y1 = (int) (n1.y * img1.height);
            y2 = (int) (n2.y * img1.height);
            y3 = (int) (n3.y * img1.height);
        }

        public void run(int x, int y)
        {
            if (x <= 1 || x <= 1 || x >= img1.width - 2 || y >= img1.height - 2)
            {
                return;
            }

            final int adr = x + y * img1.getWidth();
            float v1 = (float) Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));
            float v2 = (float) Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2));
            float v3 = (float) Math.sqrt((x - x3) * (x - x3) + (y - y3) * (y - y3));

            if (v1 < 1)
            {
                v1 = 1;
            }
            if (v2 < 1)
            {
                v2 = 1;
            }
            if (v3 < 1)
            {
                v3 = 1;
            }

            n1.acumulate(v1 * errorVec[adr][0], v1 * errorVec[adr][1]);
            n2.acumulate(v2 * errorVec[adr][0], v2 * errorVec[adr][1]);
            n3.acumulate(v3 * errorVec[adr][0], v3 * errorVec[adr][1]);
        }
    }

    void accumulateVectors()
    {
        System.out.println("accumulation");
        VectorCollector collector = new VectorCollector();

        aproxy.clearAcu();

        for (int j = 0; j < aproxy.getWidth() - 1; j++)
        {
            for (int i = 0; i < aproxy.getWidth() - 1; i++)
            {
                collector.prepare(aproxy.grid[i][j], aproxy.grid[i + 1][j], aproxy.grid[i + 1][j + 1]);
                Triangle.triangleProces(collector.x1, collector.y1,
                        collector.x2, collector.y2,
                        collector.x3, collector.y3,
                        img1.getWidth(), img1.getHeight(),
                        collector);
                collector.prepare(aproxy.grid[i][j], aproxy.grid[i][j + 1], aproxy.grid[i + 1][j + 1]);
                Triangle.triangleProces(collector.x1, collector.y1,
                        collector.x2, collector.y2,
                        collector.x3, collector.y3,
                        img1.getWidth(), img1.getHeight(),
                        collector);

            }
        }

        //  aproxy.smooth(aproxy.getWidth()/2);
        aproxy.finishAcumulation();
        // aproxy.averageAcu();
        // aproxy.scale(0.003f);
    }

    void backupErrVectors()
    {
        for (int j = 0; j < aproxy.getWidth() - 1; j++)
        {
            for (int i = 0; i < aproxy.getWidth() - 1; i++)
            {
                errVecBackup[i][j][0] = aproxy.getNode(i, j).acux;
                errVecBackup[i][j][1] = aproxy.getNode(i, j).acuy;
            }
        }
    }

    public int getProbeSize(int level, int levelNum)
    {
        int probeSize = level * img1.width / 10 / (levelNum + 1);
        if (probeSize == 0)
        {
            probeSize = 1;
        }

        return probeSize;

    }

    public int getGradientContrast(int level)
    {
        return smoothing * level / 10;

    }

    public void calcVectorMap(int probeSize)
    {
        calcErrorVectors(probeSize);
        accumulateVectors();
    }

    public void relaxation()
    {
        int error = -1;
        int counter = 0;

        for (int i = 20; i >= 0; i--)
        {
            iteration();
            if (error != -1)
            {
                if (globalError > error)
                {
                    System.out.println("-- getting worse!!!");
                    break;
                }

                if ((double) globalError / error > 0.99)
                {
                    counter++;
                    if (counter == 4)
                    {
                        System.out.println("-- progress under 0.01");
                        break;
                    }
                } else
                {
                    counter = 0;
                }
            }
            error = globalError;
        }


    }

    /**
     * the greater level, the more blur
     * @param level 
     */
    public void iteration()
    {

        Function vectorSmoothing = new Function(new double[][]
                {
                    {
                        1, 0.01
                    },
                    {
                        2, 0.1
                    },
                    {
                        4, 0.8
                    }
                });

        Function vectorStrength = new Function(new double[][]
                {
                    {
                        1.0, 10
                    }, 
                    {
                        0.8, 2
                    }, 
                    {
                        0.6, 0.1
                    },
                    {
                        0.4, 0.1     
                    },
                    {
                        0.2, 0.05
                    }
                });
        
        Function tensionRate = new Function(new double[][]
            {
                {
                    1.0, 4
                }, 
                {
                    0.4, 0.5
                },
                {
                    0, 0
                }
            });
        
        
              
        

        
        
        double strength = vectorStrength.getY((double)level/levelNum);
        int range = (int) (vectorSmoothing.getY(level) * aproxy.getWidth());
        int tensionCount = (int)(tensionRate.getY((double)level/levelNum)*aproxy.getWidth());


        System.out.println("----- relaxation " + level + "/" + levelNum + ", range = " + range + "/" + aproxy.getWidth()
                + ", strength=" + strength + ", tension=" + tensionCount);

        int probeSize = getProbeSize(level, levelNum);
        System.out.println("probeSize = " + probeSize);
        calcErrorVectors(probeSize);
        if (levelNum == level)
        {
            aproxy.averageAcu();
        } else if (range > 1)
        {
            aproxy.smoothAcu(range);
        }

        aproxy.limitVectors(0.031f);
        backupErrVectors();

        aproxy.applyVectors((float)strength);


        // grid tension
        for (int i = 0; i < tensionCount; i++)
        {
            aproxy.clearAcu();
            aproxy.calcTension(0.4f);
            aproxy.applyVectors((float) level / levelNum);
        }

        System.out.println("E:" + globalError);
        applyTransform();

//        System.out.println("----- relaxation ----- ");
//        calcError(0, 0);
//        int error = globalError;
//        int count = 20;
//        do
//        {
//            System.out.println("E:" + globalError);
//            error = globalError;
//            calcError(0, 0);
//            
//            aproxy.applyVectors(1.0f);
//            
//            applyTransform();
//            
//        }while(--count > 0 && globalError > 0 || Math.abs((double)(error - globalError))/globalError > 0.2);
//        System.out.println("--- final E:" + globalError);
    }

    public void translate(float offx, float offy)
    {
        for (int i = 0; i < aproxy.getWidth(); i++)
        {
            for (int j = 0; j < aproxy.getWidth(); j++)
            {
                aproxy.getNode(i, j).x += offx;
                aproxy.getNode(i, j).y += offy;
            }
        }
    }
    /* 
    // calcs error of images. to errorImg writes normalized map of error
    // obsolete
    public void calcErrorMap(int offx, int offy)
    {
    
    //int adr = 0;
    for (int j=0; j<img1.height; j++)//, adr+=img1.width)
    {
    int y2 = j + offy;
    y2 -= (1<<scaleShift)/2;
    y2 >>= scaleShift;
    if (y2 >= img2.getHeight()) 
    break;
    if (y2 < 0) 
    continue;
    
    for (int i=0; i<img1.width; i++)
    {
    int x2 = i + offx;
    x2 -= (1<<scaleShift)/2;
    x2 >>= scaleShift;
    if (x2 < 0)
    continue;
    if (x2 >= img2.getWidth())
    break;
    
    double a = calcPixelError(i,j,x2,y2);
    
    errorMap.set((short)a, i, j);
    // errorMap.set((short)(i+j), i, j);
    }
    }
    errorLimits = errorMap.contrast();
    errorDisplay = errorMap.createImage(errorDisplay);
    // System.out.println("e:" + error + ", abs e:" + absError);
    } 
    
    
    public void calcErrorVectors2(int probesize, int contrast)
    {
    //clear
    for(int i=0; i<errorMap.px.length; i++)
    errorMap.px[i] = 0;
    
    globalError = 0;
    
    for (int j=0; j<img1.px.length; j++)//, adr+=img1.width)
    {
    int e = (img1.px[j] - img2.px[j])*contrast;
    if (e < 0)
    e = -e;
    if (e > 255)
    e = 255;
    errorMap.px[j] = (short)e;
    }   
    
    Greyscale.edgesFilter(errorMap, errorGrad);
    
    // apply contrast
    for (int j=0; j<errorGrad.px.length; j++)//, adr+=img1.width)
    {
    int v = errorGrad.px[j]<<4;
    if(v > 255)
    v = 255;
    errorGrad.px[j] = (short)v;
    }
    
    int probeOffset = probesize*img1.width;
    
    //int adr = 0;
    for (int j=probesize; j<img1.height-probesize; j++)//, adr+=img1.width)
    {
    int adr = j*img1.width;
    
    for (int i=probesize; i<img1.width-probesize; i++)
    {
    
    int e = errorMap.px[adr+i]             *errorGrad.px[adr+i];
    if (e > 255)
    e = 255;
    
    combinedMap.px[adr+i] = (short)e;
    
    int e1 = errorMap.px[adr+i+probesize]  *errorGrad.px[adr+i+probesize];
    int e2 = errorMap.px[adr+i+probeOffset]*errorGrad.px[adr+i+probeOffset];
    int e3 = errorMap.px[adr+i-probesize]  *errorGrad.px[adr+i-probesize];
    int e4 = errorMap.px[adr+i-probeOffset]*errorGrad.px[adr+i-probeOffset];
    
    globalError += e;
    
    e1 = -(e1 - e)*e/probesize;
    e2 = -(e2 - e)*e/probesize;
    e3 = -(e3 - e)*e/probesize;
    e4 = -(e4 - e)*e/probesize;
    
    // next we calc weighted average of 4 vectors
    double vx = (e1 - e3)/4; 
    double vy = (e2 - e4)/4;
    
    // [vx,vy] is desired offset to eliminate error
    // in space of img1. it has to get proportional to whole img (normalized)
    
    vx /= img1.getWidth();
    vy /= img1.getHeight();
    
    errorVec[i + j*img1.getWidth()][0] = (float)vx;
    errorVec[i + j*img1.getWidth()][1] = (float)vy;
    
    }
    }
    
    System.out.println("global error:" + globalError);
    
    
    }
    
    
    
     */
}
