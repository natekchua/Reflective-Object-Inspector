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
"indents" int variable is used to improve readability for number of indentations. I made
this separate to "depth". Depth was solely used in the inspect methods to represent current level
of hierarchy.
 */

import java.lang.reflect.*;

public class Inspector {

    public void inspect(Object obj, boolean recursive) {
        int depth = 0;
        Class c = obj.getClass();
        inspectClass(c, obj, recursive, depth);
    }

    /*
    method to inspect declaring class.
    */
    private void inspectClass(Class c, Object obj, boolean recursive, int depth) {

        format(depth,"CLASS: " + c.getSimpleName());

        if(c.isArray())
            inspectArray(c, obj, recursive, depth);

        inspectSuperclass(c, obj, recursive, depth);
        inspectInterfaces(c, obj, recursive, depth);
        inspectConstructors(c, depth);
        inspectMethods(c, depth);
        inspectFields(c, obj, recursive, depth);

        System.out.println();                                       //used to separate class information
    }

    /*
    method to inspect superclass: always fully explores inheritance hierarchy.
    */
    private void inspectSuperclass(Class c, Object obj, boolean recursive, int depth) {
        if(c.equals(Object.class))
            return;

        Class superclass = c.getSuperclass();
        int indents = depth + 1;

        if(superclass != null){
            format(indents, "SUPERCLASS: " + superclass.getSimpleName());
            inspectClass(superclass, obj, recursive, depth + 2);
        }else
            format(indents,"** NO SUPERCLASS EXISTS **");
    }

    /*
    method to inspect implemented interface(s): always fully explores inheritance hierarchy.
    */
    private void inspectInterfaces(Class c, Object obj, boolean recursive, int depth) {
        Class [] iArray = c.getInterfaces();         //array of class c's implementing interfaces
        int indents = depth + 1;

        for (Class interFace : iArray){
            format(indents, "INTERFACE: " + interFace.getSimpleName());
            inspectClass(interFace, obj, recursive,depth + 2);
        }
    }

    /*
    method to inspect constructors: prints constructor name, parameters (if any), and modifiers.
     */
    private void inspectConstructors(Class c, int depth) {
        Constructor[] cArray = c.getConstructors();                 //array of class c's constructors
        int indents = depth + 1;

        if(cArray.length > 0) {                                     //check that at least one constructor exists
            for (Constructor constructor : cArray) {
                format(indents, "CONSTRUCTOR: ");
                format(indents + 1,"- NAME: " + constructor.getName());

                Class[] pArray = constructor.getParameterTypes();   //array of constructor parameter types
                if (pArray.length > 0) {                             //check that at least one parameter exists
                    format(indents + 1, "- PARAMETER TYPES: ");
                    printTypes(indents + 2, pArray);
                }
                format(indents + 1,"- MODIFIERS: " + Modifier.toString(constructor.getModifiers()));
            }
        }
    }

    /*
    method to inspect methods: prints method name, exceptions (if any), parameters (if any), return type, and modifiers.
     */
    private void inspectMethods(Class c, int depth) {
        Method[] methods = c.getDeclaredMethods();                             //gets all methods of any visibility in Class "c"
        int indents = depth + 1;

        if(methods.length > 0){                                        //check that at least one method exists
            for(Method method : methods){
                format(indents,"METHOD: ");
                format(indents + 1, "- NAME: " + method.getName());

                Class[] eArray = method.getExceptionTypes();            //array of the method's exception types
                if(eArray.length > 0) {                                 //check that there is at least one exception
                    format(indents + 1, "- EXCEPTIONS THROWN: ");
                    printTypes(indents + 2, eArray);
                }

                Class[] pArray = method.getParameterTypes();            //array of method's parameter types
                if(pArray.length > 0){                                  //check that there is at least one parameter
                    format(indents + 1, "- PARAMETER TYPES: ");
                    printTypes(indents + 2, pArray);
                }

                format(indents + 1, "- RETURN TYPE: " + method.getReturnType());
                format(indents + 1, "- MODIFIERS: " + Modifier.toString(method.getModifiers()));
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
        int indents = depth + 1;

        if(fields.length > 0){                                                  //check that at least one field exists
            for(Field field : fields){
                field.setAccessible(true);                                      //bypass access checking
                Class type = field.getType();

                format(indents, "FIELD: ");
                format(indents + 1, "- NAME: " + field.getName());
                format(indents + 1, "- TYPE: " + type.getSimpleName());
                format(indents + 1, "- MODIFIERS: " + Modifier.toString(field.getModifiers()));

                try{
                    Object val = field.get(obj);
                    getValue(val, type, recursive, indents);
                }
                catch(IllegalAccessException e){
                    e.printStackTrace();
                }
            }
        }else
            format(indents, "** NO FIELD EXISTS **");
    }

    /*
    method to inspect fields: prints field name, field type, modifiers, and current value of the field
    this method can be enabled for recursive introspection; if recursive is true, then field will be
    inspected in the same manner as an object.
   */
    private void inspectArray(Class c, Object obj, boolean recursive, int depth) {
        int indents = depth + 1;
        format(indents, "ARRAY: ");          //figure out how to get array name***

        Class compType = c.getComponentType();
        format(indents + 1, "COMPONENT TYPE: " + compType.getSimpleName());

        int arrLength = Array.getLength(obj);
        format(indents + 1, "LENGTH: " + arrLength);

        format(indents + 1, "CONTENTS: [");
        for(int i = 0; i < arrLength; i++){
            Object element = Array.get(obj, i);
            getValue(element, compType, recursive, depth + 1);
        }
        format(indents + 1, "]");
    }

    private void getValue(Object obj, Class value, boolean recursive, int depth) {
        int indents = depth + 1;
        if(obj == null)                                     //if element at arr[i] is null
            format(indents + 1, "null");
        else if(value.isPrimitive())                         //if type is primitive
            format(indents + 1, "- VALUE: " + obj);
        else if(value.isArray()) {                           //if type is an array
            format(indents, "- VALUE: ");
            inspectArray(obj.getClass(), obj, recursive, depth + 1);
        }else
            recursiveIntrospection(obj, recursive, depth);
    }
    /*
    helper method used for further field (including arrays) introspection.
    if recursive is false, information is simply found of for the field, otherwise
    we further inspect the field/array in the same manner of a regular object.
     */
    private void recursiveIntrospection(Object obj, boolean recursive, int depth) {
        int indents = depth + 1;
        if (!recursive) {
            format(indents, "- REFERENCE VALUE: " + obj.getClass().getName() + "@" + System.identityHashCode(obj));
        } else {
            inspectClass(obj.getClass(), obj, recursive, depth);
        }
    }

    /*
    Helper method to handle parameter and exception types array traversal and printing out each element.
     */
    private void printTypes(int indents, Class[] typeArray) {
        for(Class type : typeArray)
            format(indents, type.getSimpleName());
    }

    /*
    method to format indentation for output; pads left side of str with intervals of 3 spaces
    depending on the depth.
     */
    private void format(int indents, Object obj){
        String str = String.valueOf(obj);
        for(int i = 0; i < indents; i++)
            System.out.print("   "); //I used 3 spaces, indentation using tab not as clean
        System.out.println(str);
    }
}
