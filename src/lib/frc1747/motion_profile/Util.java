package lib.frc1747.motion_profile;

/**
 * Contains utility methods used several places in the motion profile generator.
 * @author Tiger Huang
 *
 */
public class Util {
	/**
	 * A basic linear interpolation function
	 * 
	 * For example, if input=1, in_min=0, in_max=4, out_min=20, out_max=10,
	 * then output is equal to 17.5
	 * 
	 * @param input the input (usually between in_min and in_max)
	 * @param in_min the input at one point
	 * @param in_max the input at another point
	 * @param out_min what the output will be if the input is at in_min
	 * @param out_max what the output will be if the input is at in_max
	 * @return a number usually between out_min and out_max based on
	 * the relation between input and in_min and min_max
	 */
	public static double linearInterpolate(
			double input,
			double in_min, double in_max,
			double out_min, double out_max) {
		return (input - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
}
