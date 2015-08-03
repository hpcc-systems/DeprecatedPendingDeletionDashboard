function createTreeChart(divId, chartData) {
var reqData = jq.parseJSON(chartData);
	
	var chartId = reqData.chart_id;
	var treeData = reqData.chart_data;
	
	var divElement = jq('#'+divId).empty();
	divElement.append(jq("<div id='chartHolder'/>" ));		
	var divToDraw = d3.select(divElement.get(0)).select("div");
	
	var margin = {top: 20, right: 120, bottom: 20, left: 120},
    	width = divElement.width() - margin.right - margin.left,
	    height = divElement.height() - margin.top - margin.bottom;

	var i = 0,
	    duration = 750,
	    root;
	
	var tree = d3.layout.tree()
	    .size([height, width]);
	
	var diagonal = d3.svg.diagonal()
	    .projection(function(d) { return [d.y, d.x]; });
	
	var svg = divToDraw.append("svg")
	    .attr("width", width + margin.right + margin.left)
	    .attr("height", height + margin.top + margin.bottom)
	  .append("g")
	    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	
	  root = treeData;
	  root.x0 = height / 2;
	  root.y0 = 0;
	
	  function collapse(d) {
	    if (d.children) {
	      d._children = d.children;
	      d._children.forEach(collapse);
	      d.children = null;
	    }
	  }
	
	  root.children.forEach(collapse);
	  update(root);
	
	d3.select(self.frameElement).style("height", "800px");


function update(source) {

  // Compute the new tree layout.
  var nodes = tree.nodes(root).reverse(),
      links = tree.links(nodes);

  // Normalize for fixed-depth.
  nodes.forEach(function(d) { d.y = d.depth * 180; });

  // Update the nodes…
  var node = svg.selectAll("g.node")
      .data(nodes, function(d) { return d.id || (d.id = ++i); });

  // Enter any new nodes at the parent's previous position.
  var nodeEnter = node.enter().append("g")
      .attr("class", "node")
      .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
      .on("click", click);

  nodeEnter.append("circle")
      .attr("r", 1e-6)
      .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; });

  nodeEnter.append("text")
      .attr("x", function(d) { return d.children || d._children ? -10 : 10; })
      .attr("dy", ".35em")
      .attr("text-anchor", function(d) { return d.children || d._children ? "end" : "start"; })
      .text(function(d) { return d.name; })
      .style("fill-opacity", 1e-6);

  // Transition nodes to their new position.
  var nodeUpdate = node.transition()
      .duration(duration)
      .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

  nodeUpdate.select("circle")
      .attr("r", 4.5)
      .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; });

  nodeUpdate.select("text")
      .style("fill-opacity", 1);

  // Transition exiting nodes to the parent's new position.
  var nodeExit = node.exit().transition()
      .duration(duration)
      .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
      .remove();

  nodeExit.select("circle")
      .attr("r", 1e-6);

  nodeExit.select("text")
      .style("fill-opacity", 1e-6);

  // Update the links…
  var link = svg.selectAll("path.link")
      .data(links, function(d) { return d.target.id; });

  // Enter any new links at the parent's previous position.
  link.enter().insert("path", "g")
      .attr("class", "link")
      .attr("d", function(d) {
        var o = {x: source.x0, y: source.y0};
        return diagonal({source: o, target: o});
      });

  // Transition links to their new position.
  link.transition()
      .duration(duration)
      .attr("d", diagonal);

  // Transition exiting nodes to the parent's new position.
  link.exit().transition()
      .duration(duration)
      .attr("d", function(d) {
        var o = {x: source.x, y: source.y};
        return diagonal({source: o, target: o});
      })
      .remove();

  // Stash the old positions for transition.
  nodes.forEach(function(d) {
    d.x0 = d.x;
    d.y0 = d.y;
  });
}

// Toggle children on click.
function click(d) {
	
  if (d.children) {
    d._children = d.children;
    d.children = null;
    update(d);
  } else {
	if(d._children == null || d._children.length == 0) {
		var memberfilter = new Array();
		memberfilter[0] = "name";
		memberfilter[1] = "level";
		memberfilter[2] = "filters";
		memberfilter[3] = "imageSrc";
		console.log(JSON.stringify(d,memberfilter));
	    
		jq.getJSON("../heirarchical_chartdata.do?chart_id=" + chartId + "&node=" + JSON.stringify(d,memberfilter), function(addTheseJSON) {
		    var newnodes = tree.nodes(addTheseJSON.children).reverse();
		    d.children = newnodes[0];
		    d._children = null;
		    update(d);
		});
	} else {
		d.children = d._children;
	    d._children = null;
	    update(d);
	}
  }
}

}