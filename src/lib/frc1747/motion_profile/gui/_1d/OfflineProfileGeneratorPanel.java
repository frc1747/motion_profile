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
	 * The format is [ds0, dtheta0; ds1, dtheta1; ...]
	 */
	public void setProfileSetpoints(double[][] profileSegments) {
		double amax = 20;
		double vmax = 12;
		double jmax = 50;
		double r_width = 3;
		double dt = 0.05;

		// ----------------------------------------
		// Convert the segment data into point data
		// ----------------------------------------
		
		// The format is [s0, theta0, v0, a0, visited0, t0; s1, theta1, v1, a1, visited1, t1; ...]
		int length = profileSegments.length+1;
		double[][] profilePoints = new double[length][7];
		
		// Fill out the arc length and angles
		profilePoints[0][0] = 0;
		profilePoints[0][1] = 0;
		for(int i = 1;i < length;i++) {
			profilePoints[i][0] = profilePoints[i-1][0] + profileSegments[i-1][0];
			profilePoints[i][1] = profilePoints[i-1][1] + profileSegments[i-1][1];
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

			profilePoints[i][2] = vmax/(1 + r_width/2 * (dtheta/ds + ddtheta/ds/ds));
			profilePoints[i][3] = vmax/(1 + r_width/2 * (dtheta/ds + ddtheta/ds/ds));
		}
		
		// Fill out the times and visited flags
		for(int i = 0;i < length;i++) {
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
		double jerkFilterTime = amax/jmax;
		double profileTime = profilePoints[profilePoints.length-1][5];
		
		// The format is [a0, v0, s0; a1, v1, s1; ...]
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
		for(int i = 1;i < timePoints.length-1;i++) {
			timePoints[i][0] = (timePoints[i+1][1] - timePoints[i-1][1]) / dt / 2;
		}
		
		// Take the integral to fill in the positions
		double xmax = 0;
		for(int i = 1, k = 0;i < timePoints.length;i++) {
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
		for(int i = 1;i < angularTimePoints.length-1;i++) {
			angularTimePoints[i][1] = (angularTimePoints[i+1][2] - angularTimePoints[i-1][2]) / dt / 2;
		}
		
		// Take the derivative to fill in the accelerations
		for(int i = 1;i < angularTimePoints.length-1;i++) {
			angularTimePoints[i][0] = (angularTimePoints[i+1][1] - angularTimePoints[i-1][1]) / dt / 2;
		}
		
		double[] ts = new double[timePoints.length];
		for(int i = 0;i < timePoints.length;i++) {
			//System.out.format("%.2f %.2f\n", timePoints[i][0], angularTimePoints[i][0]);
			
		}
		
		//timePoints = BoxcarFilter.multiFilter(timePoints, (int)Math.ceil(jerkFilterTime/dt));
		
		System.out.println("BEGIN");
		
		translationalPanel.setProfile(timePoints, dt, amax, vmax, xmax, timePoints.length * dt);
		rotationalPanel.setProfile(angularTimePoints, dt, amax/r_width*2, vmax/r_width*2, axmax, timePoints.length * dt);
		
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
