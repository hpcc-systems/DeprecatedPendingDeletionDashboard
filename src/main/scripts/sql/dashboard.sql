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
user_id VARCHAR(40) NOT NULL,
user_name  VARCHAR(40),
password VARCHAR(40) ,
active_flag CHAR(1),
PRIMARY KEY(user_id)
) ENGINE=InnoDB;

LOCK TABLES `user_details` WRITE;
INSERT INTO user_details(user_id, user_name,password,active_flag) VALUES ('user', 'User','1234','N'),('admin','Administrator','1234','N');
UNLOCK TABLES;

DROP TABLE IF EXISTS `dashboard_details`;

create table dashboard_details (
dashboard_id  INT NOT NULL auto_increment,  
dashboard_name  VARCHAR(200) ,
user_id VARCHAR(100) ,
application_id VARCHAR(50),
visibility TINYINT,
last_updated_date TIMESTAMP,
PRIMARY KEY(dashboard_id)
) ENGINE=InnoDB;

