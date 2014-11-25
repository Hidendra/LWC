CREATE TABLE ${prefix}player_trust (
  player_id VARCHAR(36) NOT NULL,
  player_trusted VARCHAR(36) NOT NULL,
  PRIMARY KEY (player_id, player_trusted)
) ;