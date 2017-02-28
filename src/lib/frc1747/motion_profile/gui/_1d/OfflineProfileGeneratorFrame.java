package lib.frc1747.motion_profile.gui._1d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

public class OfflineProfileGeneratorFrame extends JFrame implements ActionListener {
	OfflineProfileGeneratorPanel panel;
	JMenuBar bar;
	JMenuItem saveProfile;
	JFileChooser chooser;
	
	public OfflineProfileGeneratorFrame() {
		bar = new JMenuBar();
		setJMenuBar(bar);
		
		chooser = new JFileChooser();

		saveProfile = new JMenuItem("Save Profile");
		saveProfile.addActionListener(this);
		bar.add(saveProfile);
		
		panel = new OfflineProfileGeneratorPanel();
		add(panel);
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Offline Motion Profile Generator");
		setVisible(true);
	}

	public OfflineProfileGeneratorPanel getProfilePanel() {
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == saveProfile) {
			int retVal = chooser.showSaveDialog(this);
			if(retVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				panel.saveProfile(file);
			}
		}
	}
}
