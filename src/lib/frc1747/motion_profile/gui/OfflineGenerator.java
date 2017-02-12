package lib.frc1747.motion_profile.gui;

import lib.frc1747.motion_profile.gui._1d.OfflineProfileGeneratorFrame;
import lib.frc1747.motion_profile.gui._2d.OfflineSplineGeneratorFrame;

public class OfflineGenerator {
	public static void main(String[] args) {
		OfflineProfileGeneratorFrame profile = new OfflineProfileGeneratorFrame();
		OfflineSplineGeneratorFrame spline = new OfflineSplineGeneratorFrame();
		spline.setProfileFrame(profile);
	}
}