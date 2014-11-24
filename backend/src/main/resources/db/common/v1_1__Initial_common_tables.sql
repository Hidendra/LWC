CREATE TABLE ${prefix}protection_meta (
  protection_id INTEGER NOT NULL,
  meta_name INTEGER NOT NULL,
  meta_value varchar(255) NOT NULL,
  PRIMARY KEY (protection_id,meta_name)
) ;

CREATE TABLE ${prefix}player_settings (
  protection_id INTEGER NOT NULL,
  player_id VARCHAR(36) NOT NULL,
  setting_node VARCHAR(255) NOT NULL,
  setting_value varchar(255) NOT NULL,
  PRIMARY KEY (protection_id,setting_node)
) ;

CREATE TABLE ${prefix}protection_roles (
  protection_id INTEGER NOT NULL,
  type INTEGER NOT NULL,
  name INTEGER NOT NULL,
  role INTEGER NOT NULL,
  PRIMARY KEY (protection_id,type,name)
) ;
CREATE INDEX ${prefix}protection_id ON ${prefix}protection_roles (protection_id);
CREATE UNIQUE INDEX ${prefix}roles ON ${prefix}protection_roles (protection_id, type, name);

CREATE TABLE ${prefix}protection_blocks (
  protection_id INTEGER NOT NULL,
  world INTEGER NOT NULL,
  x INTEGER NOT NULL,
  y INTEGER NOT NULL,
  z INTEGER NOT NULL
) ;
CREATE INDEX ${prefix}blocks_id ON ${prefix}protection_blocks (protection_id);
CREATE UNIQUE INDEX ${prefix}blocks on ${prefix}protection_blocks (world, x, y, z);