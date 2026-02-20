create table payments
(
    id          uuid            not null,
    order_id    uuid            not null,
    book_id     uuid            not null,
    quantity    integer         not null,
    status      varchar(50)     not null,
    primary key(id)
)