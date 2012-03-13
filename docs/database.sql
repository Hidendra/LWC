/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50519
Source Host           : localhost:3306
Source Database       : lwc5

Target Server Type    : MYSQL
Target Server Version : 50519
File Encoding         : 65001

Date: 2012-03-13 19:34:01
*/

-- ----------------------------
-- Table structure for `permissions`
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
`id`  int(11) NOT NULL ,
`protection_id`  int(11) NOT NULL ,
`type`  int(11) NOT NULL COMMENT 'ordinal() from Permission.Type' ,
`value`  varchar(20) NOT NULL COMMENT 'player name, group name, etc' ,
PRIMARY KEY (`id`),
FOREIGN KEY (`protection_id`) REFERENCES `protections` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
)
COMMENT='lwc 5.0.0 in development table schema'

;

-- ----------------------------
-- Table structure for `protections`
-- ----------------------------
DROP TABLE IF EXISTS `protections`;
CREATE TABLE `protections` (
`id`  int(11) NOT NULL ,
`type`  int(11) NOT NULL COMMENT 'ordinal() from Protection.Type' ,
`owner`  varchar(16) NOT NULL ,
`world`  varchar(20) NOT NULL ,
`x`  int(11) NOT NULL ,
`y`  int(11) NOT NULL ,
`z`  int(11) NOT NULL ,
`updated`  int(11) NOT NULL COMMENT 'unix timestamp' ,
`created`  int(11) NOT NULL COMMENT 'unix timestamp' ,
PRIMARY KEY (`id`)
)
COMMENT='lwc 5.0.0 in development table schema'

;

-- ----------------------------
-- Table structure for `traits`
-- ----------------------------
DROP TABLE IF EXISTS `traits`;
CREATE TABLE `traits` (
`id`  int(11) NOT NULL ,
`protection_id`  int(11) NOT NULL ,
`trait`  int(11) NOT NULL COMMENT 'ordinal() from Trait.Type' ,
`value`  varchar(50) NOT NULL ,
`created`  int(11) NOT NULL COMMENT 'unix timestamp' ,
PRIMARY KEY (`id`),
FOREIGN KEY (`protection_id`) REFERENCES `protections` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
)
COMMENT='lwc 5.0.0 in development table schema'

;

-- ----------------------------
-- Indexes structure for table permissions
-- ----------------------------
CREATE INDEX `ProtectionId` ON `permissions`(`protection_id`) USING BTREE ;
CREATE INDEX `type` ON `permissions`(`type`) USING BTREE ;
CREATE INDEX `value` ON `permissions`(`value`) USING BTREE ;

-- ----------------------------
-- Indexes structure for table protections
-- ----------------------------
CREATE UNIQUE INDEX `Location` ON `protections`(`world`, `x`, `y`, `z`) USING BTREE ;
CREATE INDEX `Player` ON `protections`(`owner`) USING BTREE ;
CREATE INDEX `Type` ON `protections`(`type`) USING BTREE ;

-- ----------------------------
-- Indexes structure for table traits
-- ----------------------------
CREATE INDEX `ProtectionId` ON `traits`(`protection_id`) USING BTREE ;
CREATE INDEX `trait` ON `traits`(`trait`) USING BTREE ;
CREATE INDEX `FKTrait` ON `traits`(`protection_id`, `trait`) USING BTREE ;
