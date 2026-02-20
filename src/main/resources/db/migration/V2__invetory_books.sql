create table books
(
    id    uuid primary key,
    title varchar(255) not null,
    isbn  varchar(255) not null unique,
    stock integer      not null
)