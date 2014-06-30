(ns vchain.slug
  (:require [clojure.string :as string]))

(defn make-slug [name]
  (-> name
      (string/replace #"[^\d\w_]+" "-")
      (string/replace #"-+" "-")
      (string/replace #"-$" "")
      (string/replace #"^-" "")
      (string/trim)
      (string/lower-case)))
	
