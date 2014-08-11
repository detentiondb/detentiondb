(ns vchain.entity
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [cljs.core.async :as async :refer [put! chan mult tap alts! pub sub]]
            [sablono.core :as html :refer-macros [html]]
            [clojure.string :as string]
            vchain.app
            vchain.controls
            vchain.relation
            vchain.links
            vchain.routes
            vchain.util
            vchain.history))

(defn entity-header [ent]
  [:span
   [:h2 {:style {:display "inline"}}
    (:ent_name ent)]
   (when-let [typ (:ety_short_name ent)]
     (str "[" (string/capitalize typ) "]"))
   [:a. {:on-click #(println "share!")}
    [:span.glyphicon.glyphicon-share]]])

(defn add-entity-control [ent]
  [:span.pull-right
   [:h4 {:style {:display "inline"}}"Add new entity"]
   [:a {:on-click #(vchain.routes/dispatch!
                     "/entity/new")}
    [:span.glyphicon.glyphicon-plus-sign]]])

(defn entity-readonly [app owner]
  (reify
    om/IRender
    (render [_]
      (let [mode (get-in app [:mode 0])
            ent (get app :entity)]
        (assert (not (contains? #{:edit :cancelled :saved :new} mode)) 
                (str "cannot be rendering entity-readonly in mode " mode))
        (html
          [:div
           [:div.row-fluid
            [:div.span4 (entity-header ent)]
            [:hr]
            (om/build vchain.controls/markdown-textarea ent
                      {:opts {:data-fn #(get % :ent_description)
                              :display-fn (constantly true)}})
            (om/build vchain.controls/attribution ent)]])))))

(defn entity-editable [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (vchain.util/listen-events 
        owner 
        (fn [{:keys [path old-value new-value] :as topic} value]
         (if (= [:mode] path)
           (let [[old-mode] old-value
                 [new-mode] new-value]
             (println old-mode "-->" new-mode)
             (when (= [old-mode new-mode] [:edit :saved])
               (let [ent (:entity @app)]
               (vchain.app/save-entity! ent))))))))
    om/IRenderState
    (render-state [_ {:keys [editing]}]
      (let [mode (get-in app [:mode 0])
            ent (get app :entity)]
        (assert (not= mode :new) "should not be rendering entity-editable in mode :new")
        (html [:div
               [:div.row-fluid
                [:span.span4 (entity-header ent)]
                [:span.span4.pull-right (om/build vchain.controls/edit-control app)]]
               [:hr]
               (case mode 
                 :edit
                 (om/build vchain.controls/editable-textarea ent
                           {:opts {:data-key :ent_description 
                                   :finish-edit #(om/transact! app :mode
                                                              (fn [_] [:saved]))}})
                 (:show :saved :cancelled)
                 (om/build vchain.controls/markdown-textarea ent
                           {:opts {:data-fn #(:ent_description %)
                                   :display-fn (constantly true)}}))])))))

(defn entity-view [app owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [comm]}]
      (when-let [ent (get app :entity)]
        (set! (.-title js/document) (vchain.util/title (:ent_name ent))))
      (let [user (get app :user)]
        (html [:div.container-fluid
               (if user
                 ; Logged in: can edit
                 (om/build entity-editable app)
                 ; Not logged in: read-only display only
                 (om/build entity-readonly app))
               [:hr]])))))

