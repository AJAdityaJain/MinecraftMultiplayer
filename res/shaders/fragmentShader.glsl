#version 330 core

in vec3 uv;

out vec4 out_Color;

uniform vec3 lightDirection = vec3(0.7, 0.9, 0.5); // Light direction (normalize it)

uniform sampler2DArray tex_arr;

void main(void){
    out_Color = texture(tex_arr, uv);
}