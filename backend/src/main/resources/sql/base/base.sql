CREATE TABLE __PREFIX__protection_attributes (
  protection_id INTEGER NOT NULL,
  attribute_name varchar(20) NOT NULL,
  attribute_value varchar(255) NOT NULL,
  PRIMARY KEY (protection_id,attribute_name)
) ;

CREATE TABLE __PREFIX__protection_roles (
  protection_id INTEGER NOT NULL,
  type INTEGER NOT NULL,
  name varchar(32) NOT NULL,
  role INTEGER NOT NULL,
  PRIMARY KEY (protection_id,type,name)
) ;
CREATE INDEX __PREFIX__protection_id ON __PREFIX__protection_roles (protection_id);
CREATE UNIQUE INDEX __PREFIX__roles ON __PREFIX__protection_roles (protection_id, type, name);