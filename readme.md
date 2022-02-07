## Lepton: A low-level graphics and utility framework  
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ranchordo/lepton.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.ranchordo%22%20AND%20a:%22lepton%22)  
Lepton is an LWJGL-based java library for optimization, GPU parallel computing, 3d rendering, and other features you would expect in a low-level game engine, like a physics engine (based on [JBullet](http://jbullet.advel.cz)), audio rendering, 3d animation, a wavefront (.obj) geometry loader, and much more.  
  
A quick note on expectations: Lepton is not a game engine in the way that Unity or Unreal Engine are game engines. Lepton is a library for providing a higher-level interface with OpenGL's, OpenAL's, and JBullet's functionality by managing their features through higher-level classes with more intuitive functionality and by making connections between them easier. Lepton also provides some utilities for optimization and parallel computing.  
  
A javadoc for Lepton has been provided in /docs/index.html, and is hosted on github pages [here](https://ranchordo.github.io/lepton/).  
  
For a quick test of a lepton build or your system or java configuration, you can run the jar included in dist directly, either by double-clicking or by using `java -jar target/lepton-shaded-VERSION.jar`. This should open a JFrame-based test terminal in which you can access several test configurations. Further information is available in the terminal's help menu. If you open up `target/lepton-VERSION.jar`, this jarfile will not include the correct dependencies to run standalone. If manually incorporating lepton from the jarfile, use the shaded version to get the required dependencies.  
  
The license for LWJGL has been included as /LWJGL-LICENSE.txt because LWJGL binaries are included in the shaded dist jar. No source code from LWJGL has been included. LWJGL-LICENSE is not the license for this project in its entirety. For a license pertaining to Lepton's source code, see /LICENSE.  
  
The LWJGL components required for Lepton are:  
1. LWJGL core  
2. LWJGL-OpenGL  
3. LWJGL-GLFW  
4. LWJGL-OpenAL  
5. LWJGL-STB  
  
These LWJGL components are packaged along with the jarfile found in releases and in /dist.  
All other dependencies can be found in pom.xml.  
  
Lepton is now available in the Nexus central repository! You can include it with Maven by adding  
```xml
<dependency>
  <groupId>io.github.ranchordo</groupId>
  <artifactId>lepton</artifactId>
  <version>1.0.2</version>
</dependency>
```
into your pom.xml in the `dependencies` block, or gradle by adding  
`implementation 'io.github.ranchordo:lepton:1.0.2'`  
As a dependency, or to many other build managers by clicking on the Maven Central badge and then clicking on the package and getting the appropriate addition syntax.
