package entities;

import client.rendering.DisplayManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import static client.util.Maths.TAU;

public class Camera {
	
	private final Vector3f position = new Vector3f(0,16,0);
	private float pitch;
	private float yaw = 160 * 3.14f/180;

	private boolean inGame = true;

	public Camera(){}
	
	public void move(){
		if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
			position.x+= 0.01f * (float) Math.sin(yaw);
			position.z-= 0.01f * (float) Math.cos(yaw);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
			position.x-= 0.01f * (float) Math.sin(yaw);
			position.z+= 0.01f * (float) Math.cos(yaw);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
			position.x-= 0.01f * (float) Math.cos(yaw);
			position.z-= 0.01f * (float) Math.sin(yaw);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D)){
			position.x+= 0.01f * (float) Math.cos(yaw);
			position.z+= 0.01f * (float) Math.sin(yaw);
		}

		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
			position.y+=0.02f;

		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			position.y-=0.02f;

		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			inGame = !inGame;

		if(inGame) {
			int x = Mouse.getX()- DisplayManager.WIDTH_HALF;
			int y = Mouse.getY()- DisplayManager.HEIGHT_HALF;

			yaw+=x*0.001f;
			pitch-=y*0.001f;
			pitch = Math.clamp(pitch, -1.5f, 1.5f);
			yaw %= TAU;

			Mouse.setCursorPosition(DisplayManager.WIDTH_HALF, DisplayManager.HEIGHT_HALF);
		}
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

}
