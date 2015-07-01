package sbes.symbolic;

import jbse.meta.Analysis;

public class CorrespondenceHandler {

	private Object isVisitedDuringAssume;
	private Object isVisitedDuringAssert;
	private Object corresponding;

	protected static boolean setAsCorresponding(CorrespondenceHandler obj1, CorrespondenceHandler obj2) {
		if (obj1 == null && obj2 == null) {
			return true; // nothing to be set
		} else if (obj1 == null || obj2 == null) {
			return false; // only one object, missing corresponding one
		}
		/* else: both not null */

		if (obj1 == obj2) {
			return false; // corresponding must be a distinct object
		}
		if (!doOrMayCorrespond(obj1, obj2)) {
			return false; // cannot be set as corresponding
		}

		obj1.corresponding = obj2;
		obj2.corresponding = obj1;
		return true;
	}

	protected static boolean doOrMayCorrespond(CorrespondenceHandler obj1, CorrespondenceHandler obj2) {
		if (!Analysis.isResolved(obj1, "corresponding") && !Analysis.isResolved(obj2, "corresponding"))  {
			return true; //neither resolved: symbolic objects that may be set to correspond with each other
		}
		if (!Analysis.isResolved(obj1, "corresponding") || !Analysis.isResolved(obj2, "corresponding")) { 
			return false;//only one resolved: cannot correspond anymore
		}
		/*else: both are solved. */
		else if (obj1.corresponding == null && obj2.corresponding == null) {
			return true;//both null: concrete objects that may be set to correspond with each other
		}
		else if (obj1.corresponding == null || obj2.corresponding == null) {
			return false;//only one null: cannot correspond anymore
		}
		else {
			/*else: both resolved, both non-null. */
			return obj1.corresponding == obj2;
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

	protected boolean hasCorrespondingObject() {
		return Analysis.isResolved(this, "corresponding") && corresponding != null;
	}

	protected Object getCorrespondingObject() {
		if (hasCorrespondingObject()) {
			return corresponding;
		} else {
			return null;
		}
	}

}
