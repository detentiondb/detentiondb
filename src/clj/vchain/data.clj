(ns vchain.data
  (:use korma.core korma.db vchain.data-extensions))

(defdb scratch 
       (postgres {:db "ddb"
                  :user "postgres"
                  :password "postgres"}))


