package engine.models;

import org.lwjgl.opengl.*;

public class VAO{
    int vaoID;
    int eboID;
    int vboPosID;
    int vboTexID;
    int indicesCount;


    public void bind(){

        GL30.glBindVertexArray(vaoID);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

    }
    public void render(){
        GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_INT, 0);
    }
    public void unbind(){
        GL30.glBindVertexArray(0);
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
    }

    public void clean() {
        GL30.glDeleteVertexArrays(vaoID);
        GL15.glDeleteBuffers(eboID);
        GL15.glDeleteBuffers(vboPosID);
        GL15.glDeleteBuffers(vboTexID);
    }

}
