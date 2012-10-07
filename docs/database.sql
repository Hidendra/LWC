/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50528
 Source Host           : localhost
 Source Database       : minecraft

 Target Server Type    : MySQL
 Target Server Version : 50528
 File Encoding         : utf-8

 Date: 10/07/2012 12:16:53 PM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `protection_attributes`
-- ----------------------------
DROP TABLE IF EXISTS `protection_attributes`;
CREATE TABLE `protection_attributes` (
	`protection_id` int(11) NOT NULL,
	`attribute` varchar(20) NOT NULL,
	`attribute_value` varchar(255) NOT NULL,
	UNIQUE `unique` (protection_id, attribute)
) ENGINE=`InnoDB` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ROW_FORMAT=COMPACT CHECKSUM=0 DELAY_KEY_WRITE=0;

-- ----------------------------
--  Table structure for `protection_roles`
-- ----------------------------
DROP TABLE IF EXISTS `protection_roles`;
CREATE TABLE `protection_roles` (
	`protection_id` int(11) NOT NULL,
	`user_id` int(11) NOT NULL,
	`role` int(11) NOT NULL,
	UNIQUE `entity` (protection_id, user_id)
) ENGINE=`InnoDB` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ROW_FORMAT=COMPACT CHECKSUM=0 DELAY_KEY_WRITE=0;

-- ----------------------------
--  Table structure for `protection_users`
-- ----------------------------
DROP TABLE IF EXISTS `protection_users`;
CREATE TABLE `protection_users` (
	`id` int(11) NOT NULL,
	`type` int(11) NOT NULL,
	`name` varchar(255) NOT NULL,
	UNIQUE `entity` (type, `name`)
) ENGINE=`InnoDB` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ROW_FORMAT=COMPACT CHECKSUM=0 DELAY_KEY_WRITE=0;

-- ----------------------------
--  Table structure for `protections`
-- ----------------------------
DROP TABLE IF EXISTS `protections`;
CREATE TABLE `protections` (
	`id` int(11) NOT NULL,
	`type` smallint(6) NOT NULL,
	`x` int(11) NOT NULL,
	`y` int(11) NOT NULL,
	`z` int(11) NOT NULL,
	`world` varchar(255) NOT NULL,
	`created` int(11) NOT NULL,
	`updated` int(11) NOT NULL,
	`accessed` int(11) NOT NULL,
	UNIQUE `position` (x, y, z, world)
) ENGINE=`InnoDB` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ROW_FORMAT=COMPACT CHECKSUM=0 DELAY_KEY_WRITE=0;

SET FOREIGN_KEY_CHECKS = 1;
