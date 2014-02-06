package sbes.testcase;

import java.io.IOException;
import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import sbes.logging.Logger;
import sbes.util.DirectoryUtils;
import sbes.util.NullWriter;

public class Compilation {

	private final static Logger logger = new Logger(Compilation.class);

	public static boolean compile(CompilationContext context) {
		logger.debug("Compiling file: " + context.getTestFilename());
		
		String testPath = DirectoryUtils.toPath(context.getTestDirectory(), context.getTestFilename());
		
		boolean succeed = true;
		try {
			final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
			final StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

			final String[] options = new String[] {
					"-d", ".",
					"-classpath", context.getClassPath()
			};

			/*
			 * Writer out
			 * JavaFileManager fileManager
			 * DiagnosticListener<? super JavaFileObject> diagnosticListener
			 * Iterable<String> options
			 * Iterable<String> classes
			 * Iterable<? extends JavaFileObject> compilationUnits
			 */
			compiler.getTask(NullWriter.NULL_WRITER, 
							 fileManager, 
							 diagnostics, 
							 Arrays.asList(options), 
							 null, 
							 fileManager.getJavaFileObjects(testPath)
							 ).call();

			if (diagnostics.getDiagnostics().size() > 0) {
				logger.debug("Compilation succeeded with either warnings or errors");
				for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
					if (diagnostic.getKind() == Kind.ERROR) {
						succeed = false;
						break;
					}
				}
				if (!succeed) {
					for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
						logger.error(diagnostic.toString());
					}
				} else {
					logger.debug("Compilation succeeded with warnings:");
					for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
						logger.debug(diagnostic.toString());
					}
				}
			} else {
				logger.debug("Compilation succeeded with no warnings");
			}

			fileManager.close(); // eventually throws exception
		}
		catch (IOException e) {
			logger.error(e.getMessage());
		}

		logger.debug("Compilation - Done");

		return succeed;
	}

}

