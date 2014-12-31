create database IF NOT EXISTS dashboard; 
  
use dashboard;

DROP TABLE IF EXISTS `application`;

create table application (
app_id VARCHAR(50) NOT NULL PRIMARY KEY,  
app_name VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

LOCK TABLES `application` WRITE;
INSERT INTO application(app_id,app_name) VALUES ('demo','Demo'),('telematics','Telematics'),('insurance','Insurance'),('circuit','Circuit');
UNLOCK TABLES;

DROP TABLE IF EXISTS `user`;

create table user (
id VARCHAR(40) NOT NULL,
name  VARCHAR(40),
password VARCHAR(40) ,
active_flag CHAR(1),
PRIMARY KEY(id)
) ENGINE=InnoDB;

LOCK TABLES `user` WRITE;
INSERT INTO user(id,name,password,active_flag) VALUES ('user', 'User','1234','N'),('admin','Administrator','1234','N');
UNLOCK TABLES;

DROP TABLE IF EXISTS `dashboard`;

create table dashboard (
id  INT NOT NULL auto_increment,  
name  VARCHAR(200) ,
user_id VARCHAR(100) ,
application_id VARCHAR(50),
visibility TINYINT,
last_updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
hpcc_id VARCHAR(100),
composition_name VARCHAR(200),
PRIMARY KEY(id)
) ENGINE=InnoDB;

