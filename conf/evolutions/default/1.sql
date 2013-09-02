# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table `ITEM` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`name` VARCHAR(254) NOT NULL,`price` DECIMAL(21,2) NOT NULL,`introduced` DATE,`discontinued` DATE,`shopId` BIGINT);
create table `PURCHASE` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`paid` BOOLEAN NOT NULL);
create table `SHOP` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`name` VARCHAR(254) NOT NULL);

# --- !Downs

drop table `ITEM`;
drop table `PURCHASE`;
drop table `SHOP`;

