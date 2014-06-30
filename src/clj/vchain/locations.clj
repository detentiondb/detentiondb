;(ns vchain.locations
;  (:use korma.core korma.db vchain.data vchain.data-extensions))
;
;(defentity locations-table
;           (pk :loc_short_name)
;           (table :locations)
;           (database vchain.data/scratch))
;
;(defn- jsonize [rows]
;  (map #(row->geojson-feature :loc_geometry %1) rows))
;
;(defn- page-param [p]
;  (Integer/parseInt (str p)))
;
;(defn get-locations* []
;    (-> (select* locations-table) 
;        (fields :loc_name 
;                :loc_short_name 
;                [(as-geojson :loc_geometry) :loc_geometry])))
;
;(defn- get-location* [short-name]
;  (-> (get-locations*) 
;      (where {:loc_short_name short-name})))
;
;(defn get-location [short-name]
;  "Find a location using its short name."
;  (first (jsonize (select (get-location* short-name)))))
;
;(defn- get-intersecting-locations* [wkt]
;  (-> (get-locations*)
;      (where (intersects :loc_geometry wkt))))
;
;(defn get-intersection-locations [wkt]
;  "Find locations intersecting with the specified geometry."
;  (jsonize (select (get-intersecting-locations* wkt))))
;
;(def PAGE 20)
;
;(defn all-locations [& [page]]
;  "Select all locations from the gazetteer."
;  (jsonize
;    (select
;      (if (nil? page) 
;        (get-locations*)
;        (try
;          (-> (get-locations*)
;          (limit PAGE)
;          (offset (* PAGE (page-param page))))
;          (catch Exception e []))))))
;
;(defn put-location! [short-name json]
;  "Set a location."
;  (update locations-table
;          (set-fields (geojson-feature->row :loc_geometry json))
;          (where {:loc_short_name short-name})))
;
;(defn delete-location! [short-name]
;  "Delete a location."
;  (delete locations-table
;          (where {:loc_short_name short-name})))
;
