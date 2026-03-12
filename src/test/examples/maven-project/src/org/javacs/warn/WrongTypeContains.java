package org.javacs.warn;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

class WrongTypeContains {
    void test() {
        Set<String> s = new HashSet<>();
        s.contains(42);
        List<String> list = new ArrayList<>();
        list.contains(42);
        list.indexOf(42);
        s.contains("valid");
        list.remove(0);          // List.remove(int) - must NOT warn
    }

    void testRemoveObject() {
        List<Integer> ints = new ArrayList<>();
        ints.remove("foo");     // wrong type: String vs Integer element type
    }
}
