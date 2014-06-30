(ns vchain.relation
  (:use korma.core korma.db vchain.data vchain.data-extensions vchain.entity))

;(defn- jsonize [rows]
;  (map #(row->geojson-feature :loc_geometry %1) rows))

;(defn- page-param [p]
;  (Integer/parseInt (str p)))

(declare relation-type first-entity second-entity relation)

(defentity relation-type
  (pk :rty_short_name)
  (table :relation_types))

(defentity first-entity
  (pk :ent_slug)
  (table :entities :first_entity)
  (has-many relation {:fk :rel_second}))

(defentity second-entity
  (pk :ent_slug)
  (table :entities :second_entity)
  (has-many relation {:fk :rel_first}))

(defentity relation
  (pk :rel_id)
  (table :relations)
  (belongs-to relation-type {:fk :rty_short_name})
  (belongs-to first-entity {:fk :rel_first})
  (belongs-to second-entity {:fk :rel_second})
  (database vchain.data/scratch))

(def get-relation*
  (-> (select* relation)
      (with relation-type)
      (with first-entity)
      (with second-entity)
      (fields :rel_id
              :rel_first
              :rel_second
              :rel_description 
              :relation_types.rty_short_name 
              :relation_types.rty_name
              :relation_types.rty_description 
              [:first_entity.ent_name :first_ent_name]
              [:second_entity.ent_name :second_ent_name]
              [:first_entity.ety_short_name :first_ety_short_name]
              [:second_entity.ety_short_name :second_ety_short_name]
              :modified 
              :modified_by)))

(defn get-relation-types []
  "Get the descriptive names of all entities that start with a case-insensitive prefix."
  (select relation-type))

(defn get-relations [ent-slug & {:keys [dir] :or {dir :all}}]
  "Get an entity's relations, either outgoing, incoming or both."
  (distinct
    (case dir
      :out
      (select (-> get-relation*
                  (where {:rel_first ent-slug})))
      :in
      (select (-> get-relation*
                  (where {:rel_second ent-slug})))
      :all
      (select (-> get-relation*
                  (where (or {:rel_first ent-slug} {:rel_second ent-slug}))))
      :else (throw (IllegalArgumentException. "valid directions are :out, :in, and :all")))))

(defn catv 
  "Concatenate two collections."
  ([v w] (reduce conj v w))
  ([] ()))

(defn get-2nd-relations 
  "Get all second-order relations to an entity."
  [ent-slug]
  (distinct 
    (reduce catv 
            (map #(get-relations % :dir :all) 
                 (distinct 
                   (reduce catv [(map :rel_first (get-relations ent-slug :dir :all))
                                 (map :rel_second (get-relations ent-slug :dir :all))]))))))
                 
    #_(((case dir 
      :out 
      (reduce catv (map #(get-relations %1 :dir :out) 
                        (map :rel_second (get-relations ent-slug :dir :out))))
      :in
      (reduce catv (map #(get-relations %1 :dir :in) 
                        (map :rel_first (get-relations ent-slug :dir :in))))
      :all
      (reduce catv (map #(get-relations %1 :dir :all)
                        (map (if (= :rel_first ent-slug) 
                               :rel_second 
                               :rel_first)
                             (get-relations ent-slug :dir :all)))))))

(defn get-relation [id]
  "Get a relation by its id."
  (first (select (-> get-relation*
                     (where {:rel_id id})))))

(defn all-relations []
  "Select all relations from the network."
  (select get-relation*))

(defn put-relation! [id rec]
  "Set a relation."
  (update relation
          (set-fields rec)
          (where {:rel_id id})))

(defn create-relation! [rec]
  "Insert a new relation."
  (insert relation
          (values rec)))

(defn delete-relation! [id]
  "Delete a relation."
  (delete relation
          (where {:rel_id id})))

