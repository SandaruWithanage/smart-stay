-- Revision number allows clients to observe dynamic permission/navigation changes.
CREATE TABLE ui_configuration_revision (
    id SMALLINT PRIMARY KEY CHECK (id = 1),
    version BIGINT NOT NULL DEFAULT 1 CHECK (version > 0)
);
INSERT INTO ui_configuration_revision(id, version) VALUES (1, 1);

-- UI route now exists in milestone 3; the backend still requires access.manage.
UPDATE navigation_items SET enabled = TRUE WHERE id = 'access-editor';
