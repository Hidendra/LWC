/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50519
Source Host           : localhost:3306
Source Database       : lwc5

Target Server Type    : MYSQL
Target Server Version : 50519
File Encoding         : 65001

Date: 2012-03-13 19:27:44
*/

-- ----------------------------
-- Table structure for `permissions`
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `protection_id` int(11) NOT NULL,
  `type` int(11) NOT NULL COMMENT 'ordinal() from Permission.Type',
  `value` varchar(20) NOT NULL COMMENT 'player name, group name, etc',
  PRIMARY KEY (`id`),
  KEY `ProtectionId` (`protection_id`),
  KEY `type` (`type`),
  KEY `value` (`value`),
  CONSTRAINT `ProtectionId` FOREIGN KEY (`protection_id`) REFERENCES `protections` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='lwc 5.0.0 in development table schema';

-- ----------------------------
-- Records of permissions
-- ----------------------------

-- ----------------------------
-- Table structure for `protections`
-- ----------------------------
DROP TABLE IF EXISTS `protections`;
CREATE TABLE `protections` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` int(11) NOT NULL COMMENT 'ordinal() from Protection.Type',
  `owner` varchar(16) NOT NULL,
  `world` varchar(20) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `z` int(11) NOT NULL,
  `updated` int(11) NOT NULL COMMENT 'unix timestamp',
  `created` int(11) NOT NULL COMMENT 'unix timestamp',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Location` (`world`,`x`,`y`,`z`),
  KEY `Player` (`owner`),
  KEY `Type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='lwc 5.0.0 in development table schema';

-- ----------------------------
-- Records of protections
-- ----------------------------

-- ----------------------------
-- Table structure for `traits`
-- ----------------------------
DROP TABLE IF EXISTS `traits`;
CREATE TABLE `traits` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `protection_id` int(11) NOT NULL,
  `trait` int(11) NOT NULL COMMENT 'ordinal() from Trait.Type',
  `value` varchar(50) NOT NULL,
  `created` int(11) NOT NULL COMMENT 'unix timestamp',
  PRIMARY KEY (`id`),
  KEY `ProtectionId` (`protection_id`),
  KEY `trait` (`trait`),
  KEY `FKTrait` (`protection_id`,`trait`),
  CONSTRAINT `ProtectionId2` FOREIGN KEY (`protection_id`) REFERENCES `protections` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of traits
-- ----------------------------
