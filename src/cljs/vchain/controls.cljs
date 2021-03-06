(ns vchain.controls
  (:require [om.core :as om :include-macros true]
            [jayq.core :as jq :refer [$]]
            [markdown.core :as md]
            [sablono.core :as html :refer-macros [html]]))

(def ENTER-KEY 13)

(defn handle-change [e data data-key owner]
  ;(println "handle-change" e @data edit-key owner)
  (om/transact! data data-key (fn [_] (.. e -target -value))))

(defn edit-control [app owner]
  (reify
    om/IRender
    (render [_]
      (let [mode (get-in app [:mode 0])]
        (html (case mode
                :edit
                [:div 
                 [:span {:style {:margin-right "10px"}} 
                  [:a 
                   {:on-click #(om/transact! app :mode (fn [_] [:show]))}
                   "cancel " [:span.glyphicon.glyphicon-remove]]] 
                 [:span 
                  [:a 
                   {:on-click #(om/transact! app :mode (fn [_] [:saved]))}
                   "save " [:span.glyphicon.glyphicon-ok]]]] 
                (:show :saved :cancelled)
                [:div
                 [:span
                  [:a 
                   {:on-click #(om/transact! app :mode (fn [_] [:edit]))}
                   "edit " [:span.glyphicon.glyphicon-edit]]]]))))))

(defn editable-textarea [data owner {:keys [data-key finish-edit] :as opts}]
  "Renders a text field editable by use of an Edit button at right.
   data-fn: function on data to get current edit structure.
   display-fn: whether this control is displayed. 
   submit-fn: submit the edit-structure in some way."
  (reify
    om/IRender
    (render [_]
      (let [edit-data (data-key data)]
        (html
         [:div.row
          ; usually displayed when editing
          [:div.col-md-12
           [:textarea.col-md-12
            {:rows "10"
             :width "auto"
             :style {:resize "vertical"}
             :value edit-data
             :on-change #(handle-change % data data-key owner)}]]]))))) ; submit changes 

(defn markdown-textarea [data owner {:keys [display-fn data-fn] :as opts}]
  (reify
    om/IRender
    (render [_]
      (html (if data 
              [:div.row#markdown
               [:div.col-md-12 {:style {:display (if display-fn (display-fn data) true)}
                                 :dangerouslySetInnerHTML 
                                 {:__html (.makeHtml  (js/Showdown.converter.) (or (data-fn data) "[add a description]"))}}]]
              [:div.row#markdown "Loading"])))))

(defn attribution [rec] 
  (html [:div.row 
         [:span.pull-right
          [:em
           (str "Modified by " (:modified_by rec) " " (:modified rec))]]]))

(defn combo [data id]
  (html [:select {:placeholder "relates to ..." :id id} 
         (map (fn [d] [:option d]) data)]))


