package sbes.testcase;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.ImportDeclaration;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;

import sbes.exceptions.GenerationException;
import sbes.execution.InternalClassloader;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.util.ClassUtils;
import sbes.util.IOUtils;

public class TestCaseExecutor {

	private String classpath;
	
	public TestCaseExecutor(String classpath) {
		super();
		this.classpath = classpath;
	}

	public CarvingResult executeTests() {
		String signature = Options.I().getTargetMethod();
		String packagename = IOUtils.fromCanonicalToPath(ClassUtils.getPackage(signature)).replace('/', '.');

		String testcaseName = packagename + ".TestSuite_method_under_test";
		
		Class<?> testSuite = null;
		InternalClassloader icl = new InternalClassloader(classpath);
		try {
			testSuite = Class.forName(testcaseName, false, icl.getClassLoader());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new GenerationException("Unable to execute JUnit test", e);
		}
		ConsoleRedirector cr = new ConsoleRedirector();
		cr.start();

		JUnitCore runner = new JUnitCore();
		runner.run(Request.classes(testSuite));

		String testCases = cr.stop();
		String[] tests = testCases.split(System.lineSeparator());
		CarvingResult result = null;
		try {
			result = new CarvingResult(JavaParser.parseBlock("{"+tests[0]+"}"), (List<ImportDeclaration>)new ArrayList<ImportDeclaration>());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	class ConsoleRedirector {
		private ByteArrayOutputStream baos;
	    private PrintStream previous;
	    private boolean capturing;

	    public void start() {
	        if (capturing) {
	            return;
	        }

	        capturing = true;
	        previous = System.out;      
	        baos = new ByteArrayOutputStream();
	        PrintStream ps = new PrintStream(baos);
	        System.setOut(ps);
	    }

	    public String stop() {
	        if (!capturing) {
	            return "";
	        }

	        System.out.flush();
	        System.setOut(previous);
	        String capturedValue = baos.toString();             
	        baos = null;
	        previous = null;
	        capturing = false;
	        return capturedValue;
	    }
	}
}