(ns vchain.alert
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            vchain.routes))

(defn alert-view
  [app owner]
  (reify
    om/IRender
    (render [_]
      (html [:div.row-fluid
             (let [error (get app :error)]
               (if (not (nil? error))
                 [:div.alert.alert-danger.alert-dismissable
                  {:style {:font-size "15px"}}
                  [:button.close {:type "button" :data-dismiss "alert" :aria-hidden true} \u00d7]
                  [:strong error]]))]))))

