package sbes.scenario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Paths;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import sbes.logging.Level;
import sbes.option.Options;

public class TestScenarioLoaderTest {

	@BeforeClass
	public static void setUp() throws Exception {
		Options.I().setLogLevel(Level.FATAL);
	}

	@Test
	public void test() {
		Options.I().setClassesPath("./bin");
		Options.I().setScenarioTestPath(Paths.get("./test/resources/InitialScenario.java").toFile());
		Options.I().setMethodSignature("stack.util.Stack.elementAt(int)");
		
		List<TestScenario> scenarios = TestScenarioLoader.loadTestScenarios();
		assertNotNull(scenarios);
		assertEquals(2, scenarios.size());
		
		assertEquals(TestScenarioWithGenerics.class, scenarios.get(0).getClass());
		String firstActual = scenarios.get(0).toString();
		String firstExpected = 
				"{"+
				"Stack<Integer> stack0 = new Stack<Integer>();"+
				"Integer integer0 = new Integer(234);"+
				"Integer integer1 = new Integer(55);"+
				"Integer integer2 = new Integer((-17489));"+
				"boolean boolean0 = stack0.add(integer0);"+
				"boolean boolean1 = stack0.add(integer1);"+
				"boolean boolean2 = stack0.add(integer2);"+
				"Integer integer3 = stack0.elementAt((Integer) 2);"+
				"}";
		assertEquals(firstExpected.replaceAll("\\s|\t|\n", ""), firstActual.replaceAll("\\s|\t|\n", ""));
		
		assertEquals(TestScenarioWithGenerics.class, scenarios.get(1).getClass());
		String secondActual = scenarios.get(1).toString();
		String secondExpected =
				"{"+
				"Stack<Integer> stack0 = new Stack<Integer>();"+
				"Integer integer0 = new Integer(234);"+
				"Integer integer1 = new Integer(55);"+
				"Integer integer2 = new Integer(0);"+
				"boolean boolean0 = stack0.add(integer0);"+
				"boolean boolean1 = stack0.add(integer1);"+
				"boolean boolean2 = stack0.add(integer2);"+
				"Integer integer3 = stack0.elementAt((Integer) 1);"+
				"}";
		assertEquals(secondExpected.replaceAll("\\s|\t|\n", ""), secondActual.replaceAll("\\s|\t|\n", ""));
	}
	
	@Test
	public void test2() {
		Options.I().setClassesPath("./test/resources/guava-12.0.1.jar");
		Options.I().setScenarioTestPath(Paths.get("./test/resources/InitialScenarioGuava.java").toFile());
		Options.I().setMethodSignature("com.google.common.collect.ArrayListMultimap.put(Object,Object)");
		
		List<TestScenario> scenarios = TestScenarioLoader.loadTestScenarios();
		assertNotNull(scenarios);
		assertEquals(1, scenarios.size());
		
		assertEquals(TestScenarioWithGenerics.class, scenarios.get(0).getClass());
		String firstActual = scenarios.get(0).toString();
		String firstExpected = 
				"{"+
				"ArrayListMultimap<Integer, String> arrayListMultimap0 = ArrayListMultimap.create();"+
				"Integer integer0 = new Integer(234);"+
				"List<String> list0 = new ArrayList();"+
				"list0.add(\"pippo\");"+
				"boolean boolean0 = arrayListMultimap0.putAll(integer0, list0);"+
				"Integer integer3 = new Integer(-1698);"+
				"String string0 = \"pluto\";"+
				"boolean boolean3 = arrayListMultimap0.put(integer3, string0);"+
				"}";
		assertEquals(firstExpected.replaceAll("\\s|\t|\n", ""), firstActual.replaceAll("\\s|\t|\n", ""));
	}

}
