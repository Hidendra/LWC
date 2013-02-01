CREATE TABLE __PREFIX__protections (
  id INTEGER NOT NULL PRIMARY KEY,
  x INTEGER NOT NULL,
  y INTEGER NOT NULL,
  z INTEGER NOT NULL,
  world varchar(255) NOT NULL,
  created INTEGER NOT NULL,
  updated INTEGER NOT NULL,
  accessed INTEGER NOT NULL
);
CREATE UNIQUE INDEX __PREFIX__position ON __PREFIX__protections (x, y, z, world);