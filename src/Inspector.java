/*
@author Nathan Chua

The purpose of this program is to execute complete introspection of an object at runtime.
This class is used to invoke inspect() which lists information about the object:
    Name of declaring class
    Name of super-class (and information from it's inheritance hierarchy)
    Name of Interface (and information from it's inheritance hierarchy)
    Information about Constructor(s)
    Information about Methods
    Information about Fields
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Inspector {

    public void inspect(Object obj, boolean recursive) {
        Class c = obj.getClass();
        inspectClass(c, obj, recursive, 0);
    }

    /*
    method to inspect declaring class.
    */
    private void inspectClass(Class c, Object obj, boolean recursive, int depth) {
     format("CLASS NAME: " + c.getName(), depth);

     if(c.getSuperclass() != null)
         inspectSuperclass(c, obj, depth+1);

  //   inspectInterfaces(c, obj, recursive, depth+1); //FIX INF LOOP
     inspectConstructors(c, depth+1);
     inspectMethods(c, depth+1);

    }

    /*
    method to inspect superclass: always fully explores inheritance hierarchy.
    */
    private void inspectSuperclass(Class child, Object parent, int depth) {
        Class superclass = child.getSuperclass();
        format("SUPERCLASS NAME: " + superclass, depth);
        inspectClass(superclass, parent, true,depth+1);
    }

    /*
    method to inspect implemented interface(s): always fully explores inheritance hierarchy.
    */
    private void inspectInterfaces(Class c, Object obj, int depth) {
        Class [] interfaces = c.getInterfaces();
        for (Class iFace : interfaces){
            format("INTERFACE NAME: " + iFace, depth);
            inspectClass(c, obj, true,depth+1);
        }
    }

    /*
    method to inspect constructors: prints constructor name, parameters (if any), and modifiers.
     */
    private void inspectConstructors(Class c, int depth) {
        Constructor[] cArray = c.getDeclaredConstructors();     //array of class constructors

        for(Constructor constructor : cArray){
            format("CONSTRUCTOR NAME: " + constructor.getName(), depth);

            Class[] params = constructor.getParameterTypes();   //array of parameter types
            if(params.length > 0) {
                format("- PARAMETER TYPES: ", depth+1);
                for(Class param : params)
                    format(param.getName(), depth+2);
            }
            format("- MODIFIER: " + Modifier.toString(constructor.getModifiers()), depth+1);
        }
    }

    /*
    method to inspect methods: prints method name, exceptions (if any), parameters (if any), return type, and modifiers.
     */
    private void inspectMethods(Class c, int depth) {
        Method[] methods = c.getDeclaredMethods();  //gets all methods of any visibility in Class "c"

        for(Method method : methods){
            format("METHOD NAME: " + method.getName(), depth);

            Class[] exceptions = method.getExceptionTypes();
            if(exceptions.length > 0) {
                format("- EXCEPTIONS THROWN: ", depth+1);
                for(Class exception : exceptions){
                    format(exception.getName(), depth+2);
                }
            }

            Class[] params = method.getParameterTypes();   //array of parameter types
            if(params.length > 0) {
                format("- PARAMETER TYPES: ", depth+1);
                for(Class param : params)
                    format(param.getName(), depth+2);
            }

            format("- RETURN TYPE: " + method.getReturnType(), depth+1);
            format("- MODIFIER: " + Modifier.toString(method.getModifiers()), depth+1);
        }
    }

    /*
    method to format indentation for output.
     */
    private void format(String str, int depth){
        for(int i = 0; i < depth; i++)
            System.out.print("   "); //I used 3 spaces, indentation using tab not as clean
        System.out.println(str);
    }
}
