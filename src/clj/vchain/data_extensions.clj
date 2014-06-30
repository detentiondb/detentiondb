(ns vchain.data-extensions
  (:require [clj-json.core :as json]
            [clj-time.core :as time]
            [clj-time.coerce :as tc])
  (:use korma.core korma.db korma.sql.engine)
  (:import [java.sql Timestamp]))

; Database predicate and function shorthands for Korma

(defn uppercase [term]
  "An extended Korma predicate that uses LIKE and UPPER."
  (sql-func "UPPER" term))

(defn intersects [first-geom second-geom]
  "An extended Korma predicate that uses the PostGIS function ST_Intersects." 
  (sql-func "ST_Intersects" first-geom second-geom))

(defn from-wkt [geom]
  "Create a PostGIS geometry from WKT using ST_GeomFromText."
  (sql-func "ST_GeomFromText" geom (int 4326)))

(defn from-geojson [geom]
  "Create a PostGIS geometry from WKT using ST_GeomFromText."
  (sql-func "ST_GeomFromGeoJSON" geom))

(defn as-geojson [pg-geom]
  "Create a GeoJSON geometry from a PostGIS field, adding a CRS definition in 
  'short' format and restricting decimal digits to 3."
  (sql-func "ST_AsGeoJSON" pg-geom (int 3) (int 2)))


; Result row converters 

(defn row->geojson-feature [geometry-field row]
  "Convert a row containing a key which maps to a GeoJSON string
  to an updated row containing structured data by parsing the GeoJSON."
  (let [geojson (geometry-field row)
        properties (dissoc row geometry-field)]
    {:type "Feature",
     :properties properties,
     :geometry (if (= nil geojson) 
                 nil
                 (json/parse-string geojson))}))

(defn geojson-feature->row [geometry-field json]
  "Convert a data object containing a key which maps to structured 
  GeoJSON data to a GeoJSON string suitable for PostGIS ingest."
  (assert (= (:type json) "Feature"))
  (assoc (:properties json) geometry-field (json/generate-string (:geometry json))))

; Manipulate date columns
(def DATEFORMAT "yyyy-mm-dd kk:hh:ss")

(defn- parse-timestamp [d]
  "Parse a timestamp in the format used by vchain."
  ; Sample date "2014-05-08 23:07:15.0"
  (java.sql.Timestamp. (.getTime (.parse (java.text.SimpleDateFormat. DATEFORMAT) d))))

(defn audit-dates->strings [props]
  "Convert the modified audit date in a result row to a string."
  (let [conv (fn [k ps] (assoc (dissoc ps k) k (.format (java.text.SimpleDateFormat. DATEFORMAT) (k ps))))]
    (conv :modified props)))

(defn strings->audit-dates [props]
  "Convert the modified date string back to a Java timestamp."
  (let [conv (fn [k ps] (assoc (dissoc ps k) k (parse-timestamp (k ps))))]
    (conv :modified props)))

(defn- merge-audit-fields [props user k]
  "Merge created or modified audit fields into a property set."
  (merge props {k (java.sql.Timestamp. (tc/to-long (time/now))) (keyword (str (name k) "_by")) user}))

(defn merge-created [props user]
  "Merge the created audit fields into a property set."
  (merge-audit-fields props user :created))

(defn merge-modified [props user]
  "Merge the modified audit fields into a property set."
  (merge-audit-fields props user :modified))

