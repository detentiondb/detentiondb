;; migrations/20140520222000881-create-tables.clj

(use 'clojure.java.jdbc)

(defn up []
  [
   "CREATE TABLE users
   (
   usr_name character varying(20) NOT NULL,
   usr_bio character varying(100),
   created timestamptz NOT NULL,
   created_by character varying(20) NOT NULL, 
   modified timestamptz NOT NULL, 
   modified_by character varying(20) NOT NULL,
   CONSTRAINT user_pk PRIMARY KEY (usr_name), 
   CONSTRAINT created_by_fk FOREIGN KEY (created_by)
   REFERENCES users (usr_name) MATCH SIMPLE
   ON UPDATE RESTRICT ON DELETE NO ACTION,
   CONSTRAINT modified_by_fk FOREIGN KEY (modified_by)
   REFERENCES users (usr_name) MATCH SIMPLE
   ON UPDATE RESTRICT ON DELETE NO ACTION
   )
   WITH (
   OIDS=FALSE
   );"
   "ALTER TABLE users OWNER TO postgres;"

   "CREATE TABLE entity_types
   ( 
   ety_name character varying(80),
   ety_description text,
   ety_short_name character varying(10) NOT NULL,
   created timestamptz NOT NULL,
   created_by character varying(20) NOT NULL, 
   modified timestamptz NOT NULL, 
   modified_by character varying(20) NOT NULL,
   CONSTRAINT entity_types_pk PRIMARY KEY (ety_short_name), 
   CONSTRAINT created_by_fk FOREIGN KEY (created_by)
   REFERENCES users (usr_name) MATCH SIMPLE
   ON UPDATE RESTRICT ON DELETE NO ACTION,
   CONSTRAINT modified_by_fk FOREIGN KEY (modified_by)
   REFERENCES users (usr_name) MATCH SIMPLE
   ON UPDATE RESTRICT ON DELETE NO ACTION
   )
   WITH (
   OIDS=FALSE
   );"
   "ALTER TABLE entity_types OWNER TO postgres;"

   "CREATE TABLE entities
   (
   ent_slug character varying(100) NOT NULL, 
   ent_name character varying(100) NOT NULL, 
   ent_description text,
   ety_short_name character varying(10) NOT NULL,
   ent_geometries geometry, 
   ent_centroids geometry, 
   ent_bboxes geometry,
   created timestamptz NOT NULL,
   created_by character varying(20) NOT NULL, 
   modified timestamptz NOT NULL, 
   modified_by character varying(20) NOT NULL,
   CONSTRAINT entity_pk PRIMARY KEY (ent_slug), 
   CONSTRAINT entity_type_fk FOREIGN KEY (ety_short_name) 
   REFERENCES entity_types (ety_short_name) MATCH SIMPLE 
   ON UPDATE RESTRICT ON DELETE NO ACTION, 
   CONSTRAINT created_by_fk FOREIGN KEY (created_by)
   REFERENCES users (usr_name) MATCH SIMPLE
   ON UPDATE RESTRICT ON DELETE NO ACTION,
   CONSTRAINT modified_by_fk FOREIGN KEY (modified_by)
   REFERENCES users (usr_name) MATCH SIMPLE
   ON UPDATE RESTRICT ON DELETE NO ACTION,

   CONSTRAINT enforce_dims_ent_geometries CHECK (st_ndims(ent_geometries) = 2),
   CONSTRAINT enforce_geotype_ent_geometries CHECK (geometrytype(ent_geometries) = 'GEOMETRYCOLLECTION'::text OR ent_geometries IS NULL),
   CONSTRAINT enforce_srid_ent_geometries CHECK (st_srid(ent_geometries) = 4326),

   CONSTRAINT enforce_dims_ent_centroids CHECK (st_ndims(ent_centroids) = 2),
   CONSTRAINT enforce_geotype_ent_centroids CHECK (geometrytype(ent_centroids) = 'MULTIPOINT'::text OR ent_centroids IS NULL),
   CONSTRAINT enforce_srid_ent_centroids CHECK (st_srid(ent_centroids) = 4326),

   CONSTRAINT enforce_dims_ent_bboxes CHECK (st_ndims(ent_bboxes) = 2),
   CONSTRAINT enforce_geotype_ent_bboxes CHECK (geometrytype(ent_bboxes) = 'MULTIPOLYGON'::text OR ent_bboxes IS NULL),
   CONSTRAINT enforce_srid_ent_bboxes CHECK (st_srid(ent_bboxes) = 4326)
   )
   WITH (
   OIDS=FALSE
   );"
   "ALTER TABLE entities OWNER TO postgres;"

   "CREATE INDEX ent_geometries_geom_idx
   ON entities
   USING gist
   (ent_geometries);"

   "CREATE INDEX ent_centroids_geom_idx
   ON entities
   USING gist
   (ent_centroids);"

   "CREATE INDEX ent_bboxes_geom_idx
   ON entities
   USING gist
   (ent_bboxes);"

   "CREATE TABLE relation_types
   ( 
   rty_name character varying(80),
   rty_description text,
   rty_short_name character varying(10) NOT NULL,
   created timestamptz NOT NULL,
   created_by character varying(20) NOT NULL, 
   modified timestamptz NOT NULL, 
   modified_by character varying(20) NOT NULL,
   CONSTRAINT relation_type_pk PRIMARY KEY (rty_short_name), 
   CONSTRAINT created_by_fk FOREIGN KEY (created_by)
   REFERENCES users (usr_name) MATCH SIMPLE
   ON UPDATE RESTRICT ON DELETE NO ACTION,
   CONSTRAINT modified_by_fk FOREIGN KEY (modified_by)
   REFERENCES users (usr_name) MATCH SIMPLE
   ON UPDATE RESTRICT ON DELETE NO ACTION
   )
   WITH (
   OIDS=FALSE
   );"

"ALTER TABLE relation_types OWNER TO postgres;"

"CREATE TABLE relations
(
rel_id serial NOT NULL,
rel_first character varying(100) NOT NULL, 
rel_second character varying(100) NOT NULL, 
rel_description character varying(100),
rty_short_name character varying(10) NOT NULL,
rel_value money, 
rel_valid_from date, 
rel_valid_to date, 
created timestamptz NOT NULL,
created_by character varying(20) NOT NULL, 
modified timestamptz NOT NULL, 
modified_by character varying(20) NOT NULL,
CONSTRAINT relation_pk PRIMARY KEY (rel_id),
CONSTRAINT first_entity_fk FOREIGN KEY (rel_first) 
REFERENCES entities (ent_slug) MATCH SIMPLE 
ON UPDATE RESTRICT ON DELETE NO ACTION,
CONSTRAINT second_entity_fk FOREIGN KEY (rel_second) 
REFERENCES entities (ent_slug) MATCH SIMPLE 
ON UPDATE RESTRICT ON DELETE NO ACTION,
CONSTRAINT relation_type_fk FOREIGN KEY (rty_short_name) 
REFERENCES relation_types (rty_short_name) MATCH SIMPLE 
ON UPDATE RESTRICT ON DELETE NO ACTION,
CONSTRAINT created_by_fk FOREIGN KEY (created_by)
REFERENCES users (usr_name) MATCH SIMPLE
ON UPDATE RESTRICT ON DELETE NO ACTION,
CONSTRAINT modified_by_fk FOREIGN KEY (modified_by)
REFERENCES users (usr_name) MATCH SIMPLE
ON UPDATE RESTRICT ON DELETE NO ACTION
)
WITH (
OIDS=FALSE
);"
"ALTER TABLE relations OWNER TO postgres;"

"CREATE TABLE links
(
lnk_id serial NOT NULL,
lnk_url character varying(2048) NOT NULL, 
created timestamptz NOT NULL,
created_by character varying(20) NOT NULL, 
modified timestamptz NOT NULL, 
modified_by character varying(20) NOT NULL,
CONSTRAINT link_pk PRIMARY KEY (lnk_id),
CONSTRAINT created_by_fk FOREIGN KEY (created_by)
REFERENCES users (usr_name) MATCH SIMPLE
ON UPDATE RESTRICT ON DELETE NO ACTION,
CONSTRAINT modified_by_fk FOREIGN KEY (modified_by)
REFERENCES users (usr_name) MATCH SIMPLE
ON UPDATE RESTRICT ON DELETE NO ACTION
)
WITH (
OIDS=FALSE
);"
"ALTER TABLE links OWNER TO postgres;"

"CREATE TABLE entity_links
(
ent_slug integer NOT NULL, 
lnk_id integer NOT NULL, 
created timestamptz NOT NULL,
created_by character varying(20) NOT NULL, 
modified timestamptz NOT NULL, 
modified_by character varying(20) NOT NULL,
CONSTRAINT created_by_fk FOREIGN KEY (created_by)
REFERENCES users (usr_name) MATCH SIMPLE
ON UPDATE RESTRICT ON DELETE NO ACTION,
CONSTRAINT modified_by_fk FOREIGN KEY (modified_by)
REFERENCES users (usr_name) MATCH SIMPLE
ON UPDATE RESTRICT ON DELETE NO ACTION
)
WITH (
OIDS=FALSE
);"
"ALTER TABLE entity_links OWNER TO postgres;"
])

(defn down []
  [
   (drop-table-ddl :entity_links)
   (drop-table-ddl :links)
   (drop-table-ddl :relations)
   (drop-table-ddl :relation_types)
   "DROP INDEX ent_bboxes_geom_idx;"
   "DROP INDEX ent_centroids_geom_idx;"
   "DROP INDEX ent_geometries_geom_idx;"
   (drop-table-ddl :entities)
   (drop-table-ddl :entity_types)
   (drop-table-ddl :users)])

