/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80022 (8.0.22)
 Source Host           : localhost:3306
 Source Schema         : cool

 Target Server Type    : MySQL
 Target Server Version : 80022 (8.0.22)
 File Encoding         : 65001

 Date: 28/05/2026 23:53:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for model
-- ----------------------------
DROP TABLE IF EXISTS `model`;
CREATE TABLE `model`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `sex` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '性别,options.model-sex',
  `age` int NULL DEFAULT NULL COMMENT '年龄,,1',
  `date` date NULL DEFAULT NULL COMMENT '日期',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模型' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of model
-- ----------------------------
INSERT INTO `model` VALUES (40, 'test3', '0', 3, NULL, '2025-08-29 15:19:58', '2025-09-01 09:11:01');
INSERT INTO `model` VALUES (41, 'test4', '1', 4, NULL, '2025-08-29 15:19:58', '2025-09-01 09:11:01');
INSERT INTO `model` VALUES (42, 'test1', '0', 1, '2025-01-02', '2025-08-29 16:55:28', '2025-09-01 09:11:01');
INSERT INTO `model` VALUES (43, 'test2', '0', 2, '2025-07-26', '2025-08-29 16:55:28', '2025-09-01 09:11:01');
INSERT INTO `model` VALUES (44, 'test5', '0', 5, NULL, '2025-08-29 16:55:28', '2025-09-01 09:11:01');
INSERT INTO `model` VALUES (45, 'test6', '1', 6, NULL, '2025-08-29 16:55:28', '2025-09-01 09:11:01');
INSERT INTO `model` VALUES (46, 'test7', '0', 7, NULL, '2025-08-29 16:55:28', '2025-09-01 09:11:01');
INSERT INTO `model` VALUES (47, 'test8', '1', 8, NULL, '2025-08-29 16:55:28', '2025-09-01 09:11:01');
INSERT INTO `model` VALUES (48, 'test9', '0', 9, NULL, '2025-08-29 16:55:28', '2025-09-01 09:11:01');
INSERT INTO `model` VALUES (49, 'test10', '0', 10, NULL, '2025-08-29 16:55:28', '2025-08-29 16:59:05');
INSERT INTO `model` VALUES (52, 'test22', '37', 11, NULL, '2025-08-29 16:57:06', '2025-08-29 16:57:06');
INSERT INTO `model` VALUES (53, 'test11', '0', 10, NULL, '2025-08-29 17:01:29', '2025-09-01 09:11:01');

-- ----------------------------
-- Table structure for options
-- ----------------------------
DROP TABLE IF EXISTS `options`;
CREATE TABLE `options`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型',
  `label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '值',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0=正常, 1=回收站（软删除）',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `module`(`type` ASC, `label` ASC, `value` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '选项' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of options
-- ----------------------------
INSERT INTO `options` VALUES (1, 'sex', '男', '0', 0, NULL, NULL);
INSERT INTO `options` VALUES (2, 'sex', '女', '1', 0, NULL, NULL);

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名',
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色昵称',
  `test` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'test',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC, `nickname` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 205 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, 'root', '超级管理员', NULL, '2022-08-28 16:58:01', '2023-08-26 20:31:41');
INSERT INTO `role` VALUES (2, 'admin', '系统管理员', NULL, '2022-08-28 16:58:20', '2025-01-06 15:31:34');
INSERT INTO `role` VALUES (3, 'user', '用户', NULL, '2022-08-28 17:12:32', '2022-08-28 17:12:34');

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置分组：table / bi / system',
  `config_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置标识：user / report_001 / theme',
  `user_id` bigint NOT NULL DEFAULT 0 COMMENT '0=系统默认, >0=用户偏好',
  `config_value` json NOT NULL COMMENT '配置 JSON 内容',
  `version` int NOT NULL DEFAULT 0 COMMENT '系统默认版本号（用户记录始终=0）',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0=正常, 1=回收站（软删除）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config`(`config_group` ASC, `config_key` ASC, `user_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通用配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'table', 'user', 1, '{\"search\": {\"filter\": [{\"prop\": \"username\", \"label\": \"用户名\", \"operator\": \"contains\", \"filterMode\": \"exposed\"}, {\"prop\": \"sex\", \"label\": \"性别\", \"operator\": \"eq\", \"fieldType\": \"remote-select\", \"filterMode\": \"exposed\"}, {\"prop\": \"phone\", \"label\": \"电话\", \"operator\": \"contains\", \"filterMode\": \"hide\"}, {\"prop\": \"createTime\", \"label\": \"创建时间\", \"operator\": \"between\", \"fieldType\": \"daterange\", \"filterMode\": \"exposed\"}, {\"prop\": \"updateTime\", \"label\": \"修改时间\", \"operator\": \"between\", \"fieldType\": \"daterange\", \"filterMode\": \"hide\"}], \"currentField\": \"all\", \"filterValues\": [{\"value\": \"0\", \"column\": \"sex\", \"operator\": \"eq\"}]}, \"stripe\": true, \"columns\": [{\"prop\": \"id\", \"align\": \"center\", \"label\": \"ID\", \"hidden\": true, \"minWidth\": 80}, {\"prop\": \"username\", \"align\": \"left\", \"label\": \"用户名\", \"minWidth\": 140}, {\"prop\": \"age\", \"align\": \"left\", \"label\": \"年龄\", \"minWidth\": 80}, {\"prop\": \"sex\", \"align\": \"center\", \"label\": \"性别\", \"minWidth\": 80}, {\"prop\": \"phone\", \"align\": \"left\", \"label\": \"电话\", \"minWidth\": 160}, {\"prop\": \"createTime\", \"align\": \"center\", \"label\": \"创建时间\", \"minWidth\": 180}, {\"prop\": \"updateTime\", \"align\": \"center\", \"label\": \"修改时间\", \"minWidth\": 180}]}', 0, 0, '2026-05-28 15:36:02', '2026-05-28 23:52:41');

-- ----------------------------
-- Table structure for sys_config_history
-- ----------------------------
DROP TABLE IF EXISTS `sys_config_history`;
CREATE TABLE `sys_config_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置分组',
  `config_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置标识',
  `version` int NOT NULL COMMENT '快照版本号（单调递增）',
  `snapshot` json NOT NULL COMMENT '该版本 config_value 全量快照',
  `change_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SAVE(保存) / ROLLBACK(回退)',
  `rollback_from` int NULL DEFAULT NULL COMMENT '回退来源版本号（change_type=ROLLBACK 时记录）',
  `changed_by` bigint NOT NULL COMMENT '操作人 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_history`(`config_group` ASC, `config_key` ASC, `version` ASC) USING BTREE,
  INDEX `idx_timeline`(`config_group` ASC, `config_key` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统默认配置变更历史表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config_history
-- ----------------------------

-- ----------------------------
-- Table structure for system_log
-- ----------------------------
DROP TABLE IF EXISTS `system_log`;
CREATE TABLE `system_log`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `module` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作模块',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作名',
  `type` int NULL DEFAULT NULL COMMENT '操作分类',
  `request_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法方式',
  `opt_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `request_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
  `user_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户 IP',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地理位置',
  `device` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器 UserAgent',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `duration` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行时长，单位：毫秒',
  `result_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '结果码',
  `result_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '结果提示',
  `result_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '结果数据',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2370 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_log
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `sex` tinyint NULL DEFAULT NULL COMMENT '性别',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话号码',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1027 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (23, '答义轩', 'gZZR1M4QqiHUqq_', 48, 0, '15885326305', '2025-08-15 11:22:25', '2025-08-15 11:22:25');
INSERT INTO `user` VALUES (86, '马敏', 'ea', 77, 1, '18114196861', '2024-03-01 15:47:30', '2024-03-01 15:47:30');
INSERT INTO `user` VALUES (87, '文洋', 'sunt nisi magna labore et', 61, 0, '18633239620', NULL, NULL);
INSERT INTO `user` VALUES (88, '林刚', 'qui quis officia', 79, 0, '18162537882', NULL, NULL);
INSERT INTO `user` VALUES (89, '宋强', 'incididunt', 85, 0, '18125132862', NULL, NULL);
INSERT INTO `user` VALUES (90, '方磊', 'non do ipsum', 87, 0, '18161178419', NULL, NULL);
INSERT INTO `user` VALUES (91, '马娜', 'cillum incididunt est', 80, 0, '18106335359', NULL, NULL);
INSERT INTO `user` VALUES (92, '邱平', 'fugiat ex incididunt mollit', 45, 0, '18165019238', NULL, NULL);
INSERT INTO `user` VALUES (93, '薛艳', 'reprehenderit commodo do sint', 28, 0, '19871782512', NULL, NULL);
INSERT INTO `user` VALUES (94, '龚勇', 'irure', 32, 0, '18693835845', NULL, NULL);
INSERT INTO `user` VALUES (96, '丁强', 'ullamco incididunt sint voluptate', 50, 0, '18659352321', NULL, NULL);
INSERT INTO `user` VALUES (98, '蒋超', 'voluptate ullamco', 99, 0, '18679758446', NULL, NULL);
INSERT INTO `user` VALUES (99, '袁敏', 'fugiat do', 8, 1, '13611543587', NULL, NULL);
INSERT INTO `user` VALUES (101, '赖涛', 'Ut dolor commodo laborum sint', 22, 0, '18162217139', NULL, NULL);
INSERT INTO `user` VALUES (102, '刘磊', 'dolore pariatur', 28, 0, '18611862729', '2023-12-19 15:18:31', '2023-12-19 15:18:31');
INSERT INTO `user` VALUES (1000, '马敏2', 'abc2', 33, 1, '18114196861', '2024-03-01 15:47:30', '2025-04-14 08:39:46');
INSERT INTO `user` VALUES (1003, '枚蒙', 'r0wtQcPpCGU7YQS', 42, 0, '15885326303', '2025-04-10 14:58:39', '2025-04-10 15:21:12');
INSERT INTO `user` VALUES (1004, '马敏3', 'test', 22, 1, '18114196861', '2024-03-01 15:47:30', '2025-04-14 08:39:46');

SET FOREIGN_KEY_CHECKS = 1;
