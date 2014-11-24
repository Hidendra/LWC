CREATE TABLE ${prefix}protections (
  id SERIAL NOT NULL PRIMARY KEY,
  created INTEGER NOT NULL,
  updated INTEGER NOT NULL,
  accessed INTEGER NOT NULL
);
CREATE INDEX ${prefix}protection_created ON ${prefix}protections (created);
CREATE INDEX ${prefix}protection_updated ON ${prefix}protections (updated);
CREATE INDEX ${prefix}protection_accessed ON ${prefix}protections (accessed);

CREATE TABLE ${prefix}lookup_meta_name (
  id INTEGER NOT NULL PRIMARY KEY,
  name varchar(255) NOT NULL
);

CREATE TABLE ${prefix}lookup_role_type (
  id INTEGER NOT NULL PRIMARY KEY,
  name varchar(255) NOT NULL
);

CREATE TABLE ${prefix}lookup_role_name (
  id INTEGER NOT NULL PRIMARY KEY,
  name varchar(255) NOT NULL
);

CREATE TABLE ${prefix}lookup_world_name (
  id INTEGER NOT NULL PRIMARY KEY,
  name varchar(255) NOT NULL
);