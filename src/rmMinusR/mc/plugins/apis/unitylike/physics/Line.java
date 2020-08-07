package rmMinusR.mc.plugins.apis.unitylike.physics;

import rmMinusR.mc.plugins.apis.unitylike.data.Plane;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public final class Line implements Cloneable {
	
	public Vector3 origin;
	public Vector3 direction;
	
	public Line(Vector3 origin, Vector3 direction) {
		this.origin = origin;
		this.direction = direction;
	}
	
	public Line AdvanceByDistance(float dist) {
		return new Line(GetByDistance(dist), direction);
	}
	
	public Line AdvanceByT(float t) {
		return new Line(GetByT(t), direction);
	}
	
	public Vector3 GetByDistance(float dist) {
		return origin.Add(direction.WithMagnitude(dist));
	}
	
	public Vector3 GetByT(float t) {
		//v = o + d*t
		return origin.Add(direction.Mul(t));
	}
	
	@Override
	public Line clone() {
		return new Line(origin.clone(), direction.clone());
	}
	
	@Override
	public String toString() {
		return "Line(origin="+origin.toString()+", direction="+direction.toString()+")";
	}
	
	public Vector3 GetClosestPoint(Vector3 point) {
		return new Plane(point, direction).GetIntercept(this);
	}
	
	public float GetTAt(Vector3 point) {
		return GetClosestPoint(point).Distance(origin) / direction.GetMagnitude();
	}
	
}
