/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import stereo.poi.CircleRaster;
import stereo.poi.Cntx2;
import stereo.to3d.Face;
import stereo.Greyscale;
import stereo.Matcher;
import stereo.RelationProcessor;
import stereo.Triangulator;
import stereo.Triangulator.Callback;
import stereo.poi.Relation;
import stereo.to3d.FaceMetric;

/**
 *
 * @author karol presovsky
 */
public class Manual extends StereoView
{
    Image img1;
    Image img2;
    
    Greyscale g1;
    Greyscale g2;

    Matcher matcher;
    Relation currentRelation;
    Relation newRelation;
    int dist = 8;
    boolean run = true;
    ArrayList<Integer> scanLine = null;
    GraphPanel graph; 
    
    boolean showRelations = true;
    boolean showNeighbours = true;
    int[] cirValues1;
    int[] cirValues2;
    
    double[] cirGraph1;
    double[] cirGraph2;
    
    
    JComponent view3;
    
    ArrayList<FaceMetric> faceMetrics;
    ArrayList<Face> faces;

    public void setRunn(boolean runn)
    {
        this.run = runn;
    }
    
    
    public Manual(BufferedImage img1, BufferedImage img2)
    {
        this.img1 = img1;
        this.img2 = img2;
        
        g1 = Greyscale.toGreyscale(img1);
        g2 = Greyscale.toGreyscale(img2);
        
        matcher = new Matcher(g1, g2);
        
        cirValues1 = new int[matcher.getCircleLength()];
        cirValues2 = new int[matcher.getCircleLength()];
        cirGraph1 = new double[matcher.getCircleLength()];
        cirGraph2 = new double[matcher.getCircleLength()];
        
        view1 = new JLabel(new ImageIcon(img1));
        view2 = new JLabel(new ImageIcon(img2));
        
        relations = new ArrayList<Relation>();
        
     //   view1.setEnabled(false);
     //   view2.setEnabled(false);
        
      
        
        graph = new GraphPanel();
        graph.addGraph(cirGraph1, "cir1");
        graph.addGraph(cirGraph2, "cir2");
        
        add(view1);
        add(view2);
        
        add(graph);
        
        matcher.setCallback(new Matcher.Callback() 
        {
            public void convolve(double value)
            {
            }

            public void convolveMax(double value)
            {
            }

            public void eyeValue1(int index, int val)
            {
                cirValues1[index] = val;
            }

            public void eyeValue2(int index, int val)
            {
                cirValues2[index] = val;
            }
        });
        
        setFocusable(true);
        requestFocus();
        
        setBorder(new LineBorder(Color.red, 1));
        
        addMouseMotionListener(new MouseAdapter()
        {

            @Override
            public void mouseMoved(MouseEvent e)
            {
              //  System.out.println("move");
                spot = e.getPoint();
                if (newRelation != null)
                {
                    newRelation.c2.x = spot.x - view2.getX();
                    newRelation.c2.y = spot.y - view2.getY();
                }
                highlightRelation();
                repaint();
            }
        });
        
        addKeyListener(new KeyAdapter() 
        {

            @Override
            public void keyPressed(KeyEvent e)
            {
             //   System.out.println("spot:" + spot + "view1:" + view1.contains(spot) + " view2:" + view2.contains(spot));
               // System.out.println("key pressed");
                switch(e.getKeyCode())
                {
                    case KeyEvent.VK_R:
                        onCreateRelation();
                        break;
                    
                    case KeyEvent.VK_D:
                        onDeleteRelation();
                        break;
   
                    case KeyEvent.VK_S:
                        save("relations.txt");
                        break;
                    case KeyEvent.VK_L:
                        read("relations.txt");
                        break;
                    case KeyEvent.VK_T:
                        triangulate();
                        break;
                    case KeyEvent.VK_1:
                        showRelations = !showRelations;
                        repaint();
                        break;
                    case KeyEvent.VK_N:
                        showNeighbours = !showNeighbours;
                        repaint();
                        break;
                        
                }
                repaint();
            }
        });
        
    }
    
    
    double calcCirGraphs()
    {
        int center1 = 0;
        int center2 = 0;
        
        for (int i=0; i<cirValues1.length; i++)
        {
            center1 += cirValues1[i];
            center2 += cirValues2[i];
        }
        
        center1 /= cirValues1.length;
        center2 /= cirValues2.length;
        
        double dev1 = 0;
        double dev2 = 0;
        
        for (int i=0; i<cirValues1.length; i++)
        {
            int v = cirValues1[i] - center1;
            dev1 += v*v;
            v = cirValues2[i] - center2;
            dev2 += v*v;
        }
        
        dev1 = Math.sqrt(dev1);
        dev2 = Math.sqrt(dev2);

        double error = 0;
        
        for (int i=0; i<cirValues1.length; i++)
        {
            cirGraph1[i] = (cirValues1[i] - center1)/dev1;
            cirGraph2[i] = (cirValues2[i] - center2)/dev2;
            double v = cirGraph1[i]-cirGraph2[i];
            error += v*v;
        }
        
        return error/cirGraph1.length;
        
    }
    
    void triangulate()
    {
        final Callback callback = new Callback() 
        {
            public boolean run(ArrayList<Integer> scanList)
            {
                scanLine=scanList;
                repaint();
                try
                {
                   Thread.sleep(30);
                } catch(Exception e)
                {
                    
                }
                return run;
            }
        };
        
        new Thread(new Runnable() 
        {
            public void run()
            {
                faces = RelationProcessor.triangulate(relations, callback);
                faceMetrics = FaceMetric.calcFaceMetrics(faces, relations, new Triangulator.LinkAccess() {

                    public int x1(Object o)
                    {
                        return ((Relation)o).c1.x;
                    }

                    public int x2(Object o)
                    {
                        return ((Relation)o).c2.x;
                    }

                    public int y1(Object o)
                    {
                        return ((Relation)o).c1.y;
                    }

                    public int y2(Object o)
                    {
                        return ((Relation)o).c2.y;
                    }
                });
                
                System.out.println("triangulation stopped");
               // RelationProcessor.calcZet(relations, view1.getWidth()/2, view1.getHeight()/2);
                wavefrontOutput(faces);
                
                repaint();
            }
        }).start();
        System.out.println("triangulation started");
    }
    
    void wavefrontOutput(ArrayList<Face> faces)
    {
        StringBuffer sb = new StringBuffer();
        
        for (Relation r:relations)
        {
            sb.append("v " + r.x3d + " " + r.y3d + " " + r.z3d + " 1.0\n");
        }
        
        for (Face f:faces)
        {
            sb.append("f " + (f.r1+1) + " " + (f.r2+1) + " " + (f.r3+1) + "\n");
        }
        
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream("mesh.obj");
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
            out.write(sb.toString());
            out.flush();
            System.out.println("wavefron file saved");

        } catch(Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
               fos.close();
            } catch(Exception e){e.printStackTrace();}
        }
    }
    
    void onDeleteRelation()
    {
        if (newRelation != null)
        {
            relations.remove(newRelation);
            newRelation = null;
        } else if (currentRelation != null)
        {
            relations.remove(currentRelation);
            currentRelation = null;
        }        
    }
    
    void calcError(Relation r)
    {
        double e1 = matcher.match(r.c1.x, r.c1.y, r.c2.x, r.c2.y);
        
        matcher.compareEyes(r.c1.x, r.c1.y, r.c2.x, r.c2.y);

        
        double e2 = calcCirGraphs();
        r.error = 1.0-(1.0-e1)*(1.0-e2);
        r.errors.add(e1);
        r.errors.add(e2);
        
        graph.repaint();
        System.out.println("error:" + r.error + " e1=" + r.errors.get(0) + " e2=" + r.errors.get(1));
        
    }
            
    void onCreateRelation()
    {
        if (spot != null)
        {
            int x = spot.x - view1.getX();
            int y = spot.y - view2.getY();

            if (view1.contains(x,y))
            {
                if (newRelation != null)
                {
                    relations.remove(newRelation);
                }
                int x1 = spot.x - view1.getX();
                int y1 = spot.y - view1.getY();
                int x2 = spot.x - view2.getX();
                int y2 = spot.y - view2.getY();
                newRelation = new Relation(new Cntx2(x1, y1, 0, null), new Cntx2(x2, y2, 0, null), 0);
                relations.add(newRelation);
            } else
            {
                x = spot.x - view2.getX();
                y = spot.y - view2.getY();

                if (view2.contains(x, y))
                {
                    calcError(newRelation);
                    newRelation = null;
                }
            }
        }
    }
    
    void highlightRelation()
    {
        if (spot == null) return;
        for (Relation r:relations)
        {
            if (Math.abs(r.c1.x + view1.getX() - spot.x) + Math.abs(r.c1.y+view1.getY()-spot.y) < dist)
            {
                if (currentRelation != r)
                {
                    currentRelation = r;
                    if (r.errors.size() >= 2)
                        System.out.println("error:" + r.error + " e1=" + r.errors.get(0) + " e2=" + r.errors.get(1));
                    
                }
                break;
            }
        }
    }
    
    
    void drawNeighbours(Graphics g)
    {
        
      //  g.setColor(Color.darkGray);
        g.setColor(Color.RED);
        for (Relation r:relations)
        {
            for (Cntx2.Neighbour n:r.c1.neighbours)
            {
                 g.drawLine(r.c1.x+view1.getX(), r.c1.y + view1.getY(), n.c.x+view1.getX(), n.c.y + view1.getY());
            }

            for (Cntx2.Neighbour n:r.c2.neighbours)
            {
                 g.drawLine(r.c2.x+view2.getX(), r.c2.y + view2.getY(), n.c.x+view2.getX(), n.c.y + view2.getY());
            }
        }
        
    }     

    void drawScanline(Graphics g)
    {
        if (scanLine == null) return;
        g.setColor(Color.YELLOW);
        for(int i=0; i<scanLine.size()-1; i++)
        {
            Cntx2 c1 = relations.get(scanLine.get(i)).c1;
            Cntx2 c2 = relations.get(scanLine.get(i+1)).c1;
            g.drawLine(c1.x+view1.getX(), c1.y + view1.getY(), c2.x+view1.getX(), c2.y + view1.getY());
        }
    }
    
    void drawFaces(Graphics g)
    {
        if (faces == null || faceMetrics == null) return;
        
        int[] xx = new int[3];
        int[] yy = new int[3];
        
        for (FaceMetric fm:faceMetrics)
        {
            Face f = fm.face;
            Relation r1 = relations.get(f.r1);
            Relation r2 = relations.get(f.r2);
            Relation r3 = relations.get(f.r3);
            
            xx[0] = r1.c1.x + view1.getX();
            xx[1] = r2.c1.x + view1.getX();
            xx[2] = r3.c1.x + view1.getX();
            yy[0] = r1.c1.y + view1.getY();
            yy[1] = r2.c1.y + view1.getY();
            yy[2] = r3.c1.y + view1.getY();
            
           // float c = (float)Math.random();
            
            if (fm.area1 == 0) continue;
            float c = (float)fm.area1/fm.area2;
            if (c > 1)
            {
                c -= 1f;
                c*=4;
                if (c > 1f) c = 1f;
                g.setColor(new Color(c,0,0));
            }
            else
            {
                c = 1f - c;
                c*=4;
                if (c > 1f) c = 1f;
                g.setColor(new Color(0,0,c));
            }
            g.fillPolygon(xx, yy, 3);
        }
    }
    
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);  
        if (showRelations)
        {
            g.setColor(new Color(255,255,255,100));
            paintRelations(g);
        }
        
        Relation r = newRelation;
        if (r == null)
            r = currentRelation;
        
        if (r != null)
        {
            g.setColor(Color.YELLOW);
            g.drawRect(r.c1.x - 2 + view1.getX(), r.c1.y - 2 + view1.getY(), 4, 4);
            g.drawRect(r.c2.x - 2 + view2.getX(), r.c2.y - 2 + view2.getY(), 4, 4);

            if (!showNeighbours)
            {

                if (!r.c1.neighbours.isEmpty())
                {
                    for (Cntx2.Neighbour n:r.c1.neighbours)
                    {
                        Relation nbRel = n.c.relations.get(0);
                        Cntx2 c1 = nbRel.c1;
                        Cntx2 c2 = nbRel.c2;

                        g.setColor(Color.WHITE);
                        g.drawLine(c1.x+view1.getX(), c1.y + view1.getY(), c2.x+view2.getX(), c2.y + view2.getY());


                        c2 = nbRel.c1;

                        g.setColor(Color.red);
                        g.drawLine(r.c1.x+view1.getX(), r.c1.y + view1.getY(), c2.x+view1.getX(), c2.y + view1.getY());
                    }
                }
                else
                {
                    System.out.println("no neighbr");
                }
            }
        }
        
        drawFaces(g);
        
        if (showNeighbours)
            drawNeighbours(g);
        drawScanline(g);
        
    }
    
    void save(String file)
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
            for (Relation r:relations)
            {
                out.write(r.c1.x + " " + r.c1.y + " " + r.c2.x + " " + r.c2.y + "\n");
            }
            out.flush();
            System.out.println("filed saved successfully");
        } catch (Exception ex)
        {
            Logger.getLogger(Manual.class.getName()).log(Level.SEVERE, null, ex);
        } finally
        {
            try
            {
                fos.close();
            } catch (IOException ex)
            {
                Logger.getLogger(Manual.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

       
    }
    
    void read(String file)
    {
        relations.clear();
        newRelation = null;
        currentRelation = null;
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
            InputStreamReader in = new InputStreamReader(fis, "UTF-8");
            BufferedReader reader = new BufferedReader(in);
            while(reader.ready())
            {
                String line = reader.readLine();
                String[] tokens = line.split(" ");
                if (tokens.length < 4)
                {
                    System.out.println("parse warning: can't parse line \"" + line + "\"");
                    continue;
                }
                
                try
                {
                    int x1 = new Integer(tokens[0]);
                    int y1 = new Integer(tokens[1]);
                    int x2 = new Integer(tokens[2]);
                    int y2 = new Integer(tokens[3]);
                    
                    relations.add(Relation.create(new Cntx2(x1,y1,0,null), new Cntx2(x2,y2,0,null)));
                } catch(Exception e)
                {
                    System.out.println("parse warning: 4 integers expected here:\"" + line + "\"");
                }
            }
            System.out.println("read " + relations.size() + " relations");

        } catch (Exception ex)
        {
            Logger.getLogger(Manual.class.getName()).log(Level.SEVERE, null, ex);
        } finally
        {
            try
            {
                fis.close();
            } catch (IOException ex)
            {
                Logger.getLogger(Manual.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
    
    
}
