package lib.frc1747.motion_profile.gui._1d;

/**
 * Implements a simple boxcar filter for profile smoothing.
 * 
 * @author Tiger Huang
 *
 */
public class BoxcarFilter {
	/**
	 * Filters all elements of a profile.
	 * 
	 * @param input the profile to smooth
	 * @param length the length of the boxcar filter to use<br>
	 * The actual time length of the profile will be given by length * dt
	 * @return a smoothed profile
	 */
	public static double[][] multiFilter(double[][] input, int length) {
		double[][] output = new double[input.length + length - 1][input[0].length];

		for(int i = 0;i < output.length;i++) {
			for(int j = 0;j < output[0].length;j++) {
				output[i][j] = 0;
				for(int k = i - length + 1;k <= i;k++) {
					int index = k;
					if(index < 0) index = 0;
					if(index > input.length-1) index = input.length-1;
					output[i][j] += input[index][j];
				}
				output[i][j] /= length;
			}
		}
	
		return output;
	}
}
