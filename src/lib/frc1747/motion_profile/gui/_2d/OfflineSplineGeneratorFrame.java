package lib.frc1747.motion_profile.gui._2d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import lib.frc1747.motion_profile.gui._1d.OfflineProfileGeneratorFrame;

/**
 * A Frame that contains the panel that processes 2D splines.
 * 
 * @author Tiger Huang
 *
 */
public class OfflineSplineGeneratorFrame extends JFrame implements ActionListener {
	private static final long serialVersionUID = 7436504607369102953L;
	
	private OfflineSplineGeneratorPanel panel;
	private JMenuBar bar;
	private JRadioButtonMenuItem addPoint;
	private JRadioButtonMenuItem editPoint;
	private JRadioButtonMenuItem deletePoint;
	private JMenuItem saveWaypoints;
	private JMenuItem openWaypoints;
	private ButtonGroup modeGroup;
	private JFileChooser chooser;
	
	public OfflineSplineGeneratorFrame() {
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File  
				(System.getProperty("user.home") + 
						System.getProperty("file.separator") +
						"Documents" +
						System.getProperty("file.separator") +
						"MotionProfiling" +
						System.getProperty("file.separator") +
						"waypoints"));
		
		bar = new JMenuBar();
		setJMenuBar(bar);
		
		saveWaypoints = new JMenuItem("Save Waypoints");
		saveWaypoints.addActionListener(this);
		bar.add(saveWaypoints);
		openWaypoints = new JMenuItem("Open Waypoints");
		openWaypoints.addActionListener(this);
		bar.add(openWaypoints);
		
		modeGroup = new ButtonGroup();
		addPoint = new JRadioButtonMenuItem("Add point");
		addPoint.addActionListener(this);
		bar.add(addPoint);
		modeGroup.add(addPoint);
		editPoint = new JRadioButtonMenuItem("Edit point", true);
		editPoint.addActionListener(this);
		bar.add(editPoint);
		modeGroup.add(editPoint);
		deletePoint = new JRadioButtonMenuItem("Delete point");
		deletePoint.addActionListener(this);
		bar.add(deletePoint);
		modeGroup.add(deletePoint);
		
		panel = new OfflineSplineGeneratorPanel();
		add(panel);
		
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Offline Motion Trajectory Generator");
		setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == saveWaypoints) {
			int retVal = chooser.showSaveDialog(this);
			if(retVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				panel.saveWaypoints(file);
			}
		}
		else if(e.getSource() == openWaypoints) {
			int retVal = chooser.showOpenDialog(this);
			if(retVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				panel.openWaypoints(file);
			}
		}
		else if(e.getSource() == addPoint) {
			panel.setEditMode(EditMode.ADDPOINT);
		}
		else if(e.getSource() == editPoint) {
			panel.setEditMode(EditMode.EDITPOINT);
		}
		else if(e.getSource() == deletePoint) {
			panel.setEditMode(EditMode.DELETEPOINT);
		}
	}

	public void setProfileFrame(OfflineProfileGeneratorFrame profile) {
		panel.setProfilePanel(profile.getProfilePanel());
	}
}