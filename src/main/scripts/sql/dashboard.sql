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

ALTER TABLE dash_menu ADD CONSTRAINT FOREIGN KEY (dash_application_id)  REFERENCES dash_application (dash_app_id);

LOCK TABLES `dash_application` WRITE;
INSERT INTO dash_application VALUES ('A001','Demo'),('A002','Telematics'),('A003','Insurance');
UNLOCK TABLES;

LOCK TABLES `dash_menu` WRITE;
INSERT INTO dash_menu VALUES 
('A001', 'M001', 'Profile',  '/imgs/demo/profile.png',  '/demo/profile-mvc.zul'), 
('A001', 'M002', 'Dashboard1',  '/imgs/demo/dashboard1.png',  '/demo/dash_board1.zul'), 
('A001', 'M003', 'Dashboard2',  '/imgs/demo/dashboard2.png',  '/demo/dash_board2.zul'),
('A001', 'M004', 'Dashboard3',  '/imgs/demo/dashboard3.png',  '/demo/dash_board3.zul'),
('A002', 'M001', 'Profile',  '/imgs/telematics/profile.png',  '/telematics/profile-mvc.zul'), 
('A002', 'M002', 'Dashboard1',  '/imgs/telematics/dashboard1.png',  '/telematics/dash_board1.zul'), 
('A002', 'M003', 'Dashboard2',  '/imgs/telematics/dashboard2.png',  '/ telematics /dash_board2.zul');
UNLOCK TABLES;