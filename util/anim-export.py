import bpy
if(len(bpy.context.selected_objects)!=1):
    print("Select only one object.")
    raise Exception("Select only one object.")
o=bpy.context.selected_objects[0]
with open("[FILENAME].ani","w+") as f:
    f.write("fps "+str(bpy.context.scene.render.fps)+"\n")
    for i in range(0,70):
        bpy.data.scenes['Scene'].frame_set(i+1)
        s="f "+str(i)+" "
        a=o.matrix_world.to_translation()
        b=o.matrix_world.to_quaternion() 
        c=o.scale
        s=s+str(a.x)+" "+str(a.y)+" "+str(a.z)+" "
        s=s+str(b.x)+" "+str(b.y)+" "+str(b.z)+" "+str(b.w)+" "
        if (abs(c.x-c.y)>0.01) or (abs(c.y-c.z)>0.01):
            print(c)
            raise Exception("Make sure scale is all uniform.")
        s=s+str(c.x)+"\n"
        f.write(s)
