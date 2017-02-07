package lib.frc1747.motion_profile.generator;

import java.util.ArrayList;
import java.util.Vector;

// Partially based off of the article:
// Planning Motion Trajectories for Mobile Robots Using Splines
// By: Christoph Sprunk
public class QuinticBezier {
	private double x0, y0;
	private double x1, y1;
	private double x2, y2;
	private double x3, y3;
	private double x4, y4;
	private double x5, y5;

	//Polynomial coefficients
	private double cxt5;
	private double cxt4;
	private double cxt3;
	private double cxt2;
	private double cxt1;
	private double cxt0;
	
	private double cyt5;
	private double cyt4;
	private double cyt3;
	private double cyt2;
	private double cyt1;
	private double cyt0;
	
	public QuinticBezier(Waypoint w1, Waypoint w2) {
		x0 = w1.x;
		y0 = w1.y;

		x1 = 0.2 * w1.v_m * Math.cos(w1.v_t + Math.PI/2) + x0;
		y1 = 0.2 * w1.v_m * Math.sin(w1.v_t + Math.PI/2) + y0;

		x2 = .05 * w1.a_m * Math.cos(w1.a_t + w1.v_t + Math.PI/2) + 2 * x1 - x0;
		y2 = .05 * w1.a_m * Math.sin(w1.a_t + w1.v_t + Math.PI/2) + 2 * y1 - y0;
		
		x5 = w2.x;
		y5 = w2.y;

		x4 = -0.2 * w2.v_m * Math.cos(w2.v_t + Math.PI/2) + x5;
		y4 = -0.2 * w2.v_m * Math.sin(w2.v_t + Math.PI/2) + y5;

		x3 = .05 * w2.a_m * Math.cos(w2.a_t + w2.v_t + Math.PI/2) + 2 * x4 - x5;
		y3 = .05 * w2.a_m * Math.sin(w2.a_t + w2.v_t + Math.PI/2) + 2 * y4 - y5;
		
		calculateCoefficients();
	}
	
	public double arcLength(double dt) {
		// Uses the trapazoidal approximation
		double length = 0;
		length += Math.hypot(getDX(0), getDY(0)) * dt/2;
		for(double t = dt;t < 1;t += dt) {
			length += Math.hypot(getDX(t), getDY(t)) * dt;
		}
		length += Math.hypot(getDX(1), getDY(1)) * dt/2;
		return length;
	}

	public double[] uniformTimeSample(double dt) {
		double output[] = new double[(int)(1/dt + 1) * 2];
		for(int i = 0;i < output.length/2;i++) {
			double t = i * dt;
			output[i*2] = getPX(t);
			output[i*2 + 1] = getPY(t);
		}
		return output;
	}
	
	//Returns ds, vmax
	// position, segment length, curvature
	public double[][] uniformTimeData(double dt) {
/*
		ArrayList<Double> dss = new ArrayList<>();
		while(true) {
			dss.add(Math.hypot(x, y))
		}
	
		*/
		return new double[1][1];
		//double output[][] = new double[(int)(1/dt) *2][2];
		//return output;

	}
	
	public double[] uniformLengthSample(double st_dt, double ds) {
		// Make the number of segments an integer number
		double s_total = arcLength(st_dt);
		int count = (int)Math.round(s_total / ds);
		ds = s_total / count;

		// Uses the trapazoidal approximation
		double output[] = new double[count * 2 + 2];
		double t = 0;
		output[0] = getPX(t);
		output[1] = getPY(t);
		t += ds/2 / Math.hypot(getDX(t), getDY(t));
		
		for(int i = 1;i < count;i++) {
			t += ds/2 / Math.hypot(getDX(t), getDY(t));
			output[i*2] = getPX(t);
			output[i*2+1] = getPY(t);
			t += ds/2 / Math.hypot(getDX(t), getDY(t));
		}
		t += ds/2 / Math.hypot(getDX(t), getDY(t));
		output[count*2] = getPX(t);
		output[count*2+1] = getPY(t);
		System.out.println(t);
		return output;
	}
	
	public void calculateCoefficients() {
		cxt5 = -x0 + 5*x1 - 10*x2 + 10*x3 - 5*x4 + x5;
		cxt4 = 5*x0 - 20*x1 + 30*x2 - 20*x3 + 5*x4;
		cxt3 = -10*x0 + 30*x1 - 30*x2 + 10*x3;
		cxt2 = 10*x0 - 20*x1 + 10*x2;
		cxt1 = -5*x0 + 5*x1;
		cxt0 = x0;
		
		cyt5 = -y0 + 5*y1 - 10*y2 + 10*y3 - 5*y4 + y5;
		cyt4 = 5*y0 - 20*y1 + 30*y2 - 20*y3 + 5*y4;
		cyt3 = -10*y0 + 30*y1 - 30*y2 + 10*y3;
		cyt2 = 10*y0 - 20*y1 + 10*y2;
		cyt1 = -5*y0 + 5*y1;
		cyt0 = y0;
	}

	// Point
	public double getPX(double t) {
		return
			cxt5 * t * t * t * t * t +
			cxt4 * t * t * t * t +
			cxt3 * t * t * t +
			cxt2 * t * t +
			cxt1 * t +
			cxt0;
	}
	public double getPY(double t) {
		return
			cyt5 * t * t * t * t * t +
			cyt4 * t * t * t * t +
			cyt3 * t * t * t +
			cyt2 * t * t +
			cyt1 * t +
			cyt0;
	}
	
	//1st derivative
	public double getDX(double t) {
		return
			5 * cxt5 * t * t * t * t +
			4 * cxt4 * t * t * t +
			3 * cxt3 * t * t +
			2 * cxt2 * t +
			    cxt1;
	}
	public double getDY(double t) {
		return
			5 * cyt5 * t * t * t * t +
			4 * cyt4 * t * t * t +
			3 * cyt3 * t * t +
			2 * cyt2 * t +
			    cyt1;
	}
	
	//2nd derivative
	public double getDDX(double t) {
		return
			20 * cxt5 * t * t * t +
			12 * cxt4 * t * t +
			3  * cxt3 * t +
			2  * cxt2;
	}
	public double getDDY(double t) {
		return
			20 * cyt5 * t * t * t +
			12 * cyt4 * t * t +
			6  * cyt3 * t +
			2  * cyt2;
	}
	
	// Parametric curvature
	// http://mathworld.wolfram.com/Curvature.html
	public double getCurvature(double t) {
		double dx = getDX(t);
		double dy = getDY(t);
		double ddx = getDDX(t);
		double ddy = getDDY(t);
		
		double d = Math.sqrt(dx * dx + dy * dy);
		return (dx * ddy - dy * ddx) / (d * d * d);
	}
	
}
