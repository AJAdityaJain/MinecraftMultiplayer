package engine.util;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;

@SuppressWarnings("SameReturnValue")
public class Maths {
	public final static Matrix4f matrix = new Matrix4f();
	private static final Vector3f X_AXIS = new Vector3f(1,0,0);
	private static final Vector3f Y_AXIS = new Vector3f(0,1,0);
	private static final Vector3f Z_AXIS = new Vector3f(0,0,1);



	public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry,
			float rz, float scale) {
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.rotate(rx, X_AXIS, matrix, matrix);
		Matrix4f.rotate(ry, Y_AXIS, matrix, matrix);
		Matrix4f.rotate(rz, Z_AXIS, matrix, matrix);
		Matrix4f.scale(new Vector3f(scale,scale,scale), matrix, matrix);
		return matrix;
	}
	
	public static Matrix4f createViewMatrix(Camera camera) {
		matrix.setIdentity();
		Matrix4f.rotate(camera.getPitch(), X_AXIS, matrix,
				matrix);
		Matrix4f.rotate(camera.getYaw(), Y_AXIS, matrix, matrix);
		Matrix4f.translate(camera.getPosition().negate(null), matrix, matrix);
		return matrix;
	}

}
