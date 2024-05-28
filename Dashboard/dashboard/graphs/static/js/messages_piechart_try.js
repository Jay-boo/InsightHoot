var width = 700,
      height = 400,
      radius = Math.min(width, height) / 2;


var div = d3.create("div")



// Define color scale
var color = d3.scaleOrdinal(d3.schemeTableau10);

// Define arc generators
var arc = d3.arc()
              .outerRadius(radius - 10)
              .innerRadius(73);

var labelArc = d3.arc()
                   .outerRadius(radius - 40)
                   .innerRadius(radius - 40);

// Define pie layout
var pie = d3.pie()
              .padAngle(0.00)
              .sort(null)
              .value(d => d.count_items);

// Create SVG container
var svg = d3.select("#content-right-dash").append("svg")
    .attr("width", width)
    .attr("height", height)
  .append("g")
    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");
    


  // Sample data
d3.json("/graphs/messages_with_tags/", function(error, data) {
    if (error) throw error;

    data.forEach(function(d) {
      d.count_items = +d.count_items;
    });

    // Aggregate data by theme using d3.nest
  // Aggregate data by theme
  var aggregatedData = Array.from(
    d3.rollup(
      data.flatMap(d => d.tags.map(tag => ({ theme: tag.tag.theme, count: 1 }))),
      v => d3.sum(v, d => d.count),
      d => d.theme
    ),
    ([theme, count_items]) => ({ tagid__theme: theme, count_items })
  );

    // Create tooltip
    var tooltip = d3.select("body").append("div")
                      .attr("class", "tooltip")
                      .style("position", "absolute")
                      .style("visibility", "hidden")
                      .style("background", "#fff")
                      .style("border", "1px solid #ccc")
                      .style("padding", "10px")
                      .style("border-radius", "4px")
                      .style("box-shadow", "0px 0px 10px rgba(0,0,0,0.1)");

    // Bind data and create arcs
    var arcs = g.selectAll(".arc")
                .data(pie(aggregatedData))
                .enter().append("g")
                .attr("class", "arc");

    arcs.append("path")
        .attr("d", arc)
        .style("fill", function(d) { return color(d.data.tagid__theme); })
        .on("mouseover", function(d) {
          d3.select(this)
            .transition()
            .duration(200)
            .attr("d", d3.arc().outerRadius(radius - 5).innerRadius(73))
            .attr("stroke", "white")
            .attr("stroke-width", 4);

          var total = d3.sum(aggregatedData.map(function(d) { return d.count_items; }));
          var percent = Math.round(1000 * d.data.count_items / total) / 10;
          tooltip.html(`Theme: ${d.data.tagid__theme}<br>Count: ${d.data.count_items}<br>Percentage: ${percent}%`)
                 .style("visibility", "visible");
        })
        .on("mousemove", function(d) {
          tooltip.style("top", (d3.event.pageY - 10) + "px")
                 .style("left", (d3.event.pageX + 10) + "px");
        })
        .on("mouseout", function() {
          d3.select(this)
            .transition()
            .duration(200)
            .attr("d", arc)
            .attr("stroke", "none");

          tooltip.style("visibility", "hidden");
        });

    // Add labels to arcs
    arcs.append("text")
        .attr("transform", function(d) { return `translate(${labelArc.centroid(d)})`; })
        .attr("dy", "0.35em")
        .style("text-anchor", "middle")
        .text(function(d) { return d.data.tagid__theme; })
        .style("font-size", "12px")
        .style("fill", "#fff");

    // Add title
    svg.append("text")
      .attr("x", width / 2)
      .attr("y", 20)
      .attr("text-anchor", "middle")
      .style("font-size", "20px")
      .style("font-weight", "bold")
      .text("Distribution des thèmes");
  });

