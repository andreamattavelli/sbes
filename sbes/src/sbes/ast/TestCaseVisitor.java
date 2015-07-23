package sbes.ast;

import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class TestCaseVisitor extends VoidVisitorAdapter<Void> {
	private final List<MethodDeclaration> tests;
	
	public TestCaseVisitor() {
		this.tests = new ArrayList<MethodDeclaration>();
	}
	
	@Override
	public void visit(final MethodDeclaration n, final Void arg) {
		if (!n.getName().equals("initEvoSuiteFramework") &&
				!n.getName().equals("set") &&
				!n.getName().equals("get") &&
				!n.getName().equals("getValue") &&
				!n.getName().equals("getNewInstance")) {
			this.tests.add(n);
		}
		super.visit(n, arg);
	}
	
	public List<MethodDeclaration> getTests() {
		return this.tests;
	}
	
}