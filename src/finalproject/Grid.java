package finalproject;

import java.util.ArrayList;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

class Cell {
	double h;
	double N;
	int i, j;

	public Cell(int i, int j, double h, int N) {
		this.i = i;
		this.j = j;
		this.h = h;
		this.N = N;
	}

	void display(GLAutoDrawable drawable) {

		GL2 gl = drawable.getGL().getGL2();

		gl.glColor4f(0, 0, 0, 0.5f);

		gl.glDisable(GL2.GL_LIGHTING);
		gl.glBegin(GL.GL_LINES);

		gl.glVertex2d(0, i * h);
		gl.glVertex2d((N) * h, i * h);
		gl.glVertex2d(i * h, 0);
		gl.glVertex2d(i * h, (N) * h);

		gl.glEnd();

	}

}

public class Grid {

	double h; // dim of cell
	int N;
	Cell[] cells;

	public Grid(double h, int N) {
		this.h = h;
		this.N = N;
	}

	public int IX(int i, int j) {
		return i * (N) + j;
	}

	Cell getCell(int i, int j) {
		return cells[IX(i, j)];
	}

}
