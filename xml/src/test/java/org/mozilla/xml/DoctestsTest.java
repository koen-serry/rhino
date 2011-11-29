package org.mozilla.xml;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Run doctests in folder testsrc/doctests.
 *
 * A doctest is a test in the form of an interactive shell session; Rhino
 * collects and runs the inputs to the shell prompt and compares them to the
 * expected outputs.
 *
 * @author Norris Boyd
 */
@RunWith(Parameterized.class)
public class DoctestsTest {
    static final String baseDirectory = DoctestsTest.class.getResource("/base.skip").getFile();
    static final String doctestsExtension = ".doctest";
    String name;
    String source;
    int optimizationLevel;

    public DoctestsTest(String name, String source, int optimizationLevel) {
        this.name = name;
        this.source = source;
        this.optimizationLevel = optimizationLevel;
    }

    public static File[] getDoctestFiles() {
        return TestUtils.recursiveListFiles(new File(baseDirectory).getParentFile(),
                new FileFilter() {
                    public boolean accept(File f) {
                        return f.getName().endsWith(doctestsExtension);
                    }
            });
    }

    public static String loadFile(File f) throws IOException {
        int length = (int) f.length(); // don't worry about very long files
        char[] buf = new char[length];
        new FileReader(f).read(buf, 0, length);
        return new String(buf);
    }

    @Parameters
    public static Collection<Object[]> doctestValues() throws IOException {
        File[] doctests = getDoctestFiles();
        List<Object[]> result = new ArrayList<Object[]>();
        for (File f : doctests) {
            String contents = loadFile(f);
            result.add(new Object[] { f.getName(), contents, -1 });
            result.add(new Object[] { f.getName(), contents, 0 });
            result.add(new Object[] { f.getName(), contents, 9 });
        }
        return result;
    }

    // move "@Parameters" to this method to test a single doctest
    public static Collection<Object[]> singleDoctest() throws IOException {
        List<Object[]> result = new ArrayList<Object[]>();
        File f = new File(baseDirectory, "Counter.doctest");
        String contents = loadFile(f);
        result.add(new Object[] { f.getName(), contents, -1 });
        return result;
    }

    @Test
    @Ignore("get the global context here")
    public void runDoctest() throws Exception {
        ContextFactory factory = ContextFactory.getGlobal();
        Context cx = factory.enterContext();
        try {
            cx.setOptimizationLevel(optimizationLevel);
            // TODO get the global context here
//            Global global = new Global(cx);
//            // global.runDoctest throws an exception on any failure
//            int testsPassed = global.runDoctest(cx, global, source, name, 1);
//            System.out.println(name + "(" + optimizationLevel + "): " +
//                    testsPassed + " passed.");
//            Assert.assertTrue(testsPassed > 0);
        } catch (Exception ex) {
          System.out.println(name + "(" + optimizationLevel + "): FAILED due to "+ex);
          throw ex;
        } finally {
            Context.exit();
        }
    }
}
