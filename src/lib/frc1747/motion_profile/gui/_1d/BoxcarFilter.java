package lib.frc1747.motion_profile.gui._1d;

public class BoxcarFilter {
	public static double[][] multiFilter(double[][] input, int length) {
		double[][] output = new double[input.length + length][input[0].length];

		for(int i = 0;i < input.length;i++) {
			for(int j = 0;j < input[0].length;j++) {
				output[i][j] = 0;
				for(int k = Math.max(0, i - length + 1);k <= i;k++) {
					output[i][j] += input[k][j];
				}
				output[i][j] /= length;
			}
		}

		/*
		// Apply the jerk boxcar filter
		int jerkFilterWidth = (int)Math.ceil(jerkFilterTime/dt);
		for(int i = 0;i < unfilteredVelocities.length;i++) {
			timePoints[i][1] = 0;
			for(int k = Math.max(0, i - jerkFilterWidth + 1);k <= i;k++) {
				timePoints[i][1] += unfilteredVelocities[k];
			}
			timePoints[i][1] /= jerkFilterWidth;
		}
		*/
	
		return output;
	}
}
