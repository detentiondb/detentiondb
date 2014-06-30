(ns vchain.data-tools
  "Tools for bulk loading data."
  (:require [clojure-csv.core :as csv] 
            [vchain.entity :as entity]
            [vchain.service :as service]))

(defn load-csv 
  "Load a delimited text table."
  [filename delim]
  (csv/parse-csv (slurp filename) :delimiter delim))

(defn personnel-csv []
  (load-csv "resources/data/personnel research updated.csv" \tab))

(defn initial-board-data []
  (load-csv "resources/data/init boards.csv" \,))

(defn load-initial-orgs []
  (map (fn [r] (service/create-entity! "admin" {:ent_name r :ety_short_name "ORG"})) 
       (distinct (map #(first %) (initial-board-data)))))

(defn load-initial-people []
  (map (fn [r] (service/create-entity! "admin" {:ent_name r :ety_short_name "PERSON"})) 
       (distinct (map #(second %) (initial-board-data)))))

(defn load-initial-board-relationships []
  (map (fn [r]
         (let [org-entity (entity/get-entity-by-name (first r))
               person-entity (entity/get-entity-by-name (second r))]
           (when (not (or (nil? org-entity) (nil? person-entity)))
             (service/add-relation! "admin" 
                                    (:ent_slug person-entity)
                                    {:rel_first (:ent_slug person-entity)
                                     :rel_second (:ent_slug org-entity)
                                     :rty_short_name "DIRECTOR"}))))
       (distinct (initial-board-data))))

(defn personnel-data []
  (let [people (into [] (rest (map second (personnel-csv))))
        orgs (into [] (drop 2 (first (personnel-csv))))
        matrix (into [] (map #(into [] (drop 2 %)) (rest (personnel-csv))))]
    (into []
          (filter #(not (empty? (:role %)))
                  (for [row (range (count people))
                        col (range (count orgs))]
                    {:person (nth people row)
                     :org (nth orgs col)
                     :role (nth (nth matrix row) col)}))))) 

(defn load-personnel-data []
  (map #(let [person (service/get-or-create-entity! "admin" {:ent_name (:person %) :ety_short_name "PERSON"})
              org (service/get-or-create-entity! "admin" {:ent_name (:org %) :ety_short_name "ORG"})]
          (service/add-relation! "admin" (:ent_slug person)
                                 {:rel_first (:ent_slug person)
                                  :rel_second (:ent_slug org)
                                  :rty_short_name "DIRECTOR"}))
       (personnel-data))) 



