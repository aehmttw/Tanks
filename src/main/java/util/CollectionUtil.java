package util;

import java.util.ArrayList;
import java.util.function.Function;

public final class CollectionUtil {
    public static <T> void removeIf(ArrayList<T> list, final Function<T, Boolean> predicate) {
        ArrayList<T> removalList = new ArrayList<>();
        for (T element : list) {
            if (predicate.apply(element)) {
                removalList.add(element);
            }
        }
        list.removeAll(removalList);
    }
}