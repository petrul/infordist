CREATE TABLE `vars` (
  `name` VARCHAR(200)  NOT NULL,
  `value` double,
  PRIMARY KEY (`name`)
)
ENGINE = MyISAM
CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE `terms` (
  `id` INT  NOT NULL,
  `term` VARCHAR(200) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `absfreq` INT,
  PRIMARY KEY (`id`),
  INDEX `idx_term`(`term`)
)
ENGINE = MyISAM
CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE `ngd_matrix` (
  `term1` INT  NOT NULL,
  `term2` INT  NOT NULL,
  `value` double,
  PRIMARY KEY (`term1`, `term2`)
)
ENGINE = MyISAM
CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE `ungd_matrix` (
  `term1` INT  NOT NULL,
  `term2` INT  NOT NULL,
  `value` double,
  PRIMARY KEY (`term1`, `term2`)
)
ENGINE = MyISAM
CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE `cc_from_here_matrix` (
  `term1` INT  NOT NULL,
  `term2` INT  NOT NULL,
  `value` double,
  PRIMARY KEY (`term1`, `term2`)
)
ENGINE = MyISAM
CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE `cc_to_here_matrix` (
  `term1` INT  NOT NULL,
  `term2` INT  NOT NULL,
  `value` double,
  PRIMARY KEY (`term1`, `term2`)
)
ENGINE = MyISAM
CHARACTER SET utf8 COLLATE utf8_general_ci;
