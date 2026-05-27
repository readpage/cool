/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30)
 Source Host           : localhost:3306
 Source Schema         : cool

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30)
 File Encoding         : 65001

 Date: 27/12/2025 17:29:07
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
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模型' ROW_FORMAT = Dynamic;

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
  `module` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块',
  `label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '值',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `module`(`module` ASC, `value` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '选项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of options
-- ----------------------------
INSERT INTO `options` VALUES (1, 'model-sex', '男', '0', NULL, NULL);
INSERT INTO `options` VALUES (2, 'model-sex', '女', '1', NULL, NULL);

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
INSERT INTO `role` VALUES (147, '1', '1', '7', '2025-02-15 17:17:31', '2025-02-15 17:17:31');
INSERT INTO `role` VALUES (148, '2', '2', '5', '2025-02-17 08:50:33', '2025-02-17 09:20:59');
INSERT INTO `role` VALUES (174, '2', '3', NULL, '2025-02-15 17:17:31', '2025-02-15 17:17:31');
INSERT INTO `role` VALUES (180, '3', '3', NULL, '2025-02-15 17:13:06', '2025-02-15 17:13:06');
INSERT INTO `role` VALUES (184, '3', '4', NULL, '2025-02-15 17:17:31', '2025-02-15 17:17:31');
INSERT INTO `role` VALUES (186, '5', '5', NULL, '2025-02-15 17:19:21', '2025-02-15 17:19:21');
INSERT INTO `role` VALUES (187, '4', '4', NULL, '2025-02-15 17:18:28', '2025-02-15 17:18:28');
INSERT INTO `role` VALUES (188, '6', '6', NULL, '2025-02-15 17:19:21', '2025-02-15 17:19:21');
INSERT INTO `role` VALUES (190, '7', '7', NULL, '2025-02-15 17:19:44', '2025-02-15 17:19:44');
INSERT INTO `role` VALUES (191, '8', '8', NULL, '2025-02-15 17:19:44', '2025-02-15 17:19:44');
INSERT INTO `role` VALUES (192, '9', '9', NULL, '2025-02-15 17:19:57', '2025-02-15 17:19:57');
INSERT INTO `role` VALUES (193, '10', '10', NULL, '2025-02-15 17:19:57', '2025-02-15 17:19:57');
INSERT INTO `role` VALUES (194, '11', '11', NULL, '2025-02-17 08:54:02', '2025-02-17 09:20:59');
INSERT INTO `role` VALUES (201, '12', '12', NULL, '2025-02-17 08:56:08', '2025-02-17 09:20:59');

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
INSERT INTO `system_log` VALUES (2106, '文件管理', '上传文件', 7, 'POST', 'R com.undraw.controller.FileController.upload(MultipartFile)', '/file/upload', NULL, '0:0:0:0:0:0:0:1', '未知', 'Unknown | Unknown', 'Apifox/1.0.0 (https://apifox.com)', '2025-11-11 16:50:29', '58', '200', '操作成功', 'http://192.168.1.165:7032/static/basic/2025/11/11/311907861477330944.PNG');
INSERT INTO `system_log` VALUES (2107, NULL, NULL, -1, 'GET', 'org.springframework.web.servlet.DispatcherServlet.noHandlerFound(?)', '/favicon.ico', '{query={}, body=}', '173.10.108.150', '美国华盛顿0|康卡斯特', 'Unknown | Unknown', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 Edg/142.0.0.0', '2025-11-11 16:52:29', '6', '500', '系统异常', 'org.springframework.web.servlet.NoHandlerFoundException: No endpoint GET /favicon.ico.\r\n	at org.springframework.web.servlet.DispatcherServlet.noHandlerFound(DispatcherServlet.java:1304)\r\n	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1067)\r\n	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979)\r\n	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014)\r\n	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903)\r\n	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:564)\r\n	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885)\r\n	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:205)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at cn.undraw.handler.xss.XSSFilter.doFilter(XSSFilter.java:61)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at com.undraw.filter.AuthFilter.doFilter(AuthFilter.java:23)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\r\n	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\r\n	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)\r\n	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167)\r\n	at org.apache.catalina.core.StandardContextValve.__invoke(StandardContextValve.java:90)\r\n	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:41002)\r\n	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482)\r\n	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115)\r\n	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93)\r\n	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)\r\n	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344)\r\n	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:391)\r\n	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)\r\n	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:896)\r\n	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1744)\r\n	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)\r\n	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)\r\n	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)\r\n	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63)\r\n	at java.base/java.lang.Thread.run(Thread.java:833)\r\n');
INSERT INTO `system_log` VALUES (2108, NULL, NULL, -1, 'GET', 'org.apache.catalina.connector.OutputBuffer.realWriteBytes(?)', '/static/video/city.mp4', '{query={}, body=}', '173.10.108.150', '美国华盛顿0|康卡斯特', 'Unknown | Unknown', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 Edg/142.0.0.0', '2025-11-11 16:53:21', '1', '500', '系统异常', 'org.apache.catalina.connector.ClientAbortException: java.io.IOException: 你的主机中的软件中止了一个已建立的连接。\r\n	at org.apache.catalina.connector.OutputBuffer.realWriteBytes(OutputBuffer.java:344)\r\n	at org.apache.catalina.connector.OutputBuffer.flushByteBuffer(OutputBuffer.java:773)\r\n	at org.apache.catalina.connector.OutputBuffer.append(OutputBuffer.java:676)\r\n	at org.apache.catalina.connector.OutputBuffer.writeBytes(OutputBuffer.java:379)\r\n	at org.apache.catalina.connector.OutputBuffer.write(OutputBuffer.java:357)\r\n	at org.apache.catalina.connector.CoyoteOutputStream.write(CoyoteOutputStream.java:97)\r\n	at java.base/java.io.InputStream.transferTo(InputStream.java:783)\r\n	at org.springframework.http.converter.ResourceHttpMessageConverter.writeContent(ResourceHttpMessageConverter.java:150)\r\n	at org.springframework.http.converter.ResourceHttpMessageConverter.writeInternal(ResourceHttpMessageConverter.java:139)\r\n	at org.springframework.http.converter.ResourceHttpMessageConverter.writeInternal(ResourceHttpMessageConverter.java:46)\r\n	at org.springframework.http.converter.AbstractHttpMessageConverter.write(AbstractHttpMessageConverter.java:235)\r\n	at org.springframework.web.servlet.resource.ResourceHttpRequestHandler.handleRequest(ResourceHttpRequestHandler.java:621)\r\n	at org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter.handle(HttpRequestHandlerAdapter.java:52)\r\n	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089)\r\n	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979)\r\n	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014)\r\n	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903)\r\n	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:564)\r\n	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885)\r\n	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:205)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at cn.undraw.handler.xss.XSSFilter.doFilter(XSSFilter.java:61)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at com.undraw.filter.AuthFilter.doFilter(AuthFilter.java:23)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\r\n	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\r\n	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)\r\n	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)\r\n	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)\r\n	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167)\r\n	at org.apache.catalina.core.StandardContextValve.__invoke(StandardContextValve.java:90)\r\n	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:41002)\r\n	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482)\r\n	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115)\r\n	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93)\r\n	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)\r\n	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344)\r\n	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:391)\r\n	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)\r\n	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:896)\r\n	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1744)\r\n	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)\r\n	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)\r\n	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)\r\n	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63)\r\n	at java.base/java.lang.Thread.run(Thread.java:833)\r\nCaused by: java.io.IOException: 你的主机中的软件中止了一个已建立的连接。\r\n	at java.base/sun.nio.ch.SocketDispatcher.write0(Native Method)\r\n	at java.base/sun.nio.ch.SocketDispatcher.write(SocketDispatcher.java:54)\r\n	at java.base/sun.nio.ch.IOUtil.writeFromNativeBuffer(IOUtil.java:132)\r\n	at java.base/sun.nio.ch.IOUtil.write(IOUtil.java:97)\r\n	at java.base/sun.nio.ch.IOUtil.write(IOUtil.java:53)\r\n	at java.base/sun.nio.ch.SocketChannelImpl.write(SocketChannelImpl.java:532)\r\n	at org.apache.tomcat.util.net.NioChannel.write(NioChannel.java:118)\r\n	at org.apache.tomcat.util.net.NioEndpoint$NioSocketWrapper.doWrite(NioEndpoint.java:1381)\r\n	at org.apache.tomcat.util.net.SocketWrapperBase.doWrite(SocketWrapperBase.java:764)\r\n	at org.apache.tomcat.util.net.SocketWrapperBase.writeBlocking(SocketWrapperBase.java:589)\r\n	at org.apache.tomcat.util.net.SocketWrapperBase.write(SocketWrapperBase.java:533)\r\n	at org.apache.coyote.http11.Http11OutputBuffer$SocketOutputBuffer.doWrite(Http11OutputBuffer.java:540)\r\n	at org.apache.coyote.http11.filters.IdentityOutputFilter.doWrite(IdentityOutputFilter.java:73)\r\n	at org.apache.coyote.http11.Http11OutputBuffer.doWrite(Http11OutputBuffer.java:193)\r\n	at org.apache.coyote.Response.doWrite(Response.java:616)\r\n	at org.apache.catalina.connector.OutputBuffer.realWriteBytes(OutputBuffer.java:331)\r\n	... 59 more\r\n');
-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话号码',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1013 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (23, '答义轩', 'gZZR1M4QqiHUqq_', 48, '15885326305', '2025-08-15 11:22:25', '2025-08-15 11:22:25');
INSERT INTO `user` VALUES (86, '马敏', 'ea', 77, '18114196861', '2024-03-01 15:47:30', '2024-03-01 15:47:30');
INSERT INTO `user` VALUES (87, '文洋', 'sunt nisi magna labore et', 61, '18633239620', NULL, NULL);
INSERT INTO `user` VALUES (88, '林刚', 'qui quis officia', 79, '18162537882', NULL, NULL);
INSERT INTO `user` VALUES (89, '宋强', 'incididunt', 85, '18125132862', NULL, NULL);
INSERT INTO `user` VALUES (90, '方磊', 'non do ipsum', 87, '18161178419', NULL, NULL);
INSERT INTO `user` VALUES (91, '马娜', 'cillum incididunt est', 80, '18106335359', NULL, NULL);
INSERT INTO `user` VALUES (92, '邱平', 'fugiat ex incididunt mollit', 45, '18165019238', NULL, NULL);
INSERT INTO `user` VALUES (93, '薛艳', 'reprehenderit commodo do sint', 28, '19871782512', NULL, NULL);
INSERT INTO `user` VALUES (94, '龚勇', 'irure', 32, '18693835845', NULL, NULL);
INSERT INTO `user` VALUES (96, '丁强', 'ullamco incididunt sint voluptate', 50, '18659352321', NULL, NULL);
INSERT INTO `user` VALUES (98, '蒋超', 'voluptate ullamco', 99, '18679758446', NULL, NULL);
INSERT INTO `user` VALUES (99, '袁敏', 'fugiat do', 8, '13611543587', NULL, NULL);
INSERT INTO `user` VALUES (101, '赖涛', 'Ut dolor commodo laborum sint', 22, '18162217139', NULL, NULL);
INSERT INTO `user` VALUES (102, '刘磊', 'dolore pariatur', 28, '18611862729', '2023-12-19 15:18:31', '2023-12-19 15:18:31');
INSERT INTO `user` VALUES (1000, '马敏2', 'abc2', 33, '18114196861', '2024-03-01 15:47:30', '2025-04-14 08:39:46');
INSERT INTO `user` VALUES (1003, '枚蒙', 'r0wtQcPpCGU7YQS', 42, '15885326303', '2025-04-10 14:58:39', '2025-04-10 15:21:12');
INSERT INTO `user` VALUES (1004, '马敏3', 'ab3', 33, '18114196861', '2024-03-01 15:47:30', '2025-04-14 08:39:46');
INSERT INTO `user` VALUES (1011, 'test2', 'test2', NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (1012, 'test3', 'test3', NULL, NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
