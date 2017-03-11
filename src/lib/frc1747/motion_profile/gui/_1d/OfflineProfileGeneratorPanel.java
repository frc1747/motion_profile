package lib.frc1747.motion_profile.gui._1d;

import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JPanel;

import lib.frc1747.motion_profile.generator._1d.ProfileGenerator;

public class OfflineProfileGeneratorPanel extends JPanel {
	private SingleGraphPanel translationalPanel;
	private SingleGraphPanel rotationalPanel;
	
	private double[][] savedTimePoints;
	private double[][] savedAngularTimePoints;

	private double translationScale;
	private double rotationScale;
	
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
	}
	
	/**
	 * The format is [ds0, dtheta0; ds1, dtheta1; ...]
	 */
	public void setProfileSetpoints(double[][] profileSegments) {
		double amax = 20;
		double vmax = 6;
		double jmax = 40;
		double r_width = 2.1;
		double dt = 0.01;

		// ----------------------------------------
		// Convert the segment data into point data
		// ----------------------------------------
		
		// The format is [s0, v0, a0; s1, v1, a1; ...]
		int length = profileSegments.length+1;
		double[][] profilePoints = new double[length][3];
		double[] angularProfilePoints = new double[length];
		
		// Fill out the arc length and angles
		profilePoints[0][0] = 0;
		angularProfilePoints[0] = 0;
		for(int i = 1;i < length;i++) {
			profilePoints[i][0] = profilePoints[i-1][0] + profileSegments[i-1][0];
			angularProfilePoints[i] = angularProfilePoints[i-1] + profileSegments[i-1][1];
		}
		
		// Fill out the max velocities and accelerations
		for(int i = 0;i < length;i++) {
			// Calculate ds
			double ds = 0;
			if(i > 0) ds += profileSegments[i-1][0];
			if(i < length-1) ds += profileSegments[i][0];
			ds = Math.abs(ds) / 2;
			
			// Calculate dtheta
			double dtheta = 0;
			if(i > 0) dtheta += profileSegments[i-1][1];
			if(i < length-1) dtheta += profileSegments[i][1];
			dtheta = Math.abs(dtheta) / 2;
			
			// Calculate ddtheta
			double ddtheta = 0;
			if(i > 0) ddtheta -= profileSegments[i-1][1];
			if(i < length-1) ddtheta += profileSegments[i][1];
			ddtheta = Math.abs(ddtheta);

			profilePoints[i][1] = vmax/(1 + r_width/2 * (dtheta/ds + ddtheta/ds/ds));
			profilePoints[i][2] = amax/(1 + r_width/2 * (dtheta/ds + ddtheta/ds/ds));
		}

		// Force the max everything at the endpoints of the profile to zero
		profilePoints[0][1] = 0;
		profilePoints[0][2] = 0;
		profilePoints[profilePoints.length-1][1] = 0;
		profilePoints[profilePoints.length-1][2] = 0;
		
		double[] profileTimes = ProfileGenerator.timesFromPoints(profilePoints);
		double[][] timePoints = ProfileGenerator.profileFromPoints(profilePoints, profileTimes);
		double[][] angularTimePoints = ProfileGenerator.synchronizedProfileFromProfile(timePoints, profilePoints, angularProfilePoints, profileTimes);
		
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
		double jerkFilterTime = amax/jmax;
		timePoints = BoxcarFilter.multiFilter(timePoints, (int)Math.ceil(jerkFilterTime/dt));
		angularTimePoints = BoxcarFilter.multiFilter(angularTimePoints, (int)Math.ceil(jerkFilterTime/dt));
		
		// Display the two profiles
		translationalPanel.setProfile(timePoints, dt, amax, vmax, xmax, timePoints.length * dt);
		rotationalPanel.setProfile(angularTimePoints, dt, amax/r_width*2, vmax/r_width*2, axmax, timePoints.length * dt);
		
		// Save the points so outputting can be done on them later
		// Flipping position and acceleration for compatibility
		savedTimePoints = new double[timePoints.length][3];
		savedAngularTimePoints = new double[angularTimePoints.length][3];
		for(int i = 0;i < timePoints.length;i++) {
			savedTimePoints[i][0] = timePoints[i][2];
			savedTimePoints[i][1] = timePoints[i][1];
			savedTimePoints[i][2] = timePoints[i][0];
			
			savedAngularTimePoints[i][0] = angularTimePoints[i][2];
			savedAngularTimePoints[i][1] = angularTimePoints[i][1];
			savedAngularTimePoints[i][2] = angularTimePoints[i][0];
		}
		
		repaint();
	}
	
	public void saveProfile(File file) {
		if(savedTimePoints != null) {
			try {
				PrintWriter writer = new PrintWriter(file);
				writer.println(savedTimePoints.length);
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
	
	// Basic linear interpolation function
	public double linearInterpolate(
			double input,
			double in_min, double in_max,
			double out_min, double out_max) {
		return (input - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}

	public void setTranslationScale(double scale) {
		translationScale = scale;
	}
	public void setRotationScale(double scale) {
		rotationScale = scale;
	}
}
