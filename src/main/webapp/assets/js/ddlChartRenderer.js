function visualizeDDLChart(data){
	var chartData = JSON.parse(data);
	var url = chartData.url;
	var target = chartData.target;	
	var proxymappings = null;
	var visualizeRoxie = false;
	var layout = "Hierarchy";
	
	require(["assets/js/Visualization/widgets/config"], function () {			
		
		requirejs.config({
            baseUrl: "assets/js/Visualization/widgets"
        });
		
		require(["src/marshaller/Graph"
	             ], function (GraphMarshaller
	                 ) {
					 
					 GraphMarshaller
			.createSingle(url, proxymappings, visualizeRoxie, function (graphDashboard, json) {				
	            graphDashboard
	                .target(target)
	                .layout(layout)
	                .renderDashboards()
	            ;			
				//chart persistence code
	            window.addEventListener("beforeunload", function () {
	                graphDashboard.save();
	            });
	        });	
					 
		 });		
		
    });
}