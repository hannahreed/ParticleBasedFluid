package finalproject;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import javax.vecmath.Color3f;
import javax.vecmath.Point2d;

public class Wall {

	public double x, y;

	public double h, w;

	boolean isWall = true;

	public Wall(double x, double y, double h, double w) {
		this.x = x;
		this.y = y;
		this.h = h;
		this.w = w;
	}

	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glBegin(GL.GL_TRIANGLE_STRIP);
		
		gl.glVertex2d(x - w, y - h);
		gl.glVertex2d(x - w, y + h);
		gl.glVertex2d(x + w, y - h);
		gl.glVertex2d(x + w, y + h);
		gl.glEnd();
	}
}
