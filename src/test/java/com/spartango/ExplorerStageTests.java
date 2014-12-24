package com.spartango;

import com.irislabs.sheet.FileSheet;
import com.spartango.explore.ExplorerStage;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static com.irislabs.sheet.SheetEntry.*;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

/**
 * Author: spartango
 * Date: 12/22/14
 * Time: 17:22.
 */
public class ExplorerStageTests {

    private static ExplorerStage cachedStage;

    public static FileSheet loadData() throws IOException {
        return new FileSheet("/Users/spartango/Data/LESO Program data/LESO data - all states.txt");
    }

    public static ExplorerStage startStage() throws IOException {
        return new ExplorerStage(loadData());
    }

    public static ExplorerStage cachedStartStage() throws IOException {
        return cachedStage;
    }


    @BeforeClass
    public static void setUp() throws Exception {
        final FileSheet loaded = loadData();
        cachedStage = new ExplorerStage(loaded.toCollectionSheet());
        cachedStage.getData();
    }

    @Test
    public void testFields() throws Exception {
        final ExplorerStage explorerStage = cachedStartStage();
        System.out.println("Fields: " + explorerStage.fields());
        assertTrue(explorerStage.fields()
                                .containsAll(Arrays.asList("quantity",
                                                           "total_cost",
                                                           "nsn",
                                                           "county",
                                                           "fips",
                                                           "federal_supply_category_name",
                                                           "item_name",
                                                           "federal_supply_category",
                                                           "acquisition_cost",
                                                           "ui",
                                                           "state",
                                                           "federal_supply_class_name",
                                                           "federal_supply_class",
                                                           "ship_date")));
    }

    @Test
    public void testCount() throws Exception {
        final ExplorerStage explorerStage = cachedStartStage();
        final long count = explorerStage.countData();

        System.out.println("Count: " + count);
        assertEquals(count, 243328);
    }

    @Test
    public void testHistogram() throws Exception {
        final ExplorerStage explorerStage = cachedStartStage();
        final Map<String, Long> histogram = explorerStage.histogramData("state");
        System.out.println("States: " + histogram);
        assertEquals(histogram.get("HI").longValue(), 9);
        assertEquals(histogram.get("DE").longValue(), 3424);
    }

    @Test
    public void testAllHistograms() throws Exception {
        final ExplorerStage explorerStage = cachedStartStage();
        final Map<String, Map<String, Long>> histogram = explorerStage.histogramAllData();
        System.out.println("All Histograms, fields: " + histogram.keySet());
    }

    @Test
    public void testNumericHistogram() throws Exception {
        final ExplorerStage explorerStage = cachedStartStage();
        final Map<String, Long> histogram = explorerStage.histogramNumberData("acquisition_cost", 20);
        System.out.println("Item Costs: " + histogram);
    }

    @Test
    public void testLogHistogram() throws Exception {
        final ExplorerStage explorerStage = cachedStartStage();
        final Map<String, Long> histogram = explorerStage.histogramLogData("acquisition_cost", 10);
        System.out.println("Log Item Costs: " + histogram);
    }

    @Test
    public void testFilter() throws Exception {
        final ExplorerStage explorerStage = startStage();
        final ExplorerStage filteredStage = explorerStage.filter(fieldEquals("state", "HI"));
        System.out.println("Filtered by state (HI): " + filteredStage.getData());
        assertEquals(filteredStage.countData(), 9);
    }

    @Test
    public void testFilterRange() throws Exception {
        final ExplorerStage explorerStage = startStage();
        final ExplorerStage filteredStage = explorerStage.filter(fieldInRange("acquisition_cost",
                                                                              900000,
                                                                              1800000));
        System.out.println("Filtered by price range (900000, 1800000): " + filteredStage.getData());
        assertEquals(filteredStage.countData(), 117);
    }

    @Test
    public void testFilterContains() throws Exception {
        final ExplorerStage explorerStage = startStage();
        final ExplorerStage filteredStage = explorerStage.filter(fieldContains("item_name", "RIFLE"));
//        System.out.println("Filtered by item_name (RIFLE): " + filteredStage.getData());
        assertEquals(filteredStage.countData(), 80090);
    }

    @Test
    public void testFilterAmong() throws Exception {
        final ExplorerStage explorerStage = startStage();
        final ExplorerStage filteredStage = explorerStage.filter(fieldAmong("item_name",
                                                                            "\"RIFLE,7.62 MILLIMETER\"",
                                                                            "\"RIFLE,5.56 MILLIMETER\""));
//        System.out.println("Filtered by item_names (7.62 and 5.56 RIFLES): " + filteredStage.getData());
        assertEquals(filteredStage.countData(), 61557 + 17737);
    }

    @Test
    public void testCompoundFilter() throws Exception {
        final ExplorerStage explorerStage = startStage();
        final ExplorerStage filteredStage = explorerStage.filter(fieldContains("item_name", "RIFLE"));
        final Map<String, Long> histogram = filteredStage.histogramNumberData("acquisition_cost", 20);
        System.out.println("Distribution of rifle prices: " + histogram);

        final ExplorerStage secondFilter = filteredStage.filter(fieldGreaterThan("acquisition_cost", 997.6));

        assertEquals(secondFilter.countData(), 5 + 1 + 8 + 91);
    }
}
