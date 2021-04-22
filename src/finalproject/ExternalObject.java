package finalproject;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

public class ExternalObject {
	Point2d pos;
	double r;
	double m;
	Vector2d v;

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

	public ExternalObject(double x, double y, double r) {
		this.pos = new Point2d();
		this.pos.x = x;
		this.pos.y = y;
		this.r = r / 2;
		this.m = 10;
		this.v = new Vector2d();
	}

	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glPushMatrix();

		gl.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);

		gl.glTranslated(this.pos.x, this.pos.y, 0);
		gl.glScaled(r, r, r);
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glVertexPointer(2, GL.GL_FLOAT, 0, vertexBuffer);
		gl.glDrawElements(GL.GL_LINE_LOOP, size, GL.GL_UNSIGNED_SHORT, indexBuffer);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);

		gl.glPopMatrix();

	}

}
