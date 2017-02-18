/*
package lib.frc1747.motion_profile.follower;

import java.util.Timer;
import java.util.TimerTask;

import com.ctre.CANTalon;

public class RoboRIOMotionProfileFollower {
	// Feedback and output device
	private CANTalon talon;

	// Current profile point being used
	private int index;

	// Timing in the motion profile
	private Timer timer;

	// Motion profile parameters
	private double dt;
	private double[] positions;
	private double[] velocities;
	private double[] accelerations;

	// Feedforward constants
	private double kx;
	private double kv;
	private double ka;

	// Feedback constants
	private double kp;
	private double ki;
	private double kd;

	// Clamping variables
	private double p_lim;
	private double i_lim;
	private double d_lim;
	private double q_lim;

	// Sensor position
	private double s_p;

	// Error variables
	private double e_p;
	private double e_i;
	private double e_d;

	public RoboRIOMotionProfileFollower(CANTalon talon) {
		this.talon = talon;

		// Ensure the timer is initizliaed
		timer = null;

		// Ensure the clamping variables are zeroed
		q_lim = 1;
		p_lim = 1;
		i_lim = 1;
		d_lim = 1;
	}

	public void setFeedforward(double kx, double kv, double ka) {
		this.kx = kx;
		this.kv = kv;
		this.ka = ka;
	}

	public void setFeedback(double kp, double ki, double kd) {
		this.kp = kp;
		this.ki = ki;
		this.kd = kd;
	}

	public void setProfile(double dt, double positions[], double velocities[],
			double accelerations[]) {
		this.dt = dt;
		this.positions = positions;
		this.velocities = velocities;
		this.accelerations = accelerations;
	}

	public void setOutputScaling(double q_lim) {
		this.q_lim = q_lim;
	}

	public void setPIDLimits(double p_lim, double i_lim, double d_lim) {
		this.p_lim = p_lim;
		this.i_lim = i_lim;
		this.d_lim = d_lim;
	}

	public double calculateFeedforward(double p_p, double p_v, double p_a) {
		return kx * p_p + kv * p_v + ka * p_a;
	}

	private class CalculateClass extends TimerTask {
		// Main calculation loop
		@Override
		public void run() {
			// Profile variables
			double p_p = positions[index];
			double p_v = velocities[index];
			double p_a = accelerations[index++];

			// Measured variables
			double m_p = talon.getPosition();

			// Position error
			e_p = p_p - m_p;
			// Integral error
			e_i += e_p * dt;
			// Derivative error
			e_d = (s_p - m_p) / dt;

			// Update the sensor variable
			s_p = m_p;

			// Calculate the Output Value
			double output = calculateFeedforward(p_p, p_v, p_a);
			output += Math.min(p_lim, Math.max(-p_lim, kp * e_p));
			output += Math.min(i_lim, Math.max(-i_lim, ki * e_i));
			output += Math.min(d_lim, Math.max(-d_lim, kd * e_d));

			// Set the output to the talon
			talon.set(Math.min(1, Math.max(-1, output)) * q_lim);

			if (index >= positions.length) {
				stop();
			}
		}
	}

	public void start() {
		if (!isRunning()) {
			timer = new Timer();
			timer.scheduleAtFixedRate(new CalculateClass(), 0,
					(long) (dt * 1000));

			// Start at the beginning on the profile
			index = 0;

			// Ensure all variables and accumulators at 0
			talon.setEncPosition(0);
			s_p = 0;
			e_p = 0;
			e_i = 0;
			e_d = 0;
		}
	}

	public void stop() {
		if (isRunning()) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
	}

	public boolean isRunning() {
		return timer != null;
	}
}
*/