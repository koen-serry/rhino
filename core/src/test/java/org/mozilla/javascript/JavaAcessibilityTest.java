package org.mozilla.javascript; /**
 *
 */

import junit.framework.TestCase;
import org.junit.Assert;

/**
 * @author donnamalayeri
 */
public class JavaAcessibilityTest extends TestCase {

    private static ContextFactory.GlobalSetter globalSetter;

    public static void grabContextFactoryGlobalSetter() {
        if (globalSetter == null) {
            globalSetter = ContextFactory.getGlobalSetter();
        }
    }

    String importClass = "importClass(Packages.org.mozilla.javascript.tests.PrivateAccessClass)\n";

    private ContextFactory contextFactory = new ContextFactory() {
        @Override
        protected boolean hasFeature(Context cx, int featureIndex) {
            if (featureIndex == Context.FEATURE_ENHANCED_JAVA_ACCESS)
                return true;
            return super.hasFeature(cx, featureIndex);
        }
    };
//
//    public JavaAcessibilityTest() {
//        global.init(contextFactory);
//    }

    @Override
    protected void setUp() {
        grabContextFactoryGlobalSetter();
        globalSetter.setContextFactoryGlobal(contextFactory);
    }

    @Override
    protected void tearDown() {
        grabContextFactoryGlobalSetter();
        globalSetter.setContextFactoryGlobal(null);
    }


    public void testAccessingFields() {
        Object result = runScript(importClass + "PrivateAccessClass.staticPackagePrivateInt");
        Assert.assertEquals(new Integer(0), result);

        result = runScript(importClass + "PrivateAccessClass.staticPrivateInt");
        Assert.assertEquals(new Integer(1), result);

        result = runScript(importClass + "PrivateAccessClass.staticProtectedInt");
        Assert.assertEquals(new Integer(2), result);

        result = runScript(importClass + "new PrivateAccessClass().packagePrivateString");
        Assert.assertEquals("package private", ((NativeJavaObject) result).unwrap());

        result = runScript(importClass + "new PrivateAccessClass().privateString");
        Assert.assertEquals("private", ((NativeJavaObject) result).unwrap());

        result = runScript(importClass + "new PrivateAccessClass().protectedString");
        Assert.assertEquals("protected", ((NativeJavaObject) result).unwrap());

        result = runScript(importClass + "new PrivateAccessClass.PrivateNestedClass().packagePrivateInt");
        Assert.assertEquals(new Integer(0), result);

        result = runScript(importClass + "new PrivateAccessClass.PrivateNestedClass().privateInt");
        Assert.assertEquals(new Integer(1), result);

        result = runScript(importClass + "new PrivateAccessClass.PrivateNestedClass().protectedInt");
        Assert.assertEquals(new Integer(2), result);
    }

    public void testAccessingMethods() {
        Object result = runScript(importClass + "PrivateAccessClass.staticPackagePrivateMethod()");
        Assert.assertEquals(new Integer(0), result);

        result = runScript(importClass + "PrivateAccessClass.staticPrivateMethod()");
        Assert.assertEquals(new Integer(1), result);

        result = runScript(importClass + "PrivateAccessClass.staticProtectedMethod()");
        Assert.assertEquals(new Integer(2), result);

        result = runScript(importClass + "new PrivateAccessClass().packagePrivateMethod()");
        Assert.assertEquals(new Integer(3), result);

        result = runScript(importClass + "new PrivateAccessClass().privateMethod()");
        Assert.assertEquals(new Integer(4), result);

        result = runScript(importClass + "new PrivateAccessClass().protectedMethod()");
        Assert.assertEquals(new Integer(5), result);
    }

    public void testAccessingConstructors() {
        runScript(importClass + "new PrivateAccessClass(\"foo\")");
        runScript(importClass + "new PrivateAccessClass(5)");
        runScript(importClass + "new PrivateAccessClass(5, \"foo\")");
    }

    public void testAccessingJavaBeanProperty() {
        Object result = runScript(importClass +
                "var x = new PrivateAccessClass(); x.javaBeanProperty + ' ' + x.getterCalled;");
        Assert.assertEquals("6 true", result);

        result = runScript(importClass +
                "var x = new PrivateAccessClass(); x.javaBeanProperty = 4; x.javaBeanProperty + ' ' + x.setterCalled;");
        Assert.assertEquals("4 true", result);
    }

    public void testOverloadFunctionRegression() {
        Object result = runScript(
                "(new java.util.GregorianCalendar()).set(3,4);'success';");
        Assert.assertEquals("success", result);
    }


    private Object runScript(final String scriptSourceText) {
        return contextFactory.call(new ContextAction() {
            public Object run(Context context) {
                final ScriptableObject scope = context.initStandardObjects();
                Script script = context.compileString(scriptSourceText, "", 1, null);
                return script.exec(context, scope /*global*/);
            }
        });
    }
}
