package sbes.result;

import static org.junit.Assert.*;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.stmt.BlockStmt;

import org.junit.Before;
import org.junit.Test;

import sbes.logging.Level;
import sbes.option.Options;

public class EquivalenceRepositoryTest {

	@Before
	public void setUp() throws Exception {
		Options.I().setLogLevel(Level.FATAL);
		Options.I().setTargetMethod("com.google.common.collect.LinkedListMultimap.create()");
		Options.I().setClassesPath("./test/resources/guava-12.0.1.jar");
		EquivalenceRepository.reset();
	}

	@Test
	public void test() throws ParseException {
		EquivalenceRepository repository = EquivalenceRepository.getInstance();
		
		assertNotNull(repository);
		assertEquals(0, repository.getExcluded().size());
		
		BlockStmt body = JavaParser.parseBlock(
				"{"+
				"int int0 = 0;"+
				"Integer integer0 = new Integer(int0);"+
				"com.google.common.collect.LinkedListMultimap<Integer, String> actual_result = clone.create((int) integer0);"+
				"}");
		EquivalentSequence equivalence = new EquivalentSequence(body.getStmts());
		repository.addEquivalence(equivalence);
		
		assertEquals(1, repository.getExcluded().size());
		assertNotNull(repository.getExcluded().get(0));
		assertEquals("public static com.google.common.collect.LinkedListMultimap " +
					 "com.google.common.collect.LinkedListMultimap.create(int)", repository.getExcluded().get(0).toString());
		
		body = JavaParser.parseBlock(
				"{"+
				"int int0 = 2;"+
				"Integer integer0 = new Integer(int0);"+
				"com.google.common.collect.LinkedListMultimap<Integer, String> actual_result = clone.create((int) integer0);"+
				"}");
		repository.addEquivalence(equivalence);
		
		assertEquals(2, repository.getExcluded().size());
		assertNotNull(repository.getExcluded().get(1));
		assertEquals("public static com.google.common.collect.LinkedListMultimap " +
					 "com.google.common.collect.LinkedListMultimap.create(com.google.common.collect.Multimap)", repository.getExcluded().get(1).toString());
		
	}

}
