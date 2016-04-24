/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo;

/**
 *
 * @author karol presovsky
 */
public class Triangle
{

    public static void perspectiveTriangle(int x1, int y1, double z1, int x2, int y2,
            double z2, int x3, int y3, double z3, int u1, int v1, int u2,
            int v2, int u3, int v3, int[] texture, int sidelen, int[] pixels,
            int width, int height)
    {

        double startx, endx, startu, endu, startv, endv, startz, endz;
        double slope21, slope31, slope32;
        double u1i, v1i, z1r, u2i, v2i, z2r, u3i, v3i, z3r;
        double du21, dv21, dz21, du31, dv31, dz31, du32, dv32, dz32, dx, dy, du, dv, dz, u, v, z;
        double slopeleft, sloperight, duleft, duright, dvleft, dvright, dzleft, dzright, tempz;
        int temp, off, start, end, uu, vv;

        if (y2 < y1)
        {
            temp = x2;
            x2 = x1;
            x1 = temp;
            temp = y2;
            y2 = y1;
            y1 = temp;
            temp = u2;
            u2 = u1;
            u1 = temp;
            temp = v2;
            v2 = v1;
            v1 = temp;
            tempz = z2;
            z2 = z1;
            z1 = tempz;
        }
        if (y3 < y1)
        {
            temp = x3;
            x3 = x1;
            x1 = temp;
            temp = y3;
            y3 = y1;
            y1 = temp;
            temp = u3;
            u3 = u1;
            u1 = temp;
            temp = v3;
            v3 = v1;
            v1 = temp;
            tempz = z3;
            z3 = z1;
            z1 = tempz;
        }
        if (y3 < y2)
        {
            temp = x3;
            x3 = x2;
            x2 = temp;
            temp = y3;
            y3 = y2;
            y2 = temp;
            temp = u3;
            u3 = u2;
            u2 = temp;
            temp = v3;
            v3 = v2;
            v2 = temp;
            tempz = z3;
            z3 = z2;
            z2 = tempz;
        }

        if (y1 == y3)
        {
            return;
        }

        u1i = u1 / z1;
        u2i = u2 / z2;
        u3i = u3 / z3;
        v1i = v1 / z1;
        v2i = v2 / z2;
        v3i = v3 / z3;
        z1r = 1.0d / z1;
        z2r = 1.0d / z2;
        z3r = 1.0d / z3;

        dy = 1.0d / (y2 - y1);
        slope21 = (double) (x2 - x1) * dy;
        du21 = (double) (u2i - u1i) * dy;
        dv21 = (double) (v2i - v1i) * dy;
        dz21 = (double) (z2r - z1r) * dy;

        dy = 1.0d / (y3 - y1);
        slope31 = (double) (x3 - x1) * dy;
        du31 = (double) (u3i - u1i) * dy;
        dv31 = (double) (v3i - v1i) * dy;
        dz31 = (double) (z3r - z1r) * dy;

        dy = 1.0d / (y3 - y2);
        slope32 = (double) (x3 - x2) * dy;
        du32 = (double) (u3i - u2i) * dy;
        dv32 = (double) (v3i - v2i) * dy;
        dz32 = (double) (z3r - z2r) * dy;

        startx = endx = x1;
        startu = endu = u1i;
        startv = endv = v1i;
        startz = endz = z1r;

        if (y1 != y2)
        {
            if (slope21 > slope31)
            {
                slopeleft = slope31;
                sloperight = slope21;

                duleft = du31;
                duright = du21;

                dvleft = dv31;
                dvright = dv21;

                dzleft = dz31;
                dzright = dz21;
            } else
            {
                slopeleft = slope21;
                sloperight = slope31;

                duleft = du21;
                duright = du31;

                dvleft = dv21;
                dvright = dv31;

                dzleft = dz21;
                dzright = dz31;
            }

            for (int y = y1; y != y2; y++)
            {
                if (y > 0 && y < height)
                {
                    dx = endx - startx;
                    if (dx != 0)
                    {
                        du = (endu - startu) / dx;
                        dv = (endv - startv) / dx;
                        dz = (endz - startz) / dx;
                    } else
                    {
                        du = endu - startu;
                        dv = endv - startv;
                        dz = endz - startz;
                    }
                    u = startu;
                    v = startv;
                    z = startz;

                    off = y * width;
                    start = off + (int) startx;
                    end = off + (int) endx;
                    if (start < off)
                    {
                        dx = -startx;
                        u += dx * du;
                        v += dx * dv;
                        z += dx * dz;
                        start = off;
                    }
                    if (end > off + width - 1)
                    {
                        end = off + width - 1;
                    }

                    while (start < end)
                    {
                        uu = ((int) (u / z)) & (sidelen - 1);
                        vv = ((int) (v / z)) & (sidelen - 1);
                        pixels[start++] = texture[vv * sidelen + uu];
                        u += du;
                        v += dv;
                        z += dz;
                    }
                }

                startx += slopeleft;
                endx += sloperight;

                startu += duleft;
                endu += duright;

                startv += dvleft;
                endv += dvright;

                startz += dzleft;
                endz += dzright;
            }
        } else
        {
            if (x1 > x2)
            {
                startx = x2;
                endx = x1;

                startu = u2i;
                endu = u1i;

                startv = v2i;
                endv = v1i;

                startz = z2r;
                endz = z1r;
            } else
            {
                startx = x1;
                endx = x2;

                startu = u1i;
                endu = u2i;

                startv = v1i;
                endv = v2i;

                startz = z1r;
                endz = z2r;
            }
        }

        if (y2 != y3)
        {
            if (slope32 > slope31)
            {
                slopeleft = slope32;
                sloperight = slope31;

                duleft = du32;
                duright = du31;

                dvleft = dv32;
                dvright = dv31;

                dzleft = dz32;
                dzright = dz31;
            } else
            {
                slopeleft = slope31;
                sloperight = slope32;

                duleft = du31;
                duright = du32;

                dvleft = dv31;
                dvright = dv32;

                dzleft = dz31;
                dzright = dz32;
            }

            for (int y = y2; y != y3; y++)
            {
                if (y > 0 && y < height)
                {
                    dx = endx - startx;
                    if (dx != 0)
                    {
                        du = (endu - startu) / dx;
                        dv = (endv - startv) / dx;
                        dz = (endz - startz) / dx;
                    } else
                    {
                        du = endu - startu;
                        dv = endv - startv;
                        dz = endz - startz;
                    }
                    u = startu;
                    v = startv;
                    z = startz;

                    off = y * width;
                    start = off + (int) startx;
                    end = off + (int) endx;
                    if (start < off)
                    {
                        dx = -startx;
                        u += dx * du;
                        v += dx * dv;
                        z += dx * dz;
                        start = off;
                    }
                    if (end > off + width - 1)
                    {
                        end = off + width - 1;
                    }

                    while (start < end)
                    {
                        uu = ((int) (u / z)) & (sidelen - 1);
                        vv = ((int) (v / z)) & (sidelen - 1);
                        pixels[start++] = texture[vv * sidelen + uu];
                        u += du;
                        v += dv;
                        z += dz;
                    }
                }

                startx += slopeleft;
                endx += sloperight;

                startu += duleft;
                endu += duright;

                startv += dvleft;
                endv += dvright;

                startz += dzleft;
                endz += dzright;
            }
        }
    }
    
    public static void triangleShort(int x1, int y1, int x2, int y2,
            int x3, int y3, int u1, int v1, int u2,
            int v2, int u3, int v3, short[] texture, int sidelen, short[] pixels,
            int width, int height)
    {

        double startx, endx, startu, endu, startv, endv;
        double slope21, slope31, slope32;
     //   double u1i, v1i, u2i, v2i, u3i, v3i;
        double du21, dv21, du31, dv31, du32, dv32, dx, dy, du, dv, u, v;
        double slopeleft, sloperight, duleft, duright, dvleft, dvright;
        int temp, off, start, end, uu, vv;

        if (y2 < y1)
        {
            temp = x2;
            x2 = x1;
            x1 = temp;
            temp = y2;
            y2 = y1;
            y1 = temp;
            temp = u2;
            u2 = u1;
            u1 = temp;
            temp = v2;
            v2 = v1;
            v1 = temp;
        }
        if (y3 < y1)
        {
            temp = x3;
            x3 = x1;
            x1 = temp;
            temp = y3;
            y3 = y1;
            y1 = temp;
            temp = u3;
            u3 = u1;
            u1 = temp;
            temp = v3;
            v3 = v1;
            v1 = temp;
        }
        if (y3 < y2)
        {
            temp = x3;
            x3 = x2;
            x2 = temp;
            temp = y3;
            y3 = y2;
            y2 = temp;
            temp = u3;
            u3 = u2;
            u2 = temp;
            temp = v3;
            v3 = v2;
            v2 = temp;
        }

        if (y1 == y3)
        {
            return;
        }



        dy = 1.0d / (y2 - y1);
        slope21 = (double) (x2 - x1) * dy;
        du21 = (double) (u2 - u1) * dy;
        dv21 = (double) (v2 - v1) * dy;

        dy = 1.0d / (y3 - y1);
        slope31 = (double) (x3 - x1) * dy;
        du31 = (double) (u3 - u1) * dy;
        dv31 = (double) (v3 - v1) * dy;

        dy = 1.0d / (y3 - y2);
        slope32 = (double) (x3 - x2) * dy;
        du32 = (double) (u3 - u2) * dy;
        dv32 = (double) (v3 - v2) * dy;

        startx = endx = x1;
        startu = endu = u1;
        startv = endv = v1;

        if (y1 != y2)
        {
            if (slope21 > slope31)
            {
                slopeleft = slope31;
                sloperight = slope21;

                duleft = du31;
                duright = du21;

                dvleft = dv31;
                dvright = dv21;
            } else
            {
                slopeleft = slope21;
                sloperight = slope31;

                duleft = du21;
                duright = du31;

                dvleft = dv21;
                dvright = dv31;
            }

            for (int y = y1; y != y2; y++)
            {
                if (y > 0 && y < height)
                {
                    dx = endx - startx;
                    if (dx != 0)
                    {
                        du = (endu - startu) / dx;
                        dv = (endv - startv) / dx;
                    } else
                    {
                        du = endu - startu;
                        dv = endv - startv;
                    }
                    u = startu;
                    v = startv;

                    off = y * width;
                    start = off + (int) startx;
                    end = off + (int) endx;
                    if (start < off)
                    {
                        dx = -startx;
                        u += dx * du;
                        v += dx * dv;
                        start = off;
                    }
                    if (end > off + width - 1)
                    {
                        end = off + width - 1;
                    }

                    while (start < end)
                    {
                        pixels[start++] = texture[((int)v) * sidelen + (int)u];
                        u += du;
                        v += dv;
                    }
                }

                startx += slopeleft;
                endx += sloperight;

                startu += duleft;
                endu += duright;

                startv += dvleft;
                endv += dvright;
            }
        } else
        {
            if (x1 > x2)
            {
                startx = x2;
                endx = x1;

                startu = u2;
                endu = u1;

                startv = v2;
                endv = v1;

            } else
            {
                startx = x1;
                endx = x2;

                startu = u1;
                endu = u2;

                startv = v1;
                endv = v2;
            }
        }

        if (y2 != y3)
        {
            if (slope32 > slope31)
            {
                slopeleft = slope32;
                sloperight = slope31;

                duleft = du32;
                duright = du31;

                dvleft = dv32;
                dvright = dv31;
            } else
            {
                slopeleft = slope31;
                sloperight = slope32;

                duleft = du31;
                duright = du32;

                dvleft = dv31;
                dvright = dv32;
            }

            for (int y = y2; y != y3; y++)
            {
                if (y > 0 && y < height)
                {
                    dx = endx - startx;
                    if (dx != 0)
                    {
                        du = (endu - startu) / dx;
                        dv = (endv - startv) / dx;
                    } else
                    {
                        du = endu - startu;
                        dv = endv - startv;
                    }
                    u = startu;
                    v = startv;

                    off = y * width;
                    start = off + (int) startx;
                    end = off + (int) endx;
                    if (start < off)
                    {
                        dx = -startx;
                        u += dx * du;
                        v += dx * dv;
                        start = off;
                    }
                    if (end > off + width - 1)
                    {
                        end = off + width - 1;
                    }

                    while (start < end)
                    {
                        pixels[start++] = texture[(int)(v) * sidelen + (int)u];
                        u += du;
                        v += dv;
                    }
                }

                startx += slopeleft;
                endx += sloperight;

                startu += duleft;
                endu += duright;

                startv += dvleft;
                endv += dvright;
            }
        }
    }
    
    public static void triangle(int x1, int y1, int x2, int y2,
            int x3, int y3, int u1, int v1, int u2,
            int v2, int u3, int v3, int[] texture, int sidelen, int[] pixels,
            int width, int height)
    {

        double startx, endx, startu, endu, startv, endv;
        double slope21, slope31, slope32;
     //   double u1i, v1i, u2i, v2i, u3i, v3i;
        double du21, dv21, du31, dv31, du32, dv32, dx, dy, du, dv, u, v;
        double slopeleft, sloperight, duleft, duright, dvleft, dvright;
        int temp, off, start, end, uu, vv;

        if (y2 < y1)
        {
            temp = x2;
            x2 = x1;
            x1 = temp;
            temp = y2;
            y2 = y1;
            y1 = temp;
            temp = u2;
            u2 = u1;
            u1 = temp;
            temp = v2;
            v2 = v1;
            v1 = temp;
        }
        if (y3 < y1)
        {
            temp = x3;
            x3 = x1;
            x1 = temp;
            temp = y3;
            y3 = y1;
            y1 = temp;
            temp = u3;
            u3 = u1;
            u1 = temp;
            temp = v3;
            v3 = v1;
            v1 = temp;
        }
        if (y3 < y2)
        {
            temp = x3;
            x3 = x2;
            x2 = temp;
            temp = y3;
            y3 = y2;
            y2 = temp;
            temp = u3;
            u3 = u2;
            u2 = temp;
            temp = v3;
            v3 = v2;
            v2 = temp;
        }

        if (y1 == y3)
        {
            return;
        }



        dy = 1.0d / (y2 - y1);
        slope21 = (double) (x2 - x1) * dy;
        du21 = (double) (u2 - u1) * dy;
        dv21 = (double) (v2 - v1) * dy;

        dy = 1.0d / (y3 - y1);
        slope31 = (double) (x3 - x1) * dy;
        du31 = (double) (u3 - u1) * dy;
        dv31 = (double) (v3 - v1) * dy;

        dy = 1.0d / (y3 - y2);
        slope32 = (double) (x3 - x2) * dy;
        du32 = (double) (u3 - u2) * dy;
        dv32 = (double) (v3 - v2) * dy;

        startx = endx = x1;
        startu = endu = u1;
        startv = endv = v1;

        if (y1 != y2)
        {
            if (slope21 > slope31)
            {
                slopeleft = slope31;
                sloperight = slope21;

                duleft = du31;
                duright = du21;

                dvleft = dv31;
                dvright = dv21;
            } else
            {
                slopeleft = slope21;
                sloperight = slope31;

                duleft = du21;
                duright = du31;

                dvleft = dv21;
                dvright = dv31;
            }

            for (int y = y1; y != y2; y++)
            {
                if (y > 0 && y < height)
                {
                    dx = endx - startx;
                    if (dx != 0)
                    {
                        du = (endu - startu) / dx;
                        dv = (endv - startv) / dx;
                    } else
                    {
                        du = endu - startu;
                        dv = endv - startv;
                    }
                    u = startu;
                    v = startv;

                    off = y * width;
                    start = off + (int) startx;
                    end = off + (int) endx;
                    if (start < off)
                    {
                        dx = -startx;
                        u += dx * du;
                        v += dx * dv;
                        start = off;
                    }
                    if (end > off + width - 1)
                    {
                        end = off + width - 1;
                    }

                    while (start < end)
                    {
                        pixels[start++] = texture[((int)v) * sidelen + (int)u];
                        u += du;
                        v += dv;
                    }
                }

                startx += slopeleft;
                endx += sloperight;

                startu += duleft;
                endu += duright;

                startv += dvleft;
                endv += dvright;
            }
        } else
        {
            if (x1 > x2)
            {
                startx = x2;
                endx = x1;

                startu = u2;
                endu = u1;

                startv = v2;
                endv = v1;

            } else
            {
                startx = x1;
                endx = x2;

                startu = u1;
                endu = u2;

                startv = v1;
                endv = v2;
            }
        }

        if (y2 != y3)
        {
            if (slope32 > slope31)
            {
                slopeleft = slope32;
                sloperight = slope31;

                duleft = du32;
                duright = du31;

                dvleft = dv32;
                dvright = dv31;
            } else
            {
                slopeleft = slope31;
                sloperight = slope32;

                duleft = du31;
                duright = du32;

                dvleft = dv31;
                dvright = dv32;
            }

            for (int y = y2; y != y3; y++)
            {
                if (y > 0 && y < height)
                {
                    dx = endx - startx;
                    if (dx != 0)
                    {
                        du = (endu - startu) / dx;
                        dv = (endv - startv) / dx;
                    } else
                    {
                        du = endu - startu;
                        dv = endv - startv;
                    }
                    u = startu;
                    v = startv;

                    off = y * width;
                    start = off + (int) startx;
                    end = off + (int) endx;
                    if (start < off)
                    {
                        dx = -startx;
                        u += dx * du;
                        v += dx * dv;
                        start = off;
                    }
                    if (end > off + width - 1)
                    {
                        end = off + width - 1;
                    }

                    while (start < end)
                    {
                        pixels[start++] = texture[(int)(v) * sidelen + (int)u];
                        u += du;
                        v += dv;
                    }
                }

                startx += slopeleft;
                endx += sloperight;

                startu += duleft;
                endu += duright;

                startv += dvleft;
                endv += dvright;
            }
        }
    }
    
    public interface PixelTask
    {
        void run(int x, int y);
    }
    
    public static void triangleProces(int x1, int y1, int x2, int y2,
            int x3, int y3, int width, int height, PixelTask task)
    {

        double startx, endx;
        double slope21, slope31, slope32;
        double dy;
        double slopeleft, sloperight;
        int temp;

        if (y2 < y1)
        {
            temp = x2;
            x2 = x1;
            x1 = temp;
            temp = y2;
            y2 = y1;
            y1 = temp;
        }
        if (y3 < y1)
        {
            temp = x3;
            x3 = x1;
            x1 = temp;
            temp = y3;
            y3 = y1;
            y1 = temp;
        }
        if (y3 < y2)
        {
            temp = x3;
            x3 = x2;
            x2 = temp;
            temp = y3;
            y3 = y2;
            y2 = temp;
        }

        if (y1 == y3)
        {
            return;
        }

        dy = 1.0d / (y2 - y1);
        slope21 = (double) (x2 - x1) * dy;

        dy = 1.0d / (y3 - y1);
        slope31 = (double) (x3 - x1) * dy;

        dy = 1.0d / (y3 - y2);
        slope32 = (double) (x3 - x2) * dy;

        startx = endx = x1;

        if (y1 != y2)
        {
            if (slope21 > slope31)
            {
                slopeleft = slope31;
                sloperight = slope21;
            } else
            {
                slopeleft = slope21;
                sloperight = slope31;
            }

            for (int y = y1; y != y2; y++)
            {
                if (y > 0)
                {
                    int scanX = (int)startx;
                    if (scanX < 0)
                        scanX = 0;
                    int stopX = (int)endx;
                    if (stopX > width)
                        stopX = width;
                    for (; scanX < stopX; scanX++)
                    {
                        task.run(scanX, y);
                    }
                }

                startx += slopeleft;
                endx += sloperight;
                if (startx > width)
                    break;
                if (endx < 0)
                    break;
            }
        } else
        {
            if (x1 > x2)
            {
                startx = x2;
                endx = x1;
            } else
            {
                startx = x1;
                endx = x2;
            }
        }

        if (y2 != y3)
        {
            if (slope32 > slope31)
            {
                slopeleft = slope32;
                sloperight = slope31;
            } else
            {
                slopeleft = slope31;
                sloperight = slope32;
            }

            for (int y = y2; y != y3 & y < height; y++)
            {
                if (y > 0)
                {
                    int scanX = (int)startx;
                    if (scanX < 0)
                        scanX = 0;
                    int stopX = (int)endx;
                    if (stopX > width)
                        stopX = width;
                    for (; scanX < stopX; scanX++)
                    {
                        task.run(scanX, y);
                    }
                }

                startx += slopeleft;
                endx += sloperight;
                if (startx > width)
                    break;
                if (endx < 0)
                    break;

            }
        }
    }    
}
