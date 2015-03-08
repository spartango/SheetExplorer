package com.irislabs;

import com.irislabs.sheet.QuotedFileSheet;
import com.irislabs.sheet.Sheet;
import org.junit.Test;

/**
 * Author: spartango
 * Date: 12/24/14
 * Time: 19:46.
 */
public class QuotedTests {
    @Test
    public void testLoad() throws Exception {
        final Sheet sheet = new QuotedFileSheet("/Users/spartango/Data/War Diaries/iraq-war-diary-redacted.csv", ",");
        System.out.println("Fields: " + sheet.fields());
        System.out.println("Lines: " + sheet.stream().count());

    }
}
