
import sbes.distance.Distance;
import stack.util.Stack;

public class Stack_Stub {

	@Test
	public void test0()  throws Throwable  {
		Stack<Integer> stack0 = new Stack<Integer>();
		Integer integer0 = new Integer(234);
		Integer integer1 = new Integer(55);
		Integer integer2 = new Integer((-17489));
		boolean boolean0 = stack0.add(integer0);
		boolean boolean1 = stack0.add(integer1);
		boolean boolean2 = stack0.add(integer2);
		Integer integer3 = stack0.elementAt((Integer) 2);
	}
	
	@Test
	public void test1()  throws Throwable  {
		Stack<Integer> stack0 = new Stack<Integer>();
		Integer integer0 = new Integer(234);
		Integer integer1 = new Integer(55);
		Integer integer2 = new Integer(0);
		boolean boolean0 = stack0.add(integer0);
		boolean boolean1 = stack0.add(integer1);
		boolean boolean2 = stack0.add(integer2);
		Integer integer3 = stack0.elementAt((Integer) 1);
	}
	
}
