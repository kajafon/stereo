package stereo_praha.gui;

import stereo_praha.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class FieldsOfError {
    
    String name = " ----- ";
    public static final double basicScale = 500;
    double projectionScale = basicScale;
    int stepCount = 50;
    double centerX = 0;
    double centerY = 0;
    double low;
    double high;
    double step;
    double gridStepSize;
    int gridWidth;
    double[][] vertexFOE;

    double zShift;

    Scene3D scene = new Scene3D();
    Object3D errorObject;
    Object3D pointedMark;

    boolean center = false;

    int [][] lines;

    JPanel panel;

    ProblemInterface temporaryProblem;
    double angleY;
    double angleX;
    double errorMax = Double.MIN_VALUE;
    double errorMin = Double.MAX_VALUE;
    double errorScale = 1;

    public double minAx=0;
    public double minAy=0;

    public double minI=0;
    public double minJ=0;
    
    public Runnable buildListener;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
  
    public ProblemInterface getTemporaryProblem() {
        return temporaryProblem;
    }
 
    final public void setLimits(double low, double high, int stepCount) {
       this.low = low;
       this.high = high;
       this.stepCount = stepCount;
       this.step = (high - low)/stepCount;
       this.zShift = 2*stepCount;
       this.gridWidth = stepCount + 1;
       this.vertexFOE = new double[gridWidth*gridWidth][3];
       this.errorScale = stepCount/5;
       scene.setTranslation(0,0, zShift);
    }
    
    ArrayList<MarkListener> markListeners = new ArrayList<>();
    
    public interface MarkListener {
        public void marked(double x, double y);
    }
    
    public void addMarkListener(MarkListener l) {
        markListeners.add(l);        
    }
    
    public void clear()
    {
        scene.clear();
    }

    public FieldsOfError(ProblemInterface problem) {
        temporaryProblem = problem;
        setLimits(-0.42, 0.42, 50); // legacy from early specific use 
        setupPanel();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void recalc(double x, double y){
        centerX = x;
        centerY = y;
        recalc();
    }
    
    public void recalc(){      
        buildGrid(); 
        scene.project();
        if (panel != null) {
            panel.repaint();
        }
    }

    public Object3D createPath(double[][] vertex, Color c) {
        int[][] lines = new int[vertex.length-1][];
        for (int i=1; i<vertex.length; i++) {
            lines[i-1] = new int[]{i-1, i};
        }
        Object3D o = createObject(vertex, lines, c);
        o.setColor(c);
        return o;
    }

    private Object3D createObject(double[][] vertex, int[][] triangles, Color c) {
        Object3D o = createObject(vertex, triangles);
        o.setColor(c);
        return o;
    }

    private Object3D createObject(double[][] vertex, int[][] triangles) {
        Object3D o = new Object3D(vertex, triangles);
        scene.add(o);
        return o;
    }

    double[] normalizeXY(double[] vertex)
    {
        vertex[0] = vertex[0] / this.step;    
        vertex[1] = vertex[1] / this.step;
        return vertex;
    } 
    
    double[] normalizeZ(double[] vertex)
    {
        vertex[2] -= errorMin;
        vertex[2] /= errorMax;
        vertex[2] = vertex[2] * errorScale;
        return vertex;
    }
    
    public void normalize(Object3D o) {
        double factor = stepCount / (high - low);

        for (int i=0; i<o.vertex.length; i++) {
            normalizeZ(o.vertex[i]);
            o.vertex[i][0] *= factor;
            o.vertex[i][1] *= factor;
        }
    }
    
    public void addMark(double x, double y, Color c) {
        addMark(x, y, c, 0.4);
    }

    public void addMark(double x, double y, Color c, double width) {

        addMark(x,y,0,c,width);
    }

    public void addMark(double x, double y, double z, Color c, double width) {
        
        double[] spot = normalizeXY(new double[]{x,y,z});
        normalizeXY(spot);
        x = spot[0];  y = spot[1];  z = spot[2];
        
        double[][] vertex = new double[][]
        {
            {x,y,z},
            {x + width, y, z},
            {x, y + width, z},
            {x, y, z + width},
            
        };
        
        int[][] lines = new int[][] {
            {0,1},{0,2},{0,3}
        };

        createObject(vertex, lines, c);
    }

    private void buildGrid()
    {

        ArrayList<int[]> _lines = new ArrayList<>();

        int i,j=0;

        int adr=0;

        errorMax = Double.MIN_VALUE;
        errorMin = Double.MAX_VALUE;

        for (double ay = low; ay < high; ay += step, j++) {
            i = 0;
            for (double ax = low; ax < high; ax += step, i++) {

                adr = j * gridWidth + i;

                if (j > 0) {
                    _lines.add(new int[]{adr - gridWidth, adr});
                }

                if (i > 0) {
                    _lines.add(new int[]{adr - 1, adr});
                }

                double _ax = ax;
                double _ay = ay;
                double e = temporaryProblem.calcError(_ax - centerX, _ay - centerY, 0)[0];
                
                if (buildListener != null) {
                    System.out.println(".");
                    buildListener.run();
                }

                vertexFOE[adr][0] = i - (stepCount/2);
                vertexFOE[adr][1] = j - (stepCount/2);
                vertexFOE[adr][2] = e;

                if (e < errorMin && (Math.abs(_ax)+Math.abs(_ay) > 0.00001))
                {
                    errorMin = e;
                    minAx = _ax;
                    minAy = _ay;
                    minI = vertexFOE[adr][0];
                    minJ = vertexFOE[adr][1];
                }
                if (e > errorMax) errorMax = e;
                //////
            }
        }

        if (errorMax != 0) {
            for (i = 0; i <= adr; i++) {
                normalizeZ(vertexFOE[i]);                
            }
        }

        lines = new int[_lines.size()][];
        _lines.toArray(lines);

        scene.clear();
        errorObject = createObject(vertexFOE, lines);

        addMark(0,0,0,Color.BLUE, stepCount);
        System.out.println("-minax = " + minAx + ", minay = " + minAy);
    }

    public void project()
    {
        scene.project();
    }
    
    /*
    mark a grid cell that occupies [x,y] on the screen
    */
    public void markPointed(int x, int y)
    {
        double[] point = stuff3D.toProjectionSpace(x, y, panel.getWidth(), panel.getHeight(), projectionScale, 0, 0);
        double[] ray_v = new double[]{point[0], point[1], 1};
        double[] ray_x = new double[]{0,0,-1};
        double[][] poly = new double[4][3];
        
        boolean found = false;
        for(int j = 1; j<stepCount && !found; j++) {            
            for(int i = 1; i<stepCount && !found; i++) {
                int adr = j * gridWidth + i;
                int poly_i = 0;
                try {

                    System.arraycopy(errorObject.transformed[adr], 0, poly[poly_i], 0, 3);
                    poly_i+=1;
                    System.arraycopy(errorObject.transformed[adr-gridWidth], 0, poly[poly_i], 0, 3);
                    poly_i+=1;
                    System.arraycopy(errorObject.transformed[adr-gridWidth-1], 0, poly[poly_i], 0, 3);
                    poly_i+=1;
                    System.arraycopy(errorObject.transformed[adr-1], 0, poly[poly_i], 0, 3);

                    if (Algebra.intersects(poly, ray_x, ray_v)) {
                        errorObject.clearLineColors();
                        errorObject.addLineColors(new int[][]{
                            {adr, adr-gridWidth}, 
                            {adr-gridWidth, adr-gridWidth -1},
                            {adr-gridWidth-1, adr-1},
                            {adr-1, adr}
                        }, Color.yellow);
                        
                        System.out.println("mark:" + i + ", " + j);
                        found = true;
                        
                        for (MarkListener l:markListeners) {
                            l.marked(low + i*step, low + j*step);
                        }
                        
                    }
                } catch(java.lang.ArrayIndexOutOfBoundsException ex){
                    System.out.println("kokot");
                } 
            }
        }
        
        panel.repaint();
    }

    private void setupPanel()
    {

        panel = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                g.setColor(Color.LIGHT_GRAY);
                g.drawString("Field of error '" + name + "'", 12, 12);

                g.setColor(Color.GRAY);
                scene.draw(g, projectionScale, 0, 0);
            }
        };

        MouseTracker mt = new MouseTracker() {
            long last = 0;

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);

                long now = System.currentTimeMillis();
                angleY = mouseX / -100.0;
                angleX = mouseY / 100.0;

                scene.setRotation(angleX, angleY, 0, 0);
                scene.project();
                panel.repaint();
                last = now;
                
            }
           
            public void mouseClicked(MouseEvent e) {
                markPointed(e.getX(), e.getY());
            }
            
        };

        panel.addMouseMotionListener(mt);
        panel.addMouseListener(mt);

        JSlider scaleSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 1);
        scaleSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider sldr = (JSlider)e.getSource();
                projectionScale = basicScale*(((double)sldr.getValue())/100.0 * 3 + 1);
                panel.repaint();
            }
        });

        panel.add(scaleSlider);
    }

    static FieldsOfError prepare() {

        TowardsGlory problem = new TowardsGlory();

        problem.rotateTarget(0.1, 0.2, 0);

        FieldsOfError foe = new FieldsOfError(problem);
        foe.recalc();

        return foe;
    }

    public static void main(String[] args) {

        FieldsOfError foe = prepare();

        JFrame frame = new JFrame("fields of error");

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(foe.getPanel());
        frame.setSize(500, 300);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }
}
