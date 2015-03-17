package sbes.scenario;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.util.ArrayList;

import org.junit.Test;

import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.generalizer.CounterexampleGeneralizer;

public class CounterexampleGeneralizerTest {

//	@Test
//	public void test1() throws ParseException {
//		Options.I().setClassesPath("/Users/andrea/Uni/PhD/Workspaces/sbes-synthesis/Stack-UseCase/bin");
//		Options.I().setMethodSignature("stack.util.Stack.peek()");
//		
//		BlockStmt block = JavaParser.parseBlock(
//				"{"+
//				"Stack_Stub_2 stack_Stub_2_0 = new Stack_Stub_2();"+
//				"Object object0 = new Object();"+
//				"Object[] objectArray0 = new Object[8];"+
//				"objectArray0[3] = (Object) 0;"+
//				"stack_Stub_2_0.elementData = objectArray0;"+
//				"boolean boolean0 = stack_Stub_2_0.add(object0);"+
//				"boolean boolean1 = stack_Stub_2_0.add(object0);"+
//				"Integer integer0 = new Integer(0);"+
//				"boolean boolean2 = stack_Stub_2_0.add((Object) integer0);"+
//				"stack_Stub_2_0.method_under_test();"+
//				"}");
//		
//		CarvingResult counterexample = new CarvingResult(block, new ArrayList<ImportDeclaration>());
//		TestScenario ts = CounterexampleGeneralizer.counterexampleToTestScenario(counterexample);
//		System.out.println(ts.getScenario());
//		System.out.println(ts.getInputAsFields());
//	}
//	
//	@Test
//	public void test2() throws ParseException {
//		Options.I().setClassesPath("/Users/andrea/Uni/PhD/Workspaces/sbes-synthesis/Stack-UseCase/bin");
//		Options.I().setMethodSignature("stack.util.Stack.get(int)");
//		
//		BlockStmt block = JavaParser.parseBlock(
//				"{"+
//				"Stack_Stub_2 stack_Stub_2_0 = new Stack_Stub_2();"+
//				"int int0 = 2;"+
//				"Integer integer0 = new Integer(int0);"+
//				"boolean boolean0 = stack_Stub_2_0.add(integer0);"+
//				"int int1 = 0;"+
//				"Integer integer1 = new Integer(int1);"+
//				"stack_Stub_2_0.method_under_test((int) integer1);"+
//				"}");
//		
//		CarvingResult counterexample = new CarvingResult(block, new ArrayList<ImportDeclaration>());
//		TestScenario ts = CounterexampleGeneralizer.counterexampleToTestScenario(counterexample);
//		System.out.println(ts.getScenario());
//		System.out.println(ts.getInputAsFields());
//	}
//	
	@Test
	public void test3() throws ParseException {
		Options.I().setClassesPath("/Users/andrea/Uni/PhD/Workspaces/sbes-synthesis/Stack-UseCase/bin");
		Options.I().setMethodSignature("stack.util.Stack.get(int)");
		
		BlockStmt block = JavaParser.parseBlock(
				"{"+
				"Stack_Stub_2 stack_Stub_2_0 = new Stack_Stub_2();"+
				"int int0 = (-8);"+
				"Integer integer0 = new Integer(int0);"+
				"int int1 = 3;"+
				"Integer integer1 = new Integer(int1);"+
				"Integer integer2 = new Integer((int) integer1);"+
				"stack_Stub_2_0.setSize((int) integer2);"+
				"boolean boolean0 = stack_Stub_2_0.add(integer0);"+
				"stack_Stub_2_0.method_under_test((int) integer2);"+
				"}");
		
		System.out.println(block.toString());
		
		CarvingResult counterexample = new CarvingResult(block, new ArrayList<ImportDeclaration>());
		TestScenario ts = CounterexampleGeneralizer.counterexampleToTestScenario(counterexample);
		System.out.println(ts.getScenario());
		System.out.println(ts.getInputAsFields());
	}
	
	
	
	
	
	
//	@Test
//	public void test2() throws ParseException {
//		Options.I().setClassesPath("/Users/andrea/Uni/PhD/Workspaces/sbes-synthesis/Stack-UseCase/bin");
//		Options.I().setMethodSignature("stack.util.Stack.elementAt(int)");
//		
//		BlockStmt block = JavaParser.parseBlock(
//				"{Stack_Stub_2<Integer> stack_Stub_2_0 = new Stack_Stub_2<Integer>();"+
//				"boolean boolean0 = stack_Stub_2_0.add((Integer) (-1893));"+
//				"stack_Stub_2_0.method_under_test(0);}");
//		
//		CarvingResult counterexample = new CarvingResult(block, new ArrayList<ImportDeclaration>());
//		
//		TestScenario ts = CounterexampleGeneralizer.counterexampleToTestScenario(counterexample);
//		System.out.println(ts.getScenario());
//		System.out.println(ts.getInputAsFields());
//	}
//	
//	@Test
//	public void test3() throws ParseException {
//		Options.I().setClassesPath("/Users/andrea/Uni/PhD/Workspaces/sbes-synthesis/Stack-UseCase/bin");
//		Options.I().setMethodSignature("stack.util.Stack.elementAt(int)");
//		
//		BlockStmt block = JavaParser.parseBlock(
//				"{Stack_Stub_2<Integer> stack_Stub_2_0 = new Stack_Stub_2<Integer>();"+ 
//				"stack_Stub_2_0.trimToSize();"+ 
//				"boolean boolean0 = stack_Stub_2_0.add((Integer) 55);"+ 
//				"stack_Stub_2_0.method_under_test(0);}");
//		
//		CarvingResult counterexample = new CarvingResult(block, new ArrayList<ImportDeclaration>());
//		
//		TestScenario ts = CounterexampleGeneralizer.counterexampleToTestScenario(counterexample);
//		
//		AbstractStubGenerator counterexampleGenerator = new FirstStageGeneratorStub(TestScenarioRepository.I().getScenarios());
//		Stub stub = counterexampleGenerator.generateStub();
//		System.out.println(stub.getAst().toString());
//		
//		System.out.println(ts.getScenario());
//		System.out.println(ts.getInputAsFields());
//	}

}
