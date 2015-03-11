package sbes.stub.generator.first;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import sbes.result.CarvingResult;
import sbes.scenario.GenericTestScenario;
import sbes.scenario.TestScenario;

public class FirstStageGeneratorFactoryTest {

	@Test
	public void test0()  throws Throwable  {
		LinkedList<TestScenario> linkedList0 = new LinkedList<TestScenario>();
		linkedList0.add(new TestScenario(new CarvingResult(new BlockStmt(), new ArrayList<ImportDeclaration>()), null, null));
		FirstStageGeneratorStub firstStageGeneratorStub0 = FirstStageGeneratorFactory.createGenerator((List<TestScenario>) linkedList0);
		assertNotNull(firstStageGeneratorStub0);
		assertEquals(FirstStageGeneratorStub.class, firstStageGeneratorStub0.getClass());
	}

	@Test
	public void test1()  throws Throwable  {
		LinkedList<TestScenario> linkedList0 = new LinkedList<TestScenario>();
		BlockStmt blockStmt0 = new BlockStmt();
		CarvingResult carvingResult0 = new CarvingResult(blockStmt0, (List<ImportDeclaration>) null);
		LinkedList<FieldDeclaration> linkedList1 = new LinkedList<FieldDeclaration>();
		TestScenario testScenario0 = new TestScenario(carvingResult0, blockStmt0, (List<FieldDeclaration>) linkedList1);
		linkedList0.add(new GenericTestScenario((CarvingResult) testScenario0, blockStmt0, (List<FieldDeclaration>) linkedList1, (String) null));
		FirstStageGeneratorStub firstStageGeneratorStubWithGenerics0 = FirstStageGeneratorFactory.createGenerator((List<TestScenario>) linkedList0);
		assertNotNull(firstStageGeneratorStubWithGenerics0);
		assertEquals(FirstStageGeneratorStubWithGenerics.class, firstStageGeneratorStubWithGenerics0.getClass());
	}

}
