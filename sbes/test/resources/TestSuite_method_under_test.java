package stack.util;

import static org.junit.Assert.*;

import sun.misc.Unsafe;
import java.lang.reflect.Field;
import org.junit.Test;

public class TestSuite_method_under_test {
    private static class AccessibleObject {
        private final Object target;
        AccessibleObject(Object o) {
            target = o;
        }
        void set(String fieldName, Object value) {
            try {
                final Field p = target.getClass().getDeclaredField(fieldName);
                p.setAccessible(true);
                p.set(target, value);
            } catch (IllegalArgumentException | IllegalAccessException
                | NoSuchFieldException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        AccessibleObject get(String fieldName) {
            try {
                Field p = target.getClass().getDeclaredField(fieldName);
                p.setAccessible(true);
                return new AccessibleObject(p.get(target));
            } catch (IllegalArgumentException | IllegalAccessException
                | NoSuchFieldException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        Object getValue() {
            return target;
        }
    }

   private static Unsafe unsafe;

   static {
       Field f;
       try {
           f = Unsafe.class.getDeclaredField("theUnsafe");
           f.setAccessible(true);
           unsafe = (Unsafe) f.get(null);
       } catch (NoSuchFieldException e) {
           throw new RuntimeException(e);
       } catch (IllegalAccessException e) {
           throw new RuntimeException(e);
       } 
   }

   private static Object getNewInstance(String classname) {
       try {
           Class<?> type = Class.forName(classname);
           return type.cast(unsafe.allocateInstance(type));
       } catch (ClassNotFoundException e) {
           throw new RuntimeException(e);
       } catch (InstantiationException e) {
           throw new RuntimeException(e);
       }
   }


    @Test
    public void testCase0() {
        //test case for state .1.2.1.1.1.1.1.1.1.1.1.1[328]
        stack.util.Stack_Stub_2 __ROOT_this = new stack.util.Stack_Stub_2(); // {R0} == Object[0] (fresh)
        ; // pre_init(stack/util/Stack_Stub_2)
        ; // pre_init(java/lang/Object)
        new AccessibleObject(__ROOT_this).set("v_Stack1", new sbes.symbolic.mock.Stack()); // {R1} == Object[1] (fresh)
        new AccessibleObject(__ROOT_this).set("p0", null); // {R10} == null
        new AccessibleObject(__ROOT_this).get("v_Stack1").set("elementData", new sbes.symbolic.mock.DoubleLinkedList()); // {R11} == Object[2] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").set("size", 1);  // {V3} >= 0
        ; // pre_init(jbse/meta/Analysis)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").set("header", getNewInstance("sbes.symbolic.mock.DoubleLinkedList$Entry")); // {R15} == Object[3] (fresh)
        ; // pre_init(sbes/symbolic/mock/DoubleLinkedList$Entry)
        ; // pre_init(sbes/symbolic/CorrespondenceHandler)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").get("header").set("previous", getNewInstance("sbes.symbolic.mock.DoubleLinkedList$Entry")); // {R22} == Object[5] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").get("header").get("previous").set("_owner", new sbes.symbolic.mock.DoubleLinkedList()); // {R30} == Object[6] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").get("header").get("previous").get("_owner").set("size", 1);  // {V8} >= 0
        ; // pre_init(sbes/symbolic/mock/DoubleLinkedList)
         // {V8} >= 1
        new AccessibleObject(__ROOT_this).set("v_Stack2", new sbes.symbolic.mock.Stack()); // {R2} == Object[7] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").set("elementData", new sbes.symbolic.mock.DoubleLinkedList()); // {R38} == Object[8] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").set("size", 1);  // {V14} >= 0
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").set("header", getNewInstance("sbes.symbolic.mock.DoubleLinkedList$Entry")); // {R42} == Object[9] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").get("header").set("previous", getNewInstance("sbes.symbolic.mock.DoubleLinkedList$Entry")); // {R48} == Object[11] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").get("header").get("previous").set("_owner", new sbes.symbolic.mock.DoubleLinkedList()); // {R56} == Object[12] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").get("header").get("previous").get("_owner").set("size", 1);  // {V18} >= 0
         // {V18} >= 1
        ; // pre_init(sbes/symbolic/mock/IntegerMock)
        ; // pre_init(sbes/symbolic/mock/Stack)
        ; // pre_init(java/lang/String)
         // {V3} + 1 == {V14} + 1
        new AccessibleObject(__ROOT_this).set("forceConservativeRepOk", null); // {R3} == null
        new AccessibleObject(__ROOT_this).set("forceConservativeRepOk2", null); // {R4} == null
        new AccessibleObject(__ROOT_this).set("forceConservativeRepOk3", null); // {R5} == null
        System.out.println(__ROOT_this.v_Stack2.toCode());
    }

    @Test
    public void testCase1() {
        //test case for state .1.2.1.1.1.1.1.1.2.1.1[328]
        stack.util.Stack_Stub_2 __ROOT_this = new stack.util.Stack_Stub_2(); // {R0} == Object[0] (fresh)
        ; // pre_init(stack/util/Stack_Stub_2)
        ; // pre_init(java/lang/Object)
        new AccessibleObject(__ROOT_this).set("v_Stack1", new sbes.symbolic.mock.Stack()); // {R1} == Object[1] (fresh)
        new AccessibleObject(__ROOT_this).set("p0", null); // {R10} == null
        new AccessibleObject(__ROOT_this).get("v_Stack1").set("elementData", new sbes.symbolic.mock.DoubleLinkedList()); // {R11} == Object[2] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").set("size", 1);  // {V3} >= 0
        ; // pre_init(jbse/meta/Analysis)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").set("header", getNewInstance("sbes.symbolic.mock.DoubleLinkedList$Entry")); // {R15} == Object[3] (fresh)
        ; // pre_init(sbes/symbolic/mock/DoubleLinkedList$Entry)
        ; // pre_init(sbes/symbolic/CorrespondenceHandler)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").get("header").set("previous", getNewInstance("sbes.symbolic.mock.DoubleLinkedList$Entry")); // {R22} == Object[5] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").get("header").get("previous").set("_owner", new sbes.symbolic.mock.DoubleLinkedList()); // {R30} == Object[6] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").get("header").get("previous").get("_owner").set("size", 1);  // {V8} >= 0
        ; // pre_init(sbes/symbolic/mock/DoubleLinkedList)
         // {V8} >= 1
        new AccessibleObject(__ROOT_this).set("v_Stack2", new sbes.symbolic.mock.Stack()); // {R2} == Object[7] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").set("elementData", new sbes.symbolic.mock.DoubleLinkedList()); // {R38} == Object[8] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").set("size", 1);  // {V14} >= 0
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").set("header", getNewInstance("sbes.symbolic.mock.DoubleLinkedList$Entry")); // {R42} == Object[9] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").get("header").set("previous", getNewInstance("sbes.symbolic.mock.DoubleLinkedList$Entry")); // {R48} == Object[11] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").get("header").get("previous").set("_owner", new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").getValue()); // {R56} == Object[8] (aliases {ROOT}:this.v_Stack2.elementData)
         // {V14} >= 1
        ; // pre_init(sbes/symbolic/mock/IntegerMock)
        ; // pre_init(sbes/symbolic/mock/Stack)
        ; // pre_init(java/lang/String)
         // {V3} + 1 == {V14} + 1
        new AccessibleObject(__ROOT_this).set("forceConservativeRepOk", null); // {R3} == null
        new AccessibleObject(__ROOT_this).set("forceConservativeRepOk2", null); // {R4} == null
        new AccessibleObject(__ROOT_this).set("forceConservativeRepOk3", null); // {R5} == null
        System.out.println(__ROOT_this.v_Stack2.toCode());
    }

    @Test
    public void testCase2() {
        //test case for state .1.2.1.1.2.1.1.1.1.1.1[328]
        stack.util.Stack_Stub_2 __ROOT_this = new stack.util.Stack_Stub_2(); // {R0} == Object[0] (fresh)
        ; // pre_init(stack/util/Stack_Stub_2)
        ; // pre_init(java/lang/Object)
        new AccessibleObject(__ROOT_this).set("v_Stack1", new sbes.symbolic.mock.Stack()); // {R1} == Object[1] (fresh)
        new AccessibleObject(__ROOT_this).set("p0", null); // {R10} == null
        new AccessibleObject(__ROOT_this).get("v_Stack1").set("elementData", new sbes.symbolic.mock.DoubleLinkedList()); // {R11} == Object[2] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").set("size", 1);  // {V3} >= 0
        ; // pre_init(jbse/meta/Analysis)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").set("header", getNewInstance("sbes.symbolic.mock.DoubleLinkedList$Entry")); // {R15} == Object[3] (fresh)
        ; // pre_init(sbes/symbolic/mock/DoubleLinkedList$Entry)
        ; // pre_init(sbes/symbolic/CorrespondenceHandler)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").get("header").set("previous", getNewInstance("sbes.symbolic.mock.DoubleLinkedList$Entry")); // {R22} == Object[5] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").get("header").get("previous").set("_owner", new AccessibleObject(__ROOT_this).get("v_Stack1").get("elementData").getValue()); // {R30} == Object[2] (aliases {ROOT}:this.v_Stack1.elementData)
        ; // pre_init(sbes/symbolic/mock/DoubleLinkedList)
         // {V3} >= 1
        new AccessibleObject(__ROOT_this).set("v_Stack2", new sbes.symbolic.mock.Stack()); // {R2} == Object[6] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").set("elementData", new sbes.symbolic.mock.DoubleLinkedList()); // {R34} == Object[7] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").set("size", 1);  // {V10} >= 0
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").set("header", getNewInstance("sbes.symbolic.mock.DoubleLinkedList$Entry")); // {R38} == Object[8] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").get("header").set("previous", getNewInstance("sbes.symbolic.mock.DoubleLinkedList$Entry")); // {R44} == Object[10] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").get("header").get("previous").set("_owner", new sbes.symbolic.mock.DoubleLinkedList()); // {R52} == Object[11] (fresh)
        new AccessibleObject(__ROOT_this).get("v_Stack2").get("elementData").get("header").get("previous").get("_owner").set("size", 1);  // {V14} >= 0
         // {V14} >= 1
        ; // pre_init(sbes/symbolic/mock/IntegerMock)
        ; // pre_init(sbes/symbolic/mock/Stack)
        ; // pre_init(java/lang/String)
         // {V3} + 1 == {V10} + 1
        new AccessibleObject(__ROOT_this).set("forceConservativeRepOk", null); // {R3} == null
        new AccessibleObject(__ROOT_this).set("forceConservativeRepOk2", null); // {R4} == null
        new AccessibleObject(__ROOT_this).set("forceConservativeRepOk3", null); // {R5} == null
        System.out.println(__ROOT_this.v_Stack2.toCode());
    }

}

