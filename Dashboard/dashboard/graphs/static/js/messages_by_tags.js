var width = 700,
    height = 400,
    radius = Math.min(width, height) / 2;

var color = d3.scaleOrdinal(d3.schemeCategory10);

var arc = d3.arc()
    .outerRadius(radius - 10)
    .innerRadius(73);

var labelArc = d3.arc()
    .outerRadius(radius - 40)
    .innerRadius(radius - 40);

var pie = d3.pie()
    .padAngle(0.042)
    .sort(null)
    .value(function(d) { return d.count_items; });

var svg = d3.select("#content-right-dash").append("svg")
    .attr("width", width)
    .attr("height", height)
  .append("g")
    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

d3.json("/graphs/messages_by_tag/", function(error, data) {
  if (error) throw error;

  data.forEach(function(d) {
    d.count_items = +d.count_items;
  });

  var g = svg.selectAll(".arc")
      .data(pie(data))
    .enter().append("g")
      .attr("class", "arc");

  g.append("path")
      .attr("d", arc)
      .style("fill", function(d) { return color(d.data.tagid__label); });

  g.append("text")
      .attr("transform", function(d) { return "translate(" + labelArc.centroid(d) + ")"; })
      .attr("dy", ".35em")
      .text(function(d) { return d.data.tagid__label; });
});
