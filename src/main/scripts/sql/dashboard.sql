create database IF NOT EXISTS dashboard;

use dashboard;

DROP TABLE IF EXISTS `dash_application`;

create table dash_application (
dash_app_id VARCHAR(50) NOT NULL PRIMARY KEY,  
dash_app_name VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

LOCK TABLES `dash_application` WRITE;
INSERT INTO dash_application(dash_app_id,dash_app_name) VALUES ('A001','Demo'),('A002','Telematics'),('A003','Insurance'),('circuit','Circuit');
UNLOCK TABLES;

DROP TABLE IF EXISTS `user_details`;

create table user_details (
user_id  INT NOT NULL auto_increment,  
user_name  VARCHAR(40) ,
password VARCHAR(40) ,
active_flag CHAR(1),
PRIMARY KEY(user_id)
) ENGINE=InnoDB;

LOCK TABLES `user_details` WRITE;
INSERT INTO user_details(user_name,password,active_flag) VALUES ('anonymous','1234','N'),('admin','1234','N'),('zkoss','1234','N');
UNLOCK TABLES;

DROP TABLE IF EXISTS `dashboard_details`;

create table dashboard_details (
dashboard_id  INT NOT NULL auto_increment,  
dashboard_name  VARCHAR(200) ,
user_id INT ,
column_count TINYINT,
sequence INT,
source_id VARCHAR(100),
application_id VARCHAR(50),
last_updated_date TIMESTAMP,
PRIMARY KEY(dashboard_id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `widget_details`;

create table widget_details (
widget_id  INT NOT NULL auto_increment,  
dashboard_id  INT ,
widget_name VARCHAR(50) ,
widget_state CHAR(1) ,
chart_type TINYINT,
column_identifier TINYINT,
widget_sequence TINYINT,
chart_data TEXT ,
PRIMARY KEY(widget_id),
FOREIGN KEY(dashboard_id) REFERENCES dashboard_details(dashboard_id)
) ENGINE=InnoDB;
