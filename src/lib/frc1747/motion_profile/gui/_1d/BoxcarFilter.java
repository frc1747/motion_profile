package lib.frc1747.motion_profile.gui._1d;

public class BoxcarFilter {
	public static double[][] multiFilter(double[][] input, int length) {
		double[][] output = new double[input.length + length][input[0].length];

		for(int i = 0;i < output.length;i++) {
			for(int j = 0;j < input[0].length;j++) {
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
