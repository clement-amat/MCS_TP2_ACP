import java.util.Set;
import java.util.TreeSet;

public class Utils {

    public static void insertInSetIfLowerThanOne(TreeSet<KPPVResult> set,KPPVResult  toInsert) {
        if (set.last().compareTo(toInsert) > 0) {
            set.pollLast();
            set.add(toInsert);
        }
    }

}
