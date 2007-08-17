CREATE TABLE logs
(
  logs_id integer NOT NULL,
  event_date timestamp without time zone,
  "action" text,
  ip character varying(16),
  CONSTRAINT logs_pkey PRIMARY KEY (logs_id)
) 
WITHOUT OIDS;
ALTER TABLE logs OWNER TO dspace;

CREATE TABLE logs_attributes
(
  attributes_id integer NOT NULL,
  param text,
  value text,
  logs_id integer,
  CONSTRAINT logs_attributes_pkey PRIMARY KEY (attributes_id)
) 
WITHOUT OIDS;
ALTER TABLE logs_attributes OWNER TO dspace;


CREATE SEQUENCE logs_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 111
  CACHE 1;
ALTER TABLE logs_seq OWNER TO dspace;

CREATE SEQUENCE logs_attributes_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 267
  CACHE 1;
ALTER TABLE logs_attributes_seq OWNER TO dspace;