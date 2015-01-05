function visualizeDDLChart(data){
	var chartData = JSON.parse(data);
	var url = chartData.url;
	var target = chartData.target;	
	var proxymappings = null;
	var visualizeRoxie = false;
	var layout = "Hierarchy";
	var hpccID = chartData.hpccID;
	
	require(["assets/js/Visualization/widgets/config"], function () {			
		
		requirejs.config({
            baseUrl: "assets/js/Visualization/widgets"
        });
		
		require(["src/marshaller/Graph"
	             ], function (GraphMarshaller
	                 ) {
			
			            if (!window.location.origin) 
			            window.location.origin = window.location.protocol+"//"+window.location.host;
			            
			            var hostUrl = window.location.origin + "/Dashboard/" + hpccID;

						var proxyMappings = jq.parseJSON('{"' + params.WsWorkunits
					+ '/WUResult.json":"' + hostUrl + '/proxy-WUResult.do","'
					+ params.WsWorkunits + '/WUInfo.json":"' + hostUrl
					+ '/proxy-WUInfo.do","' + params.WsEcl
					+ '/submit/query/":"' + hostUrl + '/proxy-WsEcl.do"}');

					 
					 GraphMarshaller.createSingle(url, proxymappings, visualizeRoxie,
					function(graphDashboard, json) {
						graphDashboard.target(target).layout(layout)
								.renderDashboards();			
				// chart persistence code
						            window.addEventListener("beforeunload", function() {
							graphDashboard.save();
						});
					});

		});

	});
}