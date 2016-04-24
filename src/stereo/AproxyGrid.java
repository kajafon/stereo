
/*
 * 
 * 
 */

package stereo;

/**
 *
 * @author Karol Presovsky
 */
public class AproxyGrid
{

    public Node[][] grid;

    public int getWidth()
    {
        return grid.length;
    }

    public AproxyGrid(int gridWidth)
    {
        int a = gridWidth + 1;
        grid = new Node[a][a];
        for (int i=0;i<a;i++)
        {
            for (int j=0;j<a;j++)
            {
                grid[i][j] = new Node((float)i/(a-1), (float)j/(a-1));
            }
        }
    }

    public Node getNode(int i, int j)
    {
        if (i<0 || j<0 || i>=grid.length || j>=grid.length) return null;
        return grid[i][j];
    }

    /*
     * grid -> img or source img to transformed img
     * [(0,1),(0,1)] -> [(0,1),(0,1)]
     * 
     * n input and output
     */
    public void fnc(Node n)
    {
        float fi = n.x*(grid.length-1);
        float fj = n.y*(grid[0].length-1);

        int i = (int)Math.floor(fi);
        int j = (int)Math.floor(fj);

        if (i < 1|| j < 1 || i >=grid.length-1 || j >= grid.length-1) return;

        float fri = fi - i;
        float frj = fj - j;

        if (fri < frj)
        {
            float vix = grid[i+1][j+1].x - grid[i][j+1].x;
            float viy = grid[i+1][j+1].y - grid[i][j+1].y;

            float vjx = grid[i][j+1].x - grid[i][j].x;
            float vjy = grid[i][j+1].y - grid[i][j].y;

            n.x = grid[i][j].x + vix*fri + vjx*frj;
            n.y = grid[i][j].y + viy*fri + vjy*frj;
        } else
        {
            float vix = grid[i+1][j].x - grid[i][j].x;
            float viy = grid[i+1][j].y - grid[i][j].y;

            float vjx = grid[i+1][j+1].x - grid[i+1][j].x;
            float vjy = grid[i+1][j+1].y - grid[i+1][j].y;

            n.x = grid[i][j].x + vix*fri + vjx*frj;
            n.y = grid[i][j].y + viy*fri + vjy*frj;
        }
    }

    public void invFnc(Node n)
    {
        float dist = Float.MAX_VALUE;
        int mini = -1;
        int minj = -1;

        for (int i=0; i<grid.length; i++)
        {
            for (int j=0; j<grid.length; j++)
            {
                float d = Math.abs(n.x-grid[i][j].x) + Math.abs(n.y-grid[i][j].y);
                if (d < dist)
                {
                    dist = d;
                    mini = i;
                    minj = j;
                }
            }
        }

        n.x = (float)mini/grid.length;
        n.y = (float)minj/grid.length;
    }
    
    
    public void addNoise()
    {
        for (int i=0; i<getWidth(); i++)
        {
            for (int j=0; j<getWidth(); j++)
            {
                getNode(i, j).x += (float)((Math.random()-0.5)/20);
                getNode(i, j).y += (float)((Math.random()-0.5)/20);
            }
        }
    }
    
    public void scale(float w)
    {

        for (int j=0; j<grid.length; j++)
        {
            for (int i=0; i<grid.length; i++)
            {
                grid[i][j].acux*=w;
                grid[i][j].acuy*=w;
            }
        }
    }    
    
    public void clearAcu()
    {
        for (int j=0; j<getWidth(); j++)
        {
            for (int i=0; i<getWidth(); i++)
            {
                grid[i][j].clearAcu();
            }
        }        
    }
    
    public void reset()
    {
        int a = getWidth();
        for (int i=0;i<a;i++)
        {
            for (int j=0;j<a;j++)
            {
                grid[i][j].x = (float)i/a;
                grid[i][j].y = (float)j/a;
                grid[i][j].clearAcu();
            }
        }       
    }
    
    /**
     * after accumulation process where multiple components where cumulated in [acux, acuy] 
     * and the count of components stored in acu
     * we now normalize acumulated vector: [acux,acuy] /= acu
     */
    public void finishAcumulation()
    {
        for (int j=0; j<getWidth(); j++)
        {
            for (int i=0; i<getWidth(); i++)
            {
                if (grid[i][j].acu > 0)
                {
                    grid[i][j].acux /=  grid[i][j].acu;
                    grid[i][j].acuy /=  grid[i][j].acu;
                    grid[i][j].acu = 1;
                }
            }
        }                
        
    }
    
    public void applyVectors(float scale)
    {
        for (int j=0; j<getWidth(); j++)
        {
            for (int i=0; i<getWidth(); i++)
            {
                grid[i][j].addVectors(scale);
            }
        }        
    }
    
    
    public void translate(float offsetx, float offsety)
    {
        for (int j=0; j<getWidth(); j++)
        {
            for (int i=0; i<getWidth(); i++)
            {
                grid[i][j].x += offsetx;
                grid[i][j].y += offsety;
            }
        }        
    }
    
    public void limitVectors(float max)
    {
        for (int j=0; j<getWidth(); j++)
        {
            for (int i=0; i<getWidth(); i++)
            {
                if (grid[i][j].acux > max)
                    grid[i][j].acux = max;
                if (grid[i][j].acux < -max)
                    grid[i][j].acux = -max;
            
                if (grid[i][j].acuy > max)
                    grid[i][j].acuy = max;
                if (grid[i][j].acuy < -max)
                    grid[i][j].acuy = -max;

            }
        }        
    }
    
    //public 
    public void averageAcu()
    {
        float startXSum = 0;
        float startYSum = 0;

        for (int j=0; j<grid.length; j++)
        {
            for (int i=0; i<grid.length; i++)
            {
                startXSum += grid[i][j].acux;
                startYSum += grid[i][j].acuy;
            }
        }
        
        startXSum/=grid.length*grid.length;
        startYSum/=grid.length*grid.length;
        
        for (int j=0; j<grid.length; j++)
        {
            for (int i=0; i<grid.length; i++)
            {
                grid[i][j].acux = startXSum;
                grid[i][j].acuy = startYSum;
            }
        }
    }
    
    public void smoothAcu(int range)
    {
        Node[][] tmp = new Node[grid.length][grid.length];

        // copy grid to tmp
        for (int j=0; j<grid.length; j++)
        {
            for (int i=0; i<grid.length; i++)
            {
                tmp[i][j] = new Node();
                tmp[i][j].acux = grid[i][j].acux;
                tmp[i][j].acuy = grid[i][j].acuy;
            }
        }

        float startXSum = 0;
        float startYSum = 0;
        int startCount = 0;
        int halfRange = range / 2;

        for (int j = 0; j < halfRange; j++)
        {
            for (int i = 0; i < halfRange; i++)
            {
                startXSum += tmp[i][j].acux;
                startYSum += tmp[i][j].acuy;
                startCount++;
            }
        }

        if (startCount == 0)
        {
            System.out.println("error with better smoothing, count == 0");
            return;
        }

        for (int j = 0; j < grid.length; j++)
        {
            float sumX = startXSum;
            float sumY = startYSum;
            int count = startCount;

            for (int i = 0; i < grid.length;)
            {
                if (!grid[i][j].isOut())
                {
                    grid[i][j].acux = sumX / count;
                    grid[i][j].acuy = sumY / count;
                }
                i++;

                // recalc sum in x- direction

                if (i > halfRange)
                {
                    int y1 = j > halfRange ? j - halfRange : 0;
                    int y2 = j + halfRange >= grid.length ? grid.length - 1 : j + halfRange;
                    int x = i - halfRange;

                    while (y1 <= y2)
                    {
                        sumX -= tmp[x][y1].acux;
                        sumY -= tmp[x][y1].acuy;
                        count--;
                        y1++;
                    }
                }
                
                if (i + halfRange < grid.length)
                {
                    int y1 = j > halfRange ? j - halfRange : 0;
                    int y2 = j + halfRange >= grid.length ? grid.length - 1 : j + halfRange;
                    int x = i + halfRange;

                    while (y1 <= y2)
                    {
                        sumX += tmp[x][y1].acux;
                        sumY += tmp[x][y1].acuy;
                        count++;
                        y1++;

                    }
                }
            }
            

            // recalc sum in y-direction

            if (j > halfRange)
            {
                int y = j-halfRange;
                for (int x=0; x<halfRange; x++)
                {
                    startXSum -= tmp[x][y].acux;
                    startYSum -= tmp[x][y].acuy;
                    startCount--;
                }
            }
            if (j + halfRange < grid.length)
            {
                int y = j+halfRange;
                for (int x=0; x<halfRange; x++)
                {
                    startXSum += tmp[x][y].acux;
                    startYSum += tmp[x][y].acuy;
                    startCount++;
                }
            }
        }
    }
    
    
    public void calcTension(float koef)
    {
        // size of cell in grid
        float norm = 1.0f/(getWidth()-1);
        
        // diagonal size of grid cell
        float dnorm = (float)Math.sqrt(2)*norm;
        
        for (int i=0; i<getWidth()-1; i++)
        {
            for (int j=0; j<getWidth()-1; j++)
            {
                Node n = getNode(i, j);

                tension(n, getNode(i+1, j), norm, koef);
                tension(n, getNode(i, j+1), norm, koef);
                tension(n, getNode(i+1, j+1), dnorm, koef);
                
                System.out.print("");
            }
        }
    }

    static void tension(Node n, Node n2, float norm, float koef)
    {
        if (n == null || n2 == null) return;
        
        float vx = n2.x - n.x;
        float vy = n2.y - n.y;
        float d = (float)Math.sqrt(vx*vx + vy*vy);
        if (d != 0)
        {
           vx /= d*2;
           vy /= d*2;
           vx *= (norm - d)*koef;
           vy *= (norm - d)*koef;
        
           boolean nOut = n.isOut();
           boolean n2Out = n2.isOut();
           
           if (nOut == n2Out)
           {
               n.acux -= vx;
               n.acuy -= vy;
               n2.acux += vx;
               n2.acuy += vy;
           } else if (nOut)
           {
               n.acux -= 2*vx;
               n.acuy -= 2*vy;
           } else
           {
              n2.acux += 2*vx;
              n2.acuy += 2*vy;
               
           }
           
        }
    }

    
}
