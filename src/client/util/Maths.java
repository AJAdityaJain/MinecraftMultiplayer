package client.util;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import server.Map;
import server.block.BlockState;

import static java.lang.Math.*;

@SuppressWarnings("SameReturnValue")
public class Maths {
	public static final float TAU = (float) (Math.PI*2);
	public static final Matrix4f identity = new Matrix4f();

	private static final Matrix4f matrix = new Matrix4f();
	private static final Vector3f X_AXIS = new Vector3f(1,0,0);
	private static final Vector3f Y_AXIS = new Vector3f(0,1,0);
	private static final Vector3f Z_AXIS = new Vector3f(0,0,1);

	public static BlockState drawLine(Map world, Vector3f hit, float x, float y, float z, float pitch, float yaw, float length, boolean firstHit) {

		int lx = (int) x;
		int ly = (int) y;
		int lz = (int) z;


		float dx = (float) (-cos(pitch) * sin(yaw));
		float dy = (float) (sin(pitch));
		float dz = (float) (-cos(pitch) * cos(yaw));

		int voxelX = (int) floor(x);
		int voxelY = (int) floor(y);
		int voxelZ = (int) floor(z);

		int stepX = (dx > 0) ? 1 : -1;
		int stepY = (dy > 0) ? 1 : -1;
		int stepZ = (dz > 0) ? 1 : -1;

		float deltaDistX = abs(1 / dx);
		float deltaDistY = abs(1 / dy);
		float deltaDistZ = abs(1 / dz);

		float tMaxX = (float) ((stepX > 0) ? (floor(x) + 1 - x) * deltaDistX : (x - floor(x)) * deltaDistX);
		float tMaxY = (float) ((stepY > 0) ? (floor(y) + 1 - y) * deltaDistY : (y - floor(y)) * deltaDistY);
		float tMaxZ = (float) ((stepZ > 0) ? (floor(z) + 1 - z) * deltaDistZ : (z - floor(z)) * deltaDistZ);

		for (int i = 0; i < length; i++) {
			if (tMaxX < tMaxY && tMaxX < tMaxZ) {
				voxelX += stepX;
				tMaxX += deltaDistX;
			} else if (tMaxY < tMaxZ) {
				voxelY += stepY;
				tMaxY += deltaDistY;
			} else {
				voxelZ += stepZ;
				tMaxZ += deltaDistZ;
			}

			if (!world.isAir(voxelX, voxelY, voxelZ)) {
				if (!firstHit) {
					hit.x = lx;
					hit.y = ly;
					hit.z = lz;
					return world.getBlock(lx,ly,lz);

				}
				else{
					hit.x = voxelX;
					hit.y = voxelY;
					hit.z = voxelZ;
					return world.getBlock(voxelX, voxelY, voxelZ);
				}
			}
			else{
				lx = voxelX;
				ly = voxelY;
				lz = voxelZ;

			}
		}
		return null;
	}


	public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry,
													  float rz, Vector3f scale) {
		matrix.setIdentity();
		return matrix.translate(translation).rotate(rz, Z_AXIS).rotate(ry, Y_AXIS).rotate(rx, X_AXIS).scale(scale);
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
