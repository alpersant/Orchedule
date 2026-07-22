CREATE TABLE venue (
    id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    capacity INTEGER,
    status VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_venue PRIMARY KEY (id),
    CONSTRAINT uk_venue_name UNIQUE (name),
    CONSTRAINT chk_venue_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_venue_capacity CHECK (capacity IS NULL OR capacity >= 0),
    CONSTRAINT chk_venue_status_active CHECK (
        (status = 'ACTIVE' AND active = TRUE)
        OR (status = 'INACTIVE' AND active = FALSE)
    )
);

CREATE INDEX idx_venue_status ON venue(status);
CREATE INDEX idx_venue_active ON venue(active);
CREATE INDEX idx_venue_city ON venue(city);
