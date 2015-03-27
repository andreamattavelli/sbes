package sbes.scenario.generalizer;

import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.Statement;

import java.lang.reflect.Method;

import sbes.ast.VariableUseVisitor;
import sbes.ast.inliner.FieldVariablesToInline;
import sbes.ast.inliner.Inliner;
import sbes.ast.inliner.PrimitiveVariablesToInline;
import sbes.ast.inliner.StringVariablesToInline;
import sbes.ast.renamer.ClassOrInterfaceRenamer;
import sbes.ast.renamer.ExpectedResultCounterexampleRenamer;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.util.ClassUtils;

public class CounterexampleGeneralizer extends AbstractGeneralizer {

	private static final Logger logger = new Logger(CounterexampleGeneralizer.class);

	public TestScenario counterexampleToTestScenario(CarvingResult carvedCounterexample) {
		logger.debug("Generalizing carved counterexample");
		cleanCounterexample(carvedCounterexample);
		return super.generalizeToTestScenario(carvedCounterexample);
	}

	@Override
	protected String getAndRenameExpectedResult(BlockStmt cloned, Method targetMethod, String methodName, int index) {
		ExpectedResultCounterexampleRenamer cerv = new ExpectedResultCounterexampleRenamer(targetMethod, index);
		cerv.visit(cloned, null);
		return cerv.getExpectedState();
	}
	
	private void cleanCounterexample(CarvingResult counterexample) {
		String classname = ClassUtils.getSimpleClassname(Options.I().getTargetMethod());
		ClassOrInterfaceRenamer cv = new ClassOrInterfaceRenamer(classname + "_Stub_2", classname);
		cv.visit(counterexample.getBody(), null);

		for (int i = 0; i < counterexample.getImports().size(); i++) {
			ImportDeclaration importDecl = counterexample.getImports().get(i);
			if (importDecl.getName().getName().endsWith(classname + "_Stub_2")) {
				counterexample.getImports().remove(importDecl);
				i--;
			}
		}
		
		PrimitiveVariablesToInline pvi = new PrimitiveVariablesToInline();
		pvi.visit(counterexample.getBody(), null);
		
		StringVariablesToInline svi = new StringVariablesToInline();
		svi.visit(counterexample.getBody(), null);
		
		FieldVariablesToInline fvi = new FieldVariablesToInline();
		fvi.visit(counterexample.getBody(), null);
		
		for (VariableDeclarator vd : pvi.getToInline()) {
			new Inliner().visit(counterexample.getBody(), vd);
		}
		for (VariableDeclarator vd : svi.getToInline()) {
			new Inliner().visit(counterexample.getBody(), vd);
		}
		for (VariableDeclarator vd : fvi.getToInline()) {
			new Inliner().visit(counterexample.getBody(), vd);
		}
		
		for (int i = 0; i < counterexample.getBody().getStmts().size(); i++) {
			Statement stmt = counterexample.getBody().getStmts().get(i);
			if (stmt instanceof ExpressionStmt) {
				Expression e = ((ExpressionStmt) stmt).getExpression();
				if (e instanceof VariableDeclarationExpr) {
					VariableDeclarationExpr vde = (VariableDeclarationExpr) e;
					VariableDeclarator var = vde.getVars().get(0); // safe
					
					VariableUseVisitor vuv = new VariableUseVisitor(var.getId().getName());
					vuv.visit(counterexample.getBody(), null);
					if (!vuv.isUsed()) {
						counterexample.getBody().getStmts().remove(i);
						i--;
					}
				}
			}
		}
	}

}
