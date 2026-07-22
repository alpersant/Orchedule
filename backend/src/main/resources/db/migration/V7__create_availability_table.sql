CREATE TABLE availability_rule (
    id UUID NOT NULL,
    scope VARCHAR(20) NOT NULL,
    reference_id UUID NOT NULL,
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_availability_rule PRIMARY KEY (id),
    CONSTRAINT chk_availability_scope CHECK (scope IN ('TEAM', 'VENUE')),
    CONSTRAINT chk_availability_status CHECK (status IN ('AVAILABLE', 'BLOCKED')),
    CONSTRAINT chk_availability_period CHECK (end_at > start_at)
);

CREATE INDEX idx_availability_scope_reference ON availability_rule(scope, reference_id);
CREATE INDEX idx_availability_start_at ON availability_rule(start_at);
CREATE INDEX idx_availability_end_at ON availability_rule(end_at);
CREATE INDEX idx_availability_active ON availability_rule(active);
