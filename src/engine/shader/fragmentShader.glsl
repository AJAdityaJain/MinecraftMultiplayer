#version 330 core

in vec3 uv;
in vec3 xyz;

out vec4 out_Color;

uniform vec3 lightDirection = vec3(0.7, 0.9, 0.5); // Light direction (normalize it)
uniform vec3 ambientColor = vec3(0.3, 0.3, 0.3);


uniform sampler2DArray tex_arr;

void main(void){
	vec4 texColor = texture(tex_arr, uv);

    float diffuse = max(0.0, dot(normalize(cross(dFdx(xyz), dFdy(xyz))), lightDirection)); // Diffuse factor

    out_Color = vec4((ambientColor + diffuse) * texColor.rgb, texColor.a);
}