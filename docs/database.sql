-- ----------------------------
--  Table structure for protection_attributes
-- ----------------------------
DROP TABLE IF EXISTS protection_attributes;
CREATE TABLE protection_attributes (
	protection_id INT(11) NOT NULL,
	attribute_name VARCHAR(20) NOT NULL,
	attribute_value VARCHAR(255) NOT NULL,
	PRIMARY KEY (protection_id, attribute_name)
) ENGINE=InnoDB;

-- ----------------------------
--  Table structure for protection_roles
-- ----------------------------
DROP TABLE IF EXISTS protection_roles;
CREATE TABLE protection_roles (
	protection_id INT(11) NOT NULL,
	TYPE INT(11) NOT NULL,
	NAME VARCHAR(30) NOT NULL,
	role INT(11) NOT NULL,
	INDEX (protection_id),
	PRIMARY KEY (protection_id, TYPE, NAME)
) ENGINE=InnoDB;

-- ----------------------------
--  Table structure for protections
-- ----------------------------
DROP TABLE IF EXISTS protections;
CREATE TABLE protections (
	id INT(11) NOT NULL AUTO_INCREMENT,
	x INT(11) NOT NULL,
	y INT(11) NOT NULL,
	z INT(11) NOT NULL,
	world VARCHAR(255) NOT NULL,
	created INT(11) NOT NULL,
	updated INT(11) NOT NULL,
	accessed INT(11) NOT NULL,
	UNIQUE POSITION (x, y, z, world),
	PRIMARY KEY (id)
) ENGINE=InnoDB;
