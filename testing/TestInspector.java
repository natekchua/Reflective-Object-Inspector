import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

public class TestInspector {

    private static final ByteArrayOutputStream output = new ByteArrayOutputStream();

    @Before
    public void setupOutput() {
        System.setOut(new PrintStream(output));
    }

    @After
    public void resetOutput() {
        output.reset();
    }

    @Test
    public void testClassName(){
        new Inspector().inspectClass(Object.class, new Object(), false, 0 );
        String expected = "CLASS: Object";
        assert(output.toString().contains(expected));
    }

    @Test
    public void testSuperclassName(){
        new Inspector().inspectSuperclass(String.class, "I am a string", false, 0 );
        String expected = "SUPERCLASS: Object";
        assert(output.toString().contains(expected));
    }

    @Test
    public void testSuperclassDoesntExist(){
        new Inspector().inspectSuperclass(Serializable.class, null, false, 0 );
       if(!Serializable.class.isInterface()){
        String expected = "** NO SUPERCLASS EXISTS **";
        assert(output.toString().contains(expected));
       }
    }

    @Test
    public void testInterface(){
        new Inspector().inspectInterfaces(String.class, "Hello World", false, 0);
        String expected = "INTERFACE: Serializable";
        assert(output.toString().contains(expected));
    }

    @Test
    public void testInterfaceDoesntExist(){
        TesterClass test = new TesterClass();
        new Inspector().inspectInterfaces(test.getClass(), new TesterClass(), false, 0);
        String expected = "** NO INTERFACE IMPLEMENTED **";
        assert(output.toString().contains(expected));
    }

    @Test
    public void testConstructorInspection(){
        new Inspector().inspectConstructors(String.class, 0);

        String expected = "- NAME: java.lang.String";
        assert(output.toString().contains(expected));

        expected = "- MODIFIERS: public";
        assert(output.toString().contains(expected));
    }

    @Test
    public void testPrintParameterTypes(){
        Class[] arr = new Class[2];
        arr[0] = String.class;
        arr[1] = Object.class;

        Inspector inspector = new Inspector();
        inspector.printTypes(0, arr);

        String expected = "String";
        assert(output.toString().contains(expected));

        expected = "Object";
        assert(output.toString().contains(expected));
    }

    @Test
    public void testMethodInspection(){
        new Inspector().inspectMethods(String.class, 0);

        String expected = "- NAME: isEmpty";
        assert(output.toString().contains(expected));

        expected = "- RETURN TYPE: boolean";
        assert(output.toString().contains(expected));

        expected = "- MODIFIERS: public";
        assert(output.toString().contains(expected));
    }
    @Test
    public void testFieldInspection(){
        new Inspector().inspectFields(String.class, "Hey There", false, 0);

        String expected = "- NAME: hash";
        assert(output.toString().contains(expected));

        expected = "- TYPE: int";
        assert(output.toString().contains(expected));

        expected = "- MODIFIERS: private";
        assert(output.toString().contains(expected));
    }

    @Test
    public void testFieldDoesntExist(){
        TesterClass test = new TesterClass();
        new Inspector().inspectFields(test.getClass(), new TesterClass(), false, 0);

        String expected = "** NO FIELD EXISTS **";
        assert(output.toString().contains(expected));
    }

    @Test
    public void testArrayInspection(){
        new Inspector().inspect(new ClassB[6], false);

        String expected = "COMPONENT TYPE: ClassB";
        assert(output.toString().contains(expected));

        expected = "LENGTH: 6";
        assert(output.toString().contains(expected));

        expected = "CONTENTS: [";
        assert(output.toString().contains(expected));
    }

    @Test
    public void testGetValueIsNull(){
        new Inspector().getValue(null, Object.class, false, 0);
        String expected = "null";
        assert(output.toString().contains(expected));
    }
}