package ru.spbau.mit;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;


public class Injector {

    /**
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     */

    private static Object myInitialize(String rootClassName, List<String> implementationClassNames, ArrayList<Boolean> used) throws Exception {
        Class<?> rootClass = Class.forName(rootClassName);
        Constructor constructorForRootClass = rootClass.getDeclaredConstructors()[0];
        Class<?>[] parametersForRootClass = constructorForRootClass.getParameterTypes();
        ArrayList<Object> myParameters = new ArrayList<>();
        for (int j = 0; j < parametersForRootClass.length; ++j) {
            Boolean flag = false;
            for (int i = 0; i < implementationClassNames.size(); i++) {
                Class<?> currentClass = Class.forName(implementationClassNames.get(i));
                Class<?>[] interfacesForThisClass = currentClass.getInterfaces();
                Boolean isGoodClass = currentClass.equals(parametersForRootClass[j]);
                for (int g = 0; g < interfacesForThisClass.length; ++g) {
                    if (interfacesForThisClass[g].equals(parametersForRootClass[j])) {
                        isGoodClass = true;
                    }
                }
                if (isGoodClass) {
                    if (used.get(i)) {
                        throw new InjectionCycleException();
                    }
                    if (flag) {
                        throw new AmbiguousImplementationException();
                    }
                    used.set(i, true);
                    Object goodClass = myInitialize(currentClass.getName(), implementationClassNames, used);
                    used.set(i, false);
                    myParameters.add(goodClass);
                    flag = true;
                }
            }
            if (!flag) {
                throw new ImplementationNotFoundException();
            }
        }

        return constructorForRootClass.newInstance(myParameters.toArray());
    }

    public static Object initialize(String rootClassName, List<String> implementationClassNames) throws Exception {
        ArrayList<Boolean> used = new ArrayList<>();
        ArrayList<String> myImplementationClassNames = new ArrayList<>();
        for (String s : implementationClassNames) {
            myImplementationClassNames.add(s);
        }
        myImplementationClassNames.add(rootClassName);
        for (int i = 0; i < myImplementationClassNames.size(); ++i) {
            used.add(false);
        }
        return myInitialize(rootClassName, myImplementationClassNames, used);
    }
}