package sbes.ast;

import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class TestScenarioCleaner extends VoidVisitorAdapter<Void>{

	private String originalClassname;
	private String newClassname;
	
	public TestScenarioCleaner(String originalClassname, String newClassname) {
		this.originalClassname = originalClassname;
		this.newClassname = newClassname;
	}

	@Override
	public void visit(ClassOrInterfaceType arg0, Void arg1) {
		if (arg0.getName().startsWith(originalClassname)) {
			String newClass = newClassname;
			if (arg0.getName().contains("<")) {
				String concrete = arg0.getName().substring(arg0.getName().indexOf('<'));
				newClass = newClass + concrete;
			}
			arg0.setName(newClass);
		}
		super.visit(arg0, arg1);
	}
	
}

