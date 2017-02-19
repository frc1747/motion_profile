/**
 * @author Tiger
 */

package lib.frc1747.motion_profile.generator._2d;

public class SplineGenerator {
	public static QuinticBezier[] splinesFromWaypoints(Waypoint[] waypoints) {
		//Generate splines from waypoints
		QuinticBezier[] splines = new QuinticBezier[waypoints.length - 1];
		for(int i = 0;i < splines.length;i++) {
			Waypoint wp1 = waypoints[i];
			Waypoint wp2 = waypoints[i+1];
			splines[i] = new QuinticBezier(
					wp1.reverse ? wp1.getInverse() : wp1,
					wp1.reverse ? wp2.getInverse() : wp2,
					wp1.reverse);
		}
		
		return splines;
	}

	/**
	 * The format is [ds0, dtheta0, vmax0, amax0; ds1, dtheta1, vmax1, amax1; ...]
	 * ds, dtheta, vmax are signed
	 * amax is unsigned
	 */
	public static double[][] flattenProfile(QuinticBezier[] splines) {
		//Flatten the 2d profile -> 1d profile
		double[][][] profileAccumulator = new double[splines.length][][];
		int profileLength = 0;
		for(int i = 0;i < splines.length;i++) {
			profileAccumulator[i] = splines[i].uniformLengthSegmentData(100, .005, 12, 20, 3);
			profileLength += profileAccumulator[i].length;
		}
		//ds, vmax
		double[][] profileSetpoints = new double[profileLength][4];
		//i -> which profile range to use
		//j -> the beginning index of the profile range
		for(int i = 0,j = 0;i < profileAccumulator.length;i++) {
			System.arraycopy(
					profileAccumulator[i], 0,
					profileSetpoints, j,
					profileAccumulator[i].length);
			j += profileAccumulator[i].length;
		}
		return profileSetpoints;
	}
}
