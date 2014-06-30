(require '[cemerick.pomegranate :as pom])
(pom/add-classpath "src/clj")
(require 'vchain.data-tools)

(defn up []
  (println "Loading orgs ...")
  (doall (vchain.data-tools/load-initial-orgs))
  (println "Loading people ...")
  (doall (vchain.data-tools/load-initial-people))
  (println "Loading board relationships ...")
  (doall (vchain.data-tools/load-initial-board-relationships))
  (println "Loading personnel-data ...")
  (doall (vchain.data-tools/load-personnel-data))
  [])

(defn down []
  [

   "DELETE FROM relations;"
   "DELETE FROM entities;"

   ])

