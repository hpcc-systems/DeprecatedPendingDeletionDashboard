create database IF NOT EXISTS dashboard;

use dashboard;

DROP TABLE IF EXISTS `dash_application`;

create table dash_application (
dash_app_id VARCHAR(50) NOT NULL PRIMARY KEY,  
dash_app_name VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

drop table if exists dash_menu ;

create table dash_menu (
dash_application_id    VARCHAR(50) NOT NULL,  
dash_menu_id   VARCHAR(100) NOT NULL,
dash_menu_name VARCHAR(100) NOT NULL,
dash_menu_image_location VARCHAR(100),
dash_menu_page_location  VARCHAR(100),
PRIMARY KEY(dash_application_id, dash_menu_id)
) ENGINE=InnoDB;

create table dashboard_details (
application_id    VARCHAR(50) NOT NULL,  
dashboard_id   INT(20) NOT NULL auto_increment,
dashboard_name VARCHAR(30) NOT NULL,
page_layout  VARCHAR(20),
dashboard_sequence INT(10),
PRIMARY KEY(dashboard_id)
) ENGINE=InnoDB;

ALTER TABLE dash_menu ADD CONSTRAINT FOREIGN KEY (dash_application_id)  REFERENCES dash_application (dash_app_id);

LOCK TABLES `dash_application` WRITE;
INSERT INTO dash_application(dash_app_id,dash_app_name) VALUES ('A001','Demo'),('A002','Telematics'),('A003','Insurance');
UNLOCK TABLES;

create table user_details (
user_id  INT(20) NOT NULL auto_increment,  
user_name  VARCHAR(40) ,
password VARCHAR(40) ,
active_flag CHAR(1),
PRIMARY KEY(user_id)
) ENGINE=InnoDB;

INSERT INTO user_details(USER_NAME,PASSWORD,ACTIVE_FLAG) VALUES ('anonymous','1234','N'),('admin','1234','N'),('zkoss','1234','N');

drop table if exists dashboard_details ;

create table dashboard_details (
DASHBOARD_ID  INT(30) NOT NULL auto_increment,  
DASHBOARD_NAME  VARCHAR(40) ,
USER_ID INT(20) ,
DASHBOARD_STATE  CHAR(1),
COLUMN_COUNT TINYINT(7),
SEQUENCE INT(40) ,
APPLICATION_ID VARCHAR(20) ,
PRIMARY KEY(DASHBOARD_ID),
FOREIGN KEY(user_id) REFERENCES user_details(user_id)
) ENGINE=InnoDB;

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
