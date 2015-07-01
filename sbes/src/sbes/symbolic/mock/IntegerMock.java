package sbes.symbolic.mock;

public final class IntegerMock {

	private final int value;

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

}