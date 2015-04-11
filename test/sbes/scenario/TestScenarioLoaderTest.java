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
		Options.I().setHeuristicPruningScenarios(false);
		Options.I().setLogLevel(Level.FATAL);
	}

	@Test
	public void test() {
		Options.I().setClassesPath("./bin");
		Options.I().setScenarioTestPath(Paths.get("./test/resources/InitialScenario.java").toFile());
		Options.I().setTargetMethod("stack.util.Stack.elementAt(int)");
		
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
		Options.I().setTargetMethod("com.google.common.collect.ArrayListMultimap.put(Object,Object)");
		
		List<TestScenario> scenarios = TestScenarioLoader.loadTestScenarios();
		assertNotNull(scenarios);
		assertEquals(2, scenarios.size());
		
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
		TestScenarioRepository.I().addScenario(scenarios.get(0));
		
		assertEquals(TestScenarioWithGenerics.class, scenarios.get(1).getClass());
		String secondActual = scenarios.get(1).toString();
		String secondExpected =
				"{"+
				"ArrayListMultimap<Integer, String> arrayListMultimap0 = ArrayListMultimap.create();"+
				"boolean boolean1 = arrayListMultimap0.put(-1698, \"pluto\");"+
				"boolean boolean2 = arrayListMultimap0.put(123, \"asd\");"+
				"boolean boolean3 = arrayListMultimap0.put(18, \"ginger\");"+
				"boolean boolean4 = arrayListMultimap0.remove(18, \"ginger\");"+
				"}";
		assertEquals(secondExpected.replaceAll("\\s|\t|\n", ""), secondActual.replaceAll("\\s|\t|\n", ""));
	}
	
	@Test
	public void test3() {
		Options.I().setClassesPath("./test/resources/guava-12.0.1.jar");
		Options.I().setScenarioTestPath(Paths.get("./test/resources/InitialScenarioGuava2.java").toFile());
		Options.I().setTargetMethod("com.google.common.collect.ConcurrentHashMultiset.containsAll(Collection)");
		
		List<TestScenario> scenarios = TestScenarioLoader.loadTestScenarios();
		assertNotNull(scenarios);
		assertEquals(1, scenarios.size());
		
		assertEquals(TestScenarioWithGenerics.class, scenarios.get(0).getClass());
		String firstActual = scenarios.get(0).toString();
		String firstExpected = 
				"{"+
				"ConcurrentHashMultiset<Integer> hashMultiset0 = ConcurrentHashMultiset.create();"+
				"Integer integer0 = new Integer(-18247);"+
				"Integer integer1 = new Integer(34);"+
				"Integer integer2 = new Integer(0);"+
				"boolean boolean0 = hashMultiset0.add(integer0);"+
				"boolean boolean1 = hashMultiset0.add(integer1);"+
				"boolean boolean2 = hashMultiset0.add(integer2);"+
				"List<Integer> arrayList0 = new ArrayList();"+
				"arrayList0.add(0);"+
				"arrayList0.add(34);"+
				"boolean boolean3 = hashMultiset0.containsAll(arrayList0);"+
				"}";
		assertEquals(firstExpected.replaceAll("\\s|\t|\n", ""), firstActual.replaceAll("\\s|\t|\n", ""));
		TestScenarioRepository.I().addScenario(scenarios.get(0));
	}
	
	@Test
	public void test4() {
		Options.I().setClassesPath("./test/resources/guava-12.0.1.jar");
		Options.I().setScenarioTestPath(Paths.get("./test/resources/InitialScenarioGuava2.java").toFile());
		Options.I().setTargetMethod("com.google.common.collect.ConcurrentHashMultiset.containsAll(Collection)");
		Options.I().setDontResolveGenerics(true);
		
		List<TestScenario> scenarios = TestScenarioLoader.loadTestScenarios();
		assertNotNull(scenarios);
		assertEquals(1, scenarios.size());
		
		assertEquals(TestScenario.class, scenarios.get(0).getClass());
		String firstActual = scenarios.get(0).toString();
		String firstExpected = 
				"{"+
				"ConcurrentHashMultiset<Integer> hashMultiset0 = ConcurrentHashMultiset.create();"+
				"Integer integer0 = new Integer(-18247);"+
				"Integer integer1 = new Integer(34);"+
				"Integer integer2 = new Integer(0);"+
				"boolean boolean0 = hashMultiset0.add(integer0);"+
				"boolean boolean1 = hashMultiset0.add(integer1);"+
				"boolean boolean2 = hashMultiset0.add(integer2);"+
				"List<Integer> arrayList0 = new ArrayList();"+
				"arrayList0.add(0);"+
				"arrayList0.add(34);"+
				"boolean boolean3 = hashMultiset0.containsAll(arrayList0);"+
				"}";
		assertEquals(firstExpected.replaceAll("\\s|\t|\n", ""), firstActual.replaceAll("\\s|\t|\n", ""));
		TestScenarioRepository.I().addScenario(scenarios.get(0));
	}

}
