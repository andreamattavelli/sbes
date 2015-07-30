package sbes.symbolic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import sbes.logging.Level;
import sbes.option.Options;
import sbes.symbolic.mock.IntegerMock;
import sbes.symbolic.mock.Stack;

public class DynamicSymbolicAnalysisTest {

	private void setUp(String classesPath, String methodSignature) {
		Options.I().setClassesPath(classesPath);
		Options.I().setTargetMethod(methodSignature);
		Options.I().setLogLevel(Level.ERROR);
		Options.I().setDontResolveGenerics(false);
		Options.I().setSymbolicExecutionCounterexample(true);
	}
	
	protected void assertASTEquals(String actual, String expected) {
		assertEquals(expected.replaceAll("\\s|\t|\n", ""), actual.replaceAll("\\s|\t|\n", ""));
	}
	
	@Test
	public void test() {
		setUp("./bin", "stack.util.Stack.push(Object)");
		
		Stack<IntegerMock> s = new Stack<>();
		s.push(new IntegerMock(3));
		s.push(new IntegerMock(3));
		s.push(new IntegerMock(3));
		
		DynamicSymbolicAnalysis.reset();
		
		assertNotNull(DynamicSymbolicAnalysis.getTestcases().size());
		assertEquals(0, DynamicSymbolicAnalysis.getTestcases().size());
		
		DynamicSymbolicAnalysis.generateTestCases(s);
		
		assertEquals(1, DynamicSymbolicAnalysis.getTestcases().size());
		
		String expected = "{"+
				"Stack<Integer> s = new Stack<Integer>();"+
				"s.add(3);"+
				"s.add(3);"+
				"s.add(3);"+
				"s.method_under_test(ELEMENT_0_0);"+
				"}";
		
		String actual = DynamicSymbolicAnalysis.getTestcases().get(0).toString();
		
		assertASTEquals(actual, expected);
	}
	
	@Test
	public void test2() {
		setUp("./bin", "stack.util.Stack.clear()");
		
		Stack<IntegerMock> s = new Stack<>();
		s.push(new IntegerMock(3));
		s.push(new IntegerMock(3));
		s.push(new IntegerMock(3));
		
		DynamicSymbolicAnalysis.reset();
		
		assertNotNull(DynamicSymbolicAnalysis.getTestcases().size());
		assertEquals(0, DynamicSymbolicAnalysis.getTestcases().size());
		
		DynamicSymbolicAnalysis.generateTestCases(s);
		
		assertEquals(1, DynamicSymbolicAnalysis.getTestcases().size());
		
		String expected = "{"+
				"Stack<Integer> s = new Stack<Integer>();"+
				"s.push(3);"+
				"s.push(3);"+
				"s.push(3);"+
				"s.method_under_test();"+
				"}";
		
		String actual = DynamicSymbolicAnalysis.getTestcases().get(0).toString();
		
		assertASTEquals(actual, expected);
	}

}
