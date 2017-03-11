package lib.frc1747.motion_profile.generator._1d;

import lib.frc1747.motion_profile.Util;

/**
 * Contains several utility methods to time parameterize profiles 
 * @author Tiger
 *
 */
public class ProfileGenerator {
	
	/**
	 * Adjusts the velocities so that the acceleration limits are not violated
	 * @param profilePoints - the profile to adjust velocities<br>
	 * The format is [x0, v0, a0; x1, v1, a1; ...]
	 */
	public static void limitVelocities(double[][] profilePoints) {
		double[] profileVisiteds = new double[profilePoints.length];
		for(int i = 0;i < profilePoints.length;i++) {
			profileVisiteds[i] = 0;
		}
		
		// ----------------------------------------
		// Adjust the speeds of each point so that the acceleration is limited
		// 1. Find the point that is closest to the zero velocity line
		// 2. For each immediate neighbor that is further from the zero velocity line
		// 3. Adjust its velocity if its too high
		// 4. Repeat from 1 until no points are left
		// ----------------------------------------
		for(;;) {
			// Attempt to find an unvisited point that is closest to the zero velocity line
			int index = -1;
			for(int i = 0;i < profilePoints.length;i++) {
				if(	profileVisiteds[i] == 0 &&			//Not visited yet
					(index < 0 ||						//and there is no minimum velocity point yet, or ...
					Math.abs(profilePoints[i][1]) <		//the current point has a lower velocity
					Math.abs(profilePoints[index][1]))) {
					index = i;
				}
			}
			// We have adjusted all points
			if(index < 0) 
				break;
			
			// Adjust the left neighbor
			if(index > 0) {
				double vo = profilePoints[index][1];
				double dx = profilePoints[index-1][0] - profilePoints[index][0];
				double vt = profilePoints[index-1][1];
				double ao = profilePoints[index-1][2];
				double vt2 = -Math.signum(dx) * Math.sqrt(vo * vo + 2 * ao * Math.abs(dx));
				if(Math.abs(vt2) < Math.abs(vt)) {
					profilePoints[index-1][1] = vt2;
				}
			}
			
			// Adjust the right neighbor
			if(index < profilePoints.length-1) {
				double vo = profilePoints[index][1];
				double dx = profilePoints[index+1][0] - profilePoints[index][0];
				double vt = profilePoints[index+1][1];
				double ao = profilePoints[index+1][2];
				double vt2 = Math.signum(dx) * Math.sqrt(vo * vo + 2 * ao * Math.abs(dx));
				if(Math.abs(vt2) < Math.abs(vt)) {
					profilePoints[index+1][1] = vt2;
				}
			}
			
			// Mark this point as visited
			profileVisiteds[index] = 1;
		}
	}
	
	/**
	 * Determines the time for each profile waypoint.
	 * @param profilePoints - the profile waypoints<br>
	 * The format is [x0, v0, a0; x1, v1, a1; ...]
	 * @return the time at each profile waypoint as an array
	 */
	public static double[] timesFromPoints(double[][] profilePoints) {
		double[] profileTimes = new double[profilePoints.length];
		for(int i = 0;i < profilePoints.length;i++) {
			profileTimes[i] = 0;
		}
		
		// Add times to the profile
		for(int i = 1;i < profilePoints.length;i++) {
			double v0 = profilePoints[i-1][1];
			double vt = profilePoints[i][1];
			double x0 = profilePoints[i-1][0];
			double xt = profilePoints[i][0];
			double t = 2 * (xt - x0)/(v0 + vt);
			profileTimes[i] = profileTimes[i-1] + Math.abs(t);
		}
		
		return profileTimes;
	}

	/**
	 * Creates a time parameterized profile from 1D profile waypoints.
	 * @param profilePoints - the profile waypoints<br>
	 * The format is [x0, v0, a0; x1, v1, a1; ...]
	 * @param profileTimes - the time at each waypoint
	 * @param dt - the timestep of the time parameterized profile
	 * @return a time parameterized profile<br>
	 * The format is [x0, v0, a0; x1, v1, a1; ...]
	 */
	public static double[][] profileFromPoints(double[][] profilePoints, double[] profileTimes, double dt) {
		double profileTime = profileTimes[profilePoints.length-1];
		// The format is [x0, v0, a0; x1, v1, a1; ...]
		double[][] timePoints = new double[(int)Math.ceil(profileTime / dt)][3];
		
		// Populate the time parameterized profile
		velocityPointsLoop:
		for(int i = 0, k = 0;i < timePoints.length;i++) {
			double t = i * dt;
			while(profileTimes[k+1] < t) {
				k++;
				// We done generating the profile
				if(k > profilePoints.length-2) {
					break velocityPointsLoop;
				}
			}
			
			// The arc velocity exactly corresponds with a table value
			if(t == profileTimes[k]) {
				timePoints[i][1] = profilePoints[k][1];
			}
			// Interpolate
			else {
				timePoints[i][1] = Util.linearInterpolate(
						t,
						profileTimes[k], profileTimes[k+1],
						profilePoints[k][1], profilePoints[k+1][1]);
			}
		}
		
		// Take the derivative to fill in the accelerations
		for(int i = 1;i < timePoints.length-1;i++) {
			timePoints[i][2] = (timePoints[i+1][1] - timePoints[i-1][1]) / dt / 2;
		}
		
		// Take the integral to fill in the positions
		for(int i = 1;i < timePoints.length;i++) {
			timePoints[i][0] = timePoints[i-1][0] + (timePoints[i-1][1] + timePoints[i][1])/2 * dt;
		}
		
		return timePoints;
	}
	
	/**
	 * Creates a profile that is synchronized with an existing profile
	 * @param timePoints - the time parameterized profile
	 * @param profilePoints - the profile waypoints
	 * @param profilePoints2 - the profile waypoints to synchronize with the other profile waypoints
	 * @param profileTimes - the times of the profile waypoints
	 * @param dt - the timestep the time parameterized profile is
	 * @return the time parameterized second profile
	 */
	public static double[][] synchronizedProfileFromProfile(double[][] timePoints,
			double[][] profilePoints, double[] profilePoints2, double[] profileTimes,
			double dt) {
		// Take the profile points and convert to angular positions
		double[][] angularTimePoints = new double[timePoints.length][3];
		for(int i = 0, k = 0;i < timePoints.length;i++) {
			double t = i * dt;
			double s = timePoints[i][0];
			while(profileTimes[k+1] < t) {
				k++;
				if(k > profilePoints.length-2) {
					k = profilePoints.length-2;
					break;
				}
			}
			
			// The arc length exactly corresponds with a table value
			if(t == profileTimes[k]) {
				angularTimePoints[i][0] = profilePoints2[k];
			}
			// Interpolate
			else {
				angularTimePoints[i][0] = Util.linearInterpolate(
						s,
						profilePoints[k][0], profilePoints[k+1][0],
						profilePoints2[k], profilePoints2[k+1]);
			}
		}

		// Take the derivative to fill in the velocities
		for(int i = 1;i < angularTimePoints.length-1;i++) {
			angularTimePoints[i][1] = (angularTimePoints[i+1][0] - angularTimePoints[i-1][0]) / dt / 2;
		}
		
		// Take the derivative to fill in the accelerations
		for(int i = 1;i < angularTimePoints.length-1;i++) {
			angularTimePoints[i][2] = (angularTimePoints[i+1][1] - angularTimePoints[i-1][1]) / dt / 2;
		}
		
		return angularTimePoints;
	}
}
