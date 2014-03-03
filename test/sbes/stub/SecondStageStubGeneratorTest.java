package sbes.stub;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.Comment;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BlockStmt;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.stub.generator.SecondStageStubGenerator;

public class SecondStageStubGeneratorTest {
	
	private List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
	private Stub stub;
	
	@Before
	public void setUp() throws Exception {
		List<TypeDeclaration> decls = new ArrayList<TypeDeclaration>(); 
		List<Comment> comments = new ArrayList<Comment>();

		Options.I().setClassesPath("/Users/andrea/Uni/PhD/Workspaces/sbes-synthesis/Stack-UseCase/bin");
		Options.I().setMethodSignature("stack.util.Stack.push(Object)");

		stub = new Stub(new CompilationUnit(new PackageDeclaration(new NameExpr("asda")), imports, decls, comments), "Stack_Stub");
	}
	
	@Test
	public void test() throws ParseException {
		BlockStmt body = JavaParser.parseBlock("{	Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0;"+
				"stack_Stub0.addElement(integer0);"+
				"Integer[] integerArray0 = new Integer[1];"+
				"integerArray0[0] = integer0;"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageStubGenerator sssg = new SecondStageStubGenerator(stub, candidateES);
		Stub second = sssg.generateStub();
		System.out.println(second.getAst().toString());
		System.out.println("===========================================================");
	}

	@Test
	public void test2() throws ParseException {
		BlockStmt body = JavaParser.parseBlock("{	Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer[] integerArray0 = new Integer[4];"+
				"Integer integer0 = Stack_Stub.ELEMENT_0;"+
				"integerArray0[0] = integer0;"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.addElement(integer0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageStubGenerator sssg = new SecondStageStubGenerator(stub, candidateES);
		Stub second = sssg.generateStub();
		System.out.println(second.getAst().toString());
		System.out.println("===========================================================");
	}

	@Test
	public void test3() throws ParseException {
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0;"+
				"boolean[] booleanArray0 = stack_Stub0.add(integer0);"+
				"Integer[] integerArray0 = new Integer[3];"+
				"integerArray0[0] = integer0;"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageStubGenerator sssg = new SecondStageStubGenerator(stub, candidateES);
		Stub second = sssg.generateStub();
		System.out.println(second.getAst().toString());
		System.out.println("===========================================================");
	}
	
	@Test
	public void test4() throws ParseException {
		BlockStmt body = JavaParser.parseBlock("{Stack_Stub stack_Stub0 = new Stack_Stub();"+
				"Integer integer0 = Stack_Stub.ELEMENT_0;"+
				"Integer[] integerArray0 = new Integer[5];"+
				"integerArray0[0] = integer0;"+
				"integerArray0[3] = integer0;"+
				"stack_Stub0.addElement(integerArray0[3]);"+
				"stack_Stub0.set_results(integerArray0);"+
				"stack_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageStubGenerator sssg = new SecondStageStubGenerator(stub, candidateES);
		Stub second = sssg.generateStub();
		System.out.println(second.getAst().toString());
		System.out.println("===========================================================");
	}
	
	@Test
	public void test5() throws ParseException {
		Options.I().setClassesPath("/Users/andrea/Uni/PhD/Workspaces/sbes-synthesis/sbes/gs-core-1.2.jar");
		Options.I().setMethodSignature("org.graphstream.graph.implementations.AbstractEdge.getNode0()");
		
		List<TypeDeclaration> decls = new ArrayList<TypeDeclaration>(); 
		List<Comment> comments = new ArrayList<Comment>();
		
		stub = new Stub(new CompilationUnit(new PackageDeclaration(new NameExpr("asda")), imports, decls, comments), "AbstractEdge_Stub");
		
		BlockStmt body = JavaParser.parseBlock("{AbstractEdge_Stub abstractEdge_Stub0 = new AbstractEdge_Stub();"+
				"Node[] nodeArray0 = abstractEdge_Stub0.getSourceNode();"+
				"abstractEdge_Stub0.set_results(nodeArray0);"+
				"abstractEdge_Stub0.method_under_test();}");

		CarvingResult candidateES = new CarvingResult(body, imports);
		SecondStageStubGenerator sssg = new SecondStageStubGenerator(stub, candidateES);
		Stub second = sssg.generateStub();
		System.out.println(second.getAst().toString());
		System.out.println("===========================================================");
	}

}
