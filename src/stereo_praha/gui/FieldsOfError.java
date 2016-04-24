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
    public static final double basicScale = 500;
    double scale = basicScale;
    int stepCount = 50;
    double low = -0.42;
    double high = 0.42;
    double step = (high - low)/stepCount;
    int gridWidth = stepCount + 1;
    double[][] vertexFOE = new double[gridWidth*gridWidth][3];

    double zShift = 2*stepCount;

    private ArrayList<Object3D> objects = new ArrayList<>();

    boolean center = false;

    int [][] lines;

    JPanel panel;

    ProblemInterface temporaryProblem;
    double tmp_ax;
    double tmp_ay;
    double tmp_az;
    double angleY;
    double angleX;
    double errorMax = Double.MIN_VALUE;
    double errorMin = Double.MAX_VALUE;

    public double minAx=0;
    public double minAy=0;

    public double minI=0;
    public double minJ=0;


    public void clear()
    {
        objects.clear();
    }

    public FieldsOfError() {
        setupPanel();
    }

    public JPanel getPanel() {
        return panel;
    }


    interface ValueProducer {

        double calc(ProblemInterface problem, double ax, double ay);

    }

    public void setup_basic_error_graph(ProblemInterface problem, final double tax, final double tay, final double taz) {

        temporaryProblem = problem;
        tmp_ax = tax;
        tmp_ay = tay;
        tmp_az = taz;

        buildGrid(problem, tax, tay, new ValueProducer() {
            @Override
            public double calc(ProblemInterface problem1, double ax, double ay) {
                double[] e = problem1.calcError(ax, ay, taz);
                return e[0];
            }
        });
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
        o.setRotation(angleX, angleY, 0, zShift);
        objects.add(o);
        return o;
    }

    public void normalize(Object3D o) {
        double factor = stepCount / (high - low);

        for (int i=0; i<o.vertex.length; i++) {

            o.vertex[i][2] -= errorMin;
            o.vertex[i][2] /= errorMax;
            o.vertex[i][2] = o.vertex[i][2] * stepCount/5 + zShift;

            if (!center) {
                o.vertex[i][0] -= tmp_ax;
                o.vertex[i][1] -= tmp_ay;
            }

            o.vertex[i][0] *= factor;
            o.vertex[i][1] *= factor;
        }
    }
    public void addMark(double x, double y, Color c) {
        addMark(x, y, c, 0.4);
    }

    public void addMark(double x, double y, Color c, double width) {

        if (!center) {
            x -= tmp_ax;
            y -= tmp_ay;
        }

        double factor = stepCount / (high - low);
        x *= factor;
        y *= factor;

        addMark_asis(x,y,c,width);

    }

    public void addMark_asis(double x, double y, Color c, double width) {
        double[][] vertex = new double[3][];
        int[][] lines = new int[2][];

        int adr = 0;
        int lineIndex = 0;

        vertex[adr] = new double[3];
        vertex[adr][0] = x;
        vertex[adr][1] = y;
        vertex[adr][2] = zShift;

        ++adr;
        vertex[adr] = new double[3];
        vertex[adr][0] = x + width;
        vertex[adr][1] = y;
        vertex[adr][2] = zShift - stepCount/2;

        ++adr;
        vertex[adr] = new double[3];
        vertex[adr][0] = x - width;
        vertex[adr][1] = y;
        vertex[adr][2] = zShift - stepCount/2;

        lines[lineIndex++] = new int[]{0, 1};
        lines[lineIndex++] = new int[]{0, 2};

        createObject(vertex, lines, c);


    }

    public void buildGrid(ProblemInterface problem, double tax, double tay, ValueProducer producer)
    {

        ArrayList<int[]> _lines = new ArrayList<>();

        int i,j=0;

        int adr=0;

        double _e = 100000;

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
                if (!center) {
                    _ax += tax;
                    _ay += tay;
                }

                double e = producer.calc(problem, _ax, _ay);
                if (e > 0.6)
                    e = 0.6;

                vertexFOE[adr][0] = i - (stepCount/2);
                vertexFOE[adr][1] = j - (stepCount/2);
                vertexFOE[adr][2] = e;

                if (e < errorMin && (Math.abs(_ax)+Math.abs(_ay) > 0.001))
                {
                    errorMin = e;
                    minAx = _ax;
                    minAy = _ay;
                    minI = vertexFOE[adr][0];
                    minJ = vertexFOE[adr][1];
                }
                if (e > errorMax) errorMax = e;
                //////

                if (e < _e) {
                    _e = e;
                }
            }
        }

        if (errorMax != 0) {
            for (i = 0; i < adr; i++) {
                vertexFOE[i][2] -= errorMin;
                vertexFOE[i][2] /= errorMax;
                vertexFOE[i][2] = vertexFOE[i][2] * stepCount/5 + zShift;
            }
        }

        lines = new int[_lines.size()][];
        _lines.toArray(lines);

        objects.clear();
        createObject(vertexFOE, lines);

     //   addMark_asis(minI, minJ, Color.BLUE, 0.8);
        System.out.println("-minax = " + minAx + ", minay = " + minAy);
    }

    public void transform()
    {
        for (Object3D o : objects) {
            o.project();
        }
    }

    private void setupPanel()
	{

        panel = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                g.setColor(Color.LIGHT_GRAY);
                g.drawString("Field of error", 12, 12);

                g.setColor(Color.GRAY);
                if (!objects.isEmpty()) {
                    for (int i=0; i< objects.size(); i++) {
                        Object3D o = objects.get(i);
                        o.draw(g, scale, 0, 0);
                    }
                    g.drawString(objects.size() + " objects", 20,20);

                } else {
                    g.drawString("no object", 20,20);
                }
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

                for (Object3D o : objects) {
                    o.setRotation(angleX, angleY, 0, zShift);
                    transform();
                    panel.repaint();
                    last = now;
                }
            }
        };

        panel.addMouseMotionListener(mt);
        panel.addMouseListener(mt);

        JCheckBox centerBx = new JCheckBox(new AbstractAction("center") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object src = e.getSource();
                if (src instanceof JCheckBox) {
                    center = ((JCheckBox)src).isSelected();
                    if (temporaryProblem != null) {
                        setup_basic_error_graph(temporaryProblem, tmp_ax, tmp_ay, tmp_az);
                        panel.repaint();
                    }
                }
            }
        });

        JSlider scaleSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 1);
        scaleSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider sldr = (JSlider)e.getSource();
                scale = basicScale*(((double)sldr.getValue())/100.0 * 3 + 1);
                panel.repaint();
            }
        });

        panel.add(centerBx);
        panel.add(scaleSlider);
	}

    static FieldsOfError prepare() {

        TowardsGlory problem = new TowardsGlory();

        problem.rotateTarget(0.1, 0.2, 0);

        FieldsOfError foe = new FieldsOfError();
        foe.setup_basic_error_graph(problem, 0, 0, 0);

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
