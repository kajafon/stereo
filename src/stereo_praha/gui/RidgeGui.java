package stereo_praha.gui;

import stereo_praha.AbstractReliever;
import stereo_praha.ProblemInterface;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Karol Presovsky on 12/6/14.
 */
public class RidgeGui {

    RidgeGraph ridge = new RidgeGraph();

    public AbstractReliever relaxer = new AbstractReliever(new double[2], 0.01) {
        @Override
        public double getTension(double[] x) {
            return ridge.calcError(x[0], x[1], 0)[0];
        }
    };

    public double[][] relax(double[] start, double stepSize, int stepCount, double[] target) {

        double[][] steps = new double[stepCount][];

        relaxer.init(start, stepSize);
        relaxer.setTarget(target);

        for (int i=0; i<stepCount; i++) {
            relaxer.relax();
            double[] x = relaxer.getX();
            steps[i] = new double[]{x[0], x[1], relaxer.getVal()};
        }

        return steps;
    }

    public static void main(String[] args) {

        RidgeGui gui = new RidgeGui();

        FieldsOfError foe = new FieldsOfError();
        foe.setup_basic_error_graph(gui.ridge, 0,0,0);

        double[][] steps = gui.relax(new double[]{0.1, 1}, 0.1, 100, new double[]{0,0});

        foe.normalize(foe.createPath(steps, Color.BLACK));

        JFrame frame = new JFrame("welcome back my friends...");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(foe.getPanel(), BorderLayout.CENTER);
        frame.setSize(1100, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
