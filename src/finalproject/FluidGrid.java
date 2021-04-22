package finalproject;

import java.util.ArrayList;
import java.util.Random;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector2f;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

class FluidCell extends Cell {

	int i, j, N;

	double h;

	ArrayList<FluidParticle> particles;

	public FluidCell(int i, int j, double h, int N) {
		super(i, j, h, N);
		particles = new ArrayList<FluidParticle>();
	}

	void clear() {
		particles.clear();
	}

	void removeParticle(FluidParticle p) {
		particles.remove(p);
	}

	void addParticle(FluidParticle p) {
		particles.add(p);
	}

	void addParticles(ArrayList<FluidParticle> ps) {
		particles.addAll(ps);
	}

	void display(GLAutoDrawable drawable) {

		// super.display(drawable); // display grid lines

		GL2 gl = drawable.getGL().getGL2();

//		gl.glBegin(GL.GL_LINES);
//
//		for (FluidParticle p : particles) {
//
//			gl.glColor4f(1, 0, 0, 0.5f);
//			gl.glVertex2d(p.pos.x, p.pos.y);
//			gl.glVertex2d(p.pos.x + p.v.x, p.pos.y + p.v.y);
//		}
//
//		gl.glEnd();

		for (FluidParticle p : particles) {
			p.display(drawable);
		}
	}
}

public class FluidGrid extends Grid {

	int numParticles;
	FluidCell[] cells;
	ArrayList<FluidParticle> particles = new ArrayList<FluidParticle>();

	public FluidGrid(int N, int numParticles, double h) {
		super(h, N);
		this.cells = new FluidCell[N * N];
		this.numParticles = numParticles;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				cells[IX(i, j)] = new FluidCell(i, j, h, N);
			}
		}
	}

	void initialize() {

		int top = (N / 2);
		for (int i = 0; i < N - 1; i++) {
			for (int j = top; j < N - 1; j++) {
				Random rand = new Random();
				double xv = -1 + rand.nextDouble() * 2.0;
				FluidParticle p1 = new FluidParticle(i * h, j * h, h, new Vector2d(xv, 0));
				cells[IX(p1.i, p1.j)].addParticle(p1);
				particles.add(p1);
				FluidParticle p2 = new FluidParticle(i * h - h / 4, j * h, h, new Vector2d(xv, 0));
				cells[IX(p2.i, p2.j)].addParticle(p2);
				particles.add(p2);
				FluidParticle p3 = new FluidParticle(i * h + h / 4, j * h, h, new Vector2d(xv, 0));
				cells[IX(p3.i, p3.j)].addParticle(p3);
				particles.add(p3);
				FluidParticle p4 = new FluidParticle(i * h, j * h + h / 4, h, new Vector2d(xv, 0));
				cells[IX(p4.i, p4.j)].addParticle(p4);
				particles.add(p4);

				FluidParticle p5 = new FluidParticle(i * h - h / 4, j * h + h / 4, h, new Vector2d(xv, 0));
				cells[IX(p5.i, p5.j)].addParticle(p5);
				particles.add(p5);
				FluidParticle p6 = new FluidParticle(i * h + h / 4, j * h + h / 4, h, new Vector2d(xv, 0));
				cells[IX(p6.i, p6.j)].addParticle(p6);
				particles.add(p6);

				FluidParticle p7 = new FluidParticle(i * h - h / 4, j * h - h / 4, h, new Vector2d(xv, 0));
				cells[IX(p7.i, p7.j)].addParticle(p7);
				particles.add(p7);
				FluidParticle p8 = new FluidParticle(i * h + h / 4, j * h - h / 4, h, new Vector2d(xv, 0));
				cells[IX(p8.i, p8.j)].addParticle(p8);
				particles.add(p8);
				FluidParticle p9 = new FluidParticle(i * h, j * h - h / 4, h, new Vector2d(xv, 0));
				cells[IX(p9.i, p9.j)].addParticle(p9);
				particles.add(p9);

			}
		}
	}

	void addClump(int top) {
		for (double i = 1 * h; i < (N - 2) * h; i += h / 4) {
			for (double j = top * h; j < (N - 2) * h; j += h / 4) {
				Random rand = new Random();
				double xv = -1 + rand.nextDouble() * 2.0;
				FluidParticle p = new FluidParticle(i, j, h, new Vector2d(xv, 0));
				cells[IX(p.i, p.j)].addParticle(p);
				particles.add(p);
			}
		}
	}
	
	void addClumpSecondary(int top) {
		for (double i = 1 * h; i < (N - 2) * h; i += h / 4) {
			for (double j = top * h; j < (N - 2) * h; j += h / 4) {
				Random rand = new Random();
				double xv = -1 + rand.nextDouble() * 2.0;
				FluidParticle p = new FluidParticle(i, j, h, new Vector2d(xv, 0));
				p.setSecondary();
				cells[IX(p.i, p.j)].addParticle(p);
				particles.add(p);
			}
		}
	}

	FluidCell getCell(int i, int j) {
		return cells[IX(i, j)];
	}

	ArrayList<FluidParticle> getParticlesinCell(int i, int j) {
		if (i > N - 1 || i < 0 || j > N - 1 || j < 0) {
			return new ArrayList<FluidParticle>();
		} else {
			return getCell(i, j).particles;
		}
	}

	void addParticle(FluidParticle p) {
		int i = (int) (p.pos.x / h);
		int j = (int) (p.pos.y / h);

		cells[IX(i, j)].addParticle(p);
		particles.add(p);
	}

	void updateGrid(FluidParticle p, int originali, int originalj) {
		if (originali < 0 || originali > N || originalj < 0 || originalj > N) {

		} else {
			cells[IX(originali, originalj)].removeParticle(p);

			int i = (int) (p.pos.x / h);
			int j = (int) (p.pos.y / h);

			i = Math.max(i, 0);
			i = Math.min(i, N);

			i = Math.max(j, 0);
			i = Math.min(j, N);

			cells[IX(i, j)].addParticle(p);
		}
	}

	void clear() {
		for (FluidCell c : cells) {
			c.clear();
		}
	}

	void clearCell(int i, int j) {
		cells[IX(i, j)].clear();
	}

	void display(GLAutoDrawable drawable) {

		for (FluidCell c : cells) {
			c.display(drawable);
		}

	}

}
