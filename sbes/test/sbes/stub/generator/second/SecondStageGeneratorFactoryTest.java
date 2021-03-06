package sbes.stub.generator.second;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BlockStmt;
import sbes.logging.Level;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.stub.Stub;
import sbes.stub.generator.first.FirstStageGeneratorStub;
import sbes.stub.generator.first.FirstStageGeneratorStubWithGenerics;
import sbes.stub.generator.second.alternative.SecondStageGeneratorStubALT;
import sbes.stub.generator.second.alternative.SecondStageGeneratorStubWithGenericsALT;
import sbes.stub.generator.second.symbolic.SecondStageGeneratorStubSE;
import sbes.stub.generator.second.symbolic.SecondStageGeneratorStubWithGenericsSE;

public class SecondStageGeneratorFactoryTest {

	@BeforeClass
	public static void setUp() throws Exception {
		Options.I().setLogLevel(Level.FATAL);
	}
	
	@Test
	public void test0()  throws Throwable  {
		Options.I().setSymbolicExecutionCounterexample(false);
		LinkedList<TestScenario> linkedList0 = new LinkedList<TestScenario>();
		FirstStageGeneratorStub firstStageGeneratorStub0 = new FirstStageGeneratorStub((List<TestScenario>) linkedList0);
		PackageDeclaration packageDeclaration0 = new PackageDeclaration((List<AnnotationExpr>) null, new NameExpr(""));
		CarvingResult carvingResult0 = new CarvingResult(new BlockStmt(), (List<ImportDeclaration>) new LinkedList<ImportDeclaration>());
		CompilationUnit compilationUnit0 = new CompilationUnit(packageDeclaration0, null, null, null);
		Stub stub0 = new Stub(compilationUnit0, "");
		TestScenario testScenario0 = new TestScenario(carvingResult0, new BlockStmt(), null);
		SecondStageGeneratorStub secondStageGeneratorStub0 = SecondStageGeneratorFactory.createGenerator(firstStageGeneratorStub0, stub0, (CarvingResult) testScenario0);
		assertNotNull(secondStageGeneratorStub0);
		assertEquals(SecondStageGeneratorStub.class, secondStageGeneratorStub0.getClass());
	}

	@Test
	public void test1()  throws Throwable  {
		Options.I().setSymbolicExecutionCounterexample(false);
		LinkedList<TestScenario> linkedList0 = new LinkedList<TestScenario>();
		FirstStageGeneratorStubWithGenerics firstStageGeneratorStubWithGenerics0 = new FirstStageGeneratorStubWithGenerics((List<TestScenario>) linkedList0);
		CarvingResult carvingResult0 = new CarvingResult((BlockStmt) null, (List<ImportDeclaration>) null);
		TestScenario testScenario0 = new TestScenario(carvingResult0, null, new LinkedList<FieldDeclaration>());
		linkedList0.add(testScenario0);
		SecondStageGeneratorStubWithGenerics secondStageGeneratorStubWithGenerics0 = (SecondStageGeneratorStubWithGenerics)SecondStageGeneratorFactory.createGenerator((FirstStageGeneratorStub) firstStageGeneratorStubWithGenerics0, (Stub) null, (CarvingResult) testScenario0);
		assertNotNull(secondStageGeneratorStubWithGenerics0);
		assertEquals(SecondStageGeneratorStubWithGenerics.class, secondStageGeneratorStubWithGenerics0.getClass());
	}
	
	@Test
	public void test2()  throws Throwable  {
		Options.I().setSymbolicExecutionCounterexample(true);
		LinkedList<TestScenario> linkedList0 = new LinkedList<TestScenario>();
		FirstStageGeneratorStub firstStageGeneratorStub0 = new FirstStageGeneratorStub((List<TestScenario>) linkedList0);
		PackageDeclaration packageDeclaration0 = new PackageDeclaration((List<AnnotationExpr>) null, new NameExpr(""));
		CarvingResult carvingResult0 = new CarvingResult(new BlockStmt(), (List<ImportDeclaration>) new LinkedList<ImportDeclaration>());
		CompilationUnit compilationUnit0 = new CompilationUnit(packageDeclaration0, null, null, null);
		Stub stub0 = new Stub(compilationUnit0, "");
		TestScenario testScenario0 = new TestScenario(carvingResult0, new BlockStmt(), null);
		SecondStageGeneratorStubSE secondStageGeneratorStub0 = (SecondStageGeneratorStubSE) SecondStageGeneratorFactory.createGenerator(firstStageGeneratorStub0, stub0, (CarvingResult) testScenario0);
		assertNotNull(secondStageGeneratorStub0);
		assertEquals(SecondStageGeneratorStubSE.class, secondStageGeneratorStub0.getClass());
	}

	@Test
	public void test3()  throws Throwable  {
		Options.I().setSymbolicExecutionCounterexample(true);
		LinkedList<TestScenario> linkedList0 = new LinkedList<TestScenario>();
		FirstStageGeneratorStubWithGenerics firstStageGeneratorStubWithGenerics0 = new FirstStageGeneratorStubWithGenerics((List<TestScenario>) linkedList0);
		CarvingResult carvingResult0 = new CarvingResult((BlockStmt) null, (List<ImportDeclaration>) null);
		TestScenario testScenario0 = new TestScenario(carvingResult0, null, new LinkedList<FieldDeclaration>());
		linkedList0.add(testScenario0);
		SecondStageGeneratorStubWithGenericsSE secondStageGeneratorStubWithGenerics0 = (SecondStageGeneratorStubWithGenericsSE)SecondStageGeneratorFactory.createGenerator((FirstStageGeneratorStub) firstStageGeneratorStubWithGenerics0, (Stub) null, (CarvingResult) testScenario0);
		assertNotNull(secondStageGeneratorStubWithGenerics0);
		assertEquals(SecondStageGeneratorStubWithGenericsSE.class, secondStageGeneratorStubWithGenerics0.getClass());
	}
	
	@Test
	public void test4()  throws Throwable  {
		Options.I().setSymbolicExecutionCounterexample(false);
		Options.I().setAlternativeCounterexample(true);
		LinkedList<TestScenario> linkedList0 = new LinkedList<TestScenario>();
		FirstStageGeneratorStub firstStageGeneratorStub0 = new FirstStageGeneratorStub((List<TestScenario>) linkedList0);
		PackageDeclaration packageDeclaration0 = new PackageDeclaration((List<AnnotationExpr>) null, new NameExpr(""));
		CarvingResult carvingResult0 = new CarvingResult(new BlockStmt(), (List<ImportDeclaration>) new LinkedList<ImportDeclaration>());
		CompilationUnit compilationUnit0 = new CompilationUnit(packageDeclaration0, null, null, null);
		Stub stub0 = new Stub(compilationUnit0, "");
		TestScenario testScenario0 = new TestScenario(carvingResult0, new BlockStmt(), null);
		SecondStageGeneratorStubALT secondStageGeneratorStub0 = (SecondStageGeneratorStubALT) SecondStageGeneratorFactory.createGenerator(firstStageGeneratorStub0, stub0, (CarvingResult) testScenario0);
		assertNotNull(secondStageGeneratorStub0);
		assertEquals(SecondStageGeneratorStubALT.class, secondStageGeneratorStub0.getClass());
	}

	@Test
	public void test5()  throws Throwable  {
		Options.I().setSymbolicExecutionCounterexample(false);
		Options.I().setAlternativeCounterexample(true);
		LinkedList<TestScenario> linkedList0 = new LinkedList<TestScenario>();
		FirstStageGeneratorStubWithGenerics firstStageGeneratorStubWithGenerics0 = new FirstStageGeneratorStubWithGenerics((List<TestScenario>) linkedList0);
		CarvingResult carvingResult0 = new CarvingResult((BlockStmt) null, (List<ImportDeclaration>) null);
		TestScenario testScenario0 = new TestScenario(carvingResult0, null, new LinkedList<FieldDeclaration>());
		linkedList0.add(testScenario0);
		SecondStageGeneratorStubWithGenericsALT secondStageGeneratorStubWithGenerics0 = (SecondStageGeneratorStubWithGenericsALT)SecondStageGeneratorFactory.createGenerator((FirstStageGeneratorStub) firstStageGeneratorStubWithGenerics0, (Stub) null, (CarvingResult) testScenario0);
		assertNotNull(secondStageGeneratorStubWithGenerics0);
		assertEquals(SecondStageGeneratorStubWithGenericsALT.class, secondStageGeneratorStubWithGenerics0.getClass());
	}

}
