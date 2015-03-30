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

import org.junit.Before;
import org.junit.Test;

import sbes.logging.Level;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.generalizer.CounterexampleGeneralizer;
import sbes.scenario.generalizer.TestScenarioGeneralizer;

public class CounterexampleGeneralizerTest {

	@Before
	public void setUp() {
		TestScenarioRepository.reset();
	}
	
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
	public void test1() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.containsEntry(Object,Object)");
		
		BlockStmt scenario = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap<Integer, String> arrayListMultimap0 = ArrayListMultimap.create();"+
				"boolean boolean1 = arrayListMultimap0.put(-1698, \"pluto\");"+
				"boolean boolean2 = arrayListMultimap0.put(123, \"asd\");"+
				"boolean boolean3 = arrayListMultimap0.put(18, \"ginger\");"+
				"boolean boolean4 = arrayListMultimap0.containsEntry(18, \"ginger\");"+
				"}");
		
		CarvingResult cr = new CarvingResult(scenario, new ArrayList<ImportDeclaration>());
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario initialScenario = tsg.testToTestScenario(cr);
		
		assertEquals(TestScenarioWithGenerics.class, initialScenario.getClass());
		assertEquals(2, initialScenario.getInputAsFields().size());
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) initialScenario;
		assertEquals(2, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("String"));
		List<String> values = new ArrayList<>(tswg.getGenericToConcreteClasses().values());
		assertEquals("Integer", values.get(0));
		assertEquals("String", values.get(1));
		
		TestScenarioRepository.I().addScenario(initialScenario);
		
		// preconditions satified
		
		BlockStmt block = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap_Stub_2 arrayListMultimap_Stub_2_0 = new ArrayListMultimap_Stub_2();"+
				"Object object0 = null;"+
				"arrayListMultimap_Stub_2_0.method_under_test(object0, object0);"+
				"}");
		
		CarvingResult counterexample = new CarvingResult(block, new ArrayList<ImportDeclaration>());
		CounterexampleGeneralizer cg = new CounterexampleGeneralizer();
		TestScenario ts = cg.counterexampleToTestScenario(counterexample);
		
		String actualScenario = ts.getScenario().toString();
		String expectedScenario = 
				"{"+
				"expected_states[1] = new ArrayListMultimap();"+
				"expected_results[1] = expected_states[1].containsEntry(ELEMENT_1_0, ELEMENT_1_0);"+
				"actual_states[1] = new ArrayListMultimap();"+
				"}";
		
		assertScenarioAndPrint(actualScenario, expectedScenario);
		
		List<FieldDeclaration> actualFields = ts.getInputAsFields();
		assertFieldsAndPrint(actualFields, new String[] {"public static final Object ELEMENT_1_0 = null;"});
	}
	
	@Test
	public void test2() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.containsEntry(Object,Object)");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap_Stub_2 arrayListMultimap_Stub_2_0 = new ArrayListMultimap_Stub_2();"+
				"int int0 = 0;"+
				"Integer integer0 = new Integer(int0);"+
				"Integer integer1 = new Integer((int) integer0);"+
				"Integer integer2 = new Integer((int) integer1);"+
				"Integer integer3 = new Integer(int0);"+
				"ArrayListMultimap<Integer, String> arrayListMultimap0 = ArrayListMultimap.create((int) integer2, (int) integer3);"+
				"Integer integer4 = new Integer((int) integer2);"+
				"Integer integer5 = new Integer((int) integer4);"+
				"arrayListMultimap_Stub_2_0.method_under_test((Object) arrayListMultimap0, (Object) integer5);"+
				"arrayListMultimap_Stub_2_0.trimToSize();"+
				"Object object0 = new Object();"+
				"}");
		
		CarvingResult cr = new CarvingResult(body, new ArrayList<ImportDeclaration>());
		CounterexampleGeneralizer cg = new CounterexampleGeneralizer();
		TestScenario ts = cg.counterexampleToTestScenario(cr);
		
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
				"ArrayListMultimap arrayListMultimap_Stub_2_0_0 = new ArrayListMultimap();"+
				"expected_results[0] = expected_states[0].containsEntry((Object) ELEMENT_0_1, ELEMENT_0_0);" +
				"}";
		
		assertScenarioAndPrint(actualScenario, expectedScenario);
		
		List<FieldDeclaration> actualFields = ts.getInputAsFields();
		assertFieldsAndPrint(actualFields, new String[] {
			"public static final Integer ELEMENT_0_0 = 0;", 
			"public static final ArrayListMultimap<Integer, String> ELEMENT_0_1 = ArrayListMultimap.create((int) new Integer(0), (int) new Integer(0));"
		});
	}

}
