package sbes.scenario;

import static org.junit.Assert.*;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.util.ArrayList;

import org.junit.Test;

import sbes.Options;
import sbes.testcase.CarvingResult;

public class TestScenarioGeneratorTest {

	@Test
	public void test() {
		try {
			BlockStmt block = JavaParser.parseBlock("{ SingleGraph sg = new SingleGraph(\"graph1\");"+
					"sg.addNode(\"0\");"+
					"sg.addNode(\"1\");"+
					"sg.addNode(\"2\");"+
					"sg.addNode(\"3\");"+
					"sg.addNode(\"4\");"+
					""+
					"sg.addEdge(\"edge0\", \"0\", \"1\");"+
					"sg.addEdge(\"edge1\", \"1\", \"2\");"+
					"sg.addEdge(\"edge2\", \"2\", \"3\");"+
					"sg.addEdge(\"edge3\", \"3\", \"4\");"+
					"sg.addEdge(\"edge4\", \"4\", \"1\");"+
					""+
					"Path path = new Path();"+
					"path.setRoot(sg.getNode(\"0\"));"+
					"path.add(sg.getEdge(\"edge0\"));"+
					"path.add(sg.getEdge(\"edge1\"));"+
					"path.add(sg.getEdge(\"edge2\"));"+
					"path.add(sg.getEdge(\"edge3\"));"+
					"path.add(sg.getEdge(\"edge4\"));"+
					"int result = path.getEdgeCount();}");
			
			Options.I().setMethodSignature("org.graphstream.graph.Path.getEdgeCount[]");
			
			CarvingResult result = new CarvingResult(block, new ArrayList<ImportDeclaration>());
			
			TestScenario ts = TestScenarioGenerator.getInstance().carvedTestToScenario(result);
			
			System.out.println(ts.getScenario().toString());
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test2() throws Exception {
		try {
			BlockStmt block = JavaParser.parseBlock("{DefaultGraph defaultGraph0 = new DefaultGraph(\"cg %s%n\");"+
					"MultiNode multiNode0 = new MultiNode((AbstractGraph) defaultGraph0, \"cg %s%n\");"+
					"AbstractEdge abstractEdge0 = new AbstractEdge(\"cg %s%n\", (AbstractNode) multiNode0, (AbstractNode) multiNode0, true);"+
					"MultiNode multiNode1 = (MultiNode)abstractEdge0.getNode0();}");
			
			Options.I().setMethodSignature("org.graphstream.graph.implementations.AbstractEdge.getNode0[]");
			
			CarvingResult result = new CarvingResult(block, new ArrayList<ImportDeclaration>());
			
			TestScenario ts = TestScenarioGenerator.getInstance().carvedTestToScenario(result);
			
			System.out.println(ts.getScenario().toString());
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}