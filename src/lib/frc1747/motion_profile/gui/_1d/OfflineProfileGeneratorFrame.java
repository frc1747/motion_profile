package lib.frc1747.motion_profile.gui._1d;

import javax.swing.JFrame;

public class OfflineProfileGeneratorFrame extends JFrame {
	OfflineProfileGeneratorPanel panel;
	
	public OfflineProfileGeneratorFrame() {
		panel = new OfflineProfileGeneratorPanel();
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Offline Motion Profile Generator");
		setVisible(true);
	}

	public OfflineProfileGeneratorPanel getProfilePanel() {
		return panel;
	}
}
