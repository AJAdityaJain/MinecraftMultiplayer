package entities;

import client.rendering.DisplayManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import static client.util.Maths.TAU;

public class Camera extends DynamicEntity{

	private static final float ANG_SPEED = 1f;
	private boolean inGame = true;
	private final Vector3f camVelocity = new Vector3f();

	@Override
	public Vector3f getVelocity() {
		Vector3f vel = super.getVelocity();
		camVelocity.y += vel.y;
		camVelocity.x += vel.x;
		camVelocity.z += vel.z;
		return camVelocity;
	}

	public Camera(){
        super(null, new Vector3f(12,14f,8), new Vector3f(1,2f,1), 0, 160 * 3.14f/180, 0, 0,.02f);
    }

	public void move(float delta_time){
		camVelocity.x=0f;
		camVelocity.y=0f;
		camVelocity.z=0f;

		if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
			camVelocity.x+= speed * (float) Math.sin(rotY);
			camVelocity.z-= speed * (float) Math.cos(rotY);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
			camVelocity.x-= speed * (float) Math.sin(rotY);
			camVelocity.z+= speed * (float) Math.cos(rotY);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
			camVelocity.x-= speed * (float) Math.cos(rotY);
			camVelocity.z-= speed * (float) Math.sin(rotY);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D)){
			camVelocity.x+= speed * (float) Math.cos(rotY);
			camVelocity.z+= speed * (float) Math.sin(rotY);
		}
		if(onGround && Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			onGround = false;
			velocity.y+= speed*1.6f;
		}


		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			inGame = !inGame;

		if(inGame) {
			int x = Mouse.getX()- DisplayManager.WIDTH_HALF;
			int y = Mouse.getY()- DisplayManager.HEIGHT_HALF;

			rotY+=x*ANG_SPEED*delta_time;
			rotX-=y*ANG_SPEED*delta_time;
			rotX = Math.clamp(rotX, -1.5f, 1.5f);
			rotY %= TAU;

			Mouse.setCursorPosition(DisplayManager.WIDTH_HALF, DisplayManager.HEIGHT_HALF);
		}
	}

}
