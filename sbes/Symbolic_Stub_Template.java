package stack.util;

import jbse.meta.Analysis;
import jbse.meta.annotations.ConservativeRepOk;
import sbes.symbolic.mock.IntegerMock;
import sbes.symbolic.mock.Stack;

@SuppressWarnings("unused")
public class Symbolic_Stub_Template {

	private interface FakeVariable {}
	
	Stack<IntegerMock> v_Stack1;
	Stack<IntegerMock> v_Stack2;
	FakeVariable forceConservativeRepOk;
	FakeVariable forceConservativeRepOk2;
	FakeVariable forceConservativeRepOk3;

	@ConservativeRepOk
	boolean mirrorInitialConservative() {
		if (Analysis.isResolved(this, "v_Stack1") || Analysis.isResolved(this, "v_Stack2")) {
			if (v_Stack1 == null ^ v_Stack2 == null) {
				return false;
			} else if (v_Stack1 != null && v_Stack2 != null) {
				return Stack.mirrorEachOtherInitially_conservative(v_Stack1, v_Stack2);
			}
		}
		return true;
	}

	boolean mirrorFinalConservative(){
		if (this.v_Stack1 == null ^ v_Stack2 == null) {
			return false;
		} else if (this.v_Stack1 != null && v_Stack2 != null) {
			return Stack.mirrorEachOtherAtEnd(v_Stack1, v_Stack2);
		}
		return true;
	}
		
}
