package sbes.ast.renamer;

import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class StubRenamer extends VoidVisitorAdapter<Void> {

	private final String oldName;
	private final String newName;
	
	public StubRenamer(final String oldName, final String newName) {
		this.oldName = oldName;
		this.newName = newName;
	}
	
	
	@Override
	public void visit(ClassOrInterfaceType arg0, Void arg1) {
		if (arg0.getName().equals(oldName)) {
			arg0.setName(newName);
		}
		super.visit(arg0, arg1);
	}
}
