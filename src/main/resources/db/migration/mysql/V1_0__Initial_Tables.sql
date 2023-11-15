
CREATE TABLE IF NOT EXISTS `${prefix}Key_Value_Store_Value` (
  `id`       VARCHAR(128) NOT NULL,
  `key_text` LONGTEXT     NOT NULL,
  `data`     LONGBLOB     NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
