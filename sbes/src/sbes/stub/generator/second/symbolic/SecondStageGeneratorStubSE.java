package sbes.stub.generator.second.symbolic;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.stmt.Statement;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import sbes.exceptions.GenerationException;
import sbes.logging.Logger;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.stub.Stub;
import sbes.stub.generator.AbstractStubGenerator;
import sbes.stub.generator.second.SecondStageGeneratorStub;

public class SecondStageGeneratorStubSE extends SecondStageGeneratorStub {

	private static final Logger logger = new Logger(SecondStageGeneratorStub.class);

	protected CarvingResult candidateES;
	protected List<Statement> equivalence;
	protected Stub stub;

	public SecondStageGeneratorStubSE(final List<TestScenario> scenarios, Stub stub, CarvingResult candidateES) {
		super(scenarios, stub, candidateES);
		this.stub = stub;
		this.candidateES = candidateES;
	}
	
	@Override
	public Stub generateStub() {
		CompilationUnit cu = null;
		try {
			cu = JavaParser.parse(new File("./Symbolic_Stub_Template.java"));
		} catch (ParseException | IOException e) {
			throw new GenerationException("Unable to find symbolc execution stub for second stage!", e);
		}
		
		
		
		return null;
	}

	
	@Override protected List<ImportDeclaration> getImports() 															{ return null; }
	@Override protected TypeDeclaration getClassDeclaration(Class<?> c) 												{ return null; }
	@Override protected List<BodyDeclaration> getClassFields(Method targetMethod, Class<?> c) 							{ return null; }
	@Override protected List<BodyDeclaration> getStubConstructor(Method targetMethod, Class<?> c) 						{ return null; }
	@Override protected List<BodyDeclaration> getAdditionalMethods(Method targetMethod, Method[] methods, Class<?> c)	{ return null; }
	@Override protected MethodDeclaration getSetResultsMethod(Method targetMethod) 										{ return null; }
	@Override protected MethodDeclaration getMethodUnderTest(Method targetMethod)										{ return null; }
	
}
