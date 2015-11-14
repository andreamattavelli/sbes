package sbes.testcase;

import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;

import sbes.exceptions.GenerationException;
import sbes.execution.InternalClassloader;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.symbolic.DynamicSymbolicAnalysis;
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
			throw new GenerationException("Unable to execute JUnit test", e);
		}

		DynamicSymbolicAnalysis.reset();
		
		JUnitCore runner = new JUnitCore();
		runner.run(Request.classes(testSuite));
		
		List<BlockStmt> testCases = DynamicSymbolicAnalysis.getTestcases();
		
		if (testCases.isEmpty()) {
			throw new GenerationException("Unable to generate test cases through dynamic analysis");
		}
		
		return new CarvingResult(testCases.get(0), (List<ImportDeclaration>) new ArrayList<ImportDeclaration>());
	}
	
}
