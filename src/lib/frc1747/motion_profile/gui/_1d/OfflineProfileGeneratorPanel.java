package lib.frc1747.motion_profile.gui._1d;

import javax.swing.JPanel;

public class OfflineProfileGeneratorPanel extends JPanel {

	public OfflineProfileGeneratorPanel() {
		
	}
	
	public void setProfileSetpoints(double[][] profileSetpoints) {
		System.out.println("Profile Begin");
		for(int i = 0;i < profileSetpoints.length;i++) {
			System.out.println(	profileSetpoints[i][0] + "," +
								profileSetpoints[i][1] + "," +
								profileSetpoints[i][2]);
		}
	}
}
