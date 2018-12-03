create database jnimble;

create table jnimble.person
(
  id serial not null,
  first_name varchar(25) not null,
  last_name varchar(25) not null,
  birth_date date not null,
  gender varchar(10),
  weight double precision,
  height double precision,
  cash_amount numeric(10,2)
);

create user jnimble with password 'jnimble';
grant all privileges on jnimble.public.person to jnimble;
grant all privileges on jnimble.public.person_id_seq to jnimble;