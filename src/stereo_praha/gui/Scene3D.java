package stereo_praha.gui;

import java.awt.Graphics;
import java.util.ArrayList;


/**
 *
 * @author karol presovsky
 */
public class Scene3D extends Something3D {
    
    ArrayList<Something3D> objects = new ArrayList();
    
    public Scene3D() {}
    public Scene3D(String name) {this.name = name;}

    public boolean add(Something3D e) 
    {
        return objects.add(e);
    }
    
    @Override
    public void project_implemented(double[] tmp_matrix)
    {
        for (Something3D obj:objects) 
        {
            obj.project(tmp_matrix);
        }
    }
    
    @Override
    public void draw(Graphics g, double scale, int shiftx, int shifty) 
    {
        for (Something3D obj:objects) 
        {
            obj.draw(g, scale, shiftx, shifty);
        }        
    }
    
    public void clear()
    {
        objects.clear();
    }
    
    public boolean remove(Object3D o)
    {
        return objects.remove(o);
    }
    
}
