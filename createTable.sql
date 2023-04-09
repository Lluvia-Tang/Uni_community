CREATE TABLE `login_ticket` (
                                `id` int(11) NOT NULL AUTO_INCREMENT,
                                `user_id` int(11) NOT NULL,
                                `ticket` varchar(45) NOT NULL,
                                `status` int(11) DEFAULT '0' COMMENT '0-有效; 1-无效;',
                                `expired` timestamp NOT NULL,
                                PRIMARY KEY (`id`),
                                KEY `index_ticket` (`ticket`(20))
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `message` (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `from_id` int(11) DEFAULT NULL,
                           `to_id` int(11) DEFAULT NULL,
                           `conversation_id` varchar(45) NOT NULL,
                           `content` text,
                           `status` int(11) DEFAULT NULL COMMENT '0-未读;1-已读;2-删除;',
                           `create_time` timestamp NULL DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           KEY `index_from_id` (`from_id`),
                           KEY `index_to_id` (`to_id`),
                           KEY `index_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `user` (
                        `id` int(11) NOT NULL AUTO_INCREMENT,
                        `username` varchar(50) DEFAULT NULL,
                        `password` varchar(50) DEFAULT NULL,
                        `salt` varchar(50) DEFAULT NULL,
                        `email` varchar(100) DEFAULT NULL,
                        `type` int(11) DEFAULT NULL COMMENT '0-普通用户; 1-超级管理员; 2-版主;',
                        `status` int(11) DEFAULT NULL COMMENT '0-未激活; 1-已激活;',
                        `activation_code` varchar(100) DEFAULT NULL,
                        `header_url` varchar(200) DEFAULT NULL,
                        `create_time` timestamp NULL DEFAULT NULL,
                        PRIMARY KEY (`id`),
                        KEY `index_username` (`username`(20)),
                        KEY `index_email` (`email`(20))
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8;

CREATE TABLE `discuss_post`  (
                                 `id` int(11) NOT NULL AUTO_INCREMENT,
                                 `user_id` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                                 `title` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                                 `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
                                 `type` int(11) NULL DEFAULT NULL COMMENT '0-普通; 1-置顶;',
                                 `status` int(11) NULL DEFAULT NULL COMMENT '0-正常; 1-精华; 2-拉黑;',
                                 `create_time` timestamp(0) NULL DEFAULT NULL,
                                 `comment_count` int(11) NULL DEFAULT NULL,
                                 `score` double NULL DEFAULT NULL,
                                 PRIMARY KEY (`id`) USING BTREE,
                                 INDEX `index_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 285 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

CREATE TABLE `comment`  (
                            `id` int(11) NOT NULL AUTO_INCREMENT,
                            `user_id` int(11) NULL DEFAULT NULL,
                            `entity_type` int(11) NULL DEFAULT NULL,
                            `entity_id` int(11) NULL DEFAULT NULL,
                            `target_id` int(11) NULL DEFAULT NULL,
                            `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
                            `status` int(11) NULL DEFAULT NULL,
                            `create_time` timestamp(0) NULL DEFAULT NULL,
                            PRIMARY KEY (`id`) USING BTREE,
                            INDEX `index_user_id`(`user_id`) USING BTREE,
                            INDEX `index_entity_id`(`entity_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 246 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;