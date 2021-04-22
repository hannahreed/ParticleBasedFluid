package finalproject;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import mintools.parameters.DoubleParameter;
import mintools.swing.VerticalFlowPanel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.swing.JPanel;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

public class FluidParticle {

	public Vector2d v;

	public float pressure;

	public Vector2d forces;

	public float p0;

	public float p;

	public double r = 0.5;

	public double m;

	public Point2d pos;
	
	public Point2d posprev;

	boolean highlight = false;

	boolean tagged = false;
	
	int i, j;

//	public gridCoordinates

	public boolean isBoundaryParticle = false;

	public FluidParticle(double x, double y, double r) {
		this.pos = new Point2d();
		this.posprev = new Point2d();
		this.v = new Vector2d();
		this.forces = new Vector2d();
		this.pos.x = x + r / 2;
		this.pos.y = y + r / 2;
		this.r = r / 8;
		this.v.y = 0;
		this.v.x = 0;
		this.p = 1;
		this.p0 = 100;
		this.i = (int) (pos.x / r);
		this.j = (int) (pos.y / r);
		
	}
	
	public FluidParticle(double x, double y, double r, Vector2d vel) {
		this.pos = new Point2d();
		this.posprev = new Point2d();
		this.v = new Vector2d();
		this.forces = new Vector2d();
		this.pos.x = x + r / 2;
		this.pos.y = y + r / 2;
		this.r = r / 8;
		this.v = vel;
		this.p = 1;
		this.p0 = 100;
		this.i = (int) (pos.x / r);
		this.j = (int) (pos.y / r);
		
	}

	static private final int size = 30;
	static private FloatBuffer vertexBuffer;
	static private ShortBuffer indexBuffer;

	static {
		int numVertFloats = size * 2;
		ByteBuffer vbb = ByteBuffer.allocateDirect(numVertFloats * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		ByteBuffer ibb = ByteBuffer.allocateDirect(size * 2); // size of short is 2
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();
		for (int i = 0; i < size; i++) {
			double theta = 2.0 * i / size * Math.PI;
			vertexBuffer.put((float) Math.cos(theta));
			vertexBuffer.put((float) Math.sin(theta));
			indexBuffer.put((short) i);
		}
		vertexBuffer.position(0);
		indexBuffer.position(0);
	}

	void setBoundaryParticle() {
		this.isBoundaryParticle = true;
	}

	void update(double dt) {
		if (!isBoundaryParticle) {
			v.x += dt * forces.x / p;
			v.y += dt * forces.y / p;

			pos.x += dt * v.x;
			pos.y += dt * v.y;
		}
		
		this.i = (int) (pos.x / r);
		this.j = (int) (pos.x / r);
	}

	boolean inContact(FluidParticle p) {
		return (p.pos.x <= pos.x + r && p.pos.x >= pos.x - r && p.pos.y <= pos.y + r && p.pos.y >= pos.y - r);
	}

	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glPushMatrix();

		if (this.highlight || this.tagged) {
			gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
		} else if (this.isBoundaryParticle) {
			gl.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
		} else {
			gl.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
		}

		gl.glTranslated(this.pos.x, this.pos.y, 0);
		gl.glScaled(r, r, r);
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glVertexPointer(2, GL.GL_FLOAT, 0, vertexBuffer);
		gl.glDrawElements(GL.GL_LINE_LOOP, size, GL.GL_UNSIGNED_SHORT, indexBuffer);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);

		gl.glPopMatrix();

	}
	
//	DoubleParameter mass = new DoubleParameter("mass", 18, 0, 100);
//	
//	public JPanel getControls() {
//		VerticalFlowPanel vfp = new VerticalFlowPanel();
//		vfp.add(mass.getControls());
//		return vfp.getPanel();
//	}

}
