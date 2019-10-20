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

Certain classes will be visited multiple times because the program must ALWAYS fully traverse
inheritance hierarchy. This behavior is expected.
 */

import java.lang.reflect.*;

public class Inspector {

    public void inspect(Object obj, boolean recursive) {
        Class c = obj.getClass();
        inspectClass(c, obj, recursive, 0);
    }

    /*
    method to inspect declaring class.
    */
    private void inspectClass(Class c, Object obj, boolean recursive, int depth) {

        format("CLASS: " + c.getSimpleName(), depth);

        if(c.getSuperclass() != null)                               //check if class c has superclass
            inspectSuperclass(c, obj, recursive, depth+1);    //if yes, inspect extending superclass

        if(c.getInterfaces() != null)                               //check if class c has interface
            inspectInterfaces(c, obj, recursive, depth+1);    //if yes, inspect implementing interface

        inspectConstructors(c, depth+1);
        inspectMethods(c, depth+1);
        inspectFields(c, obj, recursive, depth+1);
        System.out.println();                                       //used to seperate class information
    }

    /*
    method to inspect superclass: always fully explores inheritance hierarchy.
    */
    private void inspectSuperclass(Class child, Object obj, boolean recursive, int depth) {
        Class superclass = child.getSuperclass();
        format("SUPERCLASS: " + superclass.getSimpleName(), depth);
        inspectClass(superclass, obj, recursive,depth+1);
    }

    /*
    method to inspect implemented interface(s): always fully explores inheritance hierarchy.
    */
    private void inspectInterfaces(Class c, Object obj, boolean recursive, int depth) {
        Class [] iArray= c.getInterfaces();         //array of class c's implementing interfaces
        for (Class interFace : iArray){
            format("INTERFACE: " + interFace.getSimpleName(), depth);
            inspectClass(interFace, obj, recursive,depth+1);
        }
    }

    /*
    method to inspect constructors: prints constructor name, parameters (if any), and modifiers.
     */
    private void inspectConstructors(Class c, int depth) {
        Constructor[] cArray = c.getConstructors();                 //array of class c's constructors

        if(cArray.length > 0) {                                     //ensure that at least one constructor exists
            for(Constructor constructor : cArray){
                format("CONSTRUCTOR: ", depth);
                format("- NAME: " + constructor.getName(), depth+1);

                Class[] pArray = constructor.getParameterTypes();   //array of constructor parameter types
                if(pArray.length > 0) {
                    format("- PARAMETER TYPES: ", depth+1);
                    for(Class param : pArray)
                        format(param.getSimpleName(), depth+2);
                }
                format("- MODIFIERS: " + Modifier.toString(constructor.getModifiers()), depth+1);
            }
        }
    }

    /*
    method to inspect methods: prints method name, exceptions (if any), parameters (if any), return type, and modifiers.
     */
    private void inspectMethods(Class c, int depth) {
        Method[] methods = c.getMethods();                             //gets all methods of any visibility in Class "c"

        if(methods.length > 0){                                        //ensure that at least one method exists
            for(Method method : methods){
                format("METHOD: ", depth);
                format("- NAME: " + method.getName(), depth+1);

                Class[] exceptions = method.getExceptionTypes();            //array of the method's exception types
                if(exceptions.length > 0) {
                    format("- EXCEPTIONS THROWN: ", depth+1);
                    for(Class exception : exceptions)
                        format(exception.getSimpleName(), depth+2);
                }

                Class[] params = method.getParameterTypes();                //array of method's parameter types
                if(params.length > 0) {
                    format("- PARAMETER TYPES: ", depth+1);
                    for(Class param : params)
                        format(param.getSimpleName(), depth+2);
                }

                format("- RETURN TYPE: " + method.getReturnType(), depth+1);
                format("- MODIFIERS: " + Modifier.toString(method.getModifiers()), depth+1);
            }
        }
    }

    /*
    method to inspect fields: prints field name, field type, modifiers, and current value of the field
    this method can be enabled for recursive introspection; if recursive is true, then field will be
    inspected in the same manner as an object.
    */
    private void inspectFields(Class c, Object obj, boolean recursive, int depth) {
        Field[] fields = c.getDeclaredFields();

        if(fields.length > 0){                                                   //ensure that at least one field exists
            for(Field field : fields){
                field.setAccessible(true);                                      //bypass access checking
                Class type = field.getType();

                format("FIELD: ", depth);
                format("- NAME: " + field.getName(), depth+1);
                format("- TYPE: " + type.getSimpleName(), depth+1);
                format("- MODIFIERS: " + Modifier.toString(field.getModifiers()), depth+1);

                try{
                    Object val = field.get(obj);

                    if(val == null)                                             //if field value is mull
                        format("- VALUE: null", depth+1);
                    else if(type.isPrimitive())                                 //is field is a primitive
                        format("- VALUE: " + val, depth+1);
                    else if(type.isArray()) {                                   //if field is an array
                        format("- VALUE: ", depth + 1);
                        inspectArray(type, val, recursive, depth + 1);
                    }else {                              //this is the case when the field is an object reference value.
                        if(!recursive)                   //if recursive is set to false -> print reference
                            format("REFERENCE VALUE: " + val.getClass().getName() + "@" + val.hashCode(), depth + 1);
                        else {
                            format("- VALUE: ", depth + 1);
                            inspectClass(type, val, true, depth + 2); //recursively inspect field as an object
                        }
                    }
                }
                catch(IllegalAccessException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /*
    method to inspect fields: prints field name, field type, modifiers, and current value of the field
    this method can be enabled for recursive introspection; if recursive is true, then field will be
    inspected in the same manner as an object.
   */
    private void inspectArray(Class c, Object obj, boolean recursive, int depth) {
        Class compType = c.getComponentType();
        format("COMPONENT TYPE: " + compType.getSimpleName(), depth+1);

        int arrLength = Array.getLength(obj);
        format("ARRAY LENGTH: " + arrLength, depth+1);

        format("ARRAY CONTENTS: [", depth+1);
        for(int i = 0; i < arrLength; i++){
            Object element = Array.get(obj, i);

            if(element == null)                                     //if element at arr[i] is null
                format("null", depth+2);
            else if(compType.isPrimitive())                         //if component type of array is primitive
                format("- VALUE: " + obj, depth+2);
            else if(compType.isArray()) {                           //if component type of array is another array
                format("- VALUE: ", depth + 2);
                inspectArray(c, obj, recursive, depth + 2);
            }else{
                if(!recursive)                                      //if recursive is set to false -> print reference
                    format("- VALUE: " + element.getClass().getName() + "@" + element.hashCode(), depth+1);
                else
                    inspectClass(element.getClass(), element, true, depth+2);   //recursively inspect array as an object
            }
        }
        format("]", depth+1);
    }

    /*
    method to format indentation for output; pads left side of str with intervals of 3 spaces
    depending on the depth.
     */
    private void format(String str, int depth){
        for(int i = 0; i < depth; i++)
            System.out.print("   "); //I used 3 spaces, indentation using tab not as clean
        System.out.println(str);
    }
}
