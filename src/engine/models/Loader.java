package engine.models;

import java.io.FileInputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import static org.lwjgl.opengl.GL11.*;

public class Loader {
	
	private static final List<VAO> vaos = new ArrayList<>();
	private static final List<Integer> textures = new ArrayList<>();

	public static int loadTexture(String fileName) {
		Texture texture = null;
		try {
			texture = TextureLoader.getTexture("PNG",
					new FileInputStream("res/textures/" + fileName + ".png"));
		} catch (Exception e) {
			System.out.println("Tried to load texture " + fileName + ".png , didn't work");
			System.exit(-1);
		}
		textures.add(texture.getTextureID());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		return texture.getTextureID();
	}


	public static void cleanUp(){
		for(VAO vao:vaos){
			vao.clean();
		}
		for(int texture:textures){
			GL11.glDeleteTextures(texture);
		}
	}

	public static VAO createVAO(float[] positions, float[] textureCoords, int[] indices){
		VAO vao = new VAO();

		int vaoID = GL30.glGenVertexArrays();
		vao.vaoID = vaoID;
		GL30.glBindVertexArray(vaoID);

		vao.eboID = bindIndicesBuffer(indices);
		vao.vboPosID = storeDataInAttributeList(0,3,positions);
		vao.vboTexID = storeDataInAttributeList(1,2,textureCoords);
		vao.indicesCount = indices.length;

		GL30.glBindVertexArray(0);
		vaos.add(vao);

		return vao;
	}

	private static int storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data){
		int vboID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);

		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);buffer.flip();

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber,coordinateSize,GL11.GL_FLOAT,false,0,0);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return vboID;
	}

	private static int bindIndicesBuffer(int[] indices){
		int eboID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID);

		IntBuffer buffer = BufferUtils.createIntBuffer(indices.length);
		buffer.put(indices);buffer.flip();

		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		return eboID;
	}
}
