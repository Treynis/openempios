-- Table Report
-- DROP TABLE report;
CREATE TABLE report
(
 report_id integer NOT NULL,
 "name" character varying(64) NOT NULL,
 name_displayed character varying(255) NOT NULL,
 description character varying(255),
 template_name character varying(128) NOT NULL,
 data_generator character varying(128) NOT NULL,
 CONSTRAINT report_pkey PRIMARY KEY (report_id)
) WITHOUT OIDS;

ALTER TABLE report OWNER TO openempi;


-- Table: report_parameter
-- DROP TABLE report_parameter;
CREATE TABLE report_parameter
(
 report_parameter_id integer NOT NULL,
 "name" character varying(64) NOT NULL,
 name_displayed character varying(255) NOT NULL,
 description character varying(255),
 report_id integer NOT NULL,
 parameter_datatype integer NOT NULL, -- D - date...
 CONSTRAINT report_parameter_pkey PRIMARY KEY (report_parameter_id),
 CONSTRAINT fk_report FOREIGN KEY (report_id)
     REFERENCES report (report_id) MATCH SIMPLE
     ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITHOUT OIDS;
ALTER TABLE report_parameter OWNER TO openempi;

COMMENT ON COLUMN report_parameter.parameter_datatype IS 'D - date
I - integer
S - string
';

-- Table: report_query
-- DROP TABLE report_query;
CREATE TABLE report_query
(
 report_query_id integer NOT NULL,
 "name" character varying(64) NOT NULL,
 query text NOT NULL,
 report_id integer NOT NULL,
 CONSTRAINT report_query_pkey PRIMARY KEY (report_query_id),
 CONSTRAINT fk_report FOREIGN KEY (report_id)
     REFERENCES report (report_id) MATCH SIMPLE
     ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITHOUT OIDS;
ALTER TABLE report_query OWNER TO openempi;

-- Table: report_query_parameter
-- DROP TABLE report_query_parameter;
CREATE TABLE report_query_parameter
(
 report_query_parameter_id integer NOT NULL,
 report_parameter_id integer NOT NULL,
 parameter_name character varying(64) NOT NULL,
 report_query_id integer NOT NULL,
 required character(1) NOT NULL DEFAULT 'Y',
 CONSTRAINT report_query_parameter_pkey PRIMARY KEY (report_query_parameter_id),
 CONSTRAINT fk_report_parameter FOREIGN KEY (report_parameter_id)
     REFERENCES report_parameter (report_parameter_id) MATCH SIMPLE
     ON UPDATE NO ACTION ON DELETE NO ACTION,
 CONSTRAINT fk_report_query FOREIGN KEY (report_query_id)
     REFERENCES report_query (report_query_id) MATCH SIMPLE
     ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITHOUT OIDS;
ALTER TABLE report_query_parameter OWNER TO openempi;

-- Table: report_request
-- DROP TABLE report_request;
CREATE TABLE report_request (
  report_request_id integer NOT NULL,
  report_id integer NOT NULL,
  user_requested_id bigint NOT NULL,
  date_requested timestamp with time zone NOT NULL,
  completed character varying(1) NOT NULL,
  report_handle character varying(255),
  date_completed timestamp with time zone,
  CONSTRAINT report_request_id_pkey
    PRIMARY KEY (report_request_id),
  CONSTRAINT fk_user_requested
    FOREIGN KEY (user_requested_id)
    REFERENCES app_user(id)
    ON DELETE NO ACTION ON UPDATE NO ACTION
)
WITHOUT OIDS;
ALTER TABLE report_request OWNER TO openempi;

CREATE SEQUENCE report_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE SEQUENCE report_query_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
    
CREATE SEQUENCE report_parameter_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE SEQUENCE report_query_parameter_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE SEQUENCE report_request_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE UNIQUE INDEX report_name_unique_idx ON report USING btree (name);

ALTER TABLE report ADD CONSTRAINT report_name_key UNIQUE (name);

ALTER TABLE user_file ADD COLUMN rows_imported integer;
ALTER TABLE user_file ADD COLUMN rows_processed integer;

ALTER TABLE "role" RENAME COLUMN id TO role_id;

CREATE TABLE permission (
  permission_id bigint NOT NULL,
  permission_name varchar(50) NOT NULL,
  permission_description varchar(255) NOT NULL,
  CONSTRAINT permission_pkey
    PRIMARY KEY (permission_id)
) WITHOUT OIDS;

ALTER TABLE permission OWNER TO openempi;

INSERT INTO permission (permission_id, permission_name, permission_description) VALUES
  (1, 'RECORD_ADD', 'Allows the user to add an entity record into the database.'),
  (2, 'RECORD_DELETE', 'Allows the user to delete an entity record from the database.'),
  (3, 'RECORD_EDIT', 'Allows the user to modify the fields of an entity record from the database'),
  (4, 'RECORD_VIEW', 'Allows the user to retrieve entity records from the database.'),
  (5, 'IDENTIFIER_DOMAIN_ADD', 'Allows the user to add a new identifier domain.'),
  (6, 'IDENTIFIER_DOMAIN_DELETE', 'Allows the user to delete an identifier domain from the database.'),
  (7, 'IDENTIFIER_DOMAIN_EDIT', 'Allows the user to modify the fields of an identifier domain.'),
  (8, 'IDENTIFIER_DOMAIN_VIEW', 'Allows the user to retrieve identifier domain records from the database.'),
  (9, 'RECORD_LINKS_REVIEW', 'Allows the user to review and resolve potential record matches.'),
  (10, 'REPORT_GENERATE', 'Allows the user to request for a report to be generated.'),
  (11, 'REPORT_VIEW', 'Allows the user to view a report that has been previously generated.'),
  (12, 'CUSTOM_FIELDS_CONFIGURE', 'Allows the user to configure the list of custom fields and their definition.'),
  (13, 'BLOCKING_CONFIGURE', 'Allows the user to configure the blocking algorithm used by the system.'),
  (14, 'MATCHING_CONFIGURE', 'Allows the user to configure the matching algorithm used by the system.'),
  (15, 'FILE_IMPORT', 'Allows the user to import a data file into the system.'),
  (16, 'USER_ADD', 'Allows the user to add new users to the system.'),
  (17, 'USER_DELETE', 'Allows the user to delete a user account from the system.'),
  (18, 'USER_EDIT', 'Allows the user to modify the attributes of a user account.'),
  (19, 'USER_VIEW', 'Allows the user to view the attributes of a user account.'),
  (20, 'EVENT_CONFIGURATION_EDIT', 'Allows the user to manage the configuration of the event notification subsystem.'),
  (21, 'GLOBAL_IDENTIFIERS_EDIT', 'Allows the user to assign global identifiers to all the users in the repository.'),
  (22, 'PIXPDQ_MANAGE', 'Allows the user to start, stop and configure the PIX/PDQ service.');

CREATE TABLE role_permission (
  role_id        bigint NOT NULL,
  permission_id  bigint NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  /* Foreign keys */
  CONSTRAINT fk_permission_id
    FOREIGN KEY (permission_id)
    REFERENCES permission(permission_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION, 
  CONSTRAINT fk_role_id
    FOREIGN KEY (role_id)
    REFERENCES "role"(role_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) WITHOUT OIDS;

ALTER TABLE role_permission OWNER TO openempi;

INSERT INTO role_permission (role_id, permission_id) VALUES
(-1, 1), (-1, 2), (-1, 3), (-1, 4), (-1, 5), (-1, 6),
(-1, 7), (-1, 8), (-1, 9), (-1, 10), (-1, 11), (-1, 12),
(-1, 13), (-1, 14), (-1, 15), (-1, 16), (-1, 17), (-1, 18),
(-1, 19), (-1, 20), (-1, 21), (-1, 22);

CREATE SEQUENCE cluster_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE person_link ADD COLUMN cluster_id integer;

ALTER TABLE report_query_parameter ADD COLUMN substitution_key character varying(64);

