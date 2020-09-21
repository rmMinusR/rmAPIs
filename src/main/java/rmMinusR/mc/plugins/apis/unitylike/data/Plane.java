package rmMinusR.mc.plugins.apis.unitylike.data;

import rmMinusR.mc.plugins.apis.unitylike.physics.Line;

/**
 * A plane, using the equation
 * ax + by + cz = d
 */
public final class Plane {

	public float d;
	public Vector3 normal;

	public Plane(Vector3 root, Vector3 normal) {
		this.normal = normal;
		/*
		 * Normal.XYZ -> ABC
		 * a(x-x0) + b(y-y0) + c(z-z0) = ax + by + cz - d
		 * ax-ax0 + by-by0 + cz-cz0 = ax + by + cz - d
		 * ax + by + cz - ax0 - by0 - cz0 = ax + by + cz - d
		 * - ax0 - by0 - cz0 = - d
		 * d = ax0 + by0 + cz0
		 * d = nx x0 + ny y0 + nz z0
		 */
		this.d = normal.x * root.x + normal.y * root.y + normal.z * root.z;
	}
	
	public Plane(Vector3 a, Vector3 b, Vector3 c) {
		this(a, Vector3.Cross(a-b, a-c));
	}
	
	public Vector3 GetIntercept(Line line) {
		/*
		 * ax + by + cz = d
		 * x = Lox + Ldx*t
		 * y = Loy + Ldy*t
		 * z = Loz + Ldz*t
		 * 
		 * a(Lox + Ldx*t) + b(Loy + Ldy*t) + c(Loz + Ldz*t) = d
		 * a Lox + at Ldx + b Loy + bt Ldy + c Loz + ct Ldz = d
		 * a Lox + b Loy + c Loz + at Ldx + bt Ldy + ct Ldz = d
		 * a Lox + b Loy + c Loz + t(a Ldx + b Ldy + ct Ldz) = d
		 * t(a Ldx + b Ldy + ct Ldz) = d - a Lox - b Loy - c Loz
		 * t = (d - a Lox - b Loy - c Loz)/(a Ldx + b Ldy + ct Ldz)
		 */
		
		return line.GetByT(
					  (d - normal.x*line.origin.x - normal.y*line.origin.y - normal.z*line.origin.z)/
					(normal.x*line.direction.x + normal.y*line.direction.y + normal.z*line.direction.z)
				);
	}
	
}
