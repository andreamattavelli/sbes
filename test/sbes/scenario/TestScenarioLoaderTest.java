package sbes.scenario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import sbes.logging.Level;
import sbes.option.Options;

public class TestScenarioLoaderTest {

	@Before
	public void setUp() throws Exception {
		Options.I().setClassesPath("./bin");
		Options.I().setMethodSignature("stack.util.Stack.elementAt(int)");
		Options.I().setScenarioTestPath(Paths.get("./test/resources/InitialScenario.java").toFile());
		Options.I().setLogLevel(Level.FATAL);
	}

	@Test
	public void test() {
		List<TestScenario> scenarios = TestScenarioLoader.loadTestScenarios();
		assertNotNull(scenarios);
		assertEquals(2, scenarios.size());
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

}
