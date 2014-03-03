package sbes.stub;

import static org.junit.Assert.assertEquals;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.result.TestScenario;
import sbes.scenario.GenericTestScenario;
import sbes.scenario.TestScenarioGeneralizer;

public class FirstStageStubGeneratorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws ParseException {
		List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
		
		Options.I().setClassesPath("/Users/andrea/Uni/PhD/Workspaces/sbes-synthesis/sbes/joda-time-2.3.jar");
		Options.I().setMethodSignature("org.joda.time.DateTime.minus(ReadableDuration)");
		
		BlockStmt body = JavaParser.parseBlock("{FixedDateTimeZone fixedDateTimeZone0 = new FixedDateTimeZone(\"\", \"\", 1836, 0);"+ 
				"DateTime dateTime0 = new DateTime((DateTimeZone) fixedDateTimeZone0);"+ 
				"Interval interval0 = new Interval((long) (-64), (long) 1836);"+ 
				"Duration duration0 = interval0.toDuration();"+ 
				"DateTime dateTime1 = dateTime0.minus((ReadableDuration) duration0);}");
		CarvingResult cr = new CarvingResult(body, imports);
		
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer(0);
		TestScenario ts = tsg.generalizeTestToScenario(cr);
		
		System.out.println(ts.getScenario().toString());
		System.out.println(ts.getInputs());
	}
	
	@Test
	public void test2() throws ParseException {
		List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
		
		Options.I().setClassesPath("/Users/andrea/Uni/PhD/Workspaces/sbes-synthesis/Stack-UseCase/bin");
		Options.I().setMethodSignature("stack.util.Stack.push(Object)");
		
		BlockStmt body = JavaParser.parseBlock("{Stack<Integer> stack0 = new Stack<Integer>();"+
				"Integer integer0 = new Integer(954);"+
				"Integer integer1 = stack0.push(integer0);}");
		CarvingResult cr = new CarvingResult(body, imports);
		
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer(0);
		TestScenario ts = tsg.generalizeTestToScenario(cr);
		
		System.out.println(ts.getScenario().toString());
		System.out.println(ts.getInputs());
		
		assertEquals(GenericTestScenario.class, ts.getClass());
	}

	@Test
	public void test3() throws ParseException {
		List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
		
		Options.I().setClassesPath("/Users/andrea/Uni/PhD/Workspaces/sbes-synthesis/sbes/joda-time-2.3.jar");
		Options.I().setMethodSignature("org.joda.time.DateTime.minus(ReadableDuration)");
		
		BlockStmt body = JavaParser.parseBlock("{BuddhistChronology buddhistChronology0 = BuddhistChronology.getInstance();"+
				"CachedDateTimeZone cachedDateTimeZone0 = (CachedDateTimeZone)buddhistChronology0.getZone();"+
				"DateTime dateTime0 = DateTime.now((DateTimeZone) cachedDateTimeZone0);"+
				"PeriodType periodType0 = PeriodType.hours();"+
				"MutablePeriod mutablePeriod0 = new MutablePeriod((Object) null, periodType0);"+
				"Duration duration0 = mutablePeriod0.toDurationFrom((ReadableInstant) dateTime0);"+
				"DateTime dateTime1 = dateTime0.minus((ReadableDuration) duration0);}");
		CarvingResult cr = new CarvingResult(body, imports);
		
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer(0);
		TestScenario ts = tsg.generalizeTestToScenario(cr);
		
		System.out.println(ts.getScenario().toString());
		
		System.out.println(ts.getInputs());
	}
	
	@Test
	public void test4() throws ParseException {
		List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
		
		Options.I().setClassesPath("/Users/andrea/Uni/PhD/Workspaces/sbes-synthesis/sbes/gs-core-1.2.jar");
		Options.I().setMethodSignature("org.graphstream.graph.implementations.AbstractEdge.getNode0()");
		
		BlockStmt body = JavaParser.parseBlock("{AdjacencyListGraph adjacencyListGraph0 = new AdjacencyListGraph(\"\");"+
												"MultiNode multiNode0 = new MultiNode((AbstractGraph) adjacencyListGraph0, \"\");"+
												"SingleNode singleNode0 = new SingleNode(adjacencyListGraph0, \"ele\");"+
												"AbstractEdge abstractEdge0 = new AbstractEdge(\"\", singleNode0, multiNode0, false);"+
												"SingleNode singleNode1 = (SingleNode)abstractEdge0.getNode0();}");
		CarvingResult cr = new CarvingResult(body, imports);
		
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer(0);
		TestScenario ts = tsg.generalizeTestToScenario(cr);
		
		System.out.println(ts.getScenario().toString());
		System.out.println(ts.getInputs());
	}
	
	@Test
	public void test5() throws ParseException {
		List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
		
		Options.I().setClassesPath("/Users/andrea/Uni/PhD/Workspaces/sbes-synthesis/Stack-UseCase/bin");
		Options.I().setMethodSignature("stack.util.Stack.push(Object)");
		
		BlockStmt body = JavaParser.parseBlock("{Stack<Integer> stack0 = new Stack<Integer>();"+
												"Integer integer0 = stack0.push((Integer) 0);}");
		CarvingResult cr = new CarvingResult(body, imports);
		
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer(0);
		TestScenario ts = tsg.generalizeTestToScenario(cr);
		
		System.out.println(ts.getScenario().toString());
		System.out.println(ts.getInputs());
		
		assertEquals(GenericTestScenario.class, ts.getClass());
	}
	
}
