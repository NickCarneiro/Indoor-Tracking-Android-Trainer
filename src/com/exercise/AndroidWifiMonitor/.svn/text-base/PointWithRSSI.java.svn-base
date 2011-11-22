package com.exercise.AndroidWifiMonitor;

import java.util.HashMap;

//each point has the coordinate stored along with a map corresponding to RSSI values at that point
public class PointWithRSSI {
	HashMap<String, AccessPoint> WeightedaccessPoints;		// the string is the ssid (unique for each access point)
	HashMap<String, Float> accessPoints;
	Point2D point;
	
	PointWithRSSI(String s) {
		if (s.equals("Weight")) {
			WeightedaccessPoints = new HashMap<String, AccessPoint> ();
		} else {
			accessPoints = new HashMap<String, Float> ();
		}
		point = null;
	}
	
	PointWithRSSI(AccessPoint ap) {
		WeightedaccessPoints = new HashMap<String, AccessPoint> ();
		WeightedaccessPoints.put(ap.getBssid(), ap);
		point = null;
	}

	PointWithRSSI(int x, int y, String s) {
		if (s.equals("Weight")) {
			WeightedaccessPoints = new HashMap<String, AccessPoint> ();
		} else {
			accessPoints = new HashMap<String, Float> ();
		}
		point = new Point2D(x,y);
	}
	
	PointWithRSSI(int x, int y, HashMap<String, AccessPoint> WeightedaccessPoints) {
		point = new Point2D(x,y);
		this.WeightedaccessPoints = WeightedaccessPoints;
	}
	
	PointWithRSSI(Point2D point, HashMap<String, Float> accessPoints) {
		this.point = point;
		this.accessPoints = accessPoints;
	}

	@Override public boolean equals (Object that) {
		if (this == that) return true;
		if (!(that instanceof PointWithRSSI)) return false;
		PointWithRSSI pr = (PointWithRSSI)that;
		if (pr.getPoint() == null) return false;
		return (pr.getPoint().equals(point));
	}
	
	public HashMap<String, Float> getAccessPoints() {
		return accessPoints;
	}

	public void setAccessPoints(HashMap<String, Float> accessPoints) {
		this.accessPoints = accessPoints;
	}

	public HashMap<String, AccessPoint> getWeightedaccessPoints() {
		return WeightedaccessPoints;
	}

	public void setWeightedaccessPoints(
			HashMap<String, AccessPoint> weightedaccessPoints) {
		WeightedaccessPoints = weightedaccessPoints;
	}

	public Point2D getPoint() {
		return point;
	}

	public void setPoint(Point2D point) {
		this.point = point;
	}

	public static class Point2D {
		private int x;
		private int y;
		
		Point2D(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override public boolean equals (Object that) {
			if (this == that) return true;
			if (!(that instanceof Point2D)) return false;
			Point2D p = (Point2D)that;
			return (p.getX() == x && p.getY() == y);
		}
		
		@Override public String toString() {
			return ("("+x+","+y+")");
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}
		
		
	}
}
