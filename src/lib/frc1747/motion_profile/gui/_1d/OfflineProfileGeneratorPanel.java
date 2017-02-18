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
	 * The format is [ds0, dtheta0, vmax0, avmax0, amax0; ds1, dtheta1, vmax1, avmax1, amax0; ...]
	 * ds, dtheta, vmax, avmax are signed
	 * amax is zero
	 */
	public void setProfileSetpoints(double[][] profileSetpoints) {
		double a = 20;
		double v = 12;
		double j = 50;
		double w = 3;
		double dt = 0.05;
		// ----------------------------------------
		// Convert the segment data into point data
		// ----------------------------------------
		
		// The format is [s0, theta0, v0, a0, visited0, t0; s1, theta1, v1, a1, visited1, t1; ...]
		double[][] profilePoints = new double[profileSetpoints.length+1][7];
		profilePoints[0][0] = 0;
		profilePoints[0][1] = 0;
		profilePoints[0][2] = 0;
		profilePoints[0][3] = 0;
		profilePoints[0][4] = 0;
		profilePoints[0][5] = 0;
		for(int i = 1;i < profilePoints.length;i++) {
			profilePoints[i][0] = profilePoints[i-1][0] + profileSetpoints[i-1][0];
			profilePoints[i][1] = profilePoints[i-1][1] + profileSetpoints[i-1][1];
			if(i < profilePoints.length-1) {
				profilePoints[i][2] = (profileSetpoints[i-1][2] + profileSetpoints[i][2])/2;
				profilePoints[i][3] = (profileSetpoints[i-1][3] + profileSetpoints[i][3])/2;
			}
			else {
				profilePoints[i][2] = 0;
				profilePoints[i][3] = 0;
			}
			profilePoints[i][4] = 0;
			profilePoints[i][5] = 0;
		}
		
		// ----------------------------------------
		// Adjust the speeds of each point so that the acceleration is limited
		// 1. Find the point that is closest to the zero velocity line
		// 2. For each immediate neighbor that is further from the zero velocity line
		// 3. Adjust its velocity if its too high
		// 4. Repeat from 1 until no points are left
		// ----------------------------------------
		for(;;) {
			// Attempt to find an unvisited point that is closest to the zero velocity line
			int index = -1;
			for(int i = 0;i < profilePoints.length;i++) {
				if(	profilePoints[i][4] == 0 &&			//Not visited yet
					(index < 0 ||						//and there is no minimum velocity point yet, or ...
					Math.abs(profilePoints[i][2]) <		//the current point has a lower velocity
					Math.abs(profilePoints[index][2]))) {
					index = i;
				}
			}
			// We have adjusted all points
			if(index < 0) 
				break;
			
			// Adjust the left neighbor
			if(index > 0) {
				double vo = profilePoints[index][2];
				double ds = profilePoints[index-1][0] - profilePoints[index][0];
				double vt = profilePoints[index-1][2];
				double ao = profilePoints[index-1][3];
				double vt2 = -Math.signum(ds) * Math.sqrt(vo * vo + 2 * ao * Math.abs(ds));
				if(Math.abs(vt2) < Math.abs(vt)) {
					profilePoints[index-1][2] = vt2;
				}
			}
			
			// Adjust the right neighbor
			if(index < profilePoints.length-1) {
				double vo = profilePoints[index][2];
				double ds = profilePoints[index+1][0] - profilePoints[index][0];
				double vt = profilePoints[index+1][2];
				double ao = profilePoints[index+1][3];
				double vt2 = Math.signum(ds) * Math.sqrt(vo * vo + 2 * ao * Math.abs(ds));
				if(Math.abs(vt2) < Math.abs(vt)) {
					profilePoints[index+1][2] = vt2;
				}
			}
			
			// Mark this point as visited
			profilePoints[index][4] = 1;
		}
		
		// ----------------------------------------
		// Time parameterize the profile
		// ----------------------------------------
		
		// Add times to the profile
		for(int i = 1;i < profilePoints.length;i++) {
			double v0 = profilePoints[i-1][2];
			double vt = profilePoints[i][2];
			double s0 = profilePoints[i-1][0];
			double st = profilePoints[i][0];
			double t = 2 * (st - s0)/(v0 + vt);
			profilePoints[i][5] = profilePoints[i-1][5] + Math.abs(t);
		}

		// Some times to use
		double jerkFilterTime = a/j;
		double profileTime = profilePoints[profilePoints.length-1][5];
		
		// The format is [a0, v0, s0; a1, v1, s1; ...]
		//double[] unfilteredVelocities = new double[(int)Math.ceil(profileTime / dt)];
		double[][] timePoints = new double[(int)Math.ceil(profileTime / dt)][3];
		
		// Populate the time parameterized profile
		velocityPointsLoop:
		for(int i = 0, k = 0;i < timePoints.length;i++) {
			double t = i * dt;
			while(profilePoints[k+1][5] < t) {
				k++;
				// We done generating the profile
				if(k > profilePoints.length-2) {
					break velocityPointsLoop;
				}
			}
			
			// The arc velocity exactly corresponds with a table value
			if(t == profilePoints[k][5]) {
				timePoints[i][1] = profilePoints[k][2];
			}
			// Interpolate
			else {
				timePoints[i][1] = linearInterpolate(
						t,
						profilePoints[k][6], profilePoints[k+1][5],
						profilePoints[k][2], profilePoints[k+1][2]);
			}
		}
		
		// Take the derivative to fill in the accelerations
		for(int i = 0;i < timePoints.length-1;i++) {
			timePoints[i][0] = (timePoints[i+1][1] - timePoints[i][1]) / dt;
		}
		
		// Take the integral to fill in the positions
		double xmax = 0;
		positionPointsLoop:
		for(int i = 1, k = 0;i < timePoints.length;i++) {
			double t = i * dt;
			while(profilePoints[k+1][5] < t) {
				k++;
				// We done generating the profile
				if(k > profilePoints.length-2) {
					break positionPointsLoop;
				}
			}
			
			timePoints[i][2] = timePoints[i-1][2] + (timePoints[i-1][1] + timePoints[i][1])/2 * dt;
			if(Math.abs(timePoints[i][2]) > xmax)
				xmax = Math.abs(timePoints[i][2]);
		}

		// Take the profile points and convert to angular positions
		double[][] angularTimePoints = new double[timePoints.length][3];
		double axmax = 0;
		for(int i = 0, k = 0;i < timePoints.length;i++) {
			double t = i * dt;
			double s = timePoints[i][2];
			while(profilePoints[k+1][5] < t) {
				k++;
				if(k > profilePoints.length-2) {
					k = profilePoints.length-2;
					break;
				}
			}
			
			// The arc length exactly corresponds with a table value
			if(t == profilePoints[k][5]) {
				angularTimePoints[i][2] = profilePoints[k][1];
			}
			// Interpolate
			else {
				angularTimePoints[i][2] = linearInterpolate(
						s,
						profilePoints[k][0], profilePoints[k+1][0],
						profilePoints[k][1], profilePoints[k+1][1]);
			}	
			if(Math.abs(angularTimePoints[i][2]) > axmax)
				axmax = Math.abs(angularTimePoints[i][2]);
		}

		// Take the derivative to fill in the velocities
		for(int i = 0;i < angularTimePoints.length-1;i++) {
			angularTimePoints[i][1] = (angularTimePoints[i+1][2] - angularTimePoints[i][2]) / dt;
		}
		
		// Take the derivative to fill in the accelerations
		for(int i = 0;i < angularTimePoints.length-1;i++) {
			angularTimePoints[i][0] = (angularTimePoints[i+1][1] - angularTimePoints[i][1]) / dt;
		}
		
		/*
		// Apply the jerk boxcar filter
		int jerkFilterWidth = (int)Math.ceil(jerkFilterTime/dt);
		for(int i = 0;i < unfilteredVelocities.length;i++) {
			timePoints[i][1] = 0;
			for(int k = Math.max(0, i - jerkFilterWidth + 1);k <= i;k++) {
				timePoints[i][1] += unfilteredVelocities[k];
			}
			timePoints[i][1] /= jerkFilterWidth;
		}
		*/
		
		System.out.println("BEGIN");
		
		translationalPanel.setProfile(timePoints, dt, a, v, xmax, timePoints.length * dt);
		rotationalPanel.setProfile(angularTimePoints, dt, a/w*2, v/w*2, axmax, timePoints.length * dt);
		
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
