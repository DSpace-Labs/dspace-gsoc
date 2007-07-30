--
-- database_schema_cis.sql
--


-------------------------------------------------------
-- Sequences for creating new IDs (primary keys) for
-- tables.  Each table must have a corresponding
-- sequence called 'tablename_seq'.
-------------------------------------------------------
CREATE SEQUENCE hashvalueofitem_seq;
CREATE SEQUENCE witness_seq;


-------------------------------------------------------
-- Table: hashvalueofitem
-------------------------------------------------------
CREATE TABLE hashvalueofitem
(
  hashvalue_id 		INTEGER PRIMARY KEY,
  time_interval_id 	INTEGER,
  item_id 			INTEGER,
  hashvalue			VARCHAR(64),
  hash_algorithm 	VARCHAR(32),
  CONSTRAINT hashvalueofitem_item_id_fkey FOREIGN KEY (item_id)
      REFERENCES item (item_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE history OWNER TO dspace;

-------------------------------------------------------
-- Table: witness
-------------------------------------------------------
CREATE TABLE witness
(
  witness_id 		INTEGER PRIMARY KEY,
  time_interval_id 	INTEGER,
  hashvalue 		VARCHAR(64),
  hash_algorithm 	VARCHAR(32)
);
ALTER TABLE history OWNER TO dspace;





