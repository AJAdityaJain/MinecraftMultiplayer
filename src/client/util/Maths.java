package client.util;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;

@SuppressWarnings("SameReturnValue")
public class Maths {
	public static final float TAU = (float) (Math.PI*2);
	public static final Matrix4f identity = new Matrix4f();

	private static final Matrix4f matrix = new Matrix4f();
	private static final Vector3f X_AXIS = new Vector3f(1,0,0);
	private static final Vector3f Y_AXIS = new Vector3f(0,1,0);
	private static final Vector3f Z_AXIS = new Vector3f(0,0,1);




	public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry,
													  float rz, Vector3f scale) {
		matrix.setIdentity();
		return matrix.translate(translation).rotate(rx, X_AXIS).rotate(ry, Y_AXIS).rotate(rz, Z_AXIS).scale(scale);
	}

	public static Matrix4f createSimpleTransformationMatrix(Vector3f translation) {
		matrix.setIdentity();
		return matrix.translate(translation);
	}

	public static Matrix4f createViewMatrix(Camera camera) {
		matrix.setIdentity();
		return matrix.rotate(camera.getRotX(),X_AXIS).rotate(camera.getRotY(),Y_AXIS).translate(camera.getPosition().negate(null));
	}

}
