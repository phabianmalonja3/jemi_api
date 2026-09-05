CREATE TABLE booking
(
    id                 UUID NOT NULL,
    client_email       VARCHAR(255),
    photographer_email VARCHAR(255),
    pickup_lat         DOUBLE PRECISION,
    pickup_lon         DOUBLE PRECISION,
    estimated_fare     DOUBLE PRECISION,
    status             VARCHAR(255),
    created_at         TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_booking PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ALTER COLUMN role SET NOT NULL;