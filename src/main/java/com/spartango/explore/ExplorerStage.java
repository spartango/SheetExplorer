package com.spartango.explore;

import com.google.gson.JsonObject;
import com.irislabs.sheet.Sheet;
import com.irislabs.sheet.SheetEntry;
import com.irislabs.stream.MultiCollectors;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.irislabs.sheet.SheetEntry.getDoubleField;
import static com.irislabs.sheet.SheetEntry.getDoubleOptionForField;

/**
 * Author: spartango
 * Date: 12/21/14
 * Time: 18:16.
 */
public class ExplorerStage implements Sheet {

    // The operation to be performed in this stage
    private Predicate<SheetEntry> operation;

    // Parent (source of data)
    private Sheet  parent;
    private String name;

    public ExplorerStage(String name, Sheet parent) {
        this(name, parent, (entry) -> true);
    }

    public ExplorerStage(Sheet parent) {
        this("Untitled", parent, (entry) -> true);
    }

    public ExplorerStage(Sheet parent, Predicate<SheetEntry> criteria) {
        this("Untitled", parent, criteria);
    }

    public ExplorerStage(String name, Sheet parent, Predicate<SheetEntry> criteria) {
        this.name = name;
        this.operation = criteria;
        this.parent = parent;
    }

    public List<String> fields() {
        return parent.fields();
    }

    public Stream<SheetEntry> stream() {
        return parent.stream().filter(operation);
    }

    public Collection<SheetEntry> getData() {
        // Check if anyone has set up the data; if not, nobody will be able to while we do
        // Drains the source into a local pool
        return stream().collect(Collectors.toList());
    }

    public long countData() {
        return stream().count();
    }

    public ExplorerStage filter(String name, Predicate<SheetEntry> criteria) {
        return new ExplorerStage(name, this, criteria);
    }

    public ExplorerStage filter(Predicate<SheetEntry> criteria) {
        return new ExplorerStage(this, criteria);
    }

    public Map<String, Map<String, Long>> histogramAllData() {
        return fields().stream()
                       .collect(Collectors.toMap(Function.identity(),
                                                 this::histogramData));
    }


    public Map<String, Long> histogramData(String key) {
        return stream()
                .filter(SheetEntry.fieldNotEmpty(key))
                .collect(MultiCollectors.toHistogram(SheetEntry.getField(key)));
    }

    public Map<String, Long> histogramNumberData(String key, int bins) {
        final DoubleSummaryStatistics stats = stream().filter(entry -> entry.isNumeric(key))
                                                      .mapToDouble(getDoubleField(key))
                                                      .summaryStatistics();
        double min = stats.getMin();
        double max = stats.getMax();
        double dx = (max - min) / bins;
        final Function<Double, String> binner = (value -> {
            int bin = (int) ((value - min) / dx);
            // Range Floor
            double floor = min + (dx * bin);
            // Range Ceil
            double ceil = min + (dx * (bin + 1));
            return floor + " to " + ceil;
        });

        return stream().map(getDoubleOptionForField(key))
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .collect(MultiCollectors.toHistogram(binner));
    }

    public Map<String, Long> histogramLogData(String key, int bins) {
        final DoubleSummaryStatistics stats = stream().filter(entry -> entry.isNumeric(key))
                                                      .mapToDouble(getDoubleField(key))
                                                      .summaryStatistics();
        double min = stats.getMin() > 0 ?
                     Math.log10(stats.getMin()) :
                     stream().filter(entry -> entry.isNumeric(key))
                             .mapToDouble(getDoubleField(key))
                             .filter(value -> value > 0)
                             .min()
                             .orElseGet(() -> 0);
        double max = stats.getMax() > 0 ? Math.log10(stats.getMax()) : 1;
        double dx = (max - min) / bins;
        final Function<Double, String> binner = (value -> {
            double logValue = Math.log10(value);
            int bin = (int) ((logValue - min) / dx);
            // Range Floor
            double floor = min + (dx * bin);
            // Range Ceil
            double ceil = min + (dx * (bin + 1));
            return Math.pow(10, floor) + " to " + Math.pow(10, ceil);
        });

        return stream().map(getDoubleOptionForField(key))
                       .filter(Optional::isPresent)
                       .map(Optional::get).filter(value -> value > 0)
                       .collect(MultiCollectors.toHistogram(binner));
    }


    public JsonObject toJSON() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("count", countData());
        return json;
    }
}