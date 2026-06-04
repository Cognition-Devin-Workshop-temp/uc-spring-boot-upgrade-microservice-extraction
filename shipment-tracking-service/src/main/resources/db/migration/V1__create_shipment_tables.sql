create table shipments (
    id varchar(255) primary key,
    tracking_number varchar(255) unique not null,
    sender_name varchar(255) not null,
    sender_address text not null,
    recipient_name varchar(255) not null,
    recipient_address text not null,
    origin varchar(255) not null,
    destination varchar(255) not null,
    weight double not null,
    status varchar(50) not null default 'CREATED',
    created_at timestamp not null,
    updated_at timestamp not null default current_timestamp
);

create table tracking_events (
    id varchar(255) primary key,
    shipment_id varchar(255) not null,
    status varchar(50) not null,
    location varchar(255),
    description text,
    created_at timestamp not null,
    foreign key (shipment_id) references shipments(id)
);
