package sbes.ast.renamer;

import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class ClassOrInterfaceRenamer extends VoidVisitorAdapter<String> {

	private String oldName;
	private String newName;
	
	public ClassOrInterfaceRenamer(String oldName, String newName) {
		this.oldName = oldName;
		this.newName = newName;
	}
	
	
	@Override
	public void visit(ClassOrInterfaceType arg0, String arg1) {
		if (arg0.getName().equals(oldName)) {
			arg0.setName(newName);
		}
		super.visit(arg0, arg1);
	}
	
}
