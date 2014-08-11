(ns vchain.relation
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [jayq.core :as jq :refer [$]]
            vchain.app
            vchain.graph
            vchain.links
            vchain.controls
            vchain.routes
            vchain.search))

(defn relation-view [rel]
  (html (if rel 
          [:tr
           [:td 
            [:a {:on-click #(vchain.routes/dispatch! 
                             (vchain.links/entity-link (:rel_first rel)))}
             (:first_ent_name rel)]]
           [:td 
            [:a {:on-click #(vchain.routes/dispatch! 
                             (vchain.links/entity-link (:rel_second rel)))}
             (:second_ent_name rel)]]
           [:td
            (:rty_description rel)]]
          [:tr])))


(defn add-relation-view [app owner]
  (reify
    om/IRender
    (render [_]
      (let [ent (get app :entity)
            relation-types (get app :relation-types)]
        (html
         [:div
          [:div.row 
           [:span.col-md-12 
            [:h4 "Add relationship"]]]
          [:div.row
           [:span.col-md-12 
            [:form.form-inline#form-add-relation {:role "form"}
             [:div.form-group
              [:span (:ent_name ent)]
              [:span [:label.sr-only {:for "form-add-relation-rty"}]
               (vchain.controls/combo 
                (map (fn [d] (:rty_description d)) (get app :relation-types))
                "form-add-relation-rty")]
              [:span [:label.sr-only {:for "form-add-relation-ent"}]
               [:div.dropdown {:style {:display "inline"}} 
                (om/build vchain.search/autocomplete app 
                          {:opts {:id "form-add-relation-ent" :set-text true}})]]
              [:span [:a {:on-click
                          #(vchain.app/add-relation! 
                            @ent
                            {:rel_first (:ent_slug @ent)
                             :second_ent_name (jq/val ($ :#form-add-relation-ent))
                             :rty_description (jq/val ($ :#form-add-relation-rty))})}
                      [:span.glyphicon.glyphicon-plus-sign]]]]]]]])))))

(defn relations-view [app owner]
  (reify
    om/IRender
    (render [_]
      (let [ent (get app :entity)
            rels (:relations ent)
            user (get app :user)]
        (html [:div
               [:div.row
                [:span.col-md-12 [:h4 "Network"]]
                [:span.col-md-12 [:table.col-md-12.table.table-condensed
                                  (apply dom/tbody (map relation-view rels))]] 
                #_[:div 
                   (apply dom/div (map relation-view rels))]
                #_[:div.col-md-6 
                   (om/build vchain.graph/graph-view rels)]] 
               [:div.row [:span.col-md-12 [:hr]]] 
               (when user
                 (om/build vchain.relation/add-relation-view app))])))))


