-- Structure for table link_source:
CREATE TABLE link_source (
    link_source_id integer NOT NULL,
    source_name varchar(64) NOT NULL,
    source_description varchar(255)
) WITHOUT OIDS;

insert into link_source(link_source_id, source_name, source_description) values (1, 'Manual', 'Manually Matched');
insert into link_source(link_source_id, source_name, source_description) values (2, 'Exact', 'Exact Matching Algorithm');
insert into link_source(link_source_id, source_name, source_description) values (3, 'Probabilistic', 'Probabilistic Matching Algorithm');

ALTER TABLE ONLY link_source
    ADD CONSTRAINT link_source_pkey PRIMARY KEY (link_source_id);
	
ALTER TABLE person_link
	ADD COLUMN link_source_id integer DEFAULT 2 NOT NULL;

ALTER TABLE person_link_review
	ADD COLUMN records_match boolean;

ALTER TABLE person_link_review
	ADD COLUMN link_source_id integer DEFAULT 3 NOT NULL;

ALTER TABLE person_link_review
	DROP COLUMN date_reviewed;

ALTER TABLE person_link_review
	ADD COLUMN date_reviewed timestamp without time zone;
	
ALTER TABLE ONLY person_link
    ADD CONSTRAINT fk_person_link_source FOREIGN KEY (link_source_id) REFERENCES link_source(link_source_id);
	



