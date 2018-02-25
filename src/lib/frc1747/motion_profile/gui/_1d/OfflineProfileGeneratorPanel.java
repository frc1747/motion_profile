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

	private double[][] waypointLimits;
	private double[][] profileSegments;
	private double[][] savedTimePoints;
	private double[][] savedAngularTimePoints;

	private double translationScale;
	private double rotationScale;
	private boolean zeroStart;
	private boolean zeroEnd;
	
	public double v_max;
	public double a_max;
	public double j_max;
	public double w_width;
	public double r_width;
	public double r_length;
	public double dt;
	public int i_sample_count;
	public double i_sample_length;
	
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

		v_max = Parameters.V_MAX;
		a_max = Parameters.A_MAX;
		j_max = Parameters.J_MAX;
		w_width = Parameters.W_WIDTH;
		r_width = Parameters.R_WIDTH;
		r_length = Parameters.R_LENGTH;
		dt = Parameters.DT;
		i_sample_count = Parameters.I_SAMPLE_COUNT;
		i_sample_length = Parameters.I_SAMPLE_LENGTH;
	}
	
	// The format is [s0, m_sv0, m_sa0, m_av0, m_aa0; s1, m_sv1, m_sa1, m_av1, m_aa1; ...] 
	public void setWaypointLimits(double[][] waypointLimits) {
		if(waypointLimits == null) return;
		this.waypointLimits = waypointLimits;
	}
	
	// The format is [ds0, dtheta0; ds1, dtheta1; ...]
	public void setProfileSetpoints(double[][] profileSegments) {
		if(profileSegments == null) return;
		this.profileSegments = profileSegments;
		
		double[][] profilePoints = ProfileGenerator.primaryProfileIntegrate(profileSegments, 0);
		double[] angularProfilePoints = ProfileGenerator.secondaryProfileIntegrate(profileSegments, 1);
		
		ProfileGenerator.skidSteerLimitVelocities(profilePoints, profileSegments,
				this.v_max, this.a_max, this.w_width);

		// Apply per point limits
		for(int i = 0, j = 0;i < profilePoints.length && j < waypointLimits.length;i++) {
			if(Math.abs(waypointLimits[j][0] - profilePoints[i][0]) < 1E-3) {
				if(Math.abs(waypointLimits[j][1]) < 1E3) {
					profilePoints[i][1] = waypointLimits[j][1];
				}
				if(Math.abs(waypointLimits[j][2]) < 1E3) {
					profilePoints[i][2] = waypointLimits[j][2];
				}
				j++;
			}
		}

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
		double[][] timePoints = ProfileGenerator.profileFromPoints(profilePoints, profileTimes, this.dt);
		double[][] angularTimePoints = ProfileGenerator.synchronizedProfileFromProfile(timePoints,
				profilePoints,
				angularProfilePoints,
				profileTimes,
				this.dt);
		
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
		double jerkFilterTime = this.a_max/this.j_max;
		timePoints = BoxcarFilter.multiFilter(timePoints, (int)Math.ceil(jerkFilterTime/this.dt));
		angularTimePoints = BoxcarFilter.multiFilter(angularTimePoints, (int)Math.ceil(jerkFilterTime/this.dt));
		
		// Display the two profiles
		translationalPanel.setProfile(timePoints, this.dt,
				this.a_max,
				this.v_max,
				xmax,
				timePoints.length * this.dt);
		rotationalPanel.setProfile(angularTimePoints, this.dt,
				this.a_max/this.w_width*2,
				this.v_max/this.w_width*2,
				axmax,
				timePoints.length * this.dt);
		
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
