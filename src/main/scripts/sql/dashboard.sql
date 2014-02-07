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
user_id  INT(20) NOT NULL auto_increment,  
user_name  VARCHAR(40) ,
password VARCHAR(40) ,
active_flag CHAR(1),
PRIMARY KEY(user_id)
) ENGINE=InnoDB;

LOCK TABLES `user_details` WRITE;
INSERT INTO user_details(USER_NAME,PASSWORD,ACTIVE_FLAG) VALUES ('anonymous','1234','N'),('admin','1234','N'),('zkoss','1234','N');
UNLOCK TABLES;

DROP TABLE IF EXISTS `dashboard_details`;

create table dashboard_details (
DASHBOARD_ID  INT(30) NOT NULL auto_increment,  
DASHBOARD_NAME  VARCHAR(40) ,
USER_ID INT(20) ,
DASHBOARD_STATE  CHAR(1),
COLUMN_COUNT TINYINT(7),
SEQUENCE INT(40) ,
SOURCE_ID VARCHAR(50),
APPLICATION_ID VARCHAR(50),
UPDATEDDATE DATE,
PRIMARY KEY(DASHBOARD_ID)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `WIDGET_DETAILS`;

create table WIDGET_DETAILS (
WIDGET_ID  INT(40) NOT NULL auto_increment,  
DASHBOARD_ID  INT(30) ,
WIDGET_NAME VARCHAR(30) ,
WIDGET_STATE CHAR(1) ,
CHART_TYPE TINYINT(7) ,
COLUMN_IDENTIFIER TINYINT(7) ,
WIDGET_SEQUENCE TINYINT(7) ,
CHART_DATA TEXT ,
PRIMARY KEY(WIDGET_ID),
FOREIGN KEY(DASHBOARD_ID) REFERENCES dashboard_details(DASHBOARD_ID)
) ENGINE=InnoDB;
