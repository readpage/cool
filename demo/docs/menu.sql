/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30)
 Source Host           : localhost:3306
 Source Schema         : core

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30)
 File Encoding         : 65001

 Date: 29/12/2025 14:57:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for menu
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` int NULL DEFAULT NULL COMMENT '父节点',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由',
  `type` int NULL DEFAULT NULL COMMENT '类型',
  `order_num` int NULL DEFAULT NULL COMMENT '排序',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `anonymous` tinyint(1) NULL DEFAULT 0 COMMENT '是否匿名访问',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件',
  `keep_alive` tinyint(1) NULL DEFAULT 0 COMMENT '是否缓存',
  `embed` int NULL DEFAULT 1 COMMENT '嵌入',
  `out_link` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外部链接',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `path`(`path` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 263 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of menu
-- ----------------------------
INSERT INTO `menu` VALUES (1, 19, '/user/page', 3, 1, '分页查询用户', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (2, 19, '/user/save', 3, 2, '添加用户', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (3, 19, '/user/update', 3, 3, '修改用户', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (4, 19, '/user/updateInfo', 3, 4, '修改个人信息', NULL, 1, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (5, 19, '/user/remove', 3, 5, '删除用户', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (6, 20, '/role/page', 3, 1, '分页查询角色', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (7, 20, '/role/save', 3, 2, '添加角色', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (8, 20, '/role/update', 3, 3, '修改角色', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (9, 20, '/role/remove', 3, 4, '删除角色', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (10, 21, '/menu/list', 3, 1, '查询所有菜单列表', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (12, 21, '/menu/save', 3, 4, '添加菜单', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (13, 21, '/menu/update', 3, 5, '修改菜单', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (14, 21, '/menu/remove', 3, 6, '删除菜单', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (15, 23, '/system-log/page', 3, 2, '分页查询日志', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (16, 23, '/system-log/clear', 3, 3, '清空日志', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (17, NULL, '/system', 1, 2, '系统管理', 'config', 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2025-09-04 16:18:06');
INSERT INTO `menu` VALUES (18, 17, '/system/auth', 1, 1, '权限管理', 'lock', 0, NULL, NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (19, 18, '/system/auth/user', 2, 1, '用户管理', 'user', 0, 'user', '/src/views/system/auth/user/index.vue', 1, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (20, 18, '/system/auth/role', 2, 2, '角色管理', 'peoples', 0, 'role', '/src/views/system/auth/role/index.vue', 1, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (21, 18, '/system/auth/menu', 2, 3, '菜单管理', 'system', 0, 'menu', '/src/views/system/auth/menu/index.vue', 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (22, 17, '/system/monitor', 1, 2, '监控管理', 'monitor', 0, '', NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (23, 22, '/system/monitor/system-log', 2, 1, '请求日志', 'log', 0, 'system-log', '/src/views/system/monitor/system-log/index.vue', 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (24, NULL, '/home', 2, 1, '首页', 'home', 0, 'home', '/src/views/home/index.vue', 0, 1, NULL, '2023-05-24 09:16:14', '2025-09-05 14:27:28');
INSERT INTO `menu` VALUES (25, NULL, '/visualizing', 1, 3, '数据可视化', 'statistic', 0, '', NULL, 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (26, 25, '/visualizing/share', 2, 1, '数据可视化演示', 'share', 0, 'share', '/src/views/visualizing/index.vue', 0, 1, NULL, '2023-05-24 09:16:14', '2023-05-24 09:16:14');
INSERT INTO `menu` VALUES (31, 20, '/role/list', 3, 1, '查询所有角色列表', NULL, 0, NULL, NULL, 0, 1, NULL, '2023-08-26 10:31:16', '2023-08-26 10:31:16');
INSERT INTO `menu` VALUES (41, NULL, '/erp', 1, 4, 'erp系统', 'whale', 0, NULL, NULL, 0, 1, NULL, '2023-09-06 09:16:17', '2024-08-07 10:48:26');
INSERT INTO `menu` VALUES (52, NULL, '/form', 2, 15, '表单', 'order2', 0, NULL, '/src/views/form/index.vue', 0, 1, NULL, '2024-03-20 12:46:14', '2025-03-26 14:03:51');
INSERT INTO `menu` VALUES (53, 63, '/erp/prod-addr', 2, 1, '产品地点', 'tag-one', 0, 'prod-addr', '/src/views/erp/prod-addr/index.vue', 1, 1, NULL, '2024-03-23 10:23:37', '2024-03-28 09:27:18');
INSERT INTO `menu` VALUES (54, 53, '/erp-service/erp/batchUpdateProdAddr', 3, NULL, '产品地点修改', NULL, 0, NULL, NULL, 0, 1, NULL, '2024-03-23 10:24:57', '2024-03-23 10:24:57');
INSERT INTO `menu` VALUES (57, 41, '/erp/stock-unlock', 2, 1, '库存解锁', 'fish-one', 0, 'stock-unlock', '/src/views/erp/stock-unlock/index.vue', 1, 1, NULL, '2024-03-23 15:19:32', '2025-01-10 13:25:47');
INSERT INTO `menu` VALUES (58, 57, '/erp-service/stock/unlock/**', 3, NULL, '库存解锁接口', NULL, 0, NULL, NULL, 0, 1, NULL, '2024-03-23 15:20:36', '2024-03-23 15:20:36');
INSERT INTO `menu` VALUES (59, 41, '/erp-service/erp/addressList', 3, 8, '产品地点信息接口', NULL, 0, NULL, NULL, 0, 1, NULL, '2024-03-23 16:23:50', '2025-01-10 13:26:11');
INSERT INTO `menu` VALUES (60, 63, '/erp/prod-bps', 2, 2, '产品供应商', 'lightning', 0, 'prod-bps', '/src/views/erp/prod-bps/index.vue', 1, 1, NULL, '2024-03-27 14:25:26', '2024-03-28 09:27:36');
INSERT INTO `menu` VALUES (61, 60, '/erp-service/erp/bpsNumList/**', 3, 7, '获取供应商编码选项', NULL, 0, NULL, NULL, 0, 1, NULL, '2024-03-27 14:27:00', '2024-03-27 14:30:47');
INSERT INTO `menu` VALUES (62, 60, '/erp-service/erp/saveBatchProdBPS', 3, NULL, '批量添加产品供应商', NULL, 0, NULL, NULL, 0, 1, NULL, '2024-03-27 14:28:33', '2024-03-27 14:28:33');
INSERT INTO `menu` VALUES (63, 41, '/erp/update', 1, 0, 'erp修改', 'lightning', 0, NULL, NULL, 0, 1, NULL, '2024-03-28 09:26:52', '2024-03-28 09:26:52');
INSERT INTO `menu` VALUES (66, 65, '/erp-service/saleWeekPlan/**', 3, NULL, '导入', NULL, 0, NULL, NULL, 0, 1, NULL, '2024-05-15 14:28:36', '2025-09-04 16:46:32');
INSERT INTO `menu` VALUES (67, 41, '/erp/stock-capacity', 2, 3, '库存容量', 'search', 0, NULL, '/src/views/erp/stock-capacity/index.vue', 0, 1, NULL, '2024-05-22 15:20:24', '2025-01-10 13:26:28');
INSERT INTO `menu` VALUES (68, 67, '/erp-service/stock-capacity/**', 3, NULL, '库存容量接口', NULL, 0, NULL, NULL, 0, 1, NULL, '2024-05-24 10:18:21', '2025-01-10 11:28:25');
INSERT INTO `menu` VALUES (69, 63, '/erp/prod-category', 2, 3, '产品种类', 'lightning', 0, 'prod-category', '/src/views/erp/prod-category/index.vue', 1, 1, NULL, '2024-06-04 11:10:34', '2024-06-04 11:10:34');
INSERT INTO `menu` VALUES (70, 69, '/erp-service/erp/updateProdType', 3, NULL, '产品种类修改', NULL, 0, NULL, NULL, 0, 1, NULL, '2024-06-04 11:11:18', '2024-06-04 11:11:18');
INSERT INTO `menu` VALUES (71, 41, '/erp/prod-type', 2, 2, '产品类型', 'tag-one', 0, 'prod-type', '/src/views/erp/prod-type/index.vue', 0, 1, NULL, '2024-07-02 10:43:22', '2025-01-10 13:26:25');
INSERT INTO `menu` VALUES (72, 71, '/erp-service/prodType/**', 3, NULL, '产品类型接口', NULL, 0, NULL, NULL, 0, 1, NULL, '2024-07-02 10:43:28', '2025-01-10 11:29:27');
INSERT INTO `menu` VALUES (73, NULL, '/asset', 2, 10, '资产清单', 'log', 0, NULL, '/src/views/assets/index.vue', 0, 1, NULL, '2024-05-15 14:25:19', '2024-09-06 21:10:02');
INSERT INTO `menu` VALUES (75, 73, '/basic-service/assets/**', 3, NULL, '资产清单接口', NULL, 0, NULL, NULL, 0, 1, NULL, '2024-07-02 10:43:28', '2024-10-18 17:19:37');
INSERT INTO `menu` VALUES (76, 41, '/erp/transit', 2, 5, '运输提单', 'fish-one', 0, NULL, '/src/views/erp/transit/index.vue', 0, 1, NULL, NULL, '2025-01-10 13:26:03');
INSERT INTO `menu` VALUES (77, 76, '/erp-service/transit/**', 3, NULL, '运输提单接口', NULL, 0, NULL, NULL, 0, 1, NULL, NULL, NULL);
INSERT INTO `menu` VALUES (78, 41, '/erp-service/options/**', 3, NULL, 'erp菜单选项接口', NULL, 0, NULL, NULL, 0, 1, NULL, NULL, NULL);
INSERT INTO `menu` VALUES (79, 17, '/basic-service/options', 2, 4, '类型选项', 'tag-one', 0, 'options', '/src/views/system/options/index.vue', 0, 1, NULL, NULL, '2025-01-10 13:20:05');
INSERT INTO `menu` VALUES (80, 79, '/basic-service/options/**', 3, NULL, '类型选项接口', NULL, 0, NULL, NULL, 0, 1, NULL, NULL, NULL);
INSERT INTO `menu` VALUES (81, 17, '/basic-service/push-task', 2, 3, '推送任务', 'news', 0, 'push-task', '/src/views/system/push-task/index.vue', 0, 1, NULL, NULL, '2025-01-10 13:20:00');
INSERT INTO `menu` VALUES (82, 81, '/basic-service/push-task/**', 3, NULL, '推送任务接口', NULL, 0, NULL, NULL, 0, 1, NULL, NULL, NULL);
INSERT INTO `menu` VALUES (83, 23, '/system-log/getTypes', 3, NULL, '日志类型选项', NULL, 0, NULL, NULL, 0, 1, NULL, '2024-10-31 09:17:52', '2024-10-31 09:18:05');
INSERT INTO `menu` VALUES (84, NULL, '/training-record', 2, 14, '培训记录', 'analysis', 0, '', '/src/views/training-record/index.vue', 0, 1, NULL, NULL, '2025-03-26 14:04:39');
INSERT INTO `menu` VALUES (85, 84, '/data-origin-service/training-record/**', 3, NULL, '培训记录所有接口', NULL, 0, NULL, NULL, 0, 1, NULL, NULL, '2025-03-26 14:08:41');
INSERT INTO `menu` VALUES (86, 41, '/erp-service/erp/optionsList/**', 3, NULL, '获取erp类型选项', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-01-08 15:52:09', '2025-01-10 11:31:45');
INSERT INTO `menu` VALUES (87, 17, '/basic-service/options/getTypes/**', 3, 6, '根据模块获取类型选项', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-01-10 13:16:29', '2025-03-22 16:09:31');
INSERT INTO `menu` VALUES (88, 41, '/system/iframe', 2, NULL, '外链', 'share', 0, '', '/src/views/system/embed/out.vue', 0, 2, 'https://leetcode.cn', '2025-01-16 14:45:08', '2025-01-16 15:11:23');
INSERT INTO `menu` VALUES (89, 92, '/erp/nbpc-sales', 2, NULL, '非大客户销售报表', 'statistic', 0, '', '/src/views/system/iframe/index.vue', 0, 2, 'http://180.100.207.79:6080/financial/notBigCust', '2025-01-21 14:39:56', '2025-09-04 16:51:45');
INSERT INTO `menu` VALUES (90, 92, '/erp/financial', 2, NULL, '销售经营报表', 'ranking', 0, '', '/src/views/system/iframe/index.vue', 0, 2, 'http://180.100.207.79:6080/financial/home', '2025-01-22 10:56:16', '2025-09-04 16:51:53');
INSERT INTO `menu` VALUES (91, NULL, '/resManager', 2, 13, '仓储预约', 'fish-one', 0, '', '/src/views/system/iframe/index.vue', 0, 2, 'http://eip.supinfood.com/safetyStock/resManager?manager=1', '2025-01-22 11:23:26', '2025-11-26 15:43:08');
INSERT INTO `menu` VALUES (92, 41, '/erp/report', 1, 0, '报表', 'analysis', 0, NULL, NULL, 0, 1, NULL, '2025-02-11 09:31:16', '2025-02-20 16:39:23');
INSERT INTO `menu` VALUES (93, 92, '/erp/warehouse', 2, 0, '仓库报表', 'find', 0, '', '/src/views/system/iframe/index.vue', 0, 2, 'http://180.100.207.79:6080/ERPReportSet/home?dept=stash', '2025-02-20 17:13:58', '2025-09-04 16:51:23');
INSERT INTO `menu` VALUES (94, 84, '/data-origin-service/training-record/statPage/**', 3, NULL, '分页查询培训记录统计信息', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-03-21 17:11:10', '2025-03-21 17:11:10');
INSERT INTO `menu` VALUES (95, 84, '/data-origin-service/training-record/statExport/**', 3, NULL, '导出培训记录统计信息', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-03-21 17:12:21', '2025-03-21 17:12:21');
INSERT INTO `menu` VALUES (96, 84, '/data-origin-service/training-record/page/**', 3, NULL, '分页查询培训记录信息', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-03-21 17:12:51', '2025-03-21 17:12:59');
INSERT INTO `menu` VALUES (97, 84, '/data-origin-service/training-record/optionsList/**', 3, NULL, '获取类型选项', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-03-21 17:13:41', '2025-03-21 17:13:41');
INSERT INTO `menu` VALUES (98, 84, '/data-origin-service/training-record/modelDownload/**', 3, NULL, '下载培训记录模板', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-03-21 17:14:12', '2025-03-21 17:14:12');
INSERT INTO `menu` VALUES (99, 84, '/data-origin-service/training-record/getByName/**', 3, NULL, '根据名称查询员工信息', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-03-21 17:14:36', '2025-03-21 17:14:36');
INSERT INTO `menu` VALUES (100, 17, '/system/inform', 2, 5, '通知', 'remind', 0, 'inform', '/src/views/system/inform/index.vue', 0, 1, NULL, NULL, '2025-03-22 16:09:37');
INSERT INTO `menu` VALUES (101, 100, '/inform/page/**', 3, NULL, '分页查询通知信息', NULL, 0, NULL, NULL, 0, 1, NULL, NULL, NULL);
INSERT INTO `menu` VALUES (102, 108, '/inform/unreadList/**', 3, NULL, '查询用户未读通知信息', NULL, 0, NULL, NULL, 0, 1, NULL, NULL, '2025-03-22 16:09:06');
INSERT INTO `menu` VALUES (103, 100, '/inform/detail/**', 3, NULL, '查询通知明细', NULL, 0, NULL, NULL, 0, 1, NULL, NULL, NULL);
INSERT INTO `menu` VALUES (104, 100, '/inform/save/**', 3, NULL, '添加通知信息', NULL, 0, NULL, NULL, 0, 1, NULL, NULL, NULL);
INSERT INTO `menu` VALUES (105, 100, '/inform/update/**', 3, NULL, '修改通知信息', NULL, 0, NULL, NULL, 0, 1, NULL, NULL, NULL);
INSERT INTO `menu` VALUES (106, 100, '/inform/remove/**', 3, NULL, '删除通知信息', NULL, 0, NULL, NULL, 0, 1, NULL, NULL, NULL);
INSERT INTO `menu` VALUES (107, 108, '/inform/markRead/**', 3, NULL, '添加用户标记已读通知信息', NULL, 0, NULL, NULL, 0, 1, NULL, NULL, '2025-03-22 16:09:19');
INSERT INTO `menu` VALUES (108, NULL, '/general', 1, NULL, '通用', NULL, 0, NULL, NULL, 0, 1, NULL, NULL, NULL);
INSERT INTO `menu` VALUES (109, NULL, '/meal', 1, 9, '报餐管理', 'eagle', 0, NULL, NULL, 0, 1, NULL, NULL, '2025-03-26 14:04:05');
INSERT INTO `menu` VALUES (110, 109, '/meal/meal', 2, 1, '报餐', 'save', 0, 'meal', '/src/views/meal/meal/index.vue', 0, 1, NULL, NULL, '2025-03-26 13:51:35');
INSERT INTO `menu` VALUES (111, 109, '/meal/meal-consume', 2, 2, '消费记录', 'tag-one', 0, 'meal-consume', '/src/views/meal/meal-consume/index.vue', 0, 1, NULL, NULL, '2025-03-26 13:52:26');
INSERT INTO `menu` VALUES (112, 109, '/meal/meal-stats', 2, 3, '报餐统计', 'ranking', 0, 'meal-stats', '/src/views/meal/meal-stats/index.vue', 0, 1, NULL, NULL, '2025-03-26 13:52:33');
INSERT INTO `menu` VALUES (113, 92, '/salesManage', 2, NULL, '销管报表', 'ranking', 0, NULL, '/src/views/system/iframe/index.vue', 0, 2, 'http://180.100.207.79:6080/ERPReportSet/home?dept=salesMange', '2025-04-28 08:59:52', '2025-09-04 16:52:10');
INSERT INTO `menu` VALUES (114, 57, '/erp-service/stock/stockPage/**', 3, NULL, '分页查询产品库存', NULL, 0, NULL, NULL, 0, 1, NULL, '2024-03-23 15:20:36', '2024-03-23 15:20:36');
INSERT INTO `menu` VALUES (115, 109, '/data-origin-service/meal/**', 3, NULL, '报餐接口', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-05-17 15:39:43', '2025-05-17 15:39:44');
INSERT INTO `menu` VALUES (116, 108, '/data-origin-service/employeeDetail/nameOptions', 3, NULL, '人员名称选项', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-05-17 15:42:48', '2025-05-17 15:42:48');
INSERT INTO `menu` VALUES (117, 92, '/erp/report/research', 2, NULL, '研发台账报表', 'ranking', 0, NULL, '/src/views/erp/report/research/index.vue', 0, 1, NULL, '2025-05-23 14:31:42', '2025-05-23 14:34:39');
INSERT INTO `menu` VALUES (118, 117, '/erp-service/research/**', 3, NULL, '研发台账权限', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-05-23 14:34:27', '2025-05-23 14:34:27');
INSERT INTO `menu` VALUES (119, 17, '/system/task', 2, NULL, '任务管理', 'order', 0, NULL, '/src/views/system/task/index.vue', 0, 1, NULL, '2025-05-30 10:06:54', '2025-05-30 10:06:54');
INSERT INTO `menu` VALUES (120, 119, '/task-log/**', 3, NULL, '任务日志接口', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-06-06 08:20:59', '2025-06-06 08:20:59');
INSERT INTO `menu` VALUES (121, 119, '/task/**', 3, NULL, '任务接口', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-06-06 08:21:30', '2025-06-06 08:21:30');
INSERT INTO `menu` VALUES (122, 119, '/crm-service/task-log/retry/**', 3, NULL, 'crm任务重试', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-06-06 08:32:42', '2025-06-06 08:32:42');
INSERT INTO `menu` VALUES (123, 41, '/erp/sales-forecast', 2, NULL, '销售预测', 'eagle', 0, NULL, '/src/views/erp/sales-forecast/index.vue', 0, 1, NULL, '2025-06-24 15:03:36', '2025-06-24 15:03:36');
INSERT INTO `menu` VALUES (124, 92, '/erp/metabase', 2, NULL, '产品', 'find', 0, NULL, '/src/views/system/iframe/index.vue', 0, 2, 'http://localhost:3000/metabase?url=/question/54', '2025-08-09 18:07:09', '2025-09-17 14:30:33');
INSERT INTO `menu` VALUES (143, 108, '/user/list2/**', 3, NULL, '用户选项', NULL, 0, NULL, '', 0, 1, NULL, '2025-09-05 14:27:53', '2025-09-10 11:01:07');
INSERT INTO `menu` VALUES (144, 108, '/basic-service/auth/**', 3, NULL, '认证', NULL, 1, NULL, NULL, 0, 1, NULL, '2025-09-05 14:42:56', '2025-09-05 14:42:56');
INSERT INTO `menu` VALUES (146, NULL, '/freight', 1, NULL, '运费平台', 'travel', 0, NULL, NULL, 0, 1, NULL, '2025-09-08 13:45:05', '2025-09-10 10:50:16');
INSERT INTO `menu` VALUES (147, 146, '/freight/basic', 1, NULL, '基础资料', 'order', 0, NULL, NULL, 0, 1, NULL, '2025-09-08 13:45:58', '2025-09-10 10:26:01');
INSERT INTO `menu` VALUES (148, 147, '/freight/basic/modeStorage', 2, 0, '贮存方式', 'ranking', 0, NULL, '/src/views/freight/basic/modeStorage/index.vue', 0, 1, NULL, '2025-09-08 13:47:02', '2025-09-08 13:47:14');
INSERT INTO `menu` VALUES (149, 147, '/freight/basic/truck', 2, 1, '运输车型', 'whale', 0, NULL, '/src/views/freight/basic/truck/index.vue', 0, 1, NULL, '2025-09-09 13:48:08', '2025-09-16 15:29:46');
INSERT INTO `menu` VALUES (151, 147, '/freight/basic/modeTran', 2, 2, '运输方式', 'dolphin', 0, NULL, '/src/views/freight/basic/modeTran/index.vue', 0, 1, NULL, '2025-09-09 13:49:08', '2025-09-16 15:29:54');
INSERT INTO `menu` VALUES (152, 147, '/freight/basic/carrier', 2, 3, '承运商', 'fish-one', 0, NULL, '/src/views/freight/basic/carrier/index.vue', 0, 1, NULL, '2025-09-09 14:57:24', '2025-09-16 15:30:00');
INSERT INTO `menu` VALUES (153, 147, '/freight/basic/qSPoint', 2, 4, '起始地点', 'eagle', 0, NULL, '/src/views/freight/basic/qSPoint/index.vue', 0, 1, NULL, '2025-09-09 15:35:21', '2025-09-16 15:30:06');
INSERT INTO `menu` VALUES (154, 148, '/basic-service/mode-storage/save/**', 3, NULL, '新增贮存方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 08:42:45', '2025-09-10 08:43:35');
INSERT INTO `menu` VALUES (156, 148, '/basic-service/mode-storage/update/**', 3, NULL, '修改贮存方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (157, 148, '/basic-service/mode-storage/page/**', 3, NULL, '分页查询贮存方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (158, 148, '/basic-service/mode-storage/remove/**', 3, NULL, '删除贮存方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (159, 148, '/basic-service/mode-storage/saveBatch/**', 3, NULL, '批量添加贮存方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (160, 148, '/basic-service/mode-storage/upload/**', 3, NULL, '导入贮存方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (161, 147, '/freight/basic/itemMode', 2, 5, '物品运输形态', 'city', 0, NULL, '/src/views/freight/basic/itemMode/index.vue', 0, 1, NULL, '2025-09-10 10:17:36', '2025-09-16 15:30:13');
INSERT INTO `menu` VALUES (162, 147, '/freight/basic/weight', 2, 6, '重量区间', 'travel', 0, NULL, '/src/views/freight/basic/weight/index.vue', 0, 1, NULL, '2025-09-10 10:36:17', '2025-09-16 15:30:25');
INSERT INTO `menu` VALUES (163, 147, '/freight/basic/volume', 2, 7, '体积区间', 'pointer', 0, NULL, '/src/views/freight/basic/volume/index.vue', 0, 1, NULL, '2025-09-10 10:53:39', '2025-09-16 15:30:33');
INSERT INTO `menu` VALUES (164, 147, '/freight/basic/tranPrice', 2, 8, '运输价格', 'find', 0, NULL, '/src/views/freight/basic/tranPrice/index.vue', 0, 1, NULL, '2025-09-10 11:27:59', '2025-09-16 15:30:40');
INSERT INTO `menu` VALUES (165, 148, '/basic-service/mode-storage/export/**', 3, NULL, '导出贮存方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 15:39:13');
INSERT INTO `menu` VALUES (166, 148, '/basic-service/mode-storage/modelDownload/**', 3, NULL, '下载贮存方式模板', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 15:39:27');
INSERT INTO `menu` VALUES (167, 149, '/basic-service/truck/save/**', 3, NULL, '新增运输车型', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (168, 149, '/basic-service/truck/update/**', 3, NULL, '修改运输车型', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (169, 149, '/basic-service/truck/page/**', 3, NULL, '分页查询运输车型', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (170, 149, '/basic-service/truck/remove/**', 3, NULL, '删除运输车型', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (171, 149, '/basic-service/truck/saveBatch/**', 3, NULL, '批量添加运输车型', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (172, 149, '/basic-service/truck/export/**', 3, NULL, '导出运输车型', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (173, 149, '/basic-service/truck/modelDownload/**', 3, NULL, '下载运输车型模板', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (174, 149, '/basic-service/truck/upload/**', 3, NULL, '导入运输车型', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (175, 151, '/basic-service/mode-tran/save/**', 3, NULL, '新增运输方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (176, 151, '/basic-service/mode-tran/update/**', 3, NULL, '修改运输方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (177, 151, '/basic-service/mode-tran/page/**', 3, NULL, '分页查询运输方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (178, 151, '/basic-service/mode-tran/remove/**', 3, NULL, '删除运输方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (179, 151, '/basic-service/mode-tran/saveBatch/**', 3, NULL, '批量添加运输方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (180, 151, '/basic-service/mode-tran/export/**', 3, NULL, '导出运输方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (181, 151, '/basic-service/mode-tran/modelDownload/**', 3, NULL, '下载运输方式模板', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (182, 151, '/basic-service/mode-tran/upload/**', 3, NULL, '导入运输方式', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (183, 152, '/basic-service/carrier/save/**', 3, NULL, '新增承运商', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (184, 152, '/basic-service/carrier/update/**', 3, NULL, '修改承运商', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (185, 152, '/basic-service/carrier/page/**', 3, NULL, '分页查询承运商', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (186, 152, '/basic-service/carrier/remove/**', 3, NULL, '删除承运商', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (187, 152, '/basic-service/carrier/saveBatch/**', 3, NULL, '批量添加承运商', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (188, 152, '/basic-service/carrier/export/**', 3, NULL, '导出承运商', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (189, 152, '/basic-service/carrier/modelDownload/**', 3, NULL, '下载承运商模板', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (190, 152, '/basic-service/carrier/upload/**', 3, NULL, '导入承运商', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (191, 153, '/basic-service/qs-point/save/**', 3, NULL, '新增起始地点', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (192, 153, '/basic-service/qs-point/update/**', 3, NULL, '修改起始地点', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (193, 153, '/basic-service/qs-point/page/**', 3, NULL, '分页查询起始地点', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (194, 153, '/basic-service/qs-point/remove/**', 3, NULL, '删除起始地点', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (195, 153, '/basic-service/qs-point/saveBatch/**', 3, NULL, '批量添加起始地点', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (196, 153, '/basic-service/qs-point/export/**', 3, NULL, '导出起始地点', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (197, 153, '/basic-service/qs-point/modelDownload/**', 3, NULL, '下载起始地点模板', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (198, 153, '/basic-service/qs-point/upload/**', 3, NULL, '导入起始地点', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (199, 161, '/basic-service/item-mode/save/**', 3, NULL, '新增物品运输形态', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (200, 161, '/basic-service/item-mode/update/**', 3, NULL, '修改物品运输形态', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (201, 161, '/basic-service/item-mode/page/**', 3, NULL, '分页查询物品运输形态', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (202, 161, '/basic-service/item-mode/remove/**', 3, NULL, '删除物品运输形态', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (203, 161, '/basic-service/item-mode/saveBatch/**', 3, NULL, '批量添加物品运输形态', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (204, 161, '/basic-service/item-mode/export/**', 3, NULL, '导出物品运输形态', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (205, 161, '/basic-service/item-mode/modelDownload/**', 3, NULL, '下载物品运输形态模板', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (206, 161, '/basic-service/item-mode/upload/**', 3, NULL, '导入物品运输形态', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (207, 162, '/basic-service/weight/save/**', 3, NULL, '新增重量区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (208, 162, '/basic-service/weight/update/**', 3, NULL, '修改重量区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (209, 162, '/basic-service/weight/page/**', 3, NULL, '分页查询重量区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (210, 162, '/basic-service/weight/remove/**', 3, NULL, '删除重量区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (211, 162, '/basic-service/weight/saveBatch/**', 3, NULL, '批量添加重量区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (212, 162, '/basic-service/weight/export/**', 3, NULL, '导出重量区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (213, 162, '/basic-service/weight/modelDownload/**', 3, NULL, '下载重量区间模板', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (214, 162, '/basic-service/weight/upload/**', 3, NULL, '导入重量区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (215, 163, '/basic-service/volume/save/**', 3, NULL, '新增体积区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (216, 163, '/basic-service/volume/update/**', 3, NULL, '修改体积区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (217, 163, '/basic-service/volume/page/**', 3, NULL, '分页查询体积区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (218, 163, '/basic-service/volume/remove/**', 3, NULL, '删除体积区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (219, 163, '/basic-service/volume/saveBatch/**', 3, NULL, '批量添加体积区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (220, 163, '/basic-service/volume/export/**', 3, NULL, '导出体积区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (221, 163, '/basic-service/volume/modelDownload/**', 3, NULL, '下载体积区间模板', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (222, 163, '/basic-service/volume/upload/**', 3, NULL, '导入体积区间', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (223, 164, '/basic-service/tran-price/save/**', 3, NULL, '新增运输价格', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (224, 164, '/basic-service/tran-price/update/**', 3, NULL, '修改运输价格', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (225, 164, '/basic-service/tran-price/page/**', 3, NULL, '分页查询运输价格', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (226, 164, '/basic-service/tran-price/remove/**', 3, NULL, '删除运输价格', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (227, 164, '/basic-service/tran-price-detail/page/**', 3, NULL, '分页查询运输价格明细', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (228, 164, '/basic-service/tran-price-detail/remove/**', 3, NULL, '删除运输价格明细', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (229, 164, '/basic-service/tran-price/localList**', 3, NULL, '地点选项', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (230, 124, '/embed/metabase/view/question/54', 3, NULL, '产品接口', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-17 14:25:16', '2025-09-17 14:28:43');
INSERT INTO `menu` VALUES (231, 232, '/freight/business/tranInformation', 2, 0, '运费信息', 'lightning', 0, NULL, '/src/views/freight/business/tranInformation/index.vue', 0, 1, NULL, '2025-09-19 08:49:39', '2025-10-11 13:41:14');
INSERT INTO `menu` VALUES (232, 146, '/freight/business', 1, NULL, '业务数据', 'chat', 0, NULL, NULL, 0, 1, NULL, '2025-09-19 08:51:01', '2025-09-19 08:51:34');
INSERT INTO `menu` VALUES (233, 232, '/freight/business/informationSave', 2, NULL, '历史信息', 'crown', 0, NULL, '/src/views/freight/business/informationSave/index.vue', 0, 1, NULL, '2025-09-23 10:40:02', '2025-10-11 13:42:15');
INSERT INTO `menu` VALUES (234, 231, '/basic-service/tran-info/gen/**', 3, NULL, '生成运输信息', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (235, 231, '/basic-service/tran-info/page/**', 3, NULL, '分页查询运输信息', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (236, 231, '/basic-service/tran-info/endLocalList/**', 3, NULL, '目的地地点选项', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (237, 231, '/basic-service/tran-info/historyPage/**', 3, NULL, '分页查询历史运输信息', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (238, 231, '/basic-service/tran-info/confirm/**', 3, NULL, '确认运输信息', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (239, 123, '/erp-service/salesForecast/**', 3, NULL, '销售预测接口', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-10-21 14:30:52', '2025-10-21 14:30:52');
INSERT INTO `menu` VALUES (240, 231, '/basic-service/tran-info/genValid/**', 3, NULL, '运输信息生成校验', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (242, 108, '/basic-service/options/allList/**', 3, NULL, '查询所有类型选项信息', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-11-26 11:05:27', '2025-11-26 11:05:27');
INSERT INTO `menu` VALUES (243, NULL, '/report', 2, 17, '报表查询', 'order', 0, NULL, '/src/views/report/index.vue', 0, 1, NULL, '2025-11-24 17:11:48', '2025-11-26 17:25:32');
INSERT INTO `menu` VALUES (244, 243, '/metabase/url/**', 3, NULL, 'metabase图表链接', NULL, 1, NULL, '', 0, 1, NULL, '2025-11-25 13:36:06', '2025-11-25 13:37:07');
INSERT INTO `menu` VALUES (245, 243, '/metabase/options/**', 3, NULL, '获取菜单选项', NULL, 1, NULL, NULL, 0, 1, NULL, '2025-11-25 17:28:15', '2025-11-25 17:28:15');
INSERT INTO `menu` VALUES (246, 108, '/basic-service/options/optionsList/**', 3, NULL, '选项列表', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-11-26 13:03:14', '2025-11-26 13:04:17');
INSERT INTO `menu` VALUES (248, 254, '/basic-service/teaGene/page/**', 3, NULL, '分页查询基因信息', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-11-13 11:36:18', '2025-11-13 11:36:18');
INSERT INTO `menu` VALUES (249, 254, '/basic-service/teaGene/save/**', 3, NULL, '添加茶基因信息', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-11-13 11:36:18', '2025-11-13 11:36:18');
INSERT INTO `menu` VALUES (250, 254, '/basic-service/teaGene/getById/**', 3, NULL, '根据id查询茶基因信息', NULL, 1, NULL, NULL, 0, 1, NULL, '2025-11-13 11:36:18', '2025-11-13 11:36:18');
INSERT INTO `menu` VALUES (251, 254, '/basic-service/teaGene/upload/**', 3, NULL, '上传文件', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-11-13 11:36:18', '2025-11-13 11:36:18');
INSERT INTO `menu` VALUES (252, 254, '/basic-service/teaGene/download/**', 3, NULL, '下载文件', NULL, 1, NULL, NULL, 0, 1, NULL, '2025-11-13 11:36:18', '2025-11-13 11:36:18');
INSERT INTO `menu` VALUES (253, 254, '/basic-service/teaGene/update/**', 3, NULL, '修改茶基因信息', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-11-13 11:36:18', '2025-11-13 11:36:18');
INSERT INTO `menu` VALUES (254, NULL, '/tea-gene', 2, NULL, '茶基因', 'fish-one', 0, NULL, '/src/views/tea/add-info/index.vue', 0, 1, NULL, '2025-11-13 11:36:18', '2025-12-26 09:21:33');
INSERT INTO `menu` VALUES (255, 254, '/tea-gene/add-info', 2, NULL, '茶基因录入', NULL, 1, NULL, '/src/views/tea/add-info/index.vue', 0, 2, NULL, '2025-11-13 11:36:18', '2025-11-13 11:36:18');
INSERT INTO `menu` VALUES (256, NULL, '/basic-service/model/meal_repair/**', 3, NULL, '就餐补报', NULL, 1, NULL, NULL, 0, 1, NULL, '2025-11-13 11:36:18', '2025-11-13 11:36:18');
INSERT INTO `menu` VALUES (257, 243, '/metabase/list/**', 3, NULL, 'metabase菜单列表', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-11-25 17:28:15', '2025-11-25 17:28:15');
INSERT INTO `menu` VALUES (258, 108, '/role/options/**', 3, NULL, '角色选项', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-11-13 11:36:18', '2025-11-13 11:36:18');
INSERT INTO `menu` VALUES (259, 243, '/metabase/save/**', 3, NULL, 'metabase配置添加', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-11-25 17:28:15', '2025-11-25 17:28:15');
INSERT INTO `menu` VALUES (260, 243, '/metabase/update/**', 3, NULL, 'metabase配置修改', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-11-25 17:28:15', '2025-11-25 17:28:15');
INSERT INTO `menu` VALUES (261, 231, '/basic-service/tran-info/getSdh/**', 3, NULL, '获取发货单信息', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-09-10 09:42:04', '2025-09-10 09:43:43');
INSERT INTO `menu` VALUES (262, 243, '/metabase/remove/**', 3, NULL, 'metabase配置删除', NULL, 0, NULL, NULL, 0, 1, NULL, '2025-11-25 17:28:15', '2025-11-25 17:28:15');

SET FOREIGN_KEY_CHECKS = 1;
