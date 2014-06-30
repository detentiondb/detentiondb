(ns vchain.links)

(defn entity-link [ent-alike]
  "Create an entity link from a record having an :ent_slug property."
  (let [result 
        (if-let [slug (:ent_slug ent-alike)]
          (str "/entity/" slug)
          (str "/entity/" ent-alike))]
    (println "entity-link" result)
    result))


