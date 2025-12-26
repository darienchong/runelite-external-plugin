package helper;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UtilsTest {
    @Test
    public void givenSalmon_whenGetItemName_shouldStripColTags() {
        var testCases = List.of(
                new String[]{ "<col=#ffffff>Salmon</col>", "Salmon" },
                new String[]{ "Salmon", "Salmon" }
        );

        for (var testCase : testCases) {
            var input = testCase[0];
            var expected = testCase[1];
            var actual = Utils.stripItemNameTags(input);
            Assert.assertEquals(expected, actual);
        }
    }
}
