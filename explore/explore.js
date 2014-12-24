/**
 * Created by spartango on 12/23/14.
 */

var margin = {top: 20, right: 20, bottom: 30, left: 40},
    width = 960 - margin.left - margin.right,
    height = 500 - margin.top - margin.bottom;

var x = d3.scale.ordinal()
    .rangeRoundBands([0, width], .1);

var y = d3.scale.linear()
    .range([height, 0]);

var xAxis = d3.svg.axis()
    .scale(x)
    .orient("bottom");

var yAxis = d3.svg.axis()
    .scale(y)
    .orient("left")
    .ticks(10, "");

var svg = d3.select("#chartarea").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
    .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

var currentField = "";
var fields = [];

function loadFields() {
    d3.json("/fields", function (error, json) {
            fields = [];
            json.fields.forEach(function (field) {
                fields.push(field);
                var button = d3.select("#controls").append("button");
                button.on("click", function () {
                    updateHistogram(field);
                }).append("text")
                    .text(field)
            });
        }
    );
}

var stages = [];
function loadStages() {
    d3.json("/stages", function (error, json) {
            d3.select("#stages").selectAll("*").remove();
            stages = [];
            json.stages.forEach(function (stage, index) {
                stages.push(stage);
                var button = d3.select("#stages").append("button");
                button.on("click", function () {
                    // Pop until we get to this
                    popBack(stages.length - index - 1);
                }).append("text")
                    .text(stage.name + " (" + stage.count + ")");

                d3.select("#stages").append("text").text("\u27EB");
            });
        }
    );
}

function renderHistogram() {
    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)
        .append("text")
        .attr("transform", "rotate(-90)")
        .attr("y", 6)
        .attr("dy", ".71em")
        .style("text-anchor", "end")
        .text("Count");

}

function requestPop() {
    d3.xhr("/pop", function () {
        console.log("Popped");
        loadStages();
        updateHistogram(currentField);
    });
}

function popBack(remaining) {
    console.log("Popping, with " + remaining);
    if (remaining > 1) {
        d3.xhr("/pop", function () {
            popBack(remaining - 1);
        });
    } else if (remaining > 0) {
        requestPop();
    }
}

function requestFilter(field, name) {
    d3.xhr("/filter?field=" + field + "&value=" + name, function () {
        console.log("Filtered " + field + " = " + name);
        loadStages();
        updateHistogram(field);
    });
}

function updateHistogram(field) {
    currentField = field;
    var data = [];
    d3.json("/histogram?field=" + field,
        function (error, json) {
            Object.keys(json.histogram).forEach(function (key) {
                data.push({name: key, value: json.histogram[key]});
            });

            x.domain(data.map(function (d) {
                return d.name;
            }));
            y.domain([0, d3.max(data, function (d) {
                return d.value;
            })]);

            svg.select(".y.axis").transition()
                .duration(250)
                .call(yAxis);

            svg.select(".x.axis").transition()
                .duration(250)
                .call(xAxis);

            // Purge the existing data
            svg.selectAll(".bar").remove();

            var bar = svg.selectAll(".bar").data(data);

            // new data:
            bar.enter().append("rect")
                .attr("class", "bar")
                .attr("name", function (d) {
                    return d.name;
                }).attr("x", function (d) {
                    return x(d.name);
                }).attr("y", function (d) {
                    return y(d.value);
                }).attr("height", function (d) {
                    return height - y(d.value);
                }).attr("width", x.rangeBand())
                .on("click", function (d) {
                    requestFilter(field, d.name);
                });

        });
}

loadStages();
renderHistogram();
loadFields();