CREATE TABLE __PREFIX__protections (
  id INTEGER NOT NULL PRIMARY KEY,
  created INTEGER NOT NULL,
  updated INTEGER NOT NULL,
  accessed INTEGER NOT NULL
);
CREATE INDEX __PREFIX__protection_created ON __PREFIX__protections (created);
CREATE INDEX __PREFIX__protection_updated ON __PREFIX__protections (updated);
CREATE INDEX __PREFIX__protection_accessed ON __PREFIX__protections (accessed);

CREATE TABLE __PREFIX__lookup_meta_name (
  id INTEGER NOT NULL PRIMARY KEY,
  name varchar(255) NOT NULL
);

CREATE TABLE __PREFIX__lookup_role_type (
  id INTEGER NOT NULL PRIMARY KEY,
  name varchar(255) NOT NULL
);

CREATE TABLE __PREFIX__lookup_role_name (
  id INTEGER NOT NULL PRIMARY KEY,
  name varchar(255) NOT NULL
);

CREATE TABLE __PREFIX__lookup_world_name (
  id INTEGER NOT NULL PRIMARY KEY,
  name varchar(255) NOT NULL
);