package lib.frc1747.motion_profile.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;

public class OfflineGeneratorFrame extends JFrame implements ActionListener {
	OfflineGeneratorPanel panel;
	JMenuBar bar;
	JRadioButtonMenuItem addPoint;
	JRadioButtonMenuItem editPoint;
	JRadioButtonMenuItem deletePoint;
	ButtonGroup modeGroup;
	public OfflineGeneratorFrame() {
		bar = new JMenuBar();
		setJMenuBar(bar);
		
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
		
		panel = new OfflineGeneratorPanel();
		add(panel);
		
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Offline Motion Profile Generator");
		setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == addPoint) {
			panel.setEditMode(EditMode.ADDPOINT);
		}
		if(e.getSource() == editPoint) {
			panel.setEditMode(EditMode.EDITPOINT);
		}
		if(e.getSource() == deletePoint) {
			panel.setEditMode(EditMode.DELETEPOINT);
		}
	}
}