CREATE TABLE __PREFIX__protections (
  id INTEGER NOT NULL PRIMARY KEY auto_increment,
  x INTEGER NOT NULL,
  y INTEGER NOT NULL,
  z INTEGER NOT NULL,
  world INTEGER NOT NULL,
  created INTEGER NOT NULL,
  updated INTEGER NOT NULL,
  accessed INTEGER NOT NULL
);
CREATE UNIQUE INDEX __PREFIX__position ON __PREFIX__protections (x, y, z, world);

CREATE TABLE __PREFIX__lookup_attribute_name (
  id INTEGER NOT NULL PRIMARY KEY auto_increment,
  name varchar(255) NOT NULL
);

CREATE TABLE __PREFIX__lookup_role_type (
  id INTEGER NOT NULL PRIMARY KEY auto_increment,
  name varchar(255) NOT NULL
);

CREATE TABLE __PREFIX__lookup_role_name (
  id INTEGER NOT NULL PRIMARY KEY auto_increment,
  name varchar(255) NOT NULL
);

CREATE TABLE __PREFIX__lookup_world_name (
  id INTEGER NOT NULL PRIMARY KEY auto_increment,
  name varchar(255) NOT NULL
);