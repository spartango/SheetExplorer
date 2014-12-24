package com.irislabs;

import com.irislabs.sheet.ExcelSheet;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Author: spartango
 * Date: 11/29/14
 * Time: 10:19.
 */
public class ExcelTests {
    @Test
    public void testParse() throws Exception {
        ExcelSheet sheet = new ExcelSheet("/Users/spartango/Data/LESO Program data/Agencies receiving equipment.xlsx");
        assertEquals(sheet.fields().size(), 3);
        System.out.println(sheet.fields());
        assertEquals(sheet.stream().count(), 7756);
        System.out.println(sheet.stream().map(entry -> entry.get("state")).distinct().collect(Collectors.toList()));
    }
}
