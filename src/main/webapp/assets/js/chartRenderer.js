function createPreview(target, chartType, data) {
	clearChart(target);

	require([ "assets/js/Visualization/widgets/config" ], function() {

		requirejs.config({
			baseUrl : "assets/js/Visualization/widgets"
		});

		var actualData = JSON.parse(data);
		console.log(actualData);		
		
		require([ "src/c3/Pie", "src/c3/Line", "src/c3/Column", "src/map/ChoroplethStates" ], function(
				C3Pie, C3Line, C3Column,ChoroplethStates) {

			console.log(actualData);
			if (chartType == "PIE") {
				new C3Pie()
					.target(target)
					.data(actualData.data)
					.render();
			}

			if (chartType == "LINE") {
				new C3Line()
				.target(target)
				.data(actualData.data)
				.render();
			}

			if (chartType == "COLUMN") {
				new C3Column()
				.target(target)
				.data(actualData.data)
				.render();
			}
			
			if (chartType == "CHORO") {
				 new ChoroplethStates()
				 .columns(actualData.columns)
				 .data(actualData.data)
                 .target(target)
                 .render();
				
			}

		});
	});
}

function clearChart(target){
	jq('#'+target).empty();
}

function visualizeDDLChart(data) {
	var chartData = JSON.parse(data);
	var url = chartData.url;
	var target = chartData.target;

	var visualizeRoxie = false;
	var layout = "Hierarchy";
	var hpccID = chartData.hpccID;

	var proxyMappings = null;
	if (!window.location.origin)
		window.location.origin = window.location.protocol + "//" + window.location.host;

	var hostUrl = window.location.origin + "/Dashboard/" + hpccID;

	proxyMappings = jq.parseJSON('{"' + chartData.WsWorkunits + '/WUResult.json":"'+ hostUrl + '/proxy-WUResult.do","'
			+ chartData.WsWorkunits + '/WUInfo.json":"' + hostUrl + '/proxy-WUInfo.do","' 
			+ chartData.WsEcl + '/submit/query/":"' + hostUrl + '/proxy-WsEcl.do"}');

	require([ "assets/js/Visualization/widgets/config" ], function() {

		requirejs.config({
			baseUrl : "assets/js/Visualization/widgets"
		});

		require([ "src/marshaller/Graph" ], function(GraphMarshaller) {

			GraphMarshaller.createSingle(url, proxyMappings, visualizeRoxie,
					function(graphDashboard, json) {
						graphDashboard
							.target(target)
							.layout(layout)
							.renderDashboards();
					});
		});

	});

}
