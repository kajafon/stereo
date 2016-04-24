package stereo_praha.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Karol Presovsky on 12/6/14.
 */
public class MouseTracker extends MouseAdapter {

    int lastX = -1;
    int lastY = -1;

    int mouseX = 0;
    int mouseY = 0;

    public void reset()
    {
        mouseX = 0;
        mouseY = 0;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        mouseX += e.getX();
        mouseY += e.getY();

        mouseX -= lastX;
        mouseY -= lastY;

        lastX = e.getX();
        lastY = e.getY();
    }
}