

CREATE SEQUENCE data_profile_attribute_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE SEQUENCE data_profile_attribute_value_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

-- Table: data_profile_attribute
DROP TABLE IF EXISTS data_profile_attribute;

CREATE TABLE data_profile_attribute
(
  attribute_id integer NOT NULL,
  attribute_name character varying(255) NOT NULL,
  datatype_id integer NOT NULL,
  average_length real,
  minimum_length integer,
  maximum_length integer,
  average_value real,
  minimum_value real,
  maximum_value real,
  variance real,
  standard_deviation real,
  row_count integer,
  distinct_count integer,
  duplicate_count integer,
  unique_count integer,
  null_count integer,
  null_rate real,
  entropy real,
  maximum_entropy real,
  u_value real,
  average_token_frequency real,
  blocking_pairs integer,
  data_source_id integer,
  CONSTRAINT data_profile_pkey PRIMARY KEY (attribute_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE data_profile_attribute OWNER TO openempi;

-- Index: idx_data_profile_source_attribute_name

-- DROP INDEX idx_data_profile_source_attribute_name;

CREATE INDEX idx_data_profile_source_attribute_name
  ON data_profile_attribute
  USING btree
  (data_source_id, attribute_name);

-- Table: data_profile_attribute_value
DROP TABLE IF EXISTS data_profile_attribute_value;

CREATE TABLE data_profile_attribute_value
(
  attribute_value_id integer NOT NULL,
  attribute_id integer NOT NULL,
  attribute_value character varying(1024),
  frequency integer NOT NULL,
  CONSTRAINT data_profile_value_pkey PRIMARY KEY (attribute_value_id),
  CONSTRAINT fk_data_profile_attribute_id FOREIGN KEY (attribute_id)
      REFERENCES data_profile_attribute (attribute_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE data_profile_attribute_value OWNER TO openempi;

-- Index: idx_attribute_id

-- DROP INDEX idx_attribute_id;

CREATE INDEX idx_attribute_id
  ON data_profile_attribute_value
  USING btree
  (attribute_id);

ALTER TABLE person
  ADD COLUMN father_name varchar(64),
  ADD COLUMN mother_name varchar(64),
  ADD COLUMN province varchar(255),
  ADD COLUMN district varchar(255),
  ADD COLUMN district_id varchar(64),
  ADD COLUMN cell varchar(255),
  ADD COLUMN cell_id varchar(64),
  ADD COLUMN sector varchar(255),
  ADD COLUMN sector_id varchar(64),
  ADD COLUMN village varchar(255),
  ADD COLUMN village_id varchar(64);


