package lib.frc1747.motion_profile;

public class Util {
	/**
	 * Basic linear interpolation function
	 */
	public static double linearInterpolate(
			double input,
			double in_min, double in_max,
			double out_min, double out_max) {
		return (input - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
}
