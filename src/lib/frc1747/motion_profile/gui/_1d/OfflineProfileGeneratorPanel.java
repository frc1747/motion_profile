package lib.frc1747.motion_profile.gui._1d;

import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JPanel;

import lib.frc1747.motion_profile.Parameters;
import lib.frc1747.motion_profile.generator._1d.ProfileGenerator;

/**
 * Panel that processes the 1D profile.
 * 
 * @author Tiger Huang
 *
 */
public class OfflineProfileGeneratorPanel extends JPanel {
	private static final long serialVersionUID = -5008093073004362148L;
	
	private SingleGraphPanel translationalPanel;
	private SingleGraphPanel rotationalPanel;
	
	private double[][] profileSegments;
	private double[][] savedTimePoints;
	private double[][] savedAngularTimePoints;

	private double translationScale;
	private double rotationScale;
	private boolean zeroStart;
	private boolean zeroEnd;
	
	public OfflineProfileGeneratorPanel() {
		setLayout(new GridLayout(2, 1));
		
		translationalPanel = new SingleGraphPanel("Translation");
		translationalPanel.setUnits("ft", "s");
		add(translationalPanel);
		rotationalPanel = new SingleGraphPanel("Rotation");
		rotationalPanel.setUnits("rad", "s");
		add(rotationalPanel);
		
		translationScale = 1;
		rotationScale = 1;
		
		zeroStart = true;
		zeroEnd = true;
	}
	
	
	// The format is [ds0, dtheta0; ds1, dtheta1; ...]
	public void setProfileSetpoints(double[][] profileSegments) {
		if(profileSegments == null) return;
		this.profileSegments = profileSegments;
		
		double[][] profilePoints = ProfileGenerator.primaryProfileIntegrate(profileSegments, 0);
		double[] angularProfilePoints = ProfileGenerator.secondaryProfileIntegrate(profileSegments, 1);
		
		ProfileGenerator.skidSteerLimitVelocities(profilePoints, profileSegments,
				Parameters.V_MAX, Parameters.A_MAX, Parameters.W_WIDTH);

		// Force the max everything at the endpoints of the profile to zero
		if(zeroStart) {
			profilePoints[0][1] = 0;
			profilePoints[0][2] = 0;
		}
		if(zeroEnd) {
			profilePoints[profilePoints.length-1][1] = 0;
			profilePoints[profilePoints.length-1][2] = 0;
		}
			
		ProfileGenerator.limitVelocities(profilePoints);
		double[] profileTimes = ProfileGenerator.timesFromPoints(profilePoints);
		double[][] timePoints = ProfileGenerator.profileFromPoints(profilePoints, profileTimes, Parameters.DT);
		double[][] angularTimePoints = ProfileGenerator.synchronizedProfileFromProfile(timePoints,
				profilePoints,
				angularProfilePoints,
				profileTimes,
				Parameters.DT);
		
		// Calculate the maximum distance and rotation so it can be displayed
		double xmax = 0;
		for(int i = 1;i < timePoints.length;i++) {
			if(Math.abs(timePoints[i][0]) > xmax)
				xmax = Math.abs(timePoints[i][0]);
		}
		double axmax = 0;
		for(int i = 1;i < angularTimePoints.length;i++) {
			if(Math.abs(angularTimePoints[i][0]) > axmax)
				axmax = Math.abs(angularTimePoints[i][0]);
		}
		
		// Limit the maximum jerk
		double jerkFilterTime = Parameters.A_MAX/Parameters.J_MAX;
		timePoints = BoxcarFilter.multiFilter(timePoints, (int)Math.ceil(jerkFilterTime/Parameters.DT));
		angularTimePoints = BoxcarFilter.multiFilter(angularTimePoints, (int)Math.ceil(jerkFilterTime/Parameters.DT));
		
		// Display the two profiles
		translationalPanel.setProfile(timePoints, Parameters.DT,
				Parameters.A_MAX,
				Parameters.V_MAX,
				xmax,
				timePoints.length * Parameters.DT);
		rotationalPanel.setProfile(angularTimePoints, Parameters.DT,
				Parameters.A_MAX/Parameters.W_WIDTH*2,
				Parameters.V_MAX/Parameters.W_WIDTH*2,
				axmax,
				timePoints.length * Parameters.DT);
		
		// Save the points so outputting can be done on them later
		savedTimePoints = timePoints;
		savedAngularTimePoints = angularTimePoints;
		
		repaint();
	}
	
	public void saveProfile(File file) {
		if(savedTimePoints != null) {
			try {
				PrintWriter writer = new PrintWriter(file);
				writer.format("%d, %d\n", 2, savedTimePoints.length);
				for(int i = 0;i < savedTimePoints.length;i++) {
					writer.format("%.4f, %.4f, %.4f, %.4f, %.4f, %.4f\n",
							translationScale * savedTimePoints[i][0],
							translationScale * savedTimePoints[i][1],
							translationScale * savedTimePoints[i][2],
							rotationScale * savedAngularTimePoints[i][0],
							rotationScale * savedAngularTimePoints[i][1],
							rotationScale * savedAngularTimePoints[i][2]);
				}
				writer.close();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void setTranslationScale(double scale) {
		translationScale = scale;
	}
	public void setRotationScale(double scale) {
		rotationScale = scale;
	}

	public void setZeroStart(boolean zeroStart) {
		this.zeroStart = zeroStart;
		setProfileSetpoints(profileSegments);
	}
	public void setZeroEnd(boolean zeroEnd) {
		this.zeroEnd = zeroEnd;
		setProfileSetpoints(profileSegments);
	}
}
