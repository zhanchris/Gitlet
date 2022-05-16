package gitlet;

import ucb.junit.textui;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Chris Zhan
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(UnitTest.class));
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() {
    }

    @Test
    public void minTimeTest() {
        ZonedDateTime testDay = ZonedDateTime.of(2017, 11, 9,
                20, 0, 5, 0, ZoneId.of(ZoneId.SHORT_IDS.get("PST")));
        String time = CommitTree.CommitNode.formatTime(testDay);
        assertEquals("Thu Nov 9 20:00:05 2017 -0800", time);

        ZonedDateTime epochDay = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0,
                ZoneId.of("UTC"));
        String epochTime = CommitTree.CommitNode.formatTime(epochDay);
    }

}


