package finalproject;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.vecmath.Point2f;
import javax.vecmath.Tuple2f;
import javax.vecmath.Vector2f;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.EasyViewer;
import mintools.viewer.Interactor;
import mintools.viewer.SceneGraphNode;

public class App implements SceneGraphNode, Interactor {

	public static void main(String[] args) {
		new App();
	}

	private double scale = 30;
	private double offset = 30;

	private int width;
	private int height;

	int gridSize;

	int N;
	double dx;

	private FluidSystem fluid = new FluidSystem();

	private EasyViewer ev;

	public App() {
		fluid.initialize();
		gridSize = fluid.gridSize;
		N = fluid.N;
		dx = fluid.h;
		ev = new EasyViewer("Particle Fluid", this, new Dimension(512, 512), new Dimension(600, 600));
		ev.addInteractor(this);
	}

	@Override
	public void attach(Component component) {
		component.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_R) {
					reset = true;
				}
				if (e.getKeyCode() == KeyEvent.VK_S) {
					stepRequest = true;
				}
			}

		});

	}

	@Override
	public void init(GLAutoDrawable drawable) {
		setViewingScale(drawable);
		GL gl = drawable.getGL();
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL.GL_LINE_SMOOTH);
		gl.glEnable(GL2.GL_POINT_SMOOTH);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glClearColor(1, 1, 1, 1);

	}

	@Override
	public void display(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		GL2 gl = drawable.getGL().getGL2();

		setViewingScale(drawable);

		if (reset) {
			fluid.initialize();
			gridSize = fluid.gridSize;
			N = fluid.N;
			dx = fluid.h;
			reset = false;
		}

		if (run.getValue()) {
			double dt = stepsize.getValue() / (int) substeps.getValue();
			for (int i = 0; i < substeps.getValue(); i++) {
				fluid.advanceTime(dt);
			}
		}

		if (stepRequest) {
			double dt = stepsize.getValue();
			fluid.advanceTime(dt);
			stepRequest = false;
		}

		EasyViewer.beginOverlay(drawable);

		gl.glPushMatrix();

		gl.glTranslated(offset, offset, 0);
		gl.glScaled(scale, scale, scale);

		fluid.display(drawable);

		gl.glPopMatrix();
		EasyViewer.endOverlay(drawable);
	}

	private DoubleParameter stepsize = new DoubleParameter("step size", 0.01, 1e-5, 1);
	private IntParameter substeps = new IntParameter("sub steps (integer)", 1, 1, 100);

	private BooleanParameter run = new BooleanParameter("animate", false);
	private boolean reset = false;
	private BooleanParameter drawGrid = new BooleanParameter("draw grid", false);
	private BooleanParameter drawBox = new BooleanParameter("draw box", false);
	private boolean stepRequest = false;

	@Override
	public JPanel getControls() {
		VerticalFlowPanel vfp = new VerticalFlowPanel();
		vfp.add(run.getControls());
//		vfp.add(numParticles.getSliderControls());
		vfp.add(stepsize.getControls());
		vfp.add(substeps.getControls());
		vfp.add(drawGrid.getControls());
		vfp.add(drawBox.getControls());
		vfp.add(fluid.getControls());
		return vfp.getPanel();
	}

	private void setViewingScale(GLAutoDrawable drawable) {
		width = drawable.getSurfaceWidth();
		height = drawable.getSurfaceHeight();
		double v = height;
		if (width < height) {
			v = width;
		}
		scale = (v - offset * 2) / (fluid.h * (fluid.N));
	}

}
