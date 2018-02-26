package lib.frc1747.motion_profile.gui._1d;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * Panel that displays the position, velocity, and acceleration of one component of the motion (translational, rotational).
 * 
 * @author Tiger Huang
 *
 */
public class SingleGraphPanel extends JPanel {
	private static final long serialVersionUID = -308204385892576695L;
	
	private String title;
	private int fontSize = 14;
	private double[][] profile;
	private double dt;
	private double amax;
	private double vmax;
	private double xmax;
	private double tmax;
	private String xUnit;
	private String tUnit;
	
	public SingleGraphPanel(String title) {
		this.title = title;
	}
	
	public void setUnits(String xUnit, String tUnit) {
		this.xUnit = xUnit;
		this.tUnit = tUnit;
	}
	
	// The format is [x0, v0, a0; x1, v1, a1; ...]
	public void setProfile(double[][] profile, double dt, 
			double amax, double vmax, double xmax, double tmax) {
		this.profile = profile;
		this.dt = dt;
		this.amax = amax;
		this.vmax = vmax;
		this.xmax = xmax;
		this.tmax = tmax;
	}

	@Override
	public void paintComponent(Graphics g2) {
		Graphics2D g = (Graphics2D)g2;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		//Temp get width and height
		int width = getWidth();
		int height = getHeight();

		//Draw the background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		
		//Draw the title
		g.setFont(new Font("Helvetica", Font.BOLD, fontSize));
		FontMetrics fontMetrics = g.getFontMetrics();
		int titleWidth = fontMetrics.stringWidth(title);
		g.setColor(Color.BLACK);
		g.drawString(title, width/2 - titleWidth/2, fontSize);
		
		//Ensure we are actually drawing a profile
		if(profile != null) {
			int graphHeight = height - fontSize * 2;
			int graphOffset = 80;
			int graphWidth = width - graphOffset;
			int itmax = (int)Math.ceil(tmax);
			g.setFont(new Font("Helvetica", Font.PLAIN, fontSize - 2));
			fontMetrics = g.getFontMetrics();
			
			//Draw the labels for the axis maximums
			g.setColor(Color.BLACK);
			g.drawString(
					String.format("\u00B1%.2f %s", xmax, xUnit),
					2, fontSize);
			g.drawString(
					String.format("\u00B1%.2f %s/%s", vmax, xUnit, tUnit), 
					2, fontSize * 2);
			g.drawString(
					String.format("\u00B1%.2f %s/%s\u00B2", amax, xUnit, tUnit),
					2, fontSize * 3);
			
			//Draw the time labels
			for(int i = 0;i <= itmax;i++) {
				int w = (int)(graphOffset + graphWidth * i / tmax);
				String timeLabel = String.format("%d s", i);
				int labelWidth = fontMetrics.stringWidth(timeLabel);
				g.drawString(timeLabel, w - labelWidth/2, height-1);
			}
			
			//Draw the time lines
			g.setColor(Color.LIGHT_GRAY);
			for(int i = 0;i <= itmax * 4;i++) {
				int w = (int)(graphOffset + graphWidth * i / tmax / 4);
				g.drawLine(w, fontSize, w, height - fontSize);
			}
			g.setColor(Color.BLACK);
			for(int i = 0;i <= itmax;i++) {
				int w = (int)(graphOffset + graphWidth * i / tmax);
				g.drawLine(w, fontSize, w, height - fontSize);
			}
			
			//Draw the value lines
			g.setColor(Color.LIGHT_GRAY);
			for(int i = 0;i <= 4;i++) {
				int y = fontSize + graphHeight * i / 4;
				g.drawLine(graphOffset, y, width, y);
			}
			g.setColor(Color.BLACK);
			int zeroY = fontSize + graphHeight * 2 / 4;
			g.drawLine(graphOffset, zeroY, width, zeroY);
			
			//Draw the acceleration graph
			g.setColor(Color.GREEN.darker());
			for(int i = 1;i < profile.length;i++) {
				g.drawLine(
						(int)(graphOffset + graphWidth * (i-1) * dt / tmax),
						(int)(fontSize + graphHeight/2 - profile[i-1][2] * graphHeight/2 / amax),
						(int)(graphOffset + graphWidth * i * dt / tmax),
						(int)(fontSize + graphHeight/2 - profile[i][2] * graphHeight/2 / amax));
			}
			g.setColor(Color.RED);
			for(int i = 1;i < profile.length;i++) {
				g.drawLine(
						(int)(graphOffset + graphWidth * (i-1) * dt / tmax),
						(int)(fontSize + graphHeight/2 - profile[i-1][1] * graphHeight/2 / vmax),
						(int)(graphOffset + graphWidth * i * dt / tmax),
						(int)(fontSize + graphHeight/2 - profile[i][1] * graphHeight/2 / vmax));
			}
			g.setColor(Color.BLUE);
			for(int i = 1;i < profile.length;i++) {
				g.drawLine(
						(int)(graphOffset + graphWidth * (i-1) * dt / tmax),
						(int)(fontSize + graphHeight/2 - profile[i-1][0] * graphHeight/2 / xmax),
						(int)(graphOffset + graphWidth * i * dt / tmax),
						(int)(fontSize + graphHeight/2 - profile[i][0] * graphHeight/2 / xmax));
			}
		}
	}
}
