var margin = {top: 20, right: 20, bottom: 30, left: 50},
    width = 480 - margin.left - margin.right,
    height = 250 - margin.top - margin.bottom;

var x = d3.scaleBand()
    .rangeRound([0, width])
    .padding(0.1);

var y = d3.scaleLinear()
    .range([height, 0]);

var xAxis = d3.axisBottom(x);
var yAxis = d3.axisLeft(y);

var svg_topic = d3.select("#content-left-dash").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

d3.json("/graphs/messages_by_topic/", function(error, data) {
  if (error) throw error;

  data.forEach(function(d) {
    d.topic__name = d.topic__name;
    d.count_items = +d.count_items;
  });

  x.domain(data.map(function(d) { return d.topic__name; }));
  y.domain([0, d3.max(data, function(d) { return d.count_items; })]);

  svg_topic.append("g")
      .attr("class", "x axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis);

  svg_topic.append("g")
      .attr("class", "y axis")
      .call(yAxis)
    .append("text")
      .attr("transform", "rotate(-90)")
      .attr("y", 6)
      .attr("dy", ".71em")
      .style("text-anchor", "end")
      
  svg_topic.selectAll(".bar")
      .data(data)
    .enter().append("rect")
      .attr("class", "bar")
      .attr("x", function(d) { return x(d.topic__name); })
      .attr("width", x.bandwidth())
      .attr("y", function(d) { return y(d.count_items); })
      .attr("height", function(d) { return height - y(d.count_items); });
});

function type(d) {
  d.count_items = +d.count_items;
  return d;
}

