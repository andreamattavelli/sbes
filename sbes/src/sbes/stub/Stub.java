package sbes.stub;

import japa.parser.ast.CompilationUnit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import sbes.logging.Logger;
import sbes.util.IOUtils;

public class Stub {

	private static final Logger logger = new Logger(Stub.class);
	
	private String stubName;
	private String packageName;
	private CompilationUnit ast;

	public Stub(CompilationUnit cu, String stubName) {
		this.ast = cu;
		this.packageName = cu.getPackage().getName().toString();
		this.stubName = stubName;
	}
	
	public String getStubName() {
		return stubName;
	}

	public void setStubName(String stubName) {
		this.stubName = stubName;
	}

	public CompilationUnit getAst() {
		return ast;
	}

	public void setAst(CompilationUnit ast) {
		this.ast = ast;
	}
	
	public void dumpStub(String directory) {
		String dir = IOUtils.concatFilePath(directory, IOUtils.fromCanonicalToPath(packageName));
		try {
			File file = new File(dir);
			if (!file.exists()) {
				file.mkdirs();
			}
		} catch(Throwable e) {
			logger.error("Unable to dump stub due to: " + e.getMessage());
		}
		
		String filename = IOUtils.concatFilePath(directory, IOUtils.fromCanonicalToPath(packageName), stubName + ".java");
		try (BufferedWriter out = new BufferedWriter(new FileWriter(filename))) {
			out.write(ast.toString());
		} catch(Throwable e) {
			logger.error("Unable to dump stub due to: " + e.getMessage());
		}
	}
	
}
