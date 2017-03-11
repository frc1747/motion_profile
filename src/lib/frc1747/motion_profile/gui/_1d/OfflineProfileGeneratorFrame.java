package lib.frc1747.motion_profile.gui._1d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

public class OfflineProfileGeneratorFrame extends JFrame implements ActionListener {
	OfflineProfileGeneratorPanel panel;
	JMenuBar bar;
	JMenuItem saveProfile;
	JCheckBoxMenuItem reverseTranslation;
	JCheckBoxMenuItem reverseRotation;
	JFileChooser chooser;
	
	public OfflineProfileGeneratorFrame() {
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File  
				(System.getProperty("user.home") + 
						System.getProperty("file.separator") +
						"Documents" +
						System.getProperty("file.separator") +
						"MotionProfiling" +
						System.getProperty("file.separator") +
						"profiles"));
		
		bar = new JMenuBar();
		setJMenuBar(bar);

		saveProfile = new JMenuItem("Save Profile");
		saveProfile.addActionListener(this);
		bar.add(saveProfile);
		reverseTranslation = new JCheckBoxMenuItem("Reverse Translation");
		reverseTranslation.addActionListener(this);
		bar.add(reverseTranslation);
		reverseRotation = new JCheckBoxMenuItem("Reverse Rotation");
		reverseRotation.addActionListener(this);
		bar.add(reverseRotation);
		
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
		else if(e.getSource() == reverseTranslation) {
			panel.setTranslationScale(reverseTranslation.isSelected() ? -1 : 1);
		}
		else if(e.getSource() == reverseRotation) {
			panel.setRotationScale(reverseRotation.isSelected() ? -1 : 1);
		}
	}
}
