package sbes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import sbes.logging.Logger;
import sbes.scenario.TestScenario;
import sbes.scenario.TestScenarioGenerator;
import sbes.stub.Stub;
import sbes.stub.generator.FirstPhaseStubStrategy;
import sbes.stub.generator.StubGenerator;
import sbes.util.ClasspathHandler;

public class SBES {

	private static final Logger logger = new Logger(SBES.class);
	
	public static void main(String args[]) {
		logger.info("SBES started");
		final Options arguments = Options.I();
		final CmdLineParser parser = new CmdLineParser(arguments);

		try {
			parser.parseArgument(processArgs(args));
		} catch (CmdLineException e) {
			printUsage(parser);
			System.exit(-1);
		}

		try {
			ClasspathHandler.checkClasspath();

			List<TestScenario> initialScenarios = TestScenarioGenerator.generateTestScenarios();
			
			System.out.println(initialScenarios.toString());
			
			StubGenerator firstPhaseGenerator = new FirstPhaseStubStrategy();
			Stub firstPhaseStub = firstPhaseGenerator.generateStub();

			firstPhaseStub.dumpStub(".");
			
			logger.info("SBES ended successfully");
		}
		catch (SBESException e) {
			logger.fatal("Execution aborted due: " + e.getMessage());
		}
	}

	private static void printUsage(final CmdLineParser parser) {
		System.err.println("java Main <options>");
		System.err.println("<options> are:");
		// print the list of available options
		parser.printUsage(System.err);
	}

	private static String[] processArgs(final String[] args) {
		Pattern argPattern = Pattern.compile("(--[a-zA-Z_-]+)=(.*)");
		Pattern quotesPattern = Pattern.compile("^['\"](.*)['\"]$");
		List<String> processedArgs = new ArrayList<String>();

		for (String arg : args) {
			Matcher matcher = argPattern.matcher(arg);
			if (matcher.matches()) {
				processedArgs.add(matcher.group(1));

				String value = matcher.group(2);
				Matcher quotesMatcher = quotesPattern.matcher(value);
				if (quotesMatcher.matches()) {
					processedArgs.add(quotesMatcher.group(1));
				} else {
					processedArgs.add(value);
				}
			} else {
				processedArgs.add(arg);
			}
		}

		return processedArgs.toArray(new String[0]);
	}
}
