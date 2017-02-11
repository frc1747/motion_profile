package lib.frc1747.motion_profile.generator._2d;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

public class Waypoint {
	public double x;
	public double y;
	public double v_t;
	public double v_m;
	public double a_t;
	public double a_m;
	
	public boolean reverse;
	
	private Shape pointShape;
	private Shape tangentShape;
	private Shape curvatureShape;
	
	public Waypoint() {
		x = 0;
		y = 0;
		v_t = 0;
		v_m = 0;
		a_t = 0;
		a_m = 0;
		reverse = false;
	}
	
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
	
	public Shape getPointShape() {
		return pointShape;
	}
	
	public Shape getTangentShape() {
		return tangentShape;
	}
	
	public Shape getCurvatureShape() {
		return curvatureShape;
	}
	
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
		Polygon diamond = new Polygon();
		for(int j = 0;j < points.length/2;j++) {
			diamond.addPoint(
					(int)(points[j * 2] + .5),
					(int)(points[j * 2 + 1] + .5));
		}
		return diamond;
	}
}
