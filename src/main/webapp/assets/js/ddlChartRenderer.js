function visualizeDDLChart(data){
	var chartData = JSON.parse(data);
	var url = chartData.url;
	var target = chartData.target;	
	
	var visualizeRoxie = false;
	var layout = "Hierarchy";
	var hpccID = chartData.hpccID;
	
	var proxyMappings = null;
	if (!window.location.origin) 
		window.location.origin = window.location.protocol+"//"+window.location.host;
	
	var hostUrl = window.location.origin + "/Dashboard/" + hpccID;
	
	proxyMappings = jq.parseJSON('{"' + chartData.WsWorkunits
			+ '/WUResult.json":"' + hostUrl + '/proxy-WUResult.do","'
			+ chartData.WsWorkunits + '/WUInfo.json":"' + hostUrl
			+ '/proxy-WUInfo.do","' + chartData.WsEcl
			+ '/submit/query/":"' + hostUrl + '/proxy-WsEcl.do"}');
	
	require(["assets/js/Visualization/widgets/config"], function () {			
		
		requirejs.config({
            baseUrl: "assets/js/Visualization/widgets"
        });
		
		require(["src/marshaller/Graph"
	             ], function (GraphMarshaller
	                 ) {

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