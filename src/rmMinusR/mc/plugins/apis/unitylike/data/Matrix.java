package rmMinusR.mc.plugins.apis.unitylike.data;

/**
 * SQUARE matrix.
 */
public final class Matrix implements Cloneable {
	
	//Fields
	
	/**
	 * Format x,y
	 * 
	 * 4x4 format
	 * 
	 * S R R T
	 * R S R T
	 * R R S T
	 * . . . .
	 */
	protected float[][] m;
	
	public final int size;

	//"Constructors"
	//See https://www.brainvoyager.com/bv/doc/UsersGuide/CoordsAndTransforms/SpatialTransformationMatrices.html
	
	public static Matrix Identity(int size) {
		Matrix out = new Matrix(size);
		for(int i = 0; i < size; i++) out.m[i][i] = 1;
		return out;
	}
	
	public static Matrix Zero(int size) { return new Matrix(size); }
	
	public static Matrix FromArray(float... in) {
		int size = (int) Math.sqrt(in.length);
		if(size*size != in.length) throw new IllegalArgumentException("Must have a square input array!");
		
		Matrix out = new Matrix(size);
		for(int i = 0; i < in.length; i++) out.m[i%in.length][i/in.length] = in[i];
		
		return out;
	}
	
	public static Matrix Translate(Vector3 vec) {
		Matrix out = Identity(4);
		out.m[3][0] = vec.x;
		out.m[3][1] = vec.y;
		out.m[3][2] = vec.z;
		return out;
	}
	
	public static Matrix Scale(float scl) {
		Matrix out = Identity(4);
		out.m[0][0] = scl;
		out.m[1][1] = scl;
		out.m[2][2] = scl;
		return out;
	}
	
	public static Matrix RotateX(float amt) {
		Matrix out = Identity(4);
		out.m[1][1] = (float) Math.cos(amt);
		out.m[2][1] = (float) Math.sin(amt);
		out.m[1][2] = (float)-Math.sin(amt);
		out.m[2][2] = (float) Math.cos(amt);
		return out;
	}
	
	public static Matrix RotateY(float amt) {
		Matrix out = Identity(4);
		out.m[0][0] = (float) Math.cos(amt);
		out.m[2][0] = (float)-Math.sin(amt);
		out.m[0][2] = (float) Math.sin(amt);
		out.m[2][2] = (float) Math.cos(amt);
		return out;
	}
	
	public static Matrix RotateZ(float amt) {
		Matrix out = Identity(4);
		out.m[0][0] = (float) Math.cos(amt);
		out.m[1][0] = (float)-Math.sin(amt);
		out.m[0][1] = (float) Math.sin(amt);
		out.m[1][1] = (float) Math.cos(amt);
		return out;
	}
	
	//Private ctor
	private Matrix(int size) {
		if(size < 1) throw new IllegalArgumentException("Matrix minimum size is 1");
		
		this.size = size;
		m = new float[size][];
		for(int i = 0; i < size; i++) {
			m[i] = new float[size];
			for(int j = 0; j < size; j++) m[i][j] = 0;
		}
	}
	
	//Data IO
	@Override
	protected Matrix clone() {
		Matrix out = new Matrix(size);
		for(int i = 0; i < size; i++) for(int j = 0; j < size; j++) out.m[i][j] = m[i][j];
		return out;
	}
	
	public Matrix Resize(int sz) {
		Matrix out = Identity(sz);
		
		for(int i = 0; i < out.size; i++) for(int j = 0; j < out.size; j++) out.m[i][j] = this.m[i][j];
		
		return out;
	}
	
	/**
	 * Should almost never be called!
	 */
	protected void CopyFrom(Matrix other) {
		for(int i = 0; i < size; i++) for(int j = 0; j < size; j++) m[i][j] = other.m[i][j];
	}
	
	public Vector3 TransformPoint(Vector3 point) {
		if(size != 4) throw new IllegalStateException("Can only transform by a 4x4 matrix");
		
		//See https://www.euclideanspace.com/maths/algebra/matrix/transforms/index.htm
		//Corrected math's YX -> code's XY index notation
		return new Vector3(
					m[0][0]*point.x + m[1][0]*point.y + m[2][0]*point.z + m[3][0],
					m[0][1]*point.x + m[1][1]*point.y + m[2][1]*point.z + m[3][1],
					m[0][2]*point.x + m[1][2]*point.y + m[2][2]*point.z + m[3][2]
				);
	}
	
	public Vector3 TransformVector(Vector3 vector) {
		if(size != 4) throw new IllegalStateException("Can only transform by a 4x4 matrix");
		
		//See https://www.euclideanspace.com/maths/algebra/matrix/transforms/index.htm
		//Corrected math's YX -> code's XY index notation
		return new Vector3(
				m[0][0]*vector.x + m[1][0]*vector.y + m[2][0]*vector.z,
				m[0][1]*vector.x + m[1][1]*vector.y + m[2][1]*vector.z,
				m[0][2]*vector.x + m[1][2]*vector.y + m[2][2]*vector.z
			);
	}
	
	//Matrix maths: Basic
	public Matrix Add(Matrix other) { return Add(this, other); }
	public static Matrix Add(Matrix a, Matrix b) {
		Matrix out = new Matrix(a.size);
		
		for(int i = 0; i < a.size; i++) for(int j = 0; j < a.size; j++) {
			out.m[i][j] = a.m[i][j] + b.m[i][j];
		}
		
		return out;
	}
	
	public Matrix Sub(Matrix other) { return Sub(this, other); }
	public static Matrix Sub(Matrix a, Matrix b) {
		Matrix out = new Matrix(a.size);
		
		for(int i = 0; i < a.size; i++) for(int j = 0; j < a.size; j++) {
			out.m[i][j] = a.m[i][j] - b.m[i][j];
		}
		
		return out;
	}
	
	public Matrix Mul(float x) {
		Matrix out = new Matrix(size);
		for(int i = 0; i < size; i++) for(int j = 0; j < size; j++) out.m[i][j] = m[i][j] * x;
		return out;
	}
	
	public Matrix Mul(Matrix other) { return Mul(this, other); }
	public static Matrix Mul(Matrix a, Matrix... etc) {
		if(etc.length == 0) {
			return a;
		} else if(etc.length == 1) {
			Matrix b = etc[0];
			Matrix out = new Matrix(a.size);
			
			for(int i = 0; i < a.size; i++) for(int j = 0; j < a.size; j++) {
				out.m[i][j] = RowColDot(a, b, i, j);
			}
			
			return out;
		} else {
			Matrix[] etc2 = new Matrix[etc.length-1];
			for(int i = 0; i < etc2.length; i++) etc2[i] = etc[i+1];
			return Mul(a, Mul(etc[0], etc2));
		}
	}
	
	private static float RowColDot(Matrix a, Matrix b, int x, int y) {
		if(a.size != b.size) throw new IllegalArgumentException("Size inequality: "+a.size+" != "+b.size);
		if(x < 0 || x >= a.size) throw new IllegalArgumentException("x: 0 <= "+x+" < size("+a.size+")");
		if(y < 0 || y >= b.size) throw new IllegalArgumentException("y: 0 <= "+y+" < size("+a.size+")");
		
		float out = 0;
		
		for(int i = 0; i < a.size; i++) out += a.m[i][y] * b.m[x][i];
		
		return out;
	}
	
	public Matrix Transpose() {
		Matrix out = new Matrix(size);
		for(int i = 0; i < size; i++) for(int j = 0; j < size; j++) out.m[j][i] = m[i][j];
		return out;
	}
	
	//Matrix maths: Advanced
	public Matrix Minors() {
		Matrix out = new Matrix(size);
		for(int i = 0; i < size; i++) for(int j = 0; j < size; j++) out.m[i][j] = DropXY(i, j).GetDeterminant();
		return out;
	}
	
	public Matrix CheckerboardSign() {
		Matrix out = new Matrix(size);
		for(int i = 0; i < size; i++) for(int j = 0; j < size; j++) out.m[i][j] = m[i][j] * (i%2==0 ? 1 : -1) * (j%2==0 ? 1 : -1);
		return out;
	}
	
	public Matrix Cofactor() { return Minors().CheckerboardSign(); }
	
	public Matrix Adjugate() { return Cofactor().Transpose(); }
	
	public Matrix Inverse() {
		float det = GetDeterminant();
		if(det == 0) throw new IllegalStateException("This matrix has no inverse!");
		
		Matrix adj = Adjugate();
		Matrix out = new Matrix(size);
		for(int i = 0; i < size; i++) for(int j = 0; j < size; j++) out.m[i][j] = adj.m[i][j] / det;
		return out;
	}
	
	//-- minor: Pseudoslicing helper function for calculating determinant
	private Matrix DropXY(int x, int y) {
		if(x < 0 || x >= size) throw new IllegalArgumentException("x: 0 <= "+x+" < size("+size+")");
		if(y < 0 || y >= size) throw new IllegalArgumentException("y: 0 <= "+y+" < size("+size+")");
		
		Matrix out = new Matrix(size-1);
		for(int ix = 0; ix < size; ix++) for(int iy = 0; iy < size; iy++) {
			
			if(ix != x && iy != y) out.m[ix<=x ? ix : ix-1][iy<=y ? iy : iy-1] = this.m[ix][iy];
			
		}
		return out;
	}
	
	public float GetDeterminant() {
		if(size == 1) {
			//Is this even a matrix?
			return m[0][0];
		} else if(size == 2) {
			//ad-bc
			return m[0][0]*m[1][1] - m[0][1]*m[1][0];
		} else {
			//Recurse
			float sum = 0;
			for(int i = 0; i < size; i++) sum += DropXY(0, i).GetDeterminant() * (i%2==0 ? 1 : -1);
			return sum;
		}
	}

	public Matrix Reortho() {
		/**
		 * http://www.euclideanspace.com/maths/algebra/matrix/orthogonal/reorthogonalising/index.htm
		 * Section "Deriving Correction Matrix"
		 * 
		 * For non-ortho matrix [M], and ortho matrix [O], there exists
		 * a correctional matrix [C] such that [O] = [C]*[M]
		 * 
		 * Following the long equation yields
		 * [C] = (3[I]-([M] * [M]T))/2
		 */
		
		//            (       [I] * 3     -    ( [M] * [M]T           )   ) / 2
		Matrix c = Sub(Identity(4).Mul(3),  Mul(this, this.Transpose())   ) .Mul(0.5f);
		
		return Mul(c, this);
	}
}
