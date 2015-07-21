package sbes.symbolic;

import jbse.meta.Analysis;

public class CorrespondenceHandler {

	private Object isVisitedDuringAssume;
	private Object isVisitedDuringAssert;
	private Object correspondingInInitialState;

	protected static boolean setAsCorrespondingInInitialState(CorrespondenceHandler obj1, CorrespondenceHandler obj2) {
		if (obj1 == null && obj2 == null) {
			return true; // nothing to be set
		} else if (obj1 == null || obj2 == null) {
			return false; // only one object, missing corresponding one
		}
		/* else: both not null */

		if (obj1 == obj2) {
			return false; // corresponding must be a distinct object
		}
		if (!doOrMayCorrespondInInitialState(obj1, obj2)) {
			return false; // cannot be set as corresponding
		}

		obj1.correspondingInInitialState = obj2;
		obj2.correspondingInInitialState = obj1;

		return true;
	}

	protected static boolean doOrMayCorrespondInInitialState(CorrespondenceHandler obj1, CorrespondenceHandler obj2) {
		if (obj1 == null || obj2 == null) {
			throw new RuntimeException("Requires non null inputs");
		}

		if (!Analysis.isResolved(obj1, "correspondingInInitialState") && !Analysis.isResolved(obj2, "correspondingInInitialState")) {
			return true; // neither resolved: symbolic objects that may be set to correspond with each other
		}
		if (!Analysis.isResolved(obj1, "correspondingInInitialState") || !Analysis.isResolved(obj2, "correspondingInInitialState")) {
			return false; // only one resolved: cannot correspond anymore
		} else {
			return obj1.correspondingInInitialState == obj2;
		}
	}

	protected boolean hasCorrespondingObjectInInitialState() {
		return Analysis.isResolved(this, "correspondingInInitialState") && correspondingInInitialState != null;
	}

	protected Object getCorrespondingObjectInInitialState() {
		if (hasCorrespondingObjectInInitialState()) {
			return correspondingInInitialState;
		} else {
			return null;
		}
	}



	protected boolean mustVisitDuringAssume() {
		if (Analysis.isResolved(this, "isVisitedDuringAssume") && isVisitedDuringAssume != null) {
			return false;
		}
		isVisitedDuringAssume = new Object();
		return true;
	}

	protected boolean mustVisitDuringAssert() {
		if (Analysis.isResolved(this, "isVisitedDuringAssert") && isVisitedDuringAssert != null) {
			return false;
		}
		isVisitedDuringAssert = new Object();
		return true;
	}

}
