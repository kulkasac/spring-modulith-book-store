create table event_publication
(
    id               uuid         not null,
    event_type       varchar(255) not null,
    listener_id      varchar(255) not null,
    publication_date timestamp    not null,
    completion_date  timestamp,
    serialized_event text         not null,
    primary key (id)
);

create index idx_event_publication_completion_date
    on event_publication (completion_date);
