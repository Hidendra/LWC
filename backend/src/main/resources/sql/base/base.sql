CREATE TABLE __PREFIX__protection_meta (
  protection_id INTEGER NOT NULL,
  meta_name INTEGER NOT NULL,
  meta_value varchar(255) NOT NULL,
  PRIMARY KEY (protection_id,meta_name)
) ;

CREATE TABLE __PREFIX__protection_roles (
  protection_id INTEGER NOT NULL,
  type INTEGER NOT NULL,
  name INTEGER NOT NULL,
  role INTEGER NOT NULL,
  PRIMARY KEY (protection_id,type,name)
) ;
CREATE INDEX __PREFIX__protection_id ON __PREFIX__protection_roles (protection_id);
CREATE UNIQUE INDEX __PREFIX__roles ON __PREFIX__protection_roles (protection_id, type, name);

CREATE TABLE __PREFIX__protection_blocks (
  protection_id INTEGER NOT NULL,
  world INTEGER NOT NULL,
  x INTEGER NOT NULL,
  y INTEGER NOT NULL,
  z INTEGER NOT NULL
) ;
CREATE INDEX __PREFIX__blocks_id ON __PREFIX__protection_blocks (protection_id);
CREATE UNIQUE INDEX __PREFIX__blocks on __PREFIX__protection_blocks (world, x, y, z);