package stereo_praha.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Karol Presovsky on 12/6/14.
 */
public class MouseTracker extends MouseAdapter {

    int dragStartX = -1;
    int dragStartY = -1;

    public int mouseX = 0;
    public int mouseY = 0;

    public int baseX = 0;
    public int baseY = 0;
    
    public int dx = 0;
    public int dy = 0;

    public void reset()
    {
        mouseX = 0;
        mouseY = 0;
        baseX = 0;
        baseY = 0;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        dragStartX = e.getX();
        dragStartY = e.getY();
        
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        dx = mouseX;
        dy = mouseY;
        mouseX = baseX + e.getX() - dragStartX;
        mouseY = baseY + e.getY() - dragStartY;
        
        dx = mouseX - dx;
        dy = mouseY - dy;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        baseX = mouseX;
        baseY = mouseY;
    }    
}