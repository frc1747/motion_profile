package lib.frc1747.motion_profile.test;

import lib.frc1747.motion_profile.Parameters;
import lib.frc1747.motion_profile.generator._1d.ProfileGenerator;
import lib.frc1747.motion_profile.generator._2d.SplineGenerator;
import lib.frc1747.motion_profile.gui._1d.BoxcarFilter;

public class PseudoProfileTest1 {
	public static void main(String[] args) {
		double[][] profileSegments = SplineGenerator.flattenPseudoProfile(
				0, 0, .0100001, Math.PI/4,
				Parameters.I_SAMPLE_LENGTH);
		double[][] profilePoints = ProfileGenerator.primaryProfileIntegrate(profileSegments, 0);
		double[] angularProfilePoints = ProfileGenerator.secondaryProfileIntegrate(profileSegments, 1);
		
		ProfileGenerator.skidSteerLimitVelocities(profilePoints, profileSegments,
				Parameters.V_MAX, Parameters.A_MAX, Parameters.W_WIDTH);
		
		// Force the max everything at the endpoints of the profile to zero
		profilePoints[0][1] = 0;
		profilePoints[0][2] = 0;
		profilePoints[profilePoints.length-1][1] = 0;
		profilePoints[profilePoints.length-1][2] = 0;
		
		ProfileGenerator.limitVelocities(profilePoints);
		double[] profileTimes = ProfileGenerator.timesFromPoints(profilePoints);
		double[][] timePoints = ProfileGenerator.profileFromPoints(profilePoints, profileTimes, Parameters.DT);
		double[][] angularTimePoints = ProfileGenerator.synchronizedProfileFromProfile(timePoints,
				profilePoints,
				angularProfilePoints,
				profileTimes,
				Parameters.DT);
		
		// Limit the maximum jerk
		double jerkFilterTime = Parameters.A_MAX/Parameters.J_MAX;
		timePoints = BoxcarFilter.multiFilter(timePoints, (int)Math.ceil(jerkFilterTime/Parameters.DT));
		angularTimePoints = BoxcarFilter.multiFilter(angularTimePoints, (int)Math.ceil(jerkFilterTime/Parameters.DT));
		
		boolean output = true;
		if(output) {
			System.out.println(timePoints.length);
			for(int i = 0;i < timePoints.length;i++) {
				System.out.format("%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\n",
						timePoints[i][0], timePoints[i][1], timePoints[i][2],
						angularTimePoints[i][0], angularTimePoints[i][1], angularTimePoints[i][2]);
			}
		}
	}
}
