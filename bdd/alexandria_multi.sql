/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 100421
 Source Host           : localhost:3306
 Source Schema         : ancestra_realm

 Target Server Type    : MySQL
 Target Server Version : 100421
 File Encoding         : 65001

 Date: 06/10/2021 00:05:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for accounts
-- ----------------------------
DROP TABLE IF EXISTS `accounts`;
CREATE TABLE `accounts`  (
  `guid` int(11) NOT NULL AUTO_INCREMENT,
  `account` varchar(30) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `pass` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `level` int(11) NOT NULL DEFAULT 0,
  `subscription` int(11) NOT NULL DEFAULT 0,
  `email` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `lastIP` varchar(15) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `lastConnectionDate` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `question` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT 'DELETE?',
  `reponse` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT 'DELETE',
  `pseudo` varchar(30) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `banned` tinyint(3) NOT NULL DEFAULT 0,
  `curIP` varchar(15) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `logged` int(1) NOT NULL DEFAULT 0,
  `giftID` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT 'giftID1;giftID2 ...',
  PRIMARY KEY (`guid`) USING BTREE,
  UNIQUE INDEX `account`(`account`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 12 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of accounts
-- ----------------------------
INSERT INTO `accounts` VALUES (11, 'admin', '123', 5, 0, '', '', '', 'DELETE?', 'DELETE', '', 0, '', 0, '');

-- ----------------------------
-- Table structure for banip
-- ----------------------------
DROP TABLE IF EXISTS `banip`;
CREATE TABLE `banip`  (
  `ip` varchar(15) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL
) ENGINE = MyISAM CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of banip
-- ----------------------------

-- ----------------------------
-- Table structure for gameservers
-- ----------------------------
DROP TABLE IF EXISTS `gameservers`;
CREATE TABLE `gameservers`  (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ServerIP` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `ServerPort` int(11) NOT NULL,
  `State` int(11) NOT NULL,
  `ServerBDD` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `ServerDBName` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `ServerUser` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `ServerPassword` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `key` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 3 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gameservers
-- ----------------------------
INSERT INTO `gameservers` VALUES (1, '127.0.0.1', 5555, 0, '127.0.0.1', 'alexandria_game', 'root', '', 'server1');

SET FOREIGN_KEY_CHECKS = 1;
