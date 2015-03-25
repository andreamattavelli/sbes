package sbes.scenario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.util.ArrayList;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import sbes.logging.Level;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.generalizer.TestScenarioGeneralizer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestScenarioGeneralizerTest {
	
	private List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
	
	private void setUp(String classesPath, String methodSignature) {
		Options.I().setClassesPath(classesPath);
		Options.I().setTargetMethod(methodSignature);
		Options.I().setLogLevel(Level.FATAL);
	}
	
	protected void assertScenarioAndPrint(String actual, String expected) {
		assertEquals(expected.replaceAll("\\s|\t|\n", ""), actual.replaceAll("\\s|\t|\n", ""));
		System.out.println(actual);
		System.out.println();
	}
	
	protected void assertFieldsAndPrint(List<FieldDeclaration> actual, String[] expected) {
		if (expected == null) {
			assertEquals(0, actual.size());
			System.out.println("No input fields");
		}
		else {
			assertEquals(expected.length, actual.size());
			for (int i = 0; i < expected.length; i++) {
				assertEquals(actual.get(i).toString().replaceAll("\\s|\t|\n", ""), expected[i].replaceAll("\\s|\t|\n", ""));
				System.out.println("Field #" + i + " " + actual.get(i));
			}
		}
		System.out.println("====================================================");
	}
	
	@Test
	public void test01() throws ParseException {
		setUp("./bin", "stack.util.Stack.push(Object)");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"Stack<Integer> stack0 = new Stack<Integer>();"+
				"Integer integer0 = new Integer(954);"+
				"Integer integer1 = stack0.push(integer0);"+
				"}");
		
		CarvingResult cr = new CarvingResult(body, imports);
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(cr);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(1, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		
		String actualScenario = ts.getScenario().toString();
		String expectedScenario = 
				"{"+
				"expected_states[0] = new Stack<Integer>();"+
				"expected_results[0] = expected_states[0].push(ELEMENT_0_0);"+
				"actual_states[0] = new Stack<Integer>();" +
				"}";
		assertScenarioAndPrint(actualScenario, expectedScenario);
		
		List<FieldDeclaration> actualFields = ts.getInputAsFields();
		assertFieldsAndPrint(actualFields, new String[]{"public static final Integer ELEMENT_0_0 = new Integer(954);"});
	}
	
	@Test
	public void test02() throws ParseException {
		setUp("./test/resources/gs-core-1.2.jar", "org.graphstream.graph.implementations.AbstractEdge.getNode0()");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"AdjacencyListGraph adjacencyListGraph0 = new AdjacencyListGraph(\"\");"+
				"MultiNode multiNode0 = new MultiNode((AbstractGraph) adjacencyListGraph0, \"\");"+
				"SingleNode singleNode0 = new SingleNode(adjacencyListGraph0, \"ele\");"+
				"AbstractEdge abstractEdge0 = new AbstractEdge(\"\", singleNode0, multiNode0, false);"+
				"SingleNode singleNode1 = (SingleNode)abstractEdge0.getNode0();"+
				"}");
		
		CarvingResult cr = new CarvingResult(body, imports);
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(cr);
		assertEquals(TestScenario.class, ts.getClass());
		
		String actualScenario = ts.getScenario().toString();
		String expectedScenario = 
				"{"+
				"AdjacencyListGraph adjacencyListGraph0_0 = new AdjacencyListGraph(\"\");"+
				"MultiNode multiNode0_0 = new MultiNode((AbstractGraph) adjacencyListGraph0_0, \"\");"+
				"SingleNode singleNode0_0 = new SingleNode(adjacencyListGraph0_0, \"ele\");"+
				"expected_states[0] = new AbstractEdge(\"\", singleNode0_0, multiNode0_0, false);"+
				"expected_results[0] = expected_states[0].getNode0();"+
				"actual_states[0] = new AbstractEdge(\"\", singleNode0_0, multiNode0_0, false);"+
				"}";
		assertScenarioAndPrint(actualScenario, expectedScenario);
		
		List<FieldDeclaration> actualFields = ts.getInputAsFields();
		assertFieldsAndPrint(actualFields, null);
	}
	
	@Test
	public void test03() throws ParseException {
		setUp("./bin", "stack.util.Stack.push(Object)");
		
		Options.I().setClassesPath("/Users/andrea/Uni/PhD/Workspaces/sbes-synthesis/Stack-UseCase/bin");
		Options.I().setTargetMethod("stack.util.Stack.push(Object)");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"Stack<Integer> stack0 = new Stack<Integer>();"+
				"Integer integer0 = stack0.push((Integer) 0);"+
				"}");
		
		CarvingResult cr = new CarvingResult(body, imports);
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(cr);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(1, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		
		String actualScenario = ts.getScenario().toString();
		String expectedScenario = 
				"{"+
				"expected_states[0] = new Stack<Integer>();"+
				"expected_results[0] = expected_states[0].push(ELEMENT_0_0);"+
				"actual_states[0] = new Stack<Integer>();"+
				"}";
		assertScenarioAndPrint(actualScenario, expectedScenario);
		
		List<FieldDeclaration> actualFields = ts.getInputAsFields();
		assertFieldsAndPrint(actualFields, new String[] {"public static final Integer ELEMENT_0_0 = 0;"});
	}
	
	@Test
	public void test04() throws ParseException {
		setUp("./bin", "stack.util.Stack.pop()");
	
		BlockStmt body = JavaParser.parseBlock(
				"{Stack<Integer> stack0 = new Stack<Integer>();"+
				"Integer integer0 = new Integer(1185);"+
				"boolean boolean0 = stack0.add(integer0);"+
				"Integer integer1 = stack0.pop();}");
		
		CarvingResult cr = new CarvingResult(body, imports);
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(cr);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(1, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		
		String actualScenario = ts.getScenario().toString();
		String expectedScenario = 
				"{"+
				"expected_states[0] = new Stack<Integer>();"+
				"Integer integer0_0 = new Integer(1185);"+
				"boolean boolean0_0 = expected_states[0].add(integer0_0);"+
				"expected_results[0] = expected_states[0].pop();"+
				"actual_states[0] = new Stack<Integer>();"+
				"actual_states[0].add(integer0_0);"+
				"}";
		assertScenarioAndPrint(actualScenario, expectedScenario);
		
		List<FieldDeclaration> actualFields = ts.getInputAsFields();
		assertFieldsAndPrint(actualFields, null);
	}
	
	@Test
	public void test05() throws ParseException {
		setUp("./test/resources/gs-core-1.2.jar", "org.graphstream.graph.implementations.AbstractEdge.addAttribute(String,Object)");
		
		BlockStmt body = JavaParser.parseBlock(
				"{AdjacencyListGraph adjacencyListGraph0 = new AdjacencyListGraph(\"-\");"+
				"MultiNode multiNode0 = new MultiNode((AbstractGraph) adjacencyListGraph0, \"Tz!\");"+
				"MultiGraph multiGraph0 = new MultiGraph(\"Tz!\", true, true);"+
				"SingleNode singleNode0 = new SingleNode(multiGraph0, \"-\");"+
				"AbstractEdge abstractEdge0 = new AbstractEdge(\"-\", multiNode0, singleNode0, true);"+
				"abstractEdge0.addAttribute(\"value\", 325);}");
		
		CarvingResult cr = new CarvingResult(body, imports);
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(cr);
		assertEquals(TestScenario.class, ts.getClass());
		
		String actualScenario = ts.getScenario().toString();
		String expectedScenario = 
				"{"+
				"AdjacencyListGraph adjacencyListGraph0_0 = new AdjacencyListGraph(\"-\");"+
				"MultiNode multiNode0_0 = new MultiNode((AbstractGraph) adjacencyListGraph0_0, \"Tz!\");"+
				"MultiGraph multiGraph0_0 = new MultiGraph(\"Tz!\", true, true);"+
				"SingleNode singleNode0_0 = new SingleNode(multiGraph0_0, \"-\");"+
				"expected_states[0] = new AbstractEdge(\"-\", multiNode0_0, singleNode0_0, true);"+
				"expected_states[0].addAttribute(ELEMENT_0_0, ELEMENT_0_1);"+
				"actual_states[0] = new AbstractEdge(\"-\", multiNode0_0, singleNode0_0, true);"+
				"}";
		assertScenarioAndPrint(actualScenario, expectedScenario);
		
		List<FieldDeclaration> actualFields = ts.getInputAsFields();
		assertFieldsAndPrint(actualFields, new String[] {
				"public static final String ELEMENT_0_0 = new String(\"value\");", 
				"public static final Integer ELEMENT_0_1 = 325;"});
	}
	
	@Test
	public void test06() throws ParseException {
		setUp("./bin", "stack.util.Stack.clear()");
		
		BlockStmt body = JavaParser.parseBlock(
				"{Stack<Integer> stack0 = new Stack<Integer>();"+
				"Integer integer0 = new Integer(234);"+
				"Integer integer1 = new Integer(55);"+
				"Integer integer2 = new Integer(2);"+
				"boolean boolean0 = stack0.add(integer0);"+
				"boolean boolean1 = stack0.add(integer1);"+
				"boolean boolean2 = stack0.add(integer2);"+
				"stack0.clear();}");
		
		CarvingResult cr = new CarvingResult(body, imports);
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(cr);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(1, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		
		String actualScenario = ts.getScenario().toString();
		String expectedScenario = 
				"{"+
				"expected_states[0] = new Stack<Integer>();"+
				"Integer integer0_0 = new Integer(234);"+
				"Integer integer1_0 = new Integer(55);"+
				"Integer integer2_0 = new Integer(2);"+
				"boolean boolean0_0 = expected_states[0].add(integer0_0);"+
				"boolean boolean1_0 = expected_states[0].add(integer1_0);"+
				"boolean boolean2_0 = expected_states[0].add(integer2_0);"+
				"expected_states[0].clear();"+
				"actual_states[0] = new Stack<Integer>();"+
				"actual_states[0].add(integer0_0);"+
				"actual_states[0].add(integer1_0);"+
				"actual_states[0].add(integer2_0);"+
				"}";
		assertScenarioAndPrint(actualScenario, expectedScenario);
		
		List<FieldDeclaration> actualFields = ts.getInputAsFields();
		assertFieldsAndPrint(actualFields, null);
	}
	
	@Test
	public void test07() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.put(Object,Object)");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap<Integer, String> arrayListMultimap0 = ArrayListMultimap.create();"+
				"Integer integer0 = new Integer(234);"+
				"List<String> list0 = new ArrayList();"+
				"list0.add(\"pippo\");"+
				"boolean boolean0 = arrayListMultimap0.putAll(integer0, list0);"+
				"Integer integer3 = new Integer(-1698);"+
				"String string0 = \"pluto\";"+
				"boolean boolean3 = arrayListMultimap0.put(integer3, string0);"+
				"}");
		
		CarvingResult cr = new CarvingResult(body, imports);
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(cr);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(2, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("String"));
		List<String> values = new ArrayList<>(tswg.getGenericToConcreteClasses().values());
		assertEquals("Integer", values.get(0));
		assertEquals("String", values.get(1));
		
		String actualScenario = ts.getScenario().toString();
		String expectedScenario = 
				"{"+
				"expected_states[0] = ArrayListMultimap.create();"+
				"Integer integer0_0 = new Integer(234);"+
				"List<String> list0_0 = new ArrayList();"+
				"list0_0.add(\"pippo\");"+
				"boolean boolean0_0 = expected_states[0].putAll(integer0_0, list0_0);"+
				"expected_results[0] = expected_states[0].put(ELEMENT_0_1, ELEMENT_0_0);"+
				"actual_states[0] = ArrayListMultimap.create();"+
				"actual_states[0].putAll(integer0_0, list0_0);"+
				"}";
		
		assertScenarioAndPrint(actualScenario, expectedScenario);
		
		List<FieldDeclaration> actualFields = ts.getInputAsFields();
		assertFieldsAndPrint(actualFields, new String[] {
				"public static final String ELEMENT_0_0 = \"pluto\";",
				"public static final Integer ELEMENT_0_1 = new Integer(-1698);"});
	}
	
}
