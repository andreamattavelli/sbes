/**
 * Copyright 2006-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sbes.cloning.objenesis.strategy;

import static sbes.cloning.objenesis.strategy.PlatformDescription.OPENJDK;
import static sbes.cloning.objenesis.strategy.PlatformDescription.SUN;
import sbes.cloning.objenesis.instantiator.ObjectInstantiator;
import sbes.cloning.objenesis.instantiator.sun.SunReflectionFactoryInstantiator;
import sbes.cloning.objenesis.instantiator.sun.UnsafeFactoryInstantiator;

/**
 * Guess the best instantiator for a given class. The instantiator will instantiate the class
 * without calling any constructor. Currently, the selection doesn't depend on the class. It relies
 * on the
 * <ul>
 * <li>JVM version</li>
 * <li>JVM vendor</li>
 * <li>JVM vendor version</li>
 * </ul>
 * However, instantiators are stateful and so dedicated to their class.
 * 
 * @author Henri Tremblay
 * @see ObjectInstantiator
 */
public class StdInstantiatorStrategy extends BaseInstantiatorStrategy {

   /**
    * Return an {@link ObjectInstantiator} allowing to create instance without any constructor being
    * called.
    * 
    * @param type Class to instantiate
    * @return The ObjectInstantiator for the class
    */
   public <T> ObjectInstantiator<T> newInstantiatorOf(Class<T> type) {

      if(PlatformDescription.isThisJVM(SUN) || PlatformDescription.isThisJVM(OPENJDK)) {
         // The UnsafeFactoryInstantiator would also work. But according to benchmarks, it is 2.5
         // times slower. So I prefer to use this one
         return new SunReflectionFactoryInstantiator<T>(type);
      }

      // Fallback instantiator, should work with most modern JVM
      return new UnsafeFactoryInstantiator<T>(type);

   }
}
