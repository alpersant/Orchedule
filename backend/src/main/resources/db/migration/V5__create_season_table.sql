CREATE TABLE season (
                        id UUID NOT NULL,
                        name VARCHAR(120) NOT NULL,
                        start_date DATE NOT NULL,
                        end_date DATE NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        active BOOLEAN NOT NULL DEFAULT FALSE,
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        CONSTRAINT pk_season PRIMARY KEY (id),
                        CONSTRAINT uk_season_name UNIQUE (name),
                        CONSTRAINT chk_season_status CHECK (status IN ('DRAFT', 'ACTIVE', 'CLOSED')),
                        CONSTRAINT chk_season_dates CHECK (end_date >= start_date),
                        CONSTRAINT chk_season_status_active CHECK (
                            (status = 'DRAFT' AND active = FALSE)
                                OR (status = 'ACTIVE' AND active = TRUE)
                                OR (status = 'CLOSED' AND active = FALSE)
                            )
);

CREATE INDEX idx_season_status ON season(status);
CREATE INDEX idx_season_active ON season(active);
CREATE INDEX idx_season_start_date ON season(start_date);
CREATE INDEX idx_season_end_date ON season(end_date);