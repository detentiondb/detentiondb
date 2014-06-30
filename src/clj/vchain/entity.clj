(ns vchain.entity
  (:require clojure.string)
  (:use korma.core korma.db vchain.data vchain.data-extensions))

;(defn- jsonize [rows]
;  (map #(row->geojson-feature :loc_geometry %1) rows))

;(defn- page-param [p]
;  (Integer/parseInt (str p)))


(defentity entity-type
  (pk :ety_short_name)
  (table :entity_types))

(defentity entity
  (pk :ent_slug)
  (table :entities)
  (belongs-to entity-type {:fk :ety_short_name})
  (database vchain.data/scratch))

(def get-entity*
  (-> (select* entity)
      (with entity-type)
      (fields :ent_slug
              :ent_name 
              :ent_description
              :ety_short_name 
              :modified 
              :modified_by)))

(defn get-entity-names [prefix]
  "Get the descriptive names of all entities that start with a case-insensitive prefix."
  (let [up (str (clojure.string/upper-case prefix) "%")]
  (select entity
          (fields :ent_slug 
                  :ent_name)
          (where (like (uppercase :ent_name) up)))))

(defn get-entity [slug]
  "Get an entity by its slug."
  (first (select (-> get-entity*
                     (where {:ent_slug slug})))))

(defn get-entity-by-name [nam]
  "Get an entity by its distinguished name."
  (first (select (-> get-entity*
                     (where {:ent_name nam})))))

(defn all-entities []
  "Get all entities."
  (select entity))

(defn put-entity! [slug rec]
  "Set a location."
  (update entity
          (set-fields rec)
          (where {:ent_slug slug})))

(defn create-entity! [rec]
  "Insert a new entity."
  (insert entity
          (values rec)))

(defn delete-entity! [slug]
  "Delete a location."
  (delete entity
          (where {:ent_slug slug})))

