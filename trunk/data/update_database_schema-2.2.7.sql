
DROP TABLE IF EXISTS entity_attribute_group_attribute;
DROP TABLE IF EXISTS entity_attribute_group;
DROP TABLE IF EXISTS entity_attribute_validation_param;
DROP TABLE IF EXISTS entity_attribute_validation;
DROP TABLE IF EXISTS entity_attribute;
DROP TABLE IF EXISTS entity_attribute_datatype;
DROP TABLE IF EXISTS entity;

CREATE TABLE entity
(
  entity_version_id serial NOT NULL,
  entity_id integer NOT NULL,
  version_id integer NOT NULL,
  "name" character varying(64) NOT NULL,
  description character varying(256),
  display_name character varying(64) NOT NULL,
  date_created timestamp without time zone NOT NULL,
  created_by_id integer NOT NULL,
  date_changed timestamp without time zone,
  changed_by_id integer,
  date_voided timestamp without time zone,
  voided_by_id integer,
  CONSTRAINT entity_pkey PRIMARY KEY (entity_version_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE entity OWNER TO openempi;

CREATE TABLE entity_attribute_datatype
(
  datatype_cd integer NOT NULL,
  "name" character varying(64) NOT NULL,
  display_name character varying(64) NOT NULL,
  CONSTRAINT entity_attribute_datatype_pkey PRIMARY KEY (datatype_cd)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE entity_attribute_datatype OWNER TO openempi;

INSERT INTO entity_attribute_datatype (datatype_cd, "name", display_name) VALUES
  (1, 'integer', 'Integer'),
  (2, 'short', 'Short'),
  (3, 'long', 'Long'),
  (4, 'double', 'Double'),
  (5, 'float', 'Float'),
  (6, 'string', 'String'),
  (7, 'boolean', 'Boolean'),
  (8, 'date', 'Date'),
  (9, 'timestamp', 'Timestamp');

CREATE TABLE entity_attribute
(
  entity_attribute_id serial NOT NULL,
  "name" character varying(64) NOT NULL,
  description character varying(256),
  display_name character varying(64) NOT NULL,
  datatype_cd integer NOT NULL,
  display_order integer NOT NULL,
  date_created timestamp without time zone NOT NULL,
  created_by_id integer NOT NULL,
  date_changed timestamp without time zone,
  changed_by_id integer,
  date_voided timestamp without time zone,
  voided_by_id integer,
  entity_version_id integer NOT NULL,
  indexed boolean NOT NULL DEFAULT false,
  is_custom boolean NOT NULL DEFAULT false,
  source_name character varying(256),
  transformation_function character varying(128),
  function_parameters character varying(256),
  CONSTRAINT entity_attribute_pkey PRIMARY KEY (entity_attribute_id),
  CONSTRAINT fk_entity_attribute_datatype FOREIGN KEY (datatype_cd)
      REFERENCES entity_attribute_datatype (datatype_cd) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_entity_version_id FOREIGN KEY (entity_version_id)
      REFERENCES entity (entity_version_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE entity_attribute OWNER TO openempi;

CREATE TABLE entity_attribute_group
(
  entity_attribute_group_id integer NOT NULL,
  "name" character varying(64) NOT NULL,
  display_name character varying(64) NOT NULL,
  display_order integer NOT NULL,
  entity_version_id integer NOT NULL,
  CONSTRAINT entity_attribute_group_pkey PRIMARY KEY (entity_attribute_group_id),
  CONSTRAINT fk_entity_group_entity FOREIGN KEY (entity_version_id)
      REFERENCES entity (entity_version_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE entity_attribute_group OWNER TO openempi;

CREATE TABLE entity_attribute_group_attribute
(
  entity_attribute_group_attribute_id integer NOT NULL,
  entity_attribute_group_id integer NOT NULL,
  entity_attribute_id integer NOT NULL,
  CONSTRAINT entity_attribute_group_attribute_pkey PRIMARY KEY (entity_attribute_group_attribute_id),
  CONSTRAINT fk_entity_attribute_group FOREIGN KEY (entity_attribute_group_id)
      REFERENCES entity_attribute_group (entity_attribute_group_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_entity_attribute FOREIGN KEY (entity_attribute_id)
      REFERENCES entity_attribute (entity_attribute_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE entity_attribute_group_attribute OWNER TO openempi;

CREATE TABLE entity_attribute_validation
(
  entity_attribute_validation_id integer NOT NULL,
  "name" character varying(64) NOT NULL,
  display_name character varying(64) NOT NULL,
  entity_attribute_id integer NOT NULL,
  CONSTRAINT entity_attribute_validation_pkey PRIMARY KEY (entity_attribute_validation_id),
  CONSTRAINT fk_entity_attribute FOREIGN KEY (entity_attribute_id)
      REFERENCES entity_attribute (entity_attribute_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE entity_attribute_validation OWNER TO openempi;

CREATE TABLE entity_attribute_validation_param
(
  entity_attribute_validation_param_id integer NOT NULL,
  "name" character varying(64) NOT NULL,
  "value" character varying(256) NOT NULL,
  entity_attribute_validation_id integer NOT NULL,
  CONSTRAINT entity_attribute_validation_param_pkey PRIMARY KEY (entity_attribute_validation_param_id),
  CONSTRAINT fk_entity_attribute_validation FOREIGN KEY (entity_attribute_validation_id)
      REFERENCES entity_attribute_validation (entity_attribute_validation_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE entity_attribute_validation_param OWNER TO openempi;

ALTER TABLE user_file ADD COLUMN profiled_ind character(1);
ALTER TABLE user_file ADD COLUMN profile_processed varchar(64);

DROP SEQUENCE IF EXISTS entity_seq;
CREATE SEQUENCE entity_seq START WITH 100 INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;
DROP SEQUENCE IF EXISTS entity_attribute_seq;
CREATE SEQUENCE entity_attribute_seq START WITH 100 INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;
DROP SEQUENCE IF EXISTS entity_attribute_group_seq;
CREATE SEQUENCE entity_attribute_group_seq START WITH 100 INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;
DROP SEQUENCE IF EXISTS entity_attribute_validation_seq;
CREATE SEQUENCE entity_attribute_validation_seq START WITH 100 INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;
    
ALTER TABLE audit_event RENAME TO audit_event_legacy;

DROP TABLE IF EXISTS audit_event;
CREATE TABLE audit_event (
    audit_event_id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    audit_event_type_cd integer NOT NULL,
    audit_event_description varchar(255),
    entity_name varchar(255),
    ref_record_id bigint,
    alt_ref_record_id bigint,
    creator_id integer NOT NULL
) WITHOUT OIDS;

INSERT INTO audit_event_type (audit_event_type_cd, audit_event_type_name, audit_event_type_description, audit_event_type_code) VALUES (101, 'Add Record', 'Add new record', 'ADDR');
INSERT INTO audit_event_type (audit_event_type_cd, audit_event_type_name, audit_event_type_description, audit_event_type_code) VALUES (102, 'Delete Record', 'Delete a record', 'DELR');
INSERT INTO audit_event_type (audit_event_type_cd, audit_event_type_name, audit_event_type_description, audit_event_type_code) VALUES (103, 'Import Record', 'Import a record', 'IMPR');
INSERT INTO audit_event_type (audit_event_type_cd, audit_event_type_name, audit_event_type_description, audit_event_type_code) VALUES (104, 'Merge Record', 'Merge a record with another', 'MRGR');
INSERT INTO audit_event_type (audit_event_type_cd, audit_event_type_name, audit_event_type_description, audit_event_type_code) VALUES (105, 'Update Record', 'Update a record', 'UPDR');
INSERT INTO audit_event_type (audit_event_type_cd, audit_event_type_name, audit_event_type_description, audit_event_type_code) VALUES (110, 'Unmerge Record', 'Unmerge a record from another', 'UMGR');
INSERT INTO audit_event_type (audit_event_type_cd, audit_event_type_name, audit_event_type_description, audit_event_type_code) VALUES (111, 'Link Records', 'Link two records together.', 'LNKR');
INSERT INTO audit_event_type (audit_event_type_cd, audit_event_type_name, audit_event_type_description, audit_event_type_code) VALUES (112, 'Unlink Records', 'Unlink two records from each other', 'ULKR');

ALTER TABLE ONLY audit_event
    ADD CONSTRAINT audit_event_new_pkey PRIMARY KEY (audit_event_id);

-- Definition for index fk_audit_event_type_cd (OID = 59469):
ALTER TABLE ONLY audit_event
    ADD CONSTRAINT fk_audit_event_new_type_cd FOREIGN KEY (audit_event_type_cd) REFERENCES audit_event_type(audit_event_type_cd);

INSERT INTO link_source(link_source_id, source_name, source_description) VALUES (4, 'Gold Standard', 'Gold Standard Dataset');

DROP SEQUENCE IF EXISTS message_log_seq;

-- Table: message_log
DROP TABLE IF EXISTS message_log;

-- Table message_type
DROP TABLE IF EXISTS message_type;

CREATE TABLE message_type (
    message_type_cd integer NOT NULL,
    message_type_name varchar(64) NOT NULL,
    message_type_description varchar(255),
    message_type_code varchar(64) NOT NULL
) WITHOUT OIDS;

INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (1, 'Admit Notification', 'Admit/visit notification', 'ADT_A01');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (2, 'Acknowledgement', 'General Application Acknowledgement', 'ACK');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (3, 'Admit/Visit', 'Admin/Visit Notification (event A01)', 'A01');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (4, 'Register Patient', 'Register a Patient (event A04)', 'A04');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (5, 'Update Patient', 'Update Patient Information (event A08)', 'A08');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (6, 'Merge Patient', 'Merge Patient - Patient Identifier List (event A40)', 'A40');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (7, 'Get Person Demographics', 'Get Person Demographics (event Q21)', 'Q21');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (8, 'Find Candidates', 'Find Candidates (event Q22)', 'Q22');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (9, 'Get Corresponding Identifiers', 'Get Corresponding Identifiers (event Q23)', 'Q23');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (10, 'Update Notification', 'Update Notification (event A31)', 'A31');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (11, 'XAD Update Notification', 'XAD Update Notification (event A43)', 'A43');

INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (12, 'Add Patient HL7v3', 'Patient Registry Record Added (PRPA_IN201301UV02)', 'urn:hl7-org:v3:PRPA_IN201301UV02');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (13, 'Update Patient HL7v3', 'Patient Registry Record Revised (PRPA_IN201302UV02)', 'urn:hl7-org:v3:PRPA_IN201302UV02');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (14, 'Merge Patient HL7v3', 'Patient Registry Duplicates Resolved (PRPA_IN201304UV02)', 'urn:hl7-org:v3:PRPA_IN201304UV02');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (15, 'Get Identifiers HL7v3', 'Patient Registry Get Identifiers Query (PRPA_IN201309UV02)', 'urn:hl7-org:v3:PRPA_IN201309UV02');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (16, 'Get Identifiers Response HL7v3', 'Patient Registry Get Identifiers Query Response (PRPA_IN201309UV02)', 'urn:hl7-org:v3:PRPA_IN201310UV02');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (17, 'Find Candidates HL7v3', 'Patient Registry Find Candidates Query (PRPA_IN201305UV02', 'urn:hl7-org:v3:PRPA_IN201305UV02');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (18, 'Find Candidates Response HL7v3', 'Patient Registry Find Candidates Query Response (PRPA_IN201305UV02', 'urn:hl7-org:v3:PRPA_IN201306UV02');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (19, 'Query Continue HL7v3', 'General Query Activate Query Continue (QUQI_IN000003UV01)', 'urn:hl7-org:v3:QUQI_IN000003UV01');
INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (20, 'Query Cancellation HL7v3', 'Query Control Act Request Continue/Cancel (QUQI_IN000001UV01)', 'urn:hl7-org:v3:QUQI_IN000003UV01_Cancel');


INSERT INTO message_type (message_type_cd, message_type_name, message_type_description, message_type_code) VALUES (99, 'Unknown', 'Unknown Message Type', 'UNKNOWN');

CREATE TABLE message_log (
  message_log_id         integer NOT NULL PRIMARY KEY,
  incoming_message       text NOT NULL,
  outgoing_message       text,
  incoming_message_type_cd integer NOT NULL,
  outgoing_message_type_cd  integer,
  date_received          timestamp WITH TIME ZONE NOT NULL
)
WITHOUT OIDS;

ALTER TABLE ONLY message_type
    ADD CONSTRAINT message_type_pkey PRIMARY KEY (message_type_cd);

ALTER TABLE ONLY message_type
    ADD CONSTRAINT idx_message_type_name UNIQUE (message_type_name);

ALTER TABLE ONLY message_log
    ADD CONSTRAINT fk_inmessage_type_cd FOREIGN KEY (incoming_message_type_cd) REFERENCES message_type (message_type_cd);

ALTER TABLE ONLY message_log
    ADD CONSTRAINT fk_outmessage_type_cd FOREIGN KEY (outgoing_message_type_cd) REFERENCES message_type (message_type_cd);

CREATE INDEX idx_incoming_message_type ON message_log (incoming_message_type_cd);

CREATE INDEX idx_date_received ON message_log (date_received);

CREATE INDEX idx_message_type_code ON message_type USING btree (message_type_code);

ALTER TABLE message_log OWNER TO openempi;

CREATE SEQUENCE message_log_seq INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;

-- Added support for auditing the link and unlink events.
INSERT INTO audit_event_type (audit_event_type_cd, audit_event_type_name, audit_event_type_description, audit_event_type_code) VALUES (11, 'Link Persons', 'Link two person records together.', 'LNK');
INSERT INTO audit_event_type (audit_event_type_cd, audit_event_type_name, audit_event_type_description, audit_event_type_code) VALUES (12, 'Unlink Persons', 'Unlink two person records from each other', 'ULK');

CREATE SEQUENCE identifier_event_seq INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;

CREATE TABLE identifier_update_event (
  identifier_update_event_id  integer NOT NULL PRIMARY KEY,
  update_recipient_id         integer NOT NULL,
  date_created                timestamp WITH TIME ZONE NOT NULL,
  source                      varchar(64) NOT NULL,
  transition                  varchar(64) NOT NULL,
  /* Foreign keys */
  CONSTRAINT fk_update_recipient_id
    FOREIGN KEY (update_recipient_id)
    REFERENCES app_user(id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) WITH (
    OIDS = FALSE
  );

ALTER TABLE identifier_update_event OWNER TO openempi;

CREATE TABLE identifier_update_entry (
  identifier_update_entry_id  integer NOT NULL PRIMARY KEY,
  identifier_domain_id        integer NOT NULL,
  identifier                  varchar(255) NOT NULL,
  /* Foreign keys */
  CONSTRAINT fk_identifier_domain_id
    FOREIGN KEY (identifier_domain_id)
    REFERENCES identifier_domain(identifier_domain_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) WITH (
    OIDS = FALSE
  );

ALTER TABLE identifier_update_entry OWNER TO openempi;

CREATE TABLE identifier_pre_update (
  identifier_update_event_id  integer NOT NULL,
  identifier_update_entry_id  integer NOT NULL,
  /* Foreign keys */
  CONSTRAINT fk_identifier_update_entry_id
    FOREIGN KEY (identifier_update_entry_id)
    REFERENCES identifier_update_entry(identifier_update_entry_id)
    ON DELETE CASCADE 
    ON UPDATE NO ACTION, 
  CONSTRAINT identifier_update_event_id
    FOREIGN KEY (identifier_update_event_id)
    REFERENCES identifier_update_event(identifier_update_event_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) WITH (
    OIDS = FALSE
  );

ALTER TABLE identifier_pre_update OWNER TO openempi;

CREATE TABLE identifier_post_update (
  identifier_update_event_id  integer NOT NULL,
  identifier_update_entry_id  integer NOT NULL,
  /* Foreign keys */
  CONSTRAINT fk_identifier_update_entry_id
    FOREIGN KEY (identifier_update_entry_id)
    REFERENCES identifier_update_entry(identifier_update_entry_id)
    ON DELETE CASCADE 
    ON UPDATE NO ACTION, 
  CONSTRAINT identifier_update_event_id
    FOREIGN KEY (identifier_update_event_id)
    REFERENCES identifier_update_event(identifier_update_event_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) WITH (
    OIDS = FALSE
  );

ALTER TABLE identifier_post_update OWNER TO openempi;

DROP TABLE IF EXISTS link_log;

CREATE TABLE link_log (
    link_id integer NOT NULL PRIMARY KEY,
    entity_version_id integer NOT NULL,
    rh_record_id integer NOT NULL,
    lh_record_id integer NOT NULL,
    date_created timestamp without time zone NOT NULL,
    creator_id integer NOT NULL,
    weight double precision,
    vector_value integer NOT NULL
) WITHOUT OIDS;

CREATE INDEX idx_link_log_vector_value ON link_log (vector_value);
CREATE INDEX idx_link_log_weight ON link_log (weight);
