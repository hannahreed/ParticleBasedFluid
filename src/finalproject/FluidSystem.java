package finalproject;

import java.net.NoRouteToHostException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import javax.swing.JPanel;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.swing.VerticalFlowPanel;

public class FluidSystem {

	public double elapsed = 0;

	public int N;

	double h = 1;

	boolean createParticleRequest = false;

	public FluidGrid fluidGrid;

	int gridSize;

	ExternalObject object;
	boolean includedObj = false;
	boolean includedSecondLiquid = false;

	HashMap<FluidParticle, ArrayList<FluidParticle>> neighborhoods;

	void initialize() {
		this.elapsed = 0;
		this.N = Nval.getValue();

		this.fluidGrid = new FluidGrid(N, N / 3, h);
		this.fluidGrid.addClump(3);
		if (twoLiquids.getValue()) {
			this.fluidGrid.addClumpSecondary(1);
		}
		calculateMasses();

		this.neighborhoods = new HashMap<FluidParticle, ArrayList<FluidParticle>>();
		this.object = null;
		this.includedObj = false;
		this.includedSecondLiquid = false;
		

	};

	void calculateMasses() {
		for (FluidParticle particle : fluidGrid.particles) {
			if (particle.secondary)
				particle.m = mass2.getValue();
			else {
				particle.m = mass.getValue();
			}
		}
	}

	void generateParticles() {
		for (int i = 0; i < 1; i++) {
			double x;
			Random rand = new Random();
			double xv = rand.nextDouble() * 0.4 + -0.2;
			x = 0 + (2) * rand.nextDouble();
			FluidParticle p = new FluidParticle(1 * h, h / 2, h, new Vector2d(xv, 0));
			// System.out.println("(" + p.pos.x + " , " + p.pos.y + ")");
			fluidGrid.addParticle(p);
		}
		calculateMasses();

	}

	void advanceTime(double dt) {
		// clear neighbors
		neighborhoods.clear();

		if (includeObject.getValue() && !includedObj) {
			this.object = new ExternalObject((N / 2) * h, (2) * h, 2);
			includedObj = true;
		} else {
//			includedObj = false;
		}

		if (twoLiquids.getValue() && !includedSecondLiquid) {
			this.fluidGrid.addClumpSecondary(N / 2);
			includedSecondLiquid = true;
		} else {
//			includedSecondLiquid = false;
		}
		calculateMasses();

		if (particleGenerator.getValue() && elapsed > interval.getValue()) {
			generateParticles();
//			this.fluidGrid.addClump(1);
			elapsed = 0;
		}

		if (dropObject.getValue()) {
			// update the external object
			object.v.y += dt * gravity.getValue();
			object.pos.y += dt * object.v.y;

			object.pos.y = Math.min(object.pos.y, ((N - 1) * h) - object.r);
		}

		for (FluidParticle p : fluidGrid.particles) {

			// find neighbors
			ArrayList<FluidParticle> neighbors = findNeighbors(p);
			neighborhoods.put(p, neighbors);

			// calculate density
			float density = 0;
			for (FluidParticle n : neighbors) {
				Vector2d r = new Vector2d();
				r.sub(p.pos, n.pos);
				density += n.m * KernelFuncs.poly6(r, h);
			}
			p.p = density;

			// calculate pressure
//			p.pressure = Math.max(k.getFloatValue() * (p.p - p.p0), 0);
			p.pressure = k.getFloatValue() * (p.p - p.p0);

		}

		// forces

		for (FluidParticle p : fluidGrid.particles) {
			ArrayList<FluidParticle> neighbors = neighborhoods.get(p);
			Vector2d fpressure = new Vector2d();
			Vector2d fviscosity = new Vector2d();
			Vector2d fgravity = new Vector2d(0, 1);
			Vector2d fsurface = new Vector2d();
			Vector2d tmpN = new Vector2d();
			double cslp = 0;
			for (FluidParticle n : neighbors) {
				if (n != p) {
					Vector2d r = new Vector2d();
					r.sub(p.pos, n.pos);
					Vector2d tmp = KernelFuncs.spikyGradient(r, h);
					tmp.scale((n.m / n.p) * ((p.pressure + n.pressure) / 2));
					fpressure.add(tmp);

					tmp = new Vector2d();
					tmp.sub(n.v, p.v);
					tmp.scale(n.m / n.p);
					tmp.scale(KernelFuncs.viscosityLaplacian(r, h));
					fviscosity.add(tmp);

					// surface tension
					Vector2d csgrad = KernelFuncs.poly6Gradient(r, h);
					csgrad.scale(n.m * 1 / n.p);
					tmpN.add(csgrad);
					cslp += KernelFuncs.poly6LaPlacian(r, h);
				}
			}
			Vector2d force = new Vector2d();
			if (tmpN.length() > threshold.getValue()) {
				fsurface.set(tmpN);
				fsurface.scale(1 / tmpN.length());
				fsurface.scale(cslp);
				fsurface.scale(-sigma.getValue());
				force.add(fsurface);
			}
			fpressure.scale(-1);
			if (p.secondary)
				fviscosity.scale(mu2.getValue());
			else {
				fviscosity.scale(mu.getValue());
			}
			fgravity.scale(gravity.getValue());
			fgravity.scale(p.m);

			force.add(fpressure);
			force.add(fviscosity);
			force.add(fgravity);

			p.forces.set(force);
		}

		// update

		for (FluidParticle p : fluidGrid.particles) {
			int i = p.i;
			int j = p.j;
			p.v.x += dt * p.forces.x / p.p;
			p.v.y += dt * p.forces.y / p.p;

			p.pos.x += dt * p.v.x;
			p.pos.y += dt * p.v.y;

			// collision detection

			double distance = p.pos.distance(new Point2d(p.pos.x, (N - 1) * h));
			if (distance < p.r * p.r || p.pos.y > (N - 1) * h) {

				Vector2d normal = new Vector2d();
				normal = new Vector2d(0, -1);
//				normal.sub(p.pos, new Point2d(p.pos.x, (N - 1) * h));
//				normal.normalize();

				// reflect
				p.v.x = normal.x * p.v.x;
				p.v.y = normal.y * p.v.y;

				p.pos.x += dt * p.v.x;
				p.pos.y += dt * p.v.y;
			}

			distance = p.pos.distance(new Point2d(0, p.pos.y));
			if (distance < p.r * p.r || p.pos.x < 0) {

				Vector2d normal = new Vector2d();
				normal = new Vector2d(1, 0);
//				normal.sub(p.pos, new Point2d(p.pos.x, (N - 1) * h));
//				normal.normalize();

				// reflect
				p.v.x = normal.x * Math.abs(p.v.x);
				p.v.y = normal.y * Math.abs(p.v.y);

				p.pos.x += dt * p.v.x;
				p.pos.y += dt * p.v.y;
			}

			distance = p.pos.distance(new Point2d(0, p.pos.y));
			if (distance < p.r * p.r || p.pos.y < 0) {

				Vector2d normal = new Vector2d();
				normal = new Vector2d(0, 1);
//				normal.sub(p.pos, new Point2d(p.pos.x, (N - 1) * h));
//				normal.normalize();

				// reflect
				p.v.x = normal.x * Math.abs(p.v.x);
				p.v.y = normal.y * Math.abs(p.v.y);

				p.pos.x += dt * p.v.x;
				p.pos.y += dt * p.v.y;
			}
			distance = p.pos.distance(new Point2d((N - 1) * h, p.pos.y));
			if (distance < p.r * p.r || p.pos.x > (N - 1) * h) {

				Vector2d normal = new Vector2d();
				normal = new Vector2d(-1, 0);
//				normal.sub(p.pos, new Point2d(p.pos.x, (N - 1) * h));
//				normal.normalize();

				// reflect
				p.v.x = normal.x * Math.abs(p.v.x);
				p.v.y = normal.y * Math.abs(p.v.y);

				p.pos.x += dt * p.v.x;
				p.pos.y += dt * p.v.y;
			}

			if (object != null) {
				distance = p.pos.distance(object.pos);
				if (distance < (p.r + object.r)) {

					Vector2d normal = new Vector2d();
					normal.sub(p.pos, object.pos);
//					Point2d contact =  s
					normal.normalize();

					// reflect
					p.v.x = normal.x * Math.abs(p.v.x);
					p.v.y = normal.y * Math.abs(p.v.y + object.v.y);

					p.pos.x += dt * p.v.x;
					p.pos.y += dt * p.v.y;
				}
			}

			// handle out of bounds
			p.pos.y = Math.max(p.pos.y, 0);
			p.pos.y = Math.min(p.pos.y, (N - 1) * h);

			p.pos.x = Math.max(p.pos.x, 0);
			p.pos.x = Math.min(p.pos.x, (N - 1) * h);

			p.i = (int) (p.pos.x / h);
			p.j = (int) (p.pos.y / h);

			if (i != p.i || j != p.j) {
				fluidGrid.getCell(i, j).removeParticle(p);
				fluidGrid.getCell(p.i, p.j).addParticle(p);
			}

		}

		elapsed += dt;

	}

	ArrayList<FluidParticle> findNeighbors(FluidParticle p) {
		ArrayList<FluidParticle> neighbors = new ArrayList<FluidParticle>();

		// current cell
		int i = p.i;
		int j = p.j;
		// add particles in current cell
		for (FluidParticle n : fluidGrid.getParticlesinCell(i, j)) {
			if (p.pos.distance(n.pos) < h) {
				neighbors.add(n);
			}
		}
		// 3 above
		for (FluidParticle n : fluidGrid.getParticlesinCell(i - 1, j - 1)) {
			if (p.pos.distance(n.pos) < h) {
				neighbors.add(n);
			}
		}
		for (FluidParticle n : fluidGrid.getParticlesinCell(i, j - 1)) {
			if (p.pos.distance(n.pos) < h) {
				neighbors.add(n);
			}
		}
		for (FluidParticle n : fluidGrid.getParticlesinCell(i + 1, j - 1)) {
			if (p.pos.distance(n.pos) < h) {
				neighbors.add(n);
			}
		}
		// 3 below
		for (FluidParticle n : fluidGrid.getParticlesinCell(i - 1, j + 1)) {
			if (p.pos.distance(n.pos) < h) {
				neighbors.add(n);
			}
		}
		for (FluidParticle n : fluidGrid.getParticlesinCell(i, j + 1)) {
			if (p.pos.distance(n.pos) < h) {
				neighbors.add(n);
			}
		}
		for (FluidParticle n : fluidGrid.getParticlesinCell(i + 1, j + 1)) {
			if (p.pos.distance(n.pos) < h) {
				neighbors.add(n);
			}
		}
		// 2 side

		for (FluidParticle n : fluidGrid.getParticlesinCell(i - 1, j)) {
			if (p.pos.distance(n.pos) < h) {
				neighbors.add(n);
			}
		}
		for (FluidParticle n : fluidGrid.getParticlesinCell(i + 1, j)) {
			if (p.pos.distance(n.pos) < h) {
				neighbors.add(n);
			}
		}

		return neighbors;
	}

	public void display(GLAutoDrawable drawable) {

		fluidGrid.display(drawable);
		if (this.object != null)
			object.display(drawable);
	}

	private IntParameter Nval = new IntParameter("Dimensions", 10, 4, 256);

	private DoubleParameter gravity = new DoubleParameter("gravity", 9.8, 0, 100);

	private DoubleParameter k = new DoubleParameter("stiffness", 5, 0, 100);

	DoubleParameter interval = new DoubleParameter("delay", 1, 0.01, 10);

	DoubleParameter mu = new DoubleParameter("viscosity coefficient for main liquid", 8.90E-4, 0, 1);

	DoubleParameter mu2 = new DoubleParameter("viscosity coefficient for secondary liquid", 8.90E-4, 0, 1);

	ArrayList<FluidParticle> boundaries = new ArrayList<FluidParticle>();

	BooleanParameter particleGenerator = new BooleanParameter("particle factory", false);

	BooleanParameter dropObject = new BooleanParameter("drop object", false);

	BooleanParameter includeObject = new BooleanParameter("include object", false);

	DoubleParameter sigma = new DoubleParameter("tension coefficient", 5, 0, 100);

	DoubleParameter threshold = new DoubleParameter("tension threshold", 0, 0, 100);

	DoubleParameter mass = new DoubleParameter("mass for main liquid", 18, 0, 100);

	DoubleParameter mass2 = new DoubleParameter("mass for secondary liquid", 18, 0, 100);

	BooleanParameter twoLiquids = new BooleanParameter("two liquid types", false);

	public JPanel getControls() {
		VerticalFlowPanel vfp = new VerticalFlowPanel();
		vfp.add(Nval.getSliderControls());
		vfp.add(gravity.getControls());
		vfp.add(k.getControls());
		vfp.add(dropObject.getControls());
		vfp.add(mu.getControls());
		vfp.add(interval.getControls());
		vfp.add(particleGenerator.getControls());
		vfp.add(sigma.getControls());
		vfp.add(threshold.getControls());
		vfp.add(mass.getControls());
		vfp.add(mu2.getControls());
		vfp.add(twoLiquids.getControls());
		vfp.add(mass2.getControls());
		vfp.add(includeObject.getControls());
		return vfp.getPanel();
	}
}
