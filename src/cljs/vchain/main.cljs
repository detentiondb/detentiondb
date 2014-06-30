(ns vchain.main
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            vchain.search
            vchain.signin
            vchain.alert
            vchain.entity
            vchain.graph
            vchain.relation
            vchain.util))

(defn main-view [app owner]
  (reify
    om/IRender 
    (render [_]
      (html [:div
             [:div.vchain-fullscreen
              (om/build vchain.graph/full-view app {:opts {:override-id "network"}})]
             [:div.navbar.navbar-inverse ;navbar-fixed-top removed
              [:div.container
               [:div.navbar-header [:p.navbar-brand (vchain.util/title "detention boycott database")]]
               [:div.collapse.navbar-collapse.pull-right
                [:ul.nav.navbar-nav
                 [:li.dropdown
                  [:div#autocomplete {:style {:margin-top "10px"}}
                   (om/build vchain.search/autocomplete app {:opts {:id "entity-search" :set-text false}})]]]]
               [:div.collapse.navbar-collapse.pull-right#signin 
                (om/build vchain.signin/signin-view app)]]]
             [:div.container-fluid.vchain-record  
              [:div#alert 
               (om/build vchain.alert/alert-view app)]
              [:div.row-fluid.top-buffer]
              [:div#entity 
               (om/build vchain.entity/entity-view app)]
              [:div#relations 
               (om/build vchain.relation/relations-view app)]
              [:div.row-fluid.top-buffer]
              ]]))))
