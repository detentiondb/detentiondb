(ns vchain.core
  (:require [om.core :as om :include-macros true]
            [cljs.core.async :as async :refer [put! chan mult]]
            [sablono.core :as html :refer-macros [html]]
            vchain.app
            vchain.main
            vchain.links
            vchain.routes))

(enable-console-print!)

; Dispatch to set up the initial data
(vchain.app/load-relation-types!)
(vchain.app/load-all-relations!)

(vchain.routes/dispatch! (vchain.links/entity-link {:ent_slug "graeme-hunt"}))

; Login automatically for testing purposes
(vchain.app/login* "admin" "admin")

(let [singlecast (chan)
      multicast (mult singlecast)]

  (om/root
    vchain.main/main-view
    vchain.app/APP-STATE 
    {:target (. js/document (getElementById "main"))
     :shared {:event-chan multicast}
     :tx-listen 
     (fn [tx-data root-cursor]
       (put! singlecast [tx-data root-cursor]))}))





