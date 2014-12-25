package com.spartango.explore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.irislabs.sheet.QuotedFileSheet;
import com.irislabs.sheet.Sheet;
import com.irislabs.sheet.SheetEntry;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Headers;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

/**
 * Author: spartango
 * Date: 12/22/14
 * Time: 17:21.
 */
public class Explorer {
    // Underlying analysis
    private final Stack<ExplorerStage> stages;

    // Web API
    private final Undertow server;

    public Explorer(Sheet source) {
        stages = new Stack<>();
        stages.push(new ExplorerStage("Source", source));

        // Web API setup
        final PathHandler pathHandler = Handlers.path()
                                                .addExactPath("/histogram", this::handleHistogramRequest)
                                                .addExactPath("/histogram/num", this::handleHistogramNumRequest)
                                                .addExactPath("/histogram/log", this::handleHistogramLogRequest)
                                                .addExactPath("/histogram/all", this::handleHistogramAllRequest)
                                                .addExactPath("/fields", this::handleFieldRequest)
                                                .addExactPath("/count", this::handleCountRequest)
                                                .addExactPath("/data", this::handleDataRequest)
                                                .addExactPath("/pop", this::handlePopRequest)
                                                .addExactPath("/reset", this::handleResetRequest)
                                                .addExactPath("/filter", this::handleFilterRequest)
                                                .addExactPath("/stages", this::handleStagesRequest)
                                                .addPrefixPath("/explore/", new ResourceHandler(
                                                        new FileResourceManager(
                                                                new File("/Users/spartango/Developer/Explorer/explore"),
                                                                0)));
        server = Undertow.builder()
                         .addHttpListener(8080, "localhost")
                         .setHandler(pathHandler)
                         .build();
        server.start();
    }

    public ExplorerStage currentStage() {
        return stages.peek();
    }

    public Optional<ExplorerStage> popStage() {
        if (stages.size() > 1) {
            return Optional.of(stages.pop());
        } else {
            return Optional.empty();
        }
    }


    private void pushStage(ExplorerStage filter) {
        stages.push(filter);
    }

    public void reset() {
        ExplorerStage firstStage = stages.firstElement();
        stages.clear();
        stages.push(firstStage);
    }

    public void handleStagesRequest(HttpServerExchange exchange) {
        JsonObject json = new JsonObject();
        JsonArray stageArray = new JsonArray();

        stages.stream()
              .map(ExplorerStage::toJSON)
              .forEach(stageArray::add);
        json.add("stages", stageArray);

        exchange.setResponseCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(json.toString());
    }

    public void handleHistogramRequest(HttpServerExchange exchange) {
        if (exchange.getQueryParameters().containsKey("field") && currentStage().fields()
                                                                                .contains(exchange.getQueryParameters()
                                                                                                  .get("field")
                                                                                                  .getFirst())) {
            final String field = exchange.getQueryParameters().get("field").getFirst();
            System.out.println("Sending histogram of " + field);

            exchange.dispatch(() -> {
                final Map<String, Long> histogram = currentStage().histogramData(field);
                JsonObject json = new JsonObject();

                JsonObject histjson = new JsonObject();
                histogram.forEach(histjson::addProperty);
                json.add("histogram", histjson);

                exchange.setResponseCode(200);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(json.toString());
            });
        } else {
            exchange.setResponseCode(400);
            exchange.endExchange();
        }
    }

    public void handleHistogramLogRequest(HttpServerExchange exchange) {
        if (exchange.getQueryParameters().containsKey("field") && currentStage().fields()
                                                                                .contains(exchange.getQueryParameters()
                                                                                                  .get("field")
                                                                                                  .getFirst())) {
            final String field = exchange.getQueryParameters().get("field").getFirst();
            int bins = 10;

            if (exchange.getQueryParameters().containsKey("bins")) {
                String binString = exchange.getQueryParameters().get("bins").getFirst();

                try {
                    bins = Integer.parseInt(binString);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }

            System.out.println("Sending log histogram of " + field + " with " + bins + " bins");

            final int finalBins = bins;

            exchange.dispatch(() -> {
                final Map<String, Long> histogram = currentStage().histogramLogData(field, finalBins);
                JsonObject json = new JsonObject();

                JsonObject histjson = new JsonObject();
                histogram.forEach(histjson::addProperty);
                json.add("histogram", histjson);

                exchange.setResponseCode(200);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(json.toString());
            });
        } else {
            exchange.setResponseCode(400);
            exchange.endExchange();
        }
    }


    public void handleHistogramNumRequest(HttpServerExchange exchange) {
        if (exchange.getQueryParameters().containsKey("field") && currentStage().fields()
                                                                                .contains(exchange.getQueryParameters()
                                                                                                  .get("field")
                                                                                                  .getFirst())) {
            final String field = exchange.getQueryParameters().get("field").getFirst();
            int bins = 10;

            if (exchange.getQueryParameters().containsKey("bins")) {
                String binString = exchange.getQueryParameters().get("bins").getFirst();

                try {
                    bins = Integer.parseInt(binString);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }

            System.out.println("Sending numeric histogram of " + field + " with " + bins + " bins");
            final int finalBins = bins;

            exchange.dispatch(() -> {
                final Map<String, Long> histogram = currentStage().histogramNumberData(field, finalBins);
                JsonObject json = new JsonObject();

                JsonObject histjson = new JsonObject();
                histogram.forEach(histjson::addProperty);
                json.add("histogram", histjson);

                exchange.setResponseCode(200);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(json.toString());
            });
        } else {
            exchange.setResponseCode(400);
            exchange.endExchange();
        }
    }


    public void handleHistogramAllRequest(HttpServerExchange exchange) {
        System.out.println("Sending all histograms");

        exchange.dispatch(() -> {
            final Map<String, Map<String, Long>> histogram = currentStage().histogramAllData();
            JsonObject json = new JsonObject();

            JsonObject histjson = new JsonObject();
            histogram.forEach((key, map) -> {
                JsonObject mapObj = new JsonObject();
                map.forEach(mapObj::addProperty);
                histjson.add(key, mapObj);
            });
            json.add("histograms", histjson);

            exchange.setResponseCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(json.toString());
        });
    }

    public void handleFieldRequest(HttpServerExchange exchange) {
        System.out.println("Sending fields");

        JsonObject json = new JsonObject();
        JsonArray arrayjson = new JsonArray();
        currentStage().fields().forEach(field -> arrayjson.add(new JsonPrimitive(field)));
        json.add("fields", arrayjson);

        exchange.setResponseCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(json.toString());
    }

    public void handleCountRequest(HttpServerExchange exchange) {
        System.out.println("Sending count");

        exchange.dispatch(() -> {
            JsonObject json = new JsonObject();
            json.addProperty("count", currentStage().countData());

            exchange.setResponseCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(json.toString());
        });
    }


    public void handleDataRequest(HttpServerExchange exchange) {
        System.out.println("Sending all data");

        exchange.dispatch(() -> {
            JsonObject json = new JsonObject();
            JsonArray arrayjson = new JsonArray();
            currentStage().getData().forEach(data -> arrayjson.add(data.toJSON()));
            json.add("data", arrayjson);

            exchange.setResponseCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(json.toString());
        });
    }

    public void handlePopRequest(HttpServerExchange exchange) {
        System.out.println("Popping last stage");

        popStage();
        exchange.setResponseCode(200);
        exchange.endExchange();
    }

    public void handleResetRequest(HttpServerExchange exchange) throws Exception {
        System.out.println("Reset stack");

        reset();
        exchange.setResponseCode(200);
        exchange.endExchange();
    }

    public void handleFilterRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.getQueryParameters().containsKey("field")
            && exchange.getQueryParameters().containsKey("value")
            && currentStage().fields()
                             .contains(exchange.getQueryParameters()
                                               .get("field")
                                               .getFirst())) {
            final String field = exchange.getQueryParameters().get("field").getFirst();
            final String value = exchange.getQueryParameters().get("value").getFirst();

            System.out.println("Filtering " + field + " = " + value);
            pushStage(currentStage().filter(field + " = " + value,
                                            SheetEntry.fieldEquals(field, value)));

            exchange.setResponseCode(200);
            exchange.endExchange();
        } else {
            exchange.setResponseCode(400);
            exchange.endExchange();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Loading file");
//        final Sheet sheet = new FileSheet("/Users/spartango/Data/LESO Program data/LESO data - all states.txt");
        final Sheet sheet = new QuotedFileSheet("/Users/spartango/Data/War Diaries/afg.csv", ",");

//        final Sheet sheet = new QuotedFileSheet("/Users/spartango/Data/War Diaries/iraq-war-diary-redacted.csv", ",");
        System.out.println("Loaded file: fields -> " + sheet.fields());
        System.out.println("Starting server on 8080");
        final Explorer explorer = new Explorer(sheet);

        synchronized (explorer) {
            System.out.println("Ready!");
            explorer.wait();
        }

        System.out.println("Stopping server on 8080");
    }

}
