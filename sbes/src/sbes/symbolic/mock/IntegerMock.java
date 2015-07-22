package sbes.symbolic.mock;

import sbes.symbolic.CorrespondenceHandler;

public final class IntegerMock extends CorrespondenceHandler  {

	private int value;

	public IntegerMock(int value) {
		this.value = value;
	}
	
	public int intValue() {
		return value;
	}

	public int hashCode() {
		return value;
	}

	public boolean equals(Object obj) {
		if (obj instanceof IntegerMock) {
			return value == ((IntegerMock) obj).value;
		}
		return false;
	}

	public static boolean mirrorFinalConservative(IntegerMock i1, IntegerMock i2) {
		return i1.value == i2.value;
	}

	public static boolean mirrorInitialConservative(IntegerMock i1,	IntegerMock i2) {
		boolean ok = CorrespondenceHandler.doOrMayCorrespondInInitialState(i1, i2);
		if(!ok) return false;			
		
		if(!i1.mustVisitDuringAssume()) return true;
		
		ok = CorrespondenceHandler.setAsCorrespondingInInitialState(i1, i2);
		if(!ok) return false;
		
 		return i1.value == i2.value;
	}

}