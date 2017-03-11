package lib.frc1747.motion_profile.generator._2d;

import lib.frc1747.motion_profile.Parameters;

/**
 * Contains several utility methods for converting waypoints (2d) into profiles.
 * @author Tiger
 *
 */
public class SplineGenerator {
	/**
	 * Creates a list of QuinticBeziers from a list of waypoints.
	 * @param waypoints - an array of waypoints to use
	 * @return an array of QuinticBeziers that satisfies the waypoints
	 */
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
	 * Flattens a list of QuinticBeziers into a list of differences of distance and rotation.
	 * @param splines - an array of QuinticBeziers to flatten
	 * @return an array containing differences of distance and rotation<br>
	 * The format is [ds0, dtheta0; ds1, dtheta1; ...]
	 */
	public static double[][] flattenProfile(QuinticBezier[] splines) {
		//Flatten the 2d profile -> 1d profile
		double[][][] profileAccumulator = new double[splines.length][][];
		int profileLength = 0;
		for(int i = 0;i < splines.length;i++) {
			profileAccumulator[i] = splines[i].uniformLengthSegmentData(Parameters.I_SAMPLE_COUNT, Parameters.I_SAMPLE_LENGTH);
			profileLength += profileAccumulator[i].length;
		}
		//ds, vmax
		double[][] profileSetpoints = new double[profileLength][2];
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
