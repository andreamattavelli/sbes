package sbes.stub;

import japa.parser.ast.CompilationUnit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import sbes.logging.Logger;

public class Stub {

	private static final Logger logger = new Logger(Stub.class);
	
	private String stubName;
	private CompilationUnit ast;

	public Stub(CompilationUnit cu, String stubName) {
		this.ast = cu;
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
		BufferedWriter out = null;
		try {
			String filename = directory + File.separator + stubName + ".java";
			out = new BufferedWriter(new FileWriter(filename));
			out.write(ast.toString());
			out.close();
		} catch(Throwable e) {
			logger.error("Unable to dump stub due to: " + e.getMessage());
		} finally{
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("Unable to correctly dump stub due to: " + e.getMessage());
				}
			}
		}
	}
	
}
