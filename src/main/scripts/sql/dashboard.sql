create database IF NOT EXISTS dashboard; 
  
use dashboard;

DROP TABLE IF EXISTS `dash_application`;

create table dash_application (
dash_app_id VARCHAR(50) NOT NULL PRIMARY KEY,  
dash_app_name VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

LOCK TABLES `dash_application` WRITE;
INSERT INTO dash_application(dash_app_id,dash_app_name) VALUES ('demo','Demo'),('telematics','Telematics'),('insurance','Insurance'),('circuit','Circuit');
UNLOCK TABLES;

DROP TABLE IF EXISTS `user_details`;

create table user_details (
id VARCHAR(50) NOT NULL,
first_name  VARCHAR(50),
last_name  VARCHAR(50),
password VARCHAR(40) ,
active_flag CHAR(1),
PRIMARY KEY(id)
) ENGINE=InnoDB;

LOCK TABLES `user_details` WRITE;
INSERT INTO user_details(id,first_name,last_name,password,active_flag) VALUES ('user', 'User','','HGq7g5SZ3oo=','N'),('admin','Administrator','','HGq7g5SZ3oo=','N');
UNLOCK TABLES;

DROP TABLE IF EXISTS `dashboard_details`;

create table dashboard_details (
dashboard_id  INT NOT NULL auto_increment,  
dashboard_name  VARCHAR(200) ,
user_id VARCHAR(100) ,
column_count TINYINT,
sequence INT,
source_id VARCHAR(100),
application_id VARCHAR(50),
visibility TINYINT,
last_updated_date TIMESTAMP,
common_filter TINYINT,
filter_order TEXT,
PRIMARY KEY(dashboard_id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `widget_details`;

create table widget_details (
widget_id  INT NOT NULL auto_increment,  
dashboard_id  INT ,
widget_name VARCHAR(250) ,
widget_state CHAR(1) ,
chart_type TINYINT,
column_identifier TINYINT,
widget_sequence TINYINT,
chart_data TEXT ,
single_widget BOOL,
PRIMARY KEY(widget_id),
FOREIGN KEY(dashboard_id) REFERENCES dashboard_details(dashboard_id)
) ENGINE=InnoDB;

ALTER TABLE dashboard_details MODIFY user_id VARCHAR(100); 

create table chart_details (
id  INT NOT NULL auto_increment,  
name  VARCHAR(100),
description VARCHAR(200),
configuration TEXT,
created_by VARCHAR(100),
category INT,
isplugin TINYINT,
PRIMARY KEY(id)
) ENGINE=InnoDB;

INSERT INTO chart_details(name,description,configuration,created_by,category,isplugin) 
VALUES('Bar Chart',
'Chart displays data visually as bars',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><xyConfiguration><dependentCssURL>css/c3.css</dependentCssURL><dependentJsURL>js/lib/d3.v3.min.js</dependentJsURL><dependentJsURL>js/lib/c3.js</dependentJsURL><functionName>createXYChart</functionName><imageURL>chart/bar.png</imageURL><jsURL>js/XYChart.js</jsURL><enableXGrouping>true</enableXGrouping><maxXColumns>2</maxXColumns><maxYColumns>0</maxYColumns></xyConfiguration>',
'2',1,false);

INSERT INTO chart_details(name,description,configuration,created_by,category,isplugin) 
VALUES('Line Chart',
'Chart displays data visually as lines',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><xyConfiguration><dependentCssURL>css/c3.css</dependentCssURL><dependentJsURL>js/lib/d3.v3.min.js</dependentJsURL><dependentJsURL>js/lib/c3.js</dependentJsURL><functionName>createXYChart</functionName><imageURL>chart/line.png</imageURL><jsURL>js/XYChart.js</jsURL><enableXGrouping>true</enableXGrouping><maxXColumns>2</maxXColumns><maxYColumns>0</maxYColumns></xyConfiguration>',
'2',1,false);


INSERT INTO chart_details(name,description,configuration,created_by,category,isplugin) 
VALUES('Pie Chart',
'Circular chart divided into sectors',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><xyConfiguration><dependentCssURL>css/c3.css</dependentCssURL><dependentJsURL>js/lib/d3.v3.min.js</dependentJsURL><dependentJsURL>js/lib/c3.js</dependentJsURL><functionName>createXYChart</functionName><imageURL>chart/pie.png</imageURL><jsURL>js/XYChart.js</jsURL><enableXGrouping>false</enableXGrouping><maxXColumns>1</maxXColumns><maxYColumns>1</maxYColumns></xyConfiguration>',
'2',2,false);

INSERT INTO chart_details(name,description,configuration,created_by,category,isplugin) 
VALUES('Donut Chart',
'A Donut chart',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><xyConfiguration><dependentCssURL>css/c3.css</dependentCssURL><dependentJsURL>js/lib/d3.v3.min.js</dependentJsURL><dependentJsURL>js/lib/c3.js</dependentJsURL><functionName>createXYChart</functionName><imageURL>chart/donut.png</imageURL><jsURL>js/XYChart.js</jsURL><enableXGrouping>false</enableXGrouping><maxXColumns>1</maxXColumns><maxYColumns>1</maxYColumns></xyConfiguration>',
'2',2,false);

INSERT INTO chart_details(name,description,configuration,created_by,category,isplugin) 
VALUES('Table Widget',
'Displays data visually in Table',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><xyConfiguration><imageURL>chart/table.png</imageURL></xyConfiguration>',
'2',4,false);

INSERT INTO chart_details(name,description,configuration,created_by,category,isplugin) 
VALUES('Tree Layout',
'Displays data visually in Tree structure',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><xyConfiguration><dependentCssURL>css/d3.css</dependentCssURL><dependentJsURL>js/lib/d3.v3.min.js</dependentJsURL><functionName>createTreeChart</functionName><imageURL>chart/tree.png</imageURL><jsURL>js/simpletree.js</jsURL><minLevels>2</minLevels><maxLevels>0</maxLevels></xyConfiguration>',
'2',3,false);

INSERT INTO chart_details(name,description,configuration,created_by,category,isplugin) 
VALUES('Force Directed Graph',
'Chart connects nodes based on relation defined',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><xyConfiguration><dependentCssURL>css/cluster.css</dependentCssURL><dependentJsURL>js/lib/d3.v3.min.js</dependentJsURL><functionName>createClusterChart</functionName><imageURL>chart/cluster.png</imageURL><jsURL>js/clusterChart.js</jsURL></xyConfiguration>',
'2',7,false);

INSERT INTO chart_details(name,description,configuration,created_by,category,isplugin) 
VALUES('Gauge Chart',
'Gauge Chart',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><xyConfiguration><dependentCssURL>css/gauge.css</dependentCssURL><dependentJsURL>js/lib/d3.v3.min.js</dependentJsURL><functionName>createGaugeChart</functionName><imageURL>chart/gauge.png</imageURL><jsURL>js/gaugeChart.js</jsURL></xyConfiguration>',
'2',5,false);

INSERT INTO chart_details(name,description,configuration,created_by,category,isplugin) 
VALUES('Text Widget',
'Enables user to type own Text',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><xyConfiguration><imageURL>chart/text_editor.png</imageURL></xyConfiguration>',
'2',6,false);

INSERT INTO chart_details(name,description,configuration,created_by,category,isplugin) 
VALUES('Choropleth Map',
'Choropleth Map',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><xyConfiguration><dependentCssURL>css/d3.css</dependentCssURL><dependentJsURL>js/lib/d3.v3.min.js</dependentJsURL><dependentJsURL>js/lib/topojson.v1.min.js</dependentJsURL><dependentJsURL>js/lib/datamaps.usa.min.js</dependentJsURL><functionName>createGeoChart</functionName><imageURL>chart/geo.png</imageURL><jsURL>js/usgeomap.js</jsURL><enableXGrouping>false</enableXGrouping><maxXColumns>1</maxXColumns><maxYColumns>1</maxYColumns></xyConfiguration>',
'2',8,false);

INSERT INTO chart_details(name,description,configuration,created_by,category,isplugin) 
VALUES('Scored Search',
'Scored Search widget used to advanced settings on Filter',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><xyConfiguration><imageURL>chart/scored_search.png</imageURL></xyConfiguration>',
'2',9,false);

INSERT INTO chart_details(name,description,configuration,created_by,category,isplugin) 
VALUES('Relevant',
'Relevant widget',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <xyConfiguration>
                                <dependentCssURL>http://handsontable.com/dist/handsontable.full.css</dependentCssURL> 								
                                <functionName>createRelevantChart</functionName>
                                <imageURL>chart/China_Relevant_Graph.jpg</imageURL>
                                <dependentCssURL>js/relevant/css/relevant.css</dependentCssURL>
								<dependentJsURL>js/relevant/visualization/bower_components/requirejs/require.js</dependentJsURL>
								<dependentJsURL>js/relevant/visualization/src/config.js</dependentJsURL>
                                <jsURL>js/relevant.js</jsURL>
                </xyConfiguration>',
'2',10,false);

create table acl_public (
dashboard_id  INT,  
group_code VARCHAR(50),
group_name VARCHAR(100),
role VARCHAR(50),
last_updated_date TIMESTAMP,
PRIMARY KEY(dashboard_id,group_code)
) ENGINE=InnoDB;

create table group_details (
group_code VARCHAR(50) NOT NULL,
group_name VARCHAR(100) NOT NULL,
PRIMARY KEY(group_code)
) ENGINE=InnoDB;

INSERT INTO group_details(group_code, group_name) VALUES ('Demo_Consumer', 'Demo Consumer'),('Demo_Admin', 'Demo Admin'),('Demo_Contributor', 'Demo Contributor');

create table user_groups (
group_code VARCHAR(50) NOT NULL,
user_id VARCHAR(50) NOT NULL,
PRIMARY KEY(group_code,user_id)
) ENGINE=InnoDB;

INSERT INTO user_groups(group_code, user_id) VALUES ('Demo_Consumer', 'user'),('Demo_Admin', 'admin'),('Demo_Contributor', 'user');

