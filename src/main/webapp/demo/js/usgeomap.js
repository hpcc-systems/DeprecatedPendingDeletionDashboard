function createGeoChart(divId, reqData) {
	var chartData = jq.parseJSON(reqData);
	console.log(chartData);

	var uniqueId = "holder" + chartData.portletId;
	
	var divElement = jq('$' + divId).empty();
	divElement.append(jq("<div id='" + uniqueId + "'/>"));

	// size of the diagram
	var width = divElement.width();
	var height = divElement.height();
		
	if (width > 1.7 * height) {
		width = height * 1.7;
	}

	var colorData = jq.extend(true, {}, chartData.states);

	var arr = Object.keys(colorData).map(function(key) {
		return colorData[key];
	});
	var min = Math.min.apply(null, arr);
	var max = Math.max.apply(null, arr);
	var median = function(values) {

		values.sort(function(a, b) {
			return a - b;
		});

		var half = Math.floor(values.length / 2);

		if (values.length % 2)
			return values[half];
		else
			return (values[half - 1] + values[half]) / 2.0;
	}

	var colors = d3.scale.linear()
		.domain([ min, median(arr), max ])
		.range([ "red", "yellow", "green" ]);

	jq.each(colorData, function(key, val) {
		colorData[key] = colors(val);
	});

	var election = new Datamap({
		height : height,
		width : width,
		scope : 'usa',
		element : document.getElementById(uniqueId),

		geographyConfig : {
			highlightBorderColor : '#bada55',
			popupTemplate : function(geography, data) {
				return '<div class="hoverinfo">' + geography.properties.name
						+ '<br>'+ chartData.primaryYAxisLabel +' : ' + data + '</div>';
			},
			highlightBorderWidth : 3
		},

		fills : {
			defaultFill : '#b2e5e5'
		},
		data : chartData.states
	});

	election.labels();

	election.updateChoropleth(colorData);
}
