package stereo_praha.gui;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Karol Presovsky on 11/1/14.
 */
public class Object3D extends Something3D {

    public double[][] vertex;
    public int[][] triangles;

    Color color = Color.GRAY;
    
    ArrayList<Object[]> lineColors = new ArrayList<>();
    
    
    public void addLineColors(int[][] indexes, Color c)
    {
        lineColors.add(new Object[]{indexes, c});
    }
    
    public void clearLineColors()
    {
        lineColors.clear();
    }

    public Object3D() {
    }
    
    public Object3D(int vertexNum, int triNum, int faceVertices) 
    {
        init(vertexNum, triNum, faceVertices);
    }
    
    public void init(int vertexNum, int triNum, int faceVertices) {
        if (vertex == null || vertex.length != vertexNum) {
            vertex = new double[vertexNum][3];
            transformed = new double[vertexNum][3];
            projected = new double[vertexNum][2];
        }
        if (triNum > 0 && (triangles == null || triangles.length != triNum)) {
            triangles = new int[triNum][faceVertices];
        }
    }
    
    public Object3D(double[][] vertex, int[][] triangles) {
        this.vertex = vertex;
        this.triangles = triangles;

        transformed = new double[vertex.length][];
        projected = new double[vertex.length][];

        /// init projected
        for (int i=0; i<vertex.length; i++) {
            projected[i] = new double[2];
            transformed[i] = new double[3];
        }

    }
    
    public void add(double[][] vert, int[][] tria) {
        double[][] newVertex = new double[vertex.length + vert.length][3];
        for (int i=0; i<vertex.length; i++)
        {
            newVertex[i][0] = vertex[i][0];
            newVertex[i][1] = vertex[i][1];
            newVertex[i][2] = vertex[i][2];
        }
        for (int i=0; i<vert.length; i++)
        {
            newVertex[vertex.length + i][0] = vert[i][0];
            newVertex[vertex.length + i][1] = vert[i][1];
            newVertex[vertex.length + i][2] = vert[i][2];
        }
        int[][] newTria = new int[triangles.length + tria.length][2];
        for (int i=0; i<triangles.length; i++)
        {
            newTria[i][0] = triangles[i][0];
            newTria[i][1] = triangles[i][1];
        }
        for (int i=0; i<tria.length; i++)
        {
            newTria[triangles.length + i][0] = tria[i][0] + vertex.length;
            newTria[triangles.length + i][1] = tria[i][1] + vertex.length;
        }

        triangles = newTria;
        vertex = newVertex;
        transformed = new double[vertex.length][3];
        projected = new double[vertex.length][2];
        

    }
    
    public void addTransformed(Object3D other) {
        add(other.transformed, other.triangles);

    }
    
    public void addOriginal(Object3D other) {
        add(other.vertex, other.triangles);
    }

    public Color getColor() {
        return color;
    }

    public Object3D setColor(Color color) {
        this.color = color;
        return this;
    }

            
    @Override
    public void project_implemented(double[] tmp_matrix) {
        
        stuff3D.project(vertex, transformed, projected, tmp_matrix);
    }

    @Override
    public void draw(Graphics g, double scale, int shiftx, int shifty) 
    {
        g.setColor(color);
        stuff3D.draw(g, scale, triangles, projected, shiftx, shifty);
        
        for(Object[] e: lineColors) {
            g.setColor((Color)e[1]);
            stuff3D.draw(g, scale, (int[][])e[0], projected, shiftx, shifty);            
        }
          
    }
    
}
    



