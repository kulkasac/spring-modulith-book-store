create table orders
(
    id         uuid         not null,
    book_id    uuid         not null,
    quantity   integer      not null,
    status     varchar(50)  not null,
    primary key (id),
    foreign key (book_id) references books (id)
);