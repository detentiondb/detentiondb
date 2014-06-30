(ns vchain.service
  (:require vchain.entity vchain.relation vchain.slug)
  (:use vchain.data-extensions))

(defn- merge-entity-slug [entity]
  "Merge the slug value for an entity into its properties, based on its name."
  (assoc entity :ent_slug (vchain.slug/make-slug (:ent_name entity))))

(defn get-entity [slug]
  "Retrieve a model of an entity and all its relations and links by its slug."
  (when-let [ent (vchain.entity/get-entity slug)]
    (assoc (audit-dates->strings ent)
           :relations (map audit-dates->strings
                           (vchain.relation/get-relations slug)))))

(defn put-entity! [user slug entity]
  "Update an entity via its model."
  (vchain.entity/put-entity! slug (-> entity 
                                      (dissoc :relations) 
                                      (strings->audit-dates)
                                      (merge-modified user))))

(defn create-entity! [user entity]
  "Create a new entity with no relationships."
  (-> entity 
      (dissoc :relations)
      (merge-created user)
      (merge-modified user)
      (merge-entity-slug)
      (vchain.entity/create-entity!)))

(defn get-or-create-entity! [user ent-alike]
  "Get an entity if it exists; otherwise create it based on a partial record."
  (let [candidate (merge-entity-slug ent-alike)
        retrieved (get-entity (:ent_slug candidate))]
    (if retrieved
      retrieved
      (create-entity! user candidate))))

(defn get-relations [slug]
  "Retrieve all an entity's relations based on its slug."
  (map audit-dates->strings (vchain.relation/get-relations slug)))

(defn get-relation-types []
  "Retrieve a list of all relation types."
  (map audit-dates->strings (vchain.relation/get-relation-types)))

(defn add-relation! [user slug relation]
  "Add a new relation to an existing entity."
  (assert (or (= slug (:rel_first relation))
              (= slug (:rel_second relation)))
          "Relation does not relate to specified entity.")
  (vchain.relation/create-relation! (-> relation 
                                        (merge-created user)
                                        (merge-modified user))))

(defn delete-entity! [user slug]
  "Remove an entity."
  (vchain.entity/delete-entity! slug))

(defn suggest-entity [p]
  "Get a list of suggested entity based on name prefix search."
  (vchain.entity/get-entity-names p))

(defn delete-relation! [user id]
  "Remove a relation based on its id."
  (vchain.relation/delete-relation! id))

(defn all-relations []
  "Return all relations."
  (map audit-dates->strings (vchain.relation/all-relations)))

