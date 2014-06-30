(ns vchain.data
  (:use korma.core korma.db vchain.data-extensions))

(defdb scratch 
       (postgres {:db "vchain"
                  :user "postgres"
                  :password "postgres"}))


