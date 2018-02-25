package lib.frc1747.motion_profile.generator._2d;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 * Waypoints for use with QuinticBezeiers.
 * 
 * Contains several features for easier GUI usage as well.
 * 
 * @author Tiger Huang
 *
 */
public class Waypoint {
	/**
	 * The x position (ft)
	 */
	public double x;
	/**
	 * The y position (ft)
	 */
	public double y;
	/**
	 * The direction of the velocity vector (rad)
	 * 
	 * Zero is in the +y direction (forward), and the angle increases counterclockwise. 
	 */
	public double v_t;
	/**
	 * The magnitude of the velocity vector (ft/s)
	 */
	public double v_m;
	/**
	 * The direction of the acceleration vector(rad)
	 * 
	 * Zero is in the direction of the velocity vector, and the angle increases counterclockwise.
	 */
	public double a_t;
	/**
	 * The magnitude of the acceleration vector (ft/s^2)
	 */
	public double a_m;
	/**
	 * The maximum linear velocity
	 */
	public double m_sv;
	/**
	 * The maximum linear acceleration
	 */
	public double m_sa;
	/**
	 * The maximum angular velocity
	 */
	public double m_av;
	/**
	 * The maximum angular acceleration
	 */
	public double m_aa;
	
	/**
	 * If the QuinticBezier immediately following this waypoint should be traveled in reverse or not.
	 */
	public boolean reverse;
	
	// Shapes for drawing and dragging
	private Shape pointShape;
	private Shape tangentShape;
	private Shape curvatureShape;
	
	/**
	 * Creates a Waypoint with all zero parameters
	 */
	public Waypoint() {
		x = 0;
		y = 0;
		v_t = 0;
		v_m = 0;
		a_t = 0;
		a_m = 0;
		m_sv = 0;
		m_sa = 0;
		m_av = 0;
		m_aa = 0;
		reverse = false;
	}
	
	/**
	 * Returns a waypoint that has the velocity component reversed so backwards QuinticBeziers can be generated.
	 * @return a waypoint with velocity and the reverse flag inverted
	 */
	public Waypoint getInverse() {
		Waypoint waypoint = new Waypoint();
		waypoint.x = x;
		waypoint.y = y;
		waypoint.v_t = v_t;
		waypoint.v_m = -v_m;
		waypoint.a_t = a_t;
		waypoint.a_m = a_m;
		waypoint.reverse = !reverse;
		return waypoint;
	}
	
	/**
	 * Precomputes the shapes for more efficient drawing and manipulation.
	 * 
	 * @param scale the scale of the distances being drawn on the window
	 * @param widgetSize how large the shapes should be
	 * @param offx the x offset at which to create the shapes
	 * @param offy the y offset at which to create the shapes
	 * @param width the width of the display window
	 * @param height the height of the display window
	 * @param v_scale how much to scale the velocity vector
	 * @param a_scale how much to scale the acceleration vector
	 */
	public void recalculateShapes(
			double scale, double widgetSize,
			double offx, double offy,
			double width, double height,
			double v_scale, double a_scale) {

		double xpos = (x + offx) * scale + width/2;
		double ypos = -(y + offy) * scale + height/2;

		// Calculate primary point shape
		pointShape = new Ellipse2D.Double(
				(int)(xpos - widgetSize/2 + .5),
				(int)(ypos - widgetSize/2 + .5),
				widgetSize, widgetSize);

		// Calculate tangent point shape
		double diamondPoints[] = {
				0, -widgetSize/2,
				-widgetSize/2, 0,
				0, widgetSize/2,
				widgetSize/2, 0,
		};
		tangentShape = Waypoint.transformPolygon(
				diamondPoints, scale,
				x + offx, y + offy,
				width, height,
				v_t, v_m * v_scale);

		// Calculate the curvature point shape
		double trianglePoints[] = {
				0, widgetSize/2,
				-widgetSize/4*Math.sqrt(3), -widgetSize/4,
				widgetSize/4*Math.sqrt(3), -widgetSize/4,
		};
		curvatureShape = Waypoint.transformPolygon(
				trianglePoints, scale,
				x + offx, y + offy,
				width, height,
				v_t + a_t, a_m * a_scale);
	}
	
	/**
	 * Gets the shape indicating the xy position of the waypoint.
	 * @return a shape indicating the xy position
	 */
	public Shape getPointShape() {
		return pointShape;
	}
	
	/**
	 * Gets the shape indicating the velocity vector of the waypoint.
	 * @return a shape indicating the velocity vector
	 */
	public Shape getTangentShape() {
		return tangentShape;
	}
	
	/**
	 * Gets a shape indicating the acceleration vector of the waypoint.
	 * @return a shape indicating the acceleration vector
	 */
	public Shape getCurvatureShape() {
		return curvatureShape;
	}
	
	/**
	 * Creates a shape to use for drawing and manipulating from the parameters.
	 * 
	 * @param points the points of the polygon<br>
	 * The format is [x0, y0; x1, y1; ...]
	 * @param scale the scale of the distances being drawn on the window
	 * @param offsetx the x position of this polygon
	 * @param offsety the y position of this polygon
	 * @param width the width of the drawing window
	 * @param height the height of the drawing window
	 * @param rotation the direction of the vector being represented
	 * @param magnitude the magnitude of the vector being represented
	 * @return a shape for drawing and manipulation
	 */
	public static Polygon transformPolygon(
			double points[], double scale,
			double offsetx, double offsety,
			double width, double height,
			double rotation, double magnitude) {
		AffineTransform transform = new AffineTransform();
		transform.translate(
				offsetx * scale + width/2,
				-offsety * scale + height/2);
		transform.rotate(-rotation);
		transform.translate(0, -magnitude * scale);
		transform.transform(points, 0, points, 0, points.length/2);
		Polygon polygon = new Polygon();
		for(int j = 0;j < points.length/2;j++) {
			polygon.addPoint(
					(int)(points[j * 2] + .5),
					(int)(points[j * 2 + 1] + .5));
		}
		return polygon;
	}
}
