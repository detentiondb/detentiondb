(ns vchain.util
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.reader :as reader]
            [goog.events :as events]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as string]
            [cljs.core.async :as async :refer [put! chan pub sub alts! mult tap]])
  (:import [goog.net XhrIo]
           goog.net.EventType
           [goog.events EventType]))

(def ^:private meths
  {:get "GET"
   :put "PUT"
   :post "POST"
   :delete "DELETE"})

(defn title [label] (str "detentionDB :: " label))

(defn edn-xhr [{:keys [method url data on-complete on-error]}]
  (let [xhr (XhrIo.)]
    (events/listen xhr goog.net.EventType.COMPLETE
                   (fn [e]
                     (when on-complete
                       (if (.isSuccess xhr)
                         (on-complete 
                           {:status (.getStatus xhr)
                            :body (reader/read-string (.getResponseText xhr))})
                         (when on-error
                           (on-error 
                             {:status (.getStatus xhr)
                              :body {:error (.getStatusText xhr)}}))))))
    (. xhr
       (send url (meths method) (when data (pr-str data))
             #js {"Content-Type" "application/edn"}))))

(defn listen-events [owner handler]
  (let [local-chan (chan)
        pub-chan (om/get-shared owner :event-chan)]
    (assert (not (nil? pub-chan)) "pub chan nil in listen-events")
    (tap pub-chan local-chan)
    (go (while true
          (let [[topic value] (<! local-chan)]
            (handler topic value))))))

(defn assert-legal-state [disallowed state]
  (assert (not (contains? disallowed state)) (str state " is disallowed here")))



