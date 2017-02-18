/**
 * @author Tiger
 */

package lib.frc1747.motion_profile.generator._2d;

// Partially based off of the article:
// Planning Motion Trajectories for Mobile Robots Using Splines by Christoph Sprunk
public class QuinticBezier {
	//Control points
	private double x0, y0;
	private double x1, y1;
	private double x2, y2;
	private double x3, y3;
	private double x4, y4;
	private double x5, y5;
	private boolean reverse;

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
	
	public QuinticBezier(Waypoint w1, Waypoint w2, boolean reverse) {
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
		
		this.reverse = reverse;
		
		calculateCoefficients();
	}

	/**
	 * Returns the specified number of position samples from this curve.
	 * @param samples - The number of segments to sample
	 * @return An array of samples+1 points<br>
	 * The points are arranged as [x0, y0, x1, y1, ...]
	 */
	public double[] uniformTimePositionSample(int samples) {
		double dt = 1.0/samples;
		double output[] = new double[(samples + 1) * 2];
		for(int i = 0;i <= samples;i++) {
			double t = i * dt;
			output[i*2] = getPX(t);
			output[i*2 + 1] = getPY(t);
		}
		return output;
	}
	
	/**
	 * Returns the total arc length of this curve.
	 * @param samples - The number of segments to sample
	 * @return The total length of the curve
	 */
	public double uniformTimeArcLength(int samples) {
		double dt = 1.0/samples;
		double s = 0;
		// Uses the trapazoidal approximation
		s += Math.hypot(getDX(0), getDY(0)) * dt/2;
		for(int i = 1;i < samples;i++) {
			double t = i * dt;
			s += Math.hypot(getDX(t), getDY(t)) * dt;
		}
		s += Math.hypot(getDX(1), getDY(1)) * dt/2;
		return s;
	}
	
	/**
	 * Returns an array containing corresponding times and arc positions.
	 * @param samples - The number of segments to sample
	 * @return An array associating times and arc positions<br>
	 * The format is [t0, s0; t1, s1; ...]
	 */
	public double[][] uniformTimeArcLengthSample(int samples) {
		double output[][] = new double[samples + 1][2];
		double dt = 1.0/samples;
		double s = 0;
		
		for(int i = 0;i <= samples;i++) {
			//Record current values
			double t = i * dt;
			output[i][0] = t;
			output[i][1] = s;
			
			//Update segment length
			s += Math.hypot(getDX(t), getDY(t)) * dt/2;
			s += Math.hypot(getDX(t+dt), getDY(t+dt)) * dt/2;
		}
		
		return output;
	}

	/**
	 * Flattens the 2D profile into a 1D profile that is parameterized for arc length.
	 * @param timeSampleCount - The number of equal time segments to initially estimate
	 * @param sampleLength - The target arc length of the output segments
	 * @param vmax - The maximum robot velocity
	 * @param avmax - The maximum robot angular velocity
	 * @param width - The width of the robot
	 * @return A flattened profile in the form delta arc length, velocity max, and delta theta.<br>
	 * The format is [ds0, dtheta0, vmax0, amax0; ds1, dtheta1, vmax1, amax1; ...]
	 * ds, dtheta, vmax are signed
	 * amax is unsigned
	 */
	public double[][] uniformLengthSegmentData(
			int timeSampleCount, double sampleLength,
			double vmax, double amax,
			double width) {
		// Adjust the sampleLength in order to make it exactly fit the arc length
		double totalLength = uniformTimeArcLength(timeSampleCount);
		int samples = (int)Math.ceil(totalLength / sampleLength);
		sampleLength = totalLength / samples;
		
		//Create the length->u lookup table
		double[][] lengthTable = uniformTimeArcLengthSample(samples * 5);
		//Create the output array
		double[][] output = new double[samples][4];

		//i -> current segment
		//j -> position in lookup table
		//old_t -> t of the last time
		double old_t = 0;
		for(int i = 0, j = 0;i < samples;i++) {
			double s = i * sampleLength;

			// Ensure that the interpolation is performed on the correct segment
			while(lengthTable[j+1][1] < s) {
				j++;
				if(j > lengthTable.length-2) {
					j = lengthTable.length-2;
					break;
				}
			}
			
			//Get splines time for the arc length from the table
			double t = 0;
			// The arc length exactly corresponds with a table value
			if(s == lengthTable[j][1]) {
				t = lengthTable[j][0];
			}
			// Interpolate
			else {
				t = linearInterpolate(
						s,
						lengthTable[j][1], lengthTable[j+1][1],
						lengthTable[j][0], lengthTable[j+1][0]);
			}
			
			//Write out the results
			output[i][0] = sampleLength * (reverse ? -1 : 1);
			if(i > 0) {
				double dtheta = getHeading(t) - getHeading(old_t);
				if(dtheta < -Math.PI) dtheta += Math.PI * 2;
				if(dtheta >  Math.PI) dtheta -= Math.PI * 2;
				output[i-1][1] = dtheta;
				output[i-1][2] = vmax/(1.0 + (Math.abs(dtheta) * width)/(sampleLength * 2)) * (reverse ? -1 : 1);
				output[i-1][3] = amax/(1.0 + (Math.abs(dtheta) * width)/(sampleLength * 2));
			}
			old_t = t;
		}
		
		double dtheta = getHeading(1) - getHeading(old_t);
		if(dtheta < -Math.PI) dtheta += Math.PI * 2;
		if(dtheta >  Math.PI) dtheta -= Math.PI * 2;
		output[samples-1][1] = dtheta;
		dtheta = Math.abs(dtheta);
		output[samples-1][2] = vmax/(1.0 + (dtheta * width)/(sampleLength * 2)) * (reverse ? -1 : 1);
		output[samples-1][3] = amax/(1.0 + (Math.abs(dtheta) * width)/(sampleLength * 2));
		
		return output;
	}
	
	// Calculate coefficients from control points
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
	
	// Calculates the current heading
	// Normalized from -PI to PI
	public double getHeading(double t) {
		double theta = Math.atan2(getDY(t), getDX(t)) + Math.PI/2;
		if(theta < -Math.PI) theta += Math.PI * 2;
		if(theta >  Math.PI) theta -= Math.PI * 2;
		return theta;
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

	// Basic linear interpolation function
	public double linearInterpolate(
			double input,
			double in_min, double in_max,
			double out_min, double out_max) {
		return (input - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
	
}
