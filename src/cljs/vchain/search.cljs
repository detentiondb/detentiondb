(ns vchain.search
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! chan put!]]
            [cljs.reader :as reader]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [jayq.core :as jq :refer [$]]
            [arosequist.om-autocomplete :as ac]
            [goog.events :as events]
            [vchain.links :as links]
            [vchain.routes :as routes])
  (:import [goog.net XhrIo]
           goog.net.EventType
           [goog.events EventType]))

(defn- results-id [id]
  (str id "-results"))

(defn container-view [_ _]
  (reify
    om/IRenderState
    (render-state [_ {:keys [input-component results-component]}]
      (html [:span input-component results-component]))))

(defn suggestions [value suggestions-ch cancel-ch]
  (let [xhr (XhrIo.)]
    (events/listen xhr goog.net.EventType.SUCCESS
                   (fn [e]
                     (put! suggestions-ch (reader/read-string (.getResponseText xhr)))))
    (go
      (<! cancel-ch)
      (.abort xhr))
    (.send xhr (str "/entity/suggest/" value) "GET")))

(defn input-view [_ _ {:keys [id] :as opts}]
  (reify
    om/IRenderState
    (render-state [_ {:keys [value-ch value]}]
      (html
        [:input.input-large.dropdown 
         {:id id
          :type "text"
          :placeholder "search ..."
          :auto-complete "off"
          :spell-check "false"
          :value value
          :on-change #(put! value-ch (.. % -target -value))
          #_(:on-blur #(display-search-results-view (results-id id) false))}]))))

(defn render-empty-suggestion []
  (html [:div]))

(defn display-search-results-view [id yn]
  (if yn
    (.show ($ (keyword (str "#" id))))
    (.hide ($ (keyword (str "#" id))))))

(defn render-suggestion [suggestion {:keys [id on-click] :as opts}]
  (html [:div
         [:a {:on-click #(do (display-search-results-view id false)
                             (if on-click
                               (on-click)
                             (routes/dispatch! 
                               (links/entity-link suggestion))))}
          (:ent_name suggestion)]]))

(defn results-view [_ _ {:keys [id set-text] :as opts}]
  (let [res-id (results-id id)]
    (reify
      om/IRenderState
      (render-state [_ {:keys [suggestions]}]
        (html [:div.search-results.dropdown-menu 
               {:display true :id res-id}
               (apply (partial dom/div nil)
                      (if suggestions
                        (map (fn [s] 
                               (let [result-opts (if set-text
                                                   {:id res-id 
                                                    :on-click 
                                                    #(jq/val ($ (str "#" id)) (:ent_name s))}
                                                   {:id res-id})] 
                                 (render-suggestion s result-opts)))
                             (take 5 suggestions))))]))
    om/IWillUpdate
    (will-update [_ _ next-state]
      (display-search-results-view res-id (not-empty (:suggestions next-state)))))))

(defn autocomplete [app owner opts]
  (reify
    om/IRenderState
    (render-state [_ _]
      (om/build ac/autocomplete app
                {:opts
                 {:container-view container-view
                  :input-view input-view
                  :input-view-opts opts
                  :results-view results-view
                  :results-view-opts opts
                  :suggestions-fn suggestions}}))))

