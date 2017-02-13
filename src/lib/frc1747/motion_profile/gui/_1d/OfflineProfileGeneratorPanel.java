package lib.frc1747.motion_profile.gui._1d;

import java.awt.GridLayout;

import javax.swing.JPanel;

public class OfflineProfileGeneratorPanel extends JPanel {
	private SingleGraphPanel translationalPanel;
	private SingleGraphPanel rotationalPanel;

	public OfflineProfileGeneratorPanel() {
		setLayout(new GridLayout(2, 1));
		
		translationalPanel = new SingleGraphPanel("Translation");
		translationalPanel.setUnits("ft", "s");
		add(translationalPanel);
		rotationalPanel = new SingleGraphPanel("Rotation");
		rotationalPanel.setUnits("rad", "s");
		add(rotationalPanel);
	}
	
	/**
	 * The format is [ds0, vmax0, dtheta0; ds1, vmax1, dtheta1; ...]
	 */
	public void setProfileSetpoints(double[][] profileSetpoints) {
		// ----------------------------------------
		// Convert the segment data into point data
		// ----------------------------------------
		
		// The format is [s0, v0, vtheta0, visited0, t0; s1, v0, vtheta1, visited1, t1; ...]
		double[][] profilePoints = new double[profileSetpoints.length+1][5];
		profilePoints[0][0] = 0;
		profilePoints[0][1] = 0;
		profilePoints[0][2] = 0;
		profilePoints[0][3] = 0;
		profilePoints[0][4] = 0;
		for(int i = 1;i < profilePoints.length;i++) {
			profilePoints[i][0] = profilePoints[i-1][0] + profileSetpoints[i-1][0];
			if(i < profilePoints.length-1) {
				profilePoints[i][1] = (profileSetpoints[i-1][1] + profileSetpoints[i][1])/2;
				double ds = (profileSetpoints[i-1][0] + profileSetpoints[i][0])/2;
				double dtheta = (profileSetpoints[i-1][2] + profileSetpoints[i][2])/2;
				profilePoints[i][2] = profilePoints[i][1] * dtheta / ds;
			}
			else {
				profilePoints[i][1] = 0;
				profilePoints[i][2] = 0;
			}
			profilePoints[i][3] = 0;
		}
		for(int i = 1;i < profilePoints.length;i++) {
			if(Math.abs(profilePoints[i][2]) > 12 * 2.0/3) {
				profilePoints[i][2] = profilePoints[i-1][2];
			}
		}
		
		// ----------------------------------------
		// Adjust the speeds of each point so that the acceleration is limited
		// 1. Find the point that is closest to the zero velocity line
		// 2. For each immediate neighbor that is further from the zero velocity line
		// 3. Adjust its velocity if its too high
		// 4. Repeat from 1 until no points are left
		// ----------------------------------------
		double a = 20;
		double v = 12;
		double j = 50;
		double dt = 0.02;
		for(;;) {
			// Attempt to find an unvisited point that is closest to the zero velocity line
			int index = -1;
			for(int i = 0;i < profilePoints.length;i++) {
				if(	profilePoints[i][3] == 0 &&			//Not visited yet
					(index < 0 ||						//and there is no minimum velocity point yet, or ...
					Math.abs(profilePoints[i][1]) <		//the current point has a lower velocity
					Math.abs(profilePoints[index][1]))) {
					index = i;
				}
			}
			// We have adjusted all points
			if(index < 0) 
				break;
			
			// Adjust the left neighbor
			if(index > 0) {
				double vo = profilePoints[index][1];
				double ds = profilePoints[index-1][0] - profilePoints[index][0];
				double vt = profilePoints[index-1][1];
				double vt2 = -Math.signum(ds) * Math.sqrt(vo * vo + 2 * a * Math.abs(ds));
				if(Math.abs(vt2) < Math.abs(vt)) {
					profilePoints[index-1][1] = vt2;
					profilePoints[index-1][2] *= vt2/vt;
				}
			}
			
			// Adjust the right neighbor
			if(index < profilePoints.length-1) {
				double vo = profilePoints[index][1];
				double ds = profilePoints[index+1][0] - profilePoints[index][0];
				double vt = profilePoints[index+1][1];
				double vt2 = Math.signum(ds) * Math.sqrt(vo * vo + 2 * a * Math.abs(ds));
				if(Math.abs(vt2) < Math.abs(vt)) {
					profilePoints[index+1][1] = vt2;
					profilePoints[index+1][2] *= vt2/vt;
				}
			}
			
			// Mark this point as visited
			profilePoints[index][3] = 1;
		}
		
		for(int i = 0;i < profilePoints.length;i++) {
			//System.out.println(profilePoints[i][2]);
		}
		
		// ----------------------------------------
		// Time parameterize the profile
		// ----------------------------------------
		
		// Add times to the profile
		for(int i = 1;i < profilePoints.length;i++) {
			double v0 = profilePoints[i-1][1];
			double vt = profilePoints[i][1];
			double s0 = profilePoints[i-1][0];
			double st = profilePoints[i][0];
			double t = 2 * (st - s0)/(v0 + vt);
			profilePoints[i][4] = profilePoints[i-1][4] + Math.abs(t);
		}

		// Some times to use
		double jerkFilterTime = a/j;
		double profileTime = profilePoints[profilePoints.length-1][4] + jerkFilterTime;
		
		// The format is [a0, v0, s0; a1, v1, s1; ...]
		double[] unfilteredVelocities = new double[(int)Math.ceil(profileTime / dt)];
		double[][] timePoints = new double[unfilteredVelocities.length][3];
		double[] angularUnfilteredVelocities = new double[unfilteredVelocities.length];
		double[][] angularTimePoints = new double[unfilteredVelocities.length][3];
		
		// Populate the time parameterized profile
		timePointsLoop:
		for(int i = 0, k = 0;i < unfilteredVelocities.length;i++) {
			double t = i * dt;
			while(profilePoints[k+1][4] < t) {
				k++;
				// We done generating the profile
				if(k >= profilePoints.length-1)
					break timePointsLoop;
			}
			
			// The arc length exactly corresponds with a table value
			if(t == profilePoints[k][4]) {
				unfilteredVelocities[i] = profilePoints[k][1];
				angularUnfilteredVelocities[i] = profilePoints[k][2];
			}
			// Interpolate
			else {
				unfilteredVelocities[i] = linearInterpolate(
						t,
						profilePoints[k][4], profilePoints[k+1][4],
						profilePoints[k][1], profilePoints[k+1][1]);
				angularUnfilteredVelocities[i] = linearInterpolate(
						t,
						profilePoints[k][4], profilePoints[k+1][4],
						profilePoints[k][2], profilePoints[k+1][2]);
			}
		}
		
		// Apply the jerk boxcar filter
		int jerkFilterWidth = (int)Math.ceil(jerkFilterTime/dt);
		for(int i = 0;i < unfilteredVelocities.length;i++) {
			timePoints[i][1] = 0;
			for(int k = Math.max(0, i - jerkFilterWidth + 1);k <= i;k++) {
				timePoints[i][1] += unfilteredVelocities[k];
			}
			timePoints[i][1] /= jerkFilterWidth;
			
			angularTimePoints[i][1] = 0;
			for(int k = Math.max(0, i - jerkFilterWidth + 1);k <= i;k++) {
				angularTimePoints[i][1] += angularUnfilteredVelocities[k];
			}
			angularTimePoints[i][1] /= jerkFilterWidth;
		}
		
		// Take the derivative to fill in the accelerations
		for(int i = 0;i < timePoints.length-1;i++) {
			timePoints[i][0] = (timePoints[i+1][1] - timePoints[i][1]) / dt;
		}
		double aamax = 0;
		for(int i = 0;i < angularTimePoints.length-1;i++) {
			angularTimePoints[i][0] = (angularTimePoints[i+1][1] - angularTimePoints[i][1]) / dt;
			if(Math.abs(angularTimePoints[i][0]) > aamax)
				aamax = Math.abs(angularTimePoints[i][0]);
		}
		
		// Take the integral to fill in the positions
		double xmax = 0;
		for(int i = 1;i < timePoints.length;i++) {
			timePoints[i][2] = timePoints[i-1][2] + timePoints[i][1] * dt;
			if(Math.abs(timePoints[i][2]) > xmax)
				xmax = Math.abs(timePoints[i][2]);
		}
		double axmax = 0;
		for(int i = 1;i < angularTimePoints.length;i++) {
			angularTimePoints[i][2] = angularTimePoints[i-1][2] + angularTimePoints[i][1] * dt;
			if(Math.abs(angularTimePoints[i][2]) > axmax)
				axmax = Math.abs(angularTimePoints[i][2]);
		}
		
		// Find the max angular velocity
		double avmax = 0;
		for(int i = 1;i < angularTimePoints.length;i++) {
			if(Math.abs(angularTimePoints[i][1]) > avmax)
				avmax = Math.abs(angularTimePoints[i][1]);
		}
		
		System.out.println("BEGIN");
		for(int i = 0;i < unfilteredVelocities.length;i++) {
			//System.out.format("%.2f\t%.2f\t%.2f\t%.2f\n", i * dt, angularTimePoints[i][0], angularTimePoints[i][1], angularTimePoints[i][2]);
		}
		translationalPanel.setProfile(timePoints, dt, a, v, xmax, timePoints.length * dt);
		rotationalPanel.setProfile(angularTimePoints, dt, aamax, avmax, axmax, timePoints.length * dt);
		
		repaint();
	}
	
	// Basic linear interpolation function
	public double linearInterpolate(
			double input,
			double in_min, double in_max,
			double out_min, double out_max) {
		return (input - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
}
