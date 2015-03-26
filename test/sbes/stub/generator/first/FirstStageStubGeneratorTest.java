package sbes.stub.generator.first;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.stmt.BlockStmt;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import sbes.logging.Level;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.scenario.TestScenarioWithGenerics;
import sbes.scenario.generalizer.TestScenarioGeneralizer;
import sbes.stub.Stub;
import sbes.testcase.Compilation;
import sbes.testcase.CompilationContext;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FirstStageStubGeneratorTest {
	
	private List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
	
	private void setUp(String classesPath, String methodSignature, String stubName) {
		Options.I().setClassesPath(classesPath);
		Options.I().setTargetMethod(methodSignature);
		Options.I().setLogLevel(Level.ERROR);
	}
	
	protected void assertAndPrint(String actual, String expected) {
		assertEquals(expected.replaceAll("\\s|\t|\n", ""), actual.replaceAll("\\s|\t|\n", ""));
		System.out.println(actual);
		System.out.println("====================================================");
	}
	
	protected void assertThatCompiles(String packagename, String filename, String classesPath) {
		CompilationContext compilationContext = new CompilationContext(
				"./test/resources/compilation/" + packagename, 
				filename + ".java", 
				"./test/resources/compilation", 
				classesPath);
		
		boolean compilationSucceeded = Compilation.compile(compilationContext);
		System.out.println("Compiles? " + compilationSucceeded);
		System.out.println();
		assertTrue(compilationSucceeded);
	}
	
	@AfterClass
	public static void tearDown() {
		try {
			Path directory = Paths.get("./test/resources/compilation");
			if (!directory.toFile().exists()) {
				return;
			}
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test01() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.put(Object,Object)", "ArrayListMultimap_Stub");
		
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.List"), false, false));
		imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.util.Collections"), false, false));
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"ArrayListMultimap<Integer, String> arrayListMultimap0 = ArrayListMultimap.create();"+
				"Integer integer0 = new Integer(234);"+
				"List<String> list0 = Collections.emptyList();"+
				"list0.add(\"pippo\");"+
				"boolean boolean0 = arrayListMultimap0.putAll(integer0, list0);"+
				"Integer integer3 = new Integer(-1698);"+
				"String string0 = \"pluto\";"+
				"boolean boolean3 = arrayListMultimap0.put(integer3, string0);"+
				"}");
		
		CarvingResult carvedScenario = new CarvingResult(body, imports);
		
		List<TestScenario> scenarios = new ArrayList<>();
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(carvedScenario);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		assertEquals(2, ts.getInputAsFields().size());
		
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(2, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("String"));
		List<String> values = new ArrayList<>(tswg.getGenericToConcreteClasses().values());
		assertEquals("Integer", values.get(0));
		assertEquals("String", values.get(1));
		
		scenarios.add(tswg);
		
		// preconditions ok
		
		FirstStageGeneratorStubWithGenerics fssg = new FirstStageGeneratorStubWithGenerics(scenarios);
		Stub first = fssg.generateStub();
		first.dumpStub("./test/resources/compilation");
		assertThatCompiles("com/google/common/collect", first.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
	}
	
	@Test
	public void test02() throws ParseException {
		setUp("./test/resources/guava-12.0.1.jar", "com.google.common.collect.ArrayListMultimap.create()", "ArrayListMultimap_Stub");
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				 "ArrayListMultimap<Integer, String> arrayListMultimap0 = ArrayListMultimap.create();" +
				"}");
		
		CarvingResult carvedScenario = new CarvingResult(body, imports);
		
		List<TestScenario> scenarios = new ArrayList<>();
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		TestScenario ts = tsg.testToTestScenario(carvedScenario);
		assertEquals(TestScenarioWithGenerics.class, ts.getClass());
		assertEquals(0, ts.getInputAsFields().size());
		
		TestScenarioWithGenerics tswg = (TestScenarioWithGenerics) ts;
		assertEquals(2, tswg.getGenericToConcreteClasses().size());
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("Integer"));
		assertTrue(tswg.getGenericToConcreteClasses().containsValue("String"));
		List<String> values = new ArrayList<>(tswg.getGenericToConcreteClasses().values());
		assertEquals("Integer", values.get(0));
		assertEquals("String", values.get(1));
		
		scenarios.add(tswg);
		
		// preconditions ok
		
		FirstStageGeneratorStubWithGenerics fssg = new FirstStageGeneratorStubWithGenerics(scenarios);
		Stub first = fssg.generateStub();
		first.dumpStub("./test/resources/compilation");
		assertThatCompiles("com/google/common/collect", first.getStubName(), "./test/resources/guava-12.0.1.jar:./bin");
	}
	
}
