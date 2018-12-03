create database if not exists jnimble;
use jnimble;
create table if not exists person (
  id int auto_increment primary key,
  first_name varchar(25) not null,
  last_name varchar(25) not null,
  birth_date date not null,
  gender varchar(10),
  weight double null,
  height double null,
  cash_amount decimal(10,2)
);
create user if not exists 'jnimble'@'localhost' identified by 'jnimble';
grant all privileges on jnimble.* to 'jnimble'@'localhost';