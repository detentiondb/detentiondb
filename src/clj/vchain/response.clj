(ns vchain.response
  (:require [clj-json.core :as json]))

(defn json [data & status]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (str (json/generate-string data) \newline)})

(defn edn [data & status]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (str (pr-str data) \newline)})

(defn html [enlive-tokens & status]
  {:status (or status 200)
   :headers {"Content-Type" "text/html"}
   :body (apply str enlive-tokens)})
