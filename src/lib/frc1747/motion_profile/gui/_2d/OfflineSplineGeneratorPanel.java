/**
 * @author Tiger
 */

package lib.frc1747.motion_profile.gui._2d;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.JPanel;

import lib.frc1747.motion_profile.generator._2d.QuinticBezier;
import lib.frc1747.motion_profile.generator._2d.SplineGenerator;
import lib.frc1747.motion_profile.generator._2d.Waypoint;
import lib.frc1747.motion_profile.gui._1d.OfflineProfileGeneratorPanel;

public class OfflineSplineGeneratorPanel
		extends JPanel
		implements MouseListener, MouseMotionListener, MouseWheelListener {
	//Global draw scale
	double scale;		// px/ft
	//Global grid spacing
	double spacing;		// ft
	//Everything drawn is offset by this vector
	double offx;		// ft
	double offy;		// ft
	//UI parameters
	double scrollRatio = 0.8;
	int widgetSize = 20;
	double v_scale = 0.5;
	double a_scale = 0.2;
	
	//Click pan location
	int panClickX;
	int panClickY;
	
	//Click edit index
	int editIndex;
	
	//Current mode
	EditMode editMode;
	EditMode prevMode;
	
	//Waypoints
	ArrayList<Waypoint> waypoints;
	QuinticBezier splines[];
	
	//Output consumer
	OfflineProfileGeneratorPanel profilePanel;
	
	public OfflineSplineGeneratorPanel() {
		//Display variables
		scale = 50;
		spacing = 1;
		offx = 0;
		offy = 0;
		
		//Add listeners
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		
		//Initialize the input
		editMode = EditMode.EDITPOINT;
		prevMode = editMode;
		editIndex = -1;
		
		//Initialize waypoints
		waypoints = new ArrayList<>();
	}
	
	public void recalculateSplines() {
		if(waypoints.size() >= 2) {
			splines = SplineGenerator.splinesFromWaypoints(
					waypoints.toArray(new Waypoint[0]));
			
			double[][] profileSetpoints = SplineGenerator.flattenProfile(splines);
			if(profilePanel != null) {
				profilePanel.setProfileSetpoints(profileSetpoints);
			}
			/*
			System.out.println("Profile Begin");
			for(int i = 0;i < profileSetpoints.length;i++) {
				System.out.println(
						profileSetpoints[i][0] + "," +
						profileSetpoints[i][1] + "," +
						profileSetpoints[i][2]);
			}
			*/
		}
		else {
			splines = null;
		}
	}
	
	@Override
	public void paintComponent(Graphics g2) {
		Graphics2D g = (Graphics2D)g2;
		//Temp get width and height
		int width = getWidth();
		int height = getHeight();

		//Draw the background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);

		//Draw the grid
		g.setColor(Color.LIGHT_GRAY);
		int hhgcount = (int)(width / (spacing * scale * 2) + 1);
		for(int i = -hhgcount;i <= hhgcount;i++) {
			g.drawLine(
					(int)((i * spacing + offx % spacing) * scale + width/2 + .5), 0,
					(int)((i * spacing + offx % spacing) * scale + width/2 + .5), height);
		}
		int hvgcount = (int)(height / (spacing * scale * 2) + 1);
		for(int i = -hvgcount;i <= hvgcount;i++) {
			g.drawLine(
					0, 	   (int)((i * spacing - offy % spacing) * scale + height/2 + .5),
					width, (int)((i * spacing - offy % spacing) * scale + height/2 + .5));
		}
		g.setColor(Color.GREEN.brighter());
		g.drawLine(
				(int)(offx * scale + width/2 + .5), 0,
				(int)(offx * scale + width/2 + .5), height);
		g.setColor(Color.RED.brighter());
		g.drawLine(
				0,	   (int)(-offy * scale + height/2 + .5),
				width, (int)(-offy * scale + height/2 + .5));
		
		//Draw the waypoints
		for(int i = 0;i < waypoints.size();i++) {
			Waypoint waypoint = waypoints.get(i);
			waypoint.recalculateShapes(
					scale, widgetSize,
					offx, offy,
					width, height,
					v_scale, a_scale);
			g.setColor(Color.BLUE);
			g.draw(waypoint.getPointShape());
			g.setColor(Color.RED);
			g.draw(waypoint.getTangentShape());
			g.setColor(Color.GREEN.darker());
			g.draw(waypoint.getCurvatureShape());
		}
		
		//Draw the splines
		if(splines != null) {
			//Share a transform
			AffineTransform transform = new AffineTransform();
			transform.translate(width/2 + .5, height/2 + .5);
			transform.scale(scale, -scale);
			transform.translate(offx, offy);
			
			g.setColor(Color.MAGENTA);
			
			for(int i = 0;i < splines.length;i++) {
				double[] points = splines[i].uniformTimePositionSample(20);
				transform.transform(points, 0, points, 0, points.length/2);
				for(int j = 0;j < points.length/2 - 1;j++) {
					g.drawLine(
							(int)(points[j * 2]), (int)(points[j * 2 + 1]),
							(int)(points[j * 2 + 2]), (int)(points[j * 2 + 3]));
				}
			}
		}
	}
	
	public void setEditMode(EditMode mode) {
		prevMode = editMode;
		editMode = mode;
		cursorModeDefault();
	}
	
	public void revertEditMode() {
		editMode = prevMode;
		cursorModeDefault();
	}
	
	private void cursorModeDefault() {
		switch(editMode) {
		case ADDPOINT:
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			break;
		case EDITPOINT:
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			break;
		case DELETEPOINT:
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			break;
		case PAN:
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			break;
		}
	}
	
	private void setPointLocation(double posx, double posy) {
		if(editIndex >= 0) {
			Waypoint waypoint = waypoints.get(editIndex/3);
			if(editIndex % 3 == 0) {
				waypoint.x = posx;
				waypoint.y = posy;
			}
			else if(editIndex % 3 == 1) {
				waypoint.v_m = Math.hypot(posx - waypoint.x, posy - waypoint.y) / v_scale;
				waypoint.v_t = Math.atan2(posy - waypoint.y, posx - waypoint.x) - Math.PI/2;
				if(waypoint.v_t < -Math.PI)	waypoint.v_t += Math.PI * 2;
				if(waypoint.v_t > Math.PI) 	waypoint.v_t -= Math.PI * 2;
			}
			else if(editIndex % 3 == 2) {
				waypoint.a_m = Math.hypot(posx - waypoint.x, posy - waypoint.y) / a_scale;
				waypoint.a_t = Math.atan2(posy - waypoint.y, posx - waypoint.x) - Math.PI/2 - waypoint.v_t;
				if(waypoint.a_t < -Math.PI)	waypoint.a_t += Math.PI * 2;
				if(waypoint.a_t > Math.PI) 	waypoint.a_t -= Math.PI * 2;	
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON2) {
			panClickX = e.getX();
			panClickY = e.getY();
			setEditMode(EditMode.PAN);
		}
		else if(editMode == EditMode.EDITPOINT && e.getButton() == MouseEvent.BUTTON1) {
			Point mousePoint = new Point(e.getX(), e.getY());
			for(int i = 0;i < waypoints.size();i++) {
				Waypoint waypoint = waypoints.get(i);
				if(		waypoint.getCurvatureShape() != null &&
						waypoint.getCurvatureShape().contains(mousePoint)) {
					editIndex = i * 3 + 2;
					break;
				}
				else if(	waypoint.getTangentShape() != null &&
							waypoint.getTangentShape().contains(mousePoint)) {
					editIndex = i * 3 + 1;
					break;
				}
				else if(	waypoint.getPointShape() != null &&
							waypoint.getPointShape().contains(mousePoint)) {
					editIndex = i * 3;
					break;
				}
			}
			setPointLocation(
					(e.getX() - getWidth()/2)/scale - offx,
					-(e.getY() - getHeight()/2)/scale - offy);
		}
		recalculateSplines();
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(editMode == EditMode.PAN) {
			int newClickX = e.getX();
			int newClickY = e.getY();

			offx += (newClickX - panClickX) / scale;
			offy -= (newClickY - panClickY) / scale;
			
			panClickX = newClickX;
			panClickY = newClickY;
		}
		else if(editMode == EditMode.EDITPOINT) {
			setPointLocation(
					(e.getX() - getWidth()/2)/scale - offx,
					-(e.getY() - getHeight()/2)/scale - offy);
		}

		recalculateSplines();
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(editMode == EditMode.PAN && e.getButton() == MouseEvent.BUTTON2) {
			int newClickX = e.getX();
			int newClickY = e.getY();

			offx += (newClickX - panClickX) / scale;
			offy -= (newClickY - panClickY) / scale;

			revertEditMode();
		}
		else if(editMode == EditMode.EDITPOINT && e.getButton() == MouseEvent.BUTTON1) {
			setPointLocation(
					(e.getX() - getWidth()/2)/scale - offx,
					-(e.getY() - getHeight()/2)/scale - offy);
			editIndex = -1;
		}
		else if(editMode == EditMode.EDITPOINT && e.getButton() == MouseEvent.BUTTON3) {
			Point mousePoint = new Point(e.getX(), e.getY());
			for(int i = 0;i < waypoints.size();i++) {
				Waypoint waypoint = waypoints.get(i);
				if(		waypoint.getPointShape() != null &&
						waypoint.getPointShape().contains(mousePoint)) {
					waypoints.get(i).reverse ^= true;
					break;
				}
			}
		}
		else if(editMode == EditMode.DELETEPOINT && e.getButton() == MouseEvent.BUTTON1) {
			Point mousePoint = new Point(e.getX(), e.getY());
			for(int i = 0;i < waypoints.size();i++) {
				Waypoint waypoint = waypoints.get(i);
				if(		waypoint.getPointShape() != null &&
						waypoint.getPointShape().contains(mousePoint)) {
					waypoints.remove(i);
					break;
				}
			}
		}
		else if(editMode == EditMode.ADDPOINT && e.getButton() == MouseEvent.BUTTON1) {
			//Ensure the point is inside the area
			if(	e.getX() >= 0 && e.getX() <= getWidth() &&
				e.getY() >= 0 && e.getY() <= getHeight()) {
				Waypoint waypoint = new Waypoint();
				waypoint.x = (e.getX() - getWidth()/2)/scale - offx;
				waypoint.y = -(e.getY() - getHeight()/2)/scale - offy;
				waypoint.v_t = 0;
				waypoint.v_m = 2;
				waypoint.a_t = -Math.PI/2;
				waypoint.a_m = 5;
				if(waypoints.size() >= 1) {
					waypoint.reverse = waypoints.get(waypoints.size() - 1).reverse;
				}
				waypoints.add(waypoint);
			}
		}

		recalculateSplines();
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(editMode == EditMode.EDITPOINT) {
			Point mousePoint = new Point(e.getX(), e.getY());
			boolean inside = false;
			for(int i = 0;i < waypoints.size();i++) {
				Waypoint waypoint = waypoints.get(i);
				if(		(waypoint.getPointShape() != null &&
						waypoint.getPointShape().contains(mousePoint)) ||
						(waypoint.getTangentShape() != null &&
						waypoint.getTangentShape().contains(mousePoint)) ||
						(waypoint.getCurvatureShape() != null &&
						waypoint.getCurvatureShape().contains(mousePoint))) {
					inside = true;
				}
			}
			if(inside)	setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			else		cursorModeDefault();
		}
		else if(editMode == EditMode.DELETEPOINT) {
			Point mousePoint = new Point(e.getX(), e.getY());
			boolean inside = false;
			for(int i = 0;i < waypoints.size();i++) {
				Waypoint waypoint = waypoints.get(i);
				if(		waypoint.getPointShape() != null &&
						waypoint.getPointShape().contains(mousePoint)) {
					inside = true;
				}
			}
			if(inside)	setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			else		cursorModeDefault();
		}
		
		repaint();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		//Scale the view and move it so the scale center is the mouse
		double posx = (e.getX() - getWidth()/2)/scale - offx;
		double posy = -(e.getY() - getHeight()/2)/scale - offy;
		scale *= Math.pow(scrollRatio, -e.getPreciseWheelRotation());
		offx = (e.getX() - getWidth()/2)/scale - posx;
		offy = -(e.getY() - getHeight()/2)/scale - posy;
		
		repaint();
	}

	public void setProfilePanel(OfflineProfileGeneratorPanel profilePanel) {
		this.profilePanel = profilePanel;
	}
}