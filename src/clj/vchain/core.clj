(ns vchain.core
  (:use
    [clojure.java.shell :only [sh]])
  (:require 
    [org.httpkit.server :as http-kit]
    vchain.web))

(defprotocol LifeCycle
  (start [this])
  (stop [this]))

(defn start-system [system]
  (doseq [s (->> system :order (map system))]
    (start s)))

(defn stop-system [system]
  (doseq [s (->> system :order (map system) reverse)]
    (stop s)))

;(defrecord PostgresServer []
;  LifeCycle
;  (start [_]
;    (sh "start-postgres.sh"))
;  (stop [_]
;    (sh "stop-postgres.sh")))

;(defn create-postgres []
;  (->PostgresServer))

(defrecord HttpKitServer [state]
  LifeCycle
  (start [_]
    (reset! state (http-kit/run-server #'vchain.web/vchain-app {:port 8082 :join? false})))
  (stop [_]
    ; The state returned by http-kit/run-server is a function that stops the server
    (when @state (@state))
    (reset! state nil)))

(defn create-httpkit []
  (->HttpKitServer (atom nil)))

(defn create-system []
    ;{:postgres (create-postgres)}
    {:httpkit (create-httpkit)
     :order [:httpkit]})
      
(defn -main []
  (start-system (create-system)))


