CREATE TABLE IF NOT EXISTS `account` (
  `id` LONG PRIMARY KEY,
  `balance` DOUBLE NOT NULL,
  `created_time` TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS `transaction` (
  `id` LONG AUTO_INCREMENT PRIMARY KEY,
  `from_account_id` LONG NOT NULL,
  `to_account_id` LONG NOT NULL,
  `amount` DOUBLE NOT NULL,
  `created_time` TIMESTAMP NULL
);
