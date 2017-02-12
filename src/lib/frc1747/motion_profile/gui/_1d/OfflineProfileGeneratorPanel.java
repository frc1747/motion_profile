package lib.frc1747.motion_profile.gui._1d;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class OfflineProfileGeneratorPanel extends JPanel {

	public OfflineProfileGeneratorPanel() {
		
	}
	
	/**
	 * The format is [ds0, vmax0, dtheta0; ds1, vmax1, dtheta1; ...]
	 */
	public void setProfileSetpoints(double[][] profileSetpoints) {
		// Format is [t0, vslew0, theta0; t1, vslew1, theta1; ...]
		double[][] breakPoints = new double[profileSetpoints.length+1][3];
		breakPoints[0][0] = 0;
		breakPoints[0][1] = profileSetpoints[0][1];
		breakPoints[0][2] = 0;
		for(int i = 0;i < breakPoints.length - 1;i++) {
			breakPoints[i+1][1] = -profileSetpoints[i][1];
			if(i < breakPoints.length - 2) {
				breakPoints[i+1][1] += profileSetpoints[i+1][1];
			}
			breakPoints[i+1][0] = breakPoints[i][0] + Math.abs(profileSetpoints[i][0] / profileSetpoints[i][1]);
			breakPoints[i+1][2] = breakPoints[i][2] + profileSetpoints[i][2];
		}

		System.out.println("Profile Begin");
		
		for(int i = 0;i < breakPoints.length;i++) {
			System.out.println(breakPoints[i][0] + "," + breakPoints[i][1] + "," + breakPoints[i][2]);
		}
		System.out.println("--------------------");
		
		double vmax = 12;
		double amax = 25;
		double dt = 0.02;
		double wmax = 15.0 / 8.0 * vmax / amax + dt * 2;
		double total_time = breakPoints[breakPoints.length-1][0] + wmax;
		double accelerations[] = new double[(int)Math.ceil(total_time/dt)];
		
		for(int i = 0;i < breakPoints.length;i++) {
			double toff = (wmax/2 + breakPoints[i][0])/dt;
			double vslew = breakPoints[i][1];
			double w = 15.0 / 8.0 * Math.abs(vslew) / amax;
			int half_width = (int)Math.ceil(w/dt/2);
			double accels[] = new double[half_width * 2 + 1];
			double area = 0;
			for(int j = -half_width;j <= half_width;j++) {
				double t = j * dt;
				accels[j + half_width] = amax * (
						16 * (t * t * t * t) / (w * w * w * w) -
						8 * (t * t) / (w * w) +
						1) * (vslew > 0 ? 1 : -1);
				area += accels[j + half_width] * dt;
			}
			double ratio = vslew / area;
			for(int j = -half_width;j <= half_width;j++) {
				int k = (int)Math.round(j + toff);
				accelerations[k] += accels[j + half_width] * ratio;
			}
		}
		
		double velocities[] = new double[accelerations.length];
		velocities[0] = 0;
		for(int i = 1;i < accelerations.length;i++) {
			velocities[i] = velocities[i-1] + accelerations[i-1] * dt;
		}
		double positions[] = new double[velocities.length];
		positions[0] = 0;
		for(int i = 1;i < velocities.length;i++) {
			positions[i] = positions[i-1] + velocities[i-1] * dt;
		}
		
		double max = 0;
		double min = 0;
		for(int i = 0;i < accelerations.length;i++) {
			if(accelerations[i] > max) max = accelerations[i];
			if(accelerations[i] < min) min = accelerations[i];
			System.out.println(accelerations[i] + "," + velocities[i] + "," + positions[i]);
		}
		//System.out.println(min);
		//System.out.println(max);
		
		repaint();
	}

	@Override
	public void paintComponent(Graphics g2) {
		Graphics2D g = (Graphics2D)g2;
		
	}
}
