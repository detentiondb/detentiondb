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
             [:nav.navbar.navbar-inverse 
               [:div.container
                [:div.navbar-header 
                [:button.navbar-toggle 
                 {:type "button" 
                  :data-target ".navbar-collapse"}
                 [:span.sr-only "Toggle navigation"]
                 [:span.glyphicon.glyphicon-list]]
                  [:span.navbar-brand (vchain.util/title "a detention industry database")]]
               [:div.collapse.navbar-collapse.pull-right
                [:ul.nav.navbar-nav
                 [:li.dropdown.navbar-text
                  [:div#autocomplete 
                   (om/build vchain.search/autocomplete app {:opts {:id "entity-search" :set-text false}})]]i
                 [:li
                  [:div#signin (om/build vchain.signin/signin-view app)]]]]]]
             [:div.container.vchain-record  
              [:div#alert 
               (om/build vchain.alert/alert-view app)]
              [:div.row.top-buffer]
              [:div#entity 
               (om/build vchain.entity/entity-view app)]
              [:div#relations 
               (om/build vchain.relation/relations-view app)]
              [:div.row.top-buffer]
              ]]))))
