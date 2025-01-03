package client.models;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.newdawn.slick.opengl.PNGDecoder;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.glTexSubImage3D;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.opengl.GL42.glTexStorage3D;
import static org.lwjgl.opengl.GL43.GL_TEXTURE_2D_ARRAY;

public class Loader {
	
	private static final List<VAO> vaos = new ArrayList<>();
	private static final List<Integer> textures = new ArrayList<>();

	public static int loadAtlas(){
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		Texture texture = null;
		try {
			texture = TextureLoader.getTexture("PNG",
					new FileInputStream("res/textures/atlas.png"));
		} catch (Exception e) {
			System.out.println("Tried to load texture atlas.png , didn't work");
			System.exit(-3);
		}
		textures.add(texture.getTextureID());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		return texture.getTextureID();
	}



	public static void loadTexture() {

		//create a texture
		int id = glGenTextures();

		//bind the texture
		glBindTexture(GL_TEXTURE_2D_ARRAY, id);

		int numSlices = 3;

		String[] filename = {"dirt", "stone", "grass"};
		glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, GL_RGBA8, 16, 16, numSlices);

		try {
			for (int i = 0; i < numSlices; i++) {
				PNGDecoder decoder = new PNGDecoder(new java.io.FileInputStream("res/textures/" + filename[i] + ".png"));
				ByteBuffer buffer = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
				decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.RGBA);
				buffer.flip();
				glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, 16, 16, 1, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
			}
		} catch (IOException e) {
			System.out.println("Tried to load texture array, didn't work");
			System.exit(-4);
		}
		//set the texture parameters, can be GL_LINEAR or GL_NEAREST
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST );
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST );

		glGenerateMipmap(GL_TEXTURE_2D_ARRAY);
		//upload texture
//		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

	}


	public static void cleanUp(){
		for(VAO vao:vaos){
			vao.clean();
		}
		for(int texture:textures){
			GL11.glDeleteTextures(texture);
		}
	}
	public static VAO createTempVAO(float[] positions, float[] textureCoords, int[] indices){

		int vaoID = GL30.glGenVertexArrays();

		GL30.glBindVertexArray(vaoID);

		VAO vao = new VAO(
				vaoID,
				bindIndicesBuffer(indices),
				storeDataInAttributeList(0,3,positions),
				storeDataInAttributeList(1,3,textureCoords),
				indices.length
		);

		GL30.glBindVertexArray(0);
		return vao;
	}
	public static VAO createVAO(float[] positions, float[] textureCoords, int[] indices){
		VAO vao = createTempVAO(positions, textureCoords, indices);
		vaos.add(vao);
		return vao;
	}
	@SuppressWarnings("SameParameterValue")
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
