package stereo_praha.gui;

import javax.swing.*;

import stereo_praha.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PlaceOfAction {
    TowardsGlory problem = new TowardsGlory();
    double scale = 500;
    double[] angles = new double[3];
    double[] anglesTest = new double[3];

    final FieldsOfError fieldsOfError = new FieldsOfError();
    final GraphPanel graphPanel = new GraphPanel();
    JPanel panel;

    void doMeasurements()
    {
        problem.rotateTarget(angles[0], angles[1], angles[2]);
        panel.repaint();

        fieldsOfError.setup_basic_error_graph(problem, angles[0], angles[1], angles[2]);
        graphPanel.clearGraphs();
        graphPanel.clearMarks();

        double[] graph = problem.rotaryProbe_devel(angles[2]);
        int minIndx = -1;
        double min = 10000;
        for (int i=0; i<graph.length; i++) {
            if (graph[i] < min) {
                min = graph[i];
                minIndx = i;
            }
        }
        if (minIndx >= 0) {
            graphPanel.addMark(minIndx);

            double[] detectedAngles = problem.minimumOnLineCorrected(Math.PI/TowardsGlory.sampleCount*minIndx);
          //  double[] detectedAngles = problem.minimumOnLine(minIndx);
            System.out.println("T angles:" + angles[0] + ", " + angles[1]);
            System.out.println("D angles:" + detectedAngles[0] + ", " + detectedAngles[1]);
        }

        graphPanel.addGraph(graph, "rotary");
        graphPanel.repaint();
        fieldsOfError.transform();
        fieldsOfError.getPanel().repaint();
    }

    void draw(Graphics g) {

        stuff3D.draw(g, (int)scale, problem.triangles, problem.projected_target, 0, 0 );
    }

    void demco()
	{


        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };


        class AngleAction extends AbstractAction {
            AngleAction(String name) {
                super(name);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                doMeasurements();
            }
        };

        final double step = 0.01;
        panel.add(new JButton(new AngleAction("mouseX-") {
            @Override
            public void actionPerformed(ActionEvent e) {
                angles[0] -= step;
                super.actionPerformed(e);
            }
        }));
        panel.add(new JButton(new AngleAction("mouseX+") {
            @Override
            public void actionPerformed(ActionEvent e) {
                angles[0] += step;
                super.actionPerformed(e);

            }
        }));
        panel.add(new JButton(new AngleAction("mouseY-") {
            @Override
            public void actionPerformed(ActionEvent e) {
                angles[1] -= step;
                super.actionPerformed(e);

            }
        }));
        panel.add(new JButton(new AngleAction("mouseY+") {
            @Override
            public void actionPerformed(ActionEvent e) {
                angles[1] += step;
                super.actionPerformed(e);

            }
        }));
        panel.add(new JButton(new AngleAction("z-") {
            @Override
            public void actionPerformed(ActionEvent e) {
                angles[2] -= step;
                super.actionPerformed(e);

            }
        }));
        panel.add(new JButton(new AngleAction("z+") {
            @Override
            public void actionPerformed(ActionEvent e) {
                angles[2] += step;
                super.actionPerformed(e);

            }
        }));

        panel.addMouseMotionListener(new MouseAdapter() {
            long last = 0;
            @Override
            public void mouseDragged(MouseEvent e) {
                long now = System.currentTimeMillis();
                if (now - last > 500) {
                    anglesTest[1] = (e.getX() - panel.getWidth() / 2) / -100.0;
                    anglesTest[0] = (e.getY() - panel.getHeight() / 2) / 100.0;
                    panel.repaint();
                    last = now;
                }
            }
        });

        panel.add(new JButton(new AbstractAction("relax") {
            @Override
            public void actionPerformed(ActionEvent e) {
                relax();
            }
        }));

        final JTextField testField = new JTextField(3);
        testField.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int num = new Integer(testField.getText());
                if (num < problem.vertex.length) {
                    System.out.println("testing " + num);
                    problem.test = num;
                } else {
                    System.out.println("vertex count is " + problem.vertex.length + ", less than " + num);
                }
            }
        });

        panel.add(testField);





        JFrame frame = new JFrame("sam proti vsetkym");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.WEST);
        frame.getContentPane().add(fieldsOfError.getPanel(), BorderLayout.CENTER);
        frame.getContentPane().add(graphPanel, BorderLayout.SOUTH);
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}

    private void relax()
    {
        StereoRelaxer relaxer = new StereoRelaxer(problem, 0.01);

        double[] start = new double[]{1,1,0};

        if (angles[0] < 0)
            start[0] *= -1;
        if (angles[1] < 0)
            start[1] *= -1;

        relaxer.setX(start);
        relaxer.init();

        for (int i=0; i<10; i++) {
            relaxer.relax();
        }


    }





    public static void main(String[] args) {
        new PlaceOfAction().demco();

        //nechapacky();
    }

}
