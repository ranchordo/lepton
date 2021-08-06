## Lepton  
Lepton is an LWJGL-based java library for optimization, GPU parallel computing, 3d rendering, and other features you would expect in a low-level game engine, like a physics engine (based on [JBullet](http://jbullet.advel.cz)), audio rendering, 3d animation, a wavefront (.obj) geometry loader, and much more.  
  
A quick note on expectations: Lepton is not a game engine in the way that Unity or Unreal Engine are game engines. Lepton is a library for providing a higher-level interface with OpenGL's, OpenAL's, and JBullet's functionality by managing their features through higher-level classes with more intuitive functionality and by making connections between them easier. Lepton also provides some utilities for optimization and parallel computing.  
  
A javadoc for Lepton has been provided in /doc/index.html, and is hosted on github pages [here](https://ranchordo.github.io/lepton/).  
  
The license for LWJGL has been included as /LWJGL-LICENSE.txt because LWJGL binaries are included in the dist jar. No source code from LWJGL has been included. LWJGL-LICENSE is not the license for this project in its entirety. For a license pertaining to Lepton's source code, see /LICENSE.  
  
The LWJGL components required for Lepton are:  
1. LWJGL core  
2. LWJGL-OpenGL  
3. LWJGL-GLFW  
4. LWJGL-OpenAL  
5. LWJGL-STB  
  
These LWJGL components are packaged along with the jarfile found in releases and in /dist.  
  
Lepton will most likely leave beta when [Space - A Portal Story](https://ranchordo.github.io/space-a-portal-story), a project based upon Lepton, releases.