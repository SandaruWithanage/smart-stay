-- Identity and constrained permissions. No user accounts or passwords are seeded.
CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    guest_id UUID UNIQUE REFERENCES guests(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    token_version BIGINT NOT NULL DEFAULT 0 CHECK (token_version >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_user_email_normalized CHECK (email = lower(email))
);

CREATE TABLE roles (
    code VARCHAR(32) PRIMARY KEY,
    name VARCHAR(80) NOT NULL
);

CREATE TABLE permissions (
    code VARCHAR(96) PRIMARY KEY,
    description VARCHAR(200) NOT NULL
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    role_code VARCHAR(32) NOT NULL REFERENCES roles(code),
    PRIMARY KEY (user_id, role_code)
);

CREATE TABLE role_permissions (
    role_code VARCHAR(32) NOT NULL REFERENCES roles(code),
    permission_code VARCHAR(96) NOT NULL REFERENCES permissions(code),
    PRIMARY KEY (role_code, permission_code)
);

-- Navigation references known frontend route and icon registries, never executable markup.
CREATE TABLE navigation_items (
    id VARCHAR(64) PRIMARY KEY,
    parent_id VARCHAR(64) REFERENCES navigation_items(id),
    label VARCHAR(100) NOT NULL,
    route_key VARCHAR(64) NOT NULL,
    icon_key VARCHAR(32) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 100,
    required_permission VARCHAR(96) REFERENCES permissions(code),
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO roles(code,name) VALUES
  ('ADMIN','Hotel Administrator'),('GUEST','Hotel Guest');

INSERT INTO permissions(code,description) VALUES
  ('catalog.read','View room packages'),
  ('room.availability.read','Search room availability'),
  ('workspace.read','View authenticated workspace'),
  ('room.read','View room records'),
  ('room.create','Create room records'),
  ('room.update','Update room records'),
  ('room.delete','Retire room records'),
  ('guest.create','Register a guest record for desk operations'),
  ('guest.read.any','View any guest record'),
  ('guest.read.own','View linked guest record'),
  ('reservation.create.any','Confirm a reservation at the desk'),
  ('reservation.read.any','Read any reservation'),
  ('reservation.read.own','Read linked reservations'),
  ('reservation.cancel.any','Cancel any eligible reservation'),
  ('reservation.cancel.own','Cancel eligible own reservation'),
  ('invoice.read.any','View any invoice'),
  ('invoice.read.own','View own reservation invoice'),
  ('access.manage','Manage role permission grants'),
  ('navigation.manage','Manage supported navigation metadata');

-- Hotel administrator has all current defined permissions. New permissions must be explicitly assigned.
INSERT INTO role_permissions(role_code,permission_code)
  SELECT 'ADMIN',code FROM permissions;

INSERT INTO role_permissions(role_code,permission_code) VALUES
  ('GUEST','catalog.read'),
  ('GUEST','room.availability.read'),
  ('GUEST','workspace.read'),
  ('GUEST','guest.read.own'),
  ('GUEST','reservation.read.own'),
  ('GUEST','reservation.cancel.own'),
  ('GUEST','invoice.read.own');

INSERT INTO navigation_items(id,label,route_key,icon_key,sort_order,required_permission,enabled) VALUES
  ('dashboard','Workspace','workspace','home',10,'workspace.read',TRUE),
  ('room-catalog','Room packages','packages','rooms',20,'catalog.read',TRUE),
  ('room-finder','Find rooms','availability','search',30,'room.availability.read',TRUE),
  ('access-editor','Access control','accessControl','rules',70,'access.manage',FALSE);
