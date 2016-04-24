/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.ui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author karol presovsky
 */
public class GraphPanel extends JPanel
{

    public interface GraphInterface
    {
        public double get(int i);
        public int length();
        public Object getSeries();
    }
    
    public class IntGraph implements GraphInterface
    {

        int [] series;

        public IntGraph(int[] series)
        {
            this.series = series;
        }
        
        
        @Override
        public double get(int i)
        {
            return series[i];
        }

        @Override
        public int length()
        {
            return series.length;
        }

        @Override
        public Object getSeries()
        {
            return series;
        }
    }
    public class ByteGraph implements GraphInterface
    {

        byte [] series;

        public ByteGraph(byte[] series)
        {
            this.series = series;
        }
        
        
        @Override
        public double get(int i)
        {
            return (double)((int)series[i] & 255);
        }

        @Override
        public int length()
        {
            return series.length;
        }

        @Override
        public Object getSeries()
        {
            return series;
        }        
    }
    
    public class DoubleGraph implements GraphInterface
    {

        double [] series;

        public DoubleGraph(double[] series)
        {
            this.series = series;
        }
        
        
        @Override
        public double get(int i)
        {
            return series[i];
        }

        @Override
        public int length()
        {
            return series.length;
        }

        @Override
        public Object getSeries()
        {
            return series;
        }        
    }
    public class IntsInBytes implements GraphInterface
    {
        byte[] data;
        boolean signed;

        @Override
        public Object getSeries()
        {
            return data;
        }
        
        public IntsInBytes(byte[] data, boolean signed)
        {
            this.data = data;
            this.signed = signed;
        }
        
        @Override
        public double get(int i)
        {
            int index = i<<1;
            int v;
            if (signed)
            {
                v = ((int)data[index+1]) << 8;
                v |= data[index];
            }
            else
            {
                v = ((int)data[index+1] & 255) << 8;
                v |= data[index];
                
            }
            return v;
            
        }

        @Override
        public int length()
        {
            return data.length/2;
        }
    }
    
    ArrayList<GraphInterface> graphs = new ArrayList<GraphInterface>();
    ArrayList<String> graphNames = new ArrayList<String>();
    ArrayList<Integer> marks = new ArrayList<Integer>();
    Color[] colors = new Color[]{Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.WHITE, Color.ORANGE};
    
    int startIndex;
    double maxValue = -1000000;
    double minValue = 1000000;
    boolean storeLimits;
    boolean suppressed = false;
    String title;
    Color normalBackgrnd = getBackground();
    
    double minimum = Double.MIN_VALUE;
    double maximum = Double.MAX_VALUE;

    public void setMinMax(double minValue, double maxValue)
    {
        this.minimum = minValue;
        this.maximum = maxValue;
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public void setSuppressed(boolean suppressed)
    {
        this.suppressed = suppressed;
        setBackground(suppressed?normalBackgrnd:Color.WHITE);
    }
    
    public void clearMarks()
    {
        marks.clear();  
    }
    
    public void setStoreLimits(boolean storeLimits)
    {
        this.storeLimits = storeLimits;
    }
    
    public int getStartIndex()
    {
        return startIndex;
    }

    public void setStartIndex(int startIndex)
    {
        this.startIndex = startIndex;
    }

    public GraphPanel()
    {
        setPreferredSize(new Dimension(500,80));
        startIndex = 0;
        
        addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                double w = e.getPoint().x;
                w = w/getWidth()*(maximum - minimum) + minimum;
                System.out.println(" ---- graph value --- > " + w);
                for (GraphInterface g : graphs)
                {
                    w = (double)e.getPoint().x/getWidth()*g.length();
                    System.out.println( "   - " + g.get((int)w));
                }       
            }
            
        });
    }

    public void clearGraphs()
    {
        startIndex = 0;
        graphs.clear();
        graphNames.clear();
        maxValue = -1000000;
        minValue = 1000000;
        
    }
    
    public void addGraph(double[] graph, String name)
    {
        this.graphs.add(new DoubleGraph(graph));
        this.graphNames.add(name);
    }
    
    public void addGraph(byte[] graph, String name)
    {
        this.graphs.add(new ByteGraph(graph));
        this.graphNames.add(name);
    }

    public void addGraph(int[] graph, String name)
    {
        this.graphs.add(new IntGraph(graph));
        this.graphNames.add(name);
    }
    public void addGraph(byte[] graph, boolean signed, String name)
    {
        this.graphs.add(new IntsInBytes(graph, signed));
        this.graphNames.add(name);
    }

    public void addMark(int mark)
    {
        this.marks.add(mark);
    }
    
    void drawGraph(Graphics g, GraphInterface graph, double min, double max )
    {
        double xscale = (double) getWidth() / graph.length();
        double yscale = (double) getHeight() / (max - min);

        synchronized (graph.getSeries())
        {
            if (getWidth() >= graph.length())
            {
                int index = startIndex % graph.length() + 1;

                for (int i = 1; i < graph.length(); i++)
                {
                    int x1 = (int) ((index - 1) * xscale);
                    int x2 = (int) ((index) * xscale);

                    int y1 = getHeight() - (int) (yscale * (graph.get(index - 1) - min));
                    int y2 = getHeight() - (int) (yscale * (graph.get(index) - min));


                    g.drawLine(x1, y1, x2, y2);

                    index++;
                    if (index >= graph.length())
                    {
                        index -= graph.length();
                    }
                }
            } else
            {
                int index = startIndex % graph.length();

                // System.out.println("-" + index);

                for (int i = 0; i < getWidth(); i++)
                {
                    double gymin = 1000000;
                    double gymax = -10000000;
                    int gx1 = (int) (i / xscale) + index;
                    int gx2 = (int) ((i + 1) / xscale) + index;

                    gx1 %= graph.length();
                    gx2 %= graph.length();

                    if (gx2 < gx1)
                    {
                        for (; gx1 < graph.length(); gx1++)
                        {
                            if (gymin > graph.get(gx1))
                            {
                                gymin = graph.get(gx1);
                            }
                            if (gymax < graph.get(gx1))
                            {
                                gymax = graph.get(gx1);
                            }
                        }
                        gx1 = 0;
                    }
                    for (; gx1 < gx2; gx1++)
                    {
                        if (gymin > graph.get(gx1))
                        {
                            gymin = graph.get(gx1);
                        }
                        if (gymax < graph.get(gx1))
                        {
                            gymax = graph.get(gx1);
                        }
                    }

                    gymin = getHeight() - (int) (yscale * (gymin - min));
                    gymax = getHeight() - (int) (yscale * (gymax - min));

                    g.drawLine(i, (int) gymin, i, (int) gymax);

                }
                // g.drawRect(getWidth() - 10, getHeight() / 2, 10, 10);
            }
        }
        
        int i=0;
        for (int m:marks)
        {
            g.setColor(colors[i%colors.length]);
            int x = (int)(m*xscale);
            g.drawLine(x, 0, x, getHeight());
            i++;
        }
    }

    void calcLimits(double[] limits, GraphInterface graph)
    {

        for (int i = 0; i < graph.length(); i++)
        {
            double v = graph.get(i);
            if (v > limits[1])
            {
                limits[1] = v;
            }
            if (v < limits[0])
            {
                limits[0] = v;
            }
        }
        
        if (storeLimits)
        {
            if (maxValue < limits[1])
                maxValue = limits[1];
            if (minValue > limits[0])
                minValue = limits[0];

            if (maxValue > limits[1])
                limits[1] = maxValue;
            if (minValue < limits[0])
                limits[0] = minValue;
        }
    }

    public void paintGraph(Graphics g)
    {

        if (getWidth() == 0 || getHeight() == 0)
        {
            return;
        }
        if (graphs.isEmpty())
        {
            g.drawString("null graph", 10, 10);
        } else 
        {

           double[] limits = new double[]{1000000,-1000000}; // min, max
           
           for (GraphInterface gr:graphs)
              calcLimits(limits, gr);
           
           double diff = limits[1] - limits[0];
           
           double max = limits[1];
           double min = limits[0];
           limits[0] -= diff*0.3;
           limits[1] += diff*0.3;

           int i=0;
           for (GraphInterface gr:graphs)
           {
               Color c = suppressed?Color.GRAY:colors[i%colors.length];
               g.setColor(c);
               g.drawString(graphNames.get(i), 10, i*12 + 20);
               drawGraph(g, gr, limits[0], limits[1]);
               i++;
           }
           
           g.setColor(Color.black);
           
           String s = String.format("%.3f", min);
           g.drawString(s, getWidth()-200, getHeight()-20);
           s = String.format("%.3f", max);
           g.drawString(""+s, getWidth()-200, 20);
           
        }
    }
    
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        paintGraph(g);
        
        if (title != null)
        {
            g.drawString(title, getWidth()/3, 12);
        }

    }
}