package lib.frc1747.motion_profile;

/**
 * Reasonable default motion profile parameters used with
 * 1747's 2017 robot.
 * 
 * All units should match those in the profile follower,
 * and parameters such as DT should match as well.
 * 
 * Note that these are for low gear.
 * @author Tiger Huang
 *
 */
public class Parameters {
	/**
	 * Maximum velocity (ft/s)
	 */
	public static final double V_MAX = 14;
	/**
	 * Maximum acceleration (ft/s^2)
	 */
	public static final double A_MAX = 20;
	/**
	 * Maximum jerk (ft/s^3)
	 */
	public static final double J_MAX = 26;
	/**
	 * The wheelbase width (the width between the wheels) (ft)
	 */
	public static final double W_WIDTH = 2.1;
	/**
	 * The robot bumper width (ft)
	 */
	public static final double R_WIDTH = 2.6;
	/**
	 * The robot bumper length (ft)
	 */
	public static final double R_LENGTH = 3.1;
	/**
	 * The timestep to use (s)
	 */
	public static final double DT = 0.01;
	/**
	 * The number of segments to use when estimating the arc length (unitless)
	 */
	public static final int I_SAMPLE_COUNT = 100;
	/**
	 * The length of each segment produced following the initial arc length parameterization (ft)
	 */
	public static final double I_SAMPLE_LENGTH = 0.005;
}
