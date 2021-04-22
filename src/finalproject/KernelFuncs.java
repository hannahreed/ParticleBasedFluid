package finalproject;

import javax.vecmath.Vector2d;

public class KernelFuncs {
	/*
	 * poly6 and viscosity kernel designed by Muller et al (2003) gradient and
	 * laplacian calculations from Ertekin Unpublished Thesis
	 */

	static double poly6(Vector2d r, double h) {
		double w = 315 / (64 * Math.PI * Math.pow(h, 9));
		if (r.length() >= 0 && r.length() <= h) {
			double h2 = Math.pow(h, 2);
			double r2 = Math.pow(r.length(), 2);
			w *= Math.pow((h2 - r2), 3);
		} else {
			w = 0;
		}
		return w;
	}

	static Vector2d poly6Gradient(Vector2d r, double h) {
		double w = -945 / (32 * Math.PI * Math.pow(h, 9));
		Vector2d W = new Vector2d();
		if (r.length() >= 0 && r.length() <= h) {
			double h2 = Math.pow(h, 2);
			W.set(r);
			W.scale(w);
			W.scale(Math.pow((h2 - Math.pow(r.length(), 2)), 2));
		} else {
			W.x = 0;
			W.y = 0;
		}
		return W;
	}
	
	static double poly6LaPlacian(Vector2d r, double h) {
		double w = -945 / (32 * Math.PI * Math.pow(h, 9));
		if (r.length() >= 0 && r.length() <= h) {
			double h2 = Math.pow(h, 2);
			double v1 = Math.pow((h2 - Math.pow(r.length(), 2)), 2);
			double v2 = (3*Math.pow(h, 3) - 7 * Math.pow(r.length(), 2));
			w*=v1*v2;
		} else {
			w = 0;
		}
		return w;
	}

	static Vector2d spikyGradient(Vector2d r, double h) {

		double w = -45 / (Math.PI * Math.pow(h, 6));
		Vector2d wVec = new Vector2d();

		if (r.length() >= 0 && r.length() <= h) {
			wVec.set(r);
			wVec.normalize();
			wVec.scale(Math.pow((h - r.length()), 2));
			wVec.scale(w);

		} else {
			wVec.x = 0;
			wVec.y = 0;
			w = 0;
		}

		return wVec;
	}

	static double viscosityLaplacian(Vector2d r, double h) {

		double w = 45 / (Math.PI * Math.pow(h, 6));
		Vector2d wVec = new Vector2d();

		if (r.length() >= 0 && r.length() <= h) {
			w *= (h - r.length());
		} else {
			wVec.x = 0;
			wVec.y = 0;
			w = 0;
		}

		return w;
	}

}
