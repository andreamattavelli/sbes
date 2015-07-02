package stack.util;

import jbse.meta.Analysis;
import jbse.meta.annotations.ConservativeRepOk;
import sbes.symbolic.mock.IntegerMock;
import sbes.symbolic.mock.Stack;

public class Symbolic_Stub_Template {

	Stack<IntegerMock> v_Stack1;
	Stack<IntegerMock> v_Stack2;

	@ConservativeRepOk
	boolean mirrorInitialConservative() {
		boolean ok = true;
		if (Analysis.isResolved(this, "v_Stack1") && Analysis.isResolved(this, "v_Stack2")) {
			if (v_Stack1 == null) {
				ok = v_Stack2 == null;
			} else if (v_Stack2 == null) {
				ok = false;
			} else {
				ok = Stack.mirrorInitialConservative(v_Stack1, v_Stack2);
			}
			if (!ok) {
				return false;
			}
		}
		return true;
	}

	boolean mirrorInitialSemiConservative(){
		boolean ok = true;
		if (Analysis.isResolved(this, "v_Stack1") || Analysis.isResolved(this, "v_Stack2")) {
			if (this.v_Stack1 == null) {
				ok = this.v_Stack2 == null;
			} else if (this.v_Stack2 == null) {
				ok = false;
			} else {
				ok = Stack.mirrorInitialSemiConservative(v_Stack1, v_Stack2);
			}
			if (!ok) {
				return false;
			}
		}
		return true;
	}

	boolean mirrorFinalConservative(){
		boolean ok = true;
		if (Analysis.isResolved(this, "v_Stack1") && Analysis.isResolved(this, "v_Stack2")) {
			if (this.v_Stack1 == null) {
				ok = this.v_Stack2 == null;
			} else if (this.v_Stack2 == null) {
				ok = false;
			} else {
				ok = v_Stack1.mirrorFinalConservative();
				if (!ok) {
					return false;
				}
				ok = v_Stack2.mirrorFinalConservative();
			}
			if (!ok) {
				return false;
			}
		}
		return true;
	}
		
}
