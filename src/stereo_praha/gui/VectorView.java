package stereo_praha.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Karol Presovsky on 10/31/14.
 */
public class VectorView extends JPanel {

    ArrayList<double[]> vectors = new ArrayList<>();

    double xmax = Double.MIN_VALUE;
    double xmin = Double.MAX_VALUE;
    double ymax = Double.MIN_VALUE;
    double ymin = Double.MAX_VALUE;

    public void addVector(double[] v) {
        vectors.add(new double[]{v[0], v[1]});

        if (xmax < v[0]) xmax = v[0];
        if (xmin > v[0]) xmin = v[0];
        if (ymax < v[1]) ymax = v[1];
        if (ymin > v[1]) ymin = v[1];

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        double w = (getWidth()-20)/(xmax - xmin);
        double _w = (getHeight()-20)/(ymax - ymin);

        if (w > _w) w = _w;

        int i = 0;
        for (double[] v : vectors) {
            int x = (int)((v[0] - xmin)*w) + 10;
            int y = (int)((v[1] - ymin)*w) + 10;

            g.setColor(Color.LIGHT_GRAY);
            g.drawString("" + i, x-10, y - 10);

            g.setColor(Color.BLACK);
            g.drawLine(x-5, y-5, x+5, y+5);
            g.drawLine(x+5, y-5, x-5, y+5);

            i ++;
        }

    }
}
