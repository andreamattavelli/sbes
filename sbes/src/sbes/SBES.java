package sbes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import sbes.exceptions.GenerationException;
import sbes.exceptions.SBESException;
import sbes.exceptions.WorkerException;
import sbes.logging.Logger;
import sbes.option.Options;
import sun.misc.Signal;

public class SBES {

	private static final Logger logger = new Logger(SBES.class);

	public static void main(final String args[]) {
		final Options arguments = Options.I();
		final CmdLineParser parser = new CmdLineParser(arguments);

		try {
			parser.parseArgument(processArgs(args));
			Options.I().checkConsistency();
		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			printUsage(parser);
			System.exit(-1);
		}

		try {
			final SBESShutdownInterceptor shutdown = new SBESShutdownInterceptor();
			Signal.handle(new Signal("INT"), shutdown);
			
			final SBESManager generator = new SBESManager();
			generator.generate();
		} catch (SBESException | GenerationException | WorkerException e) {
			logger.fatal("Execution aborted due: " + e.getMessage());
		}
	}

	private static void printUsage(final CmdLineParser parser) {
		System.err.println("java Main <options>");
		System.err.println("<options> are:");
		// print the list of available options
		parser.setUsageWidth(140);
		parser.printUsage(System.err);
	}

	private static String[] processArgs(final String[] args) {
		final Pattern argPattern = Pattern.compile("(-[a-zA-Z_-]+)=(.*)");
		final Pattern quotesPattern = Pattern.compile("^['\"](.*)['\"]$");
		final List<String> processedArgs = new ArrayList<String>();

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
