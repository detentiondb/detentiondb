(ns vchain.signin
  (:use [jayq.core :only [$]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            vchain.app))

(defn signin-view [app owner]
  (reify
    ;om/IInitState
    ;(init-state [_] {:signed-in false})
    om/IRender
    (render [_]
      (let [user (get app :user)
            login-fn #(do 
                        (om/transact! app :mode (fn [_] [:show]))
                        (vchain.app/login!))
            login-on-enter #(if (== (.-keyCode %) 13) login-fn)]
        (html
          [:div
           [:ul.nav.navbar-nav
            (if (nil? user)
              ; Not logged in
              [:li.dropdown
               [:a.dropdown-toggle
                {:data-toggle "dropdown"}
                "sign in"
                [:strong.caret]]
               [:div.dropdown-menu.pull-right
                [:label.control-label {:for "username"} "username"]
                [:input.input-block-level#username 
                 {:type "text"
                  :size "30"
                  :on-key-press login-on-enter}]
                [:label.control-label {:for "password"} "password"]
                [:input.input-block-level#password
                 {:type "password"
                  :size "30"
                  :on-key-press login-on-enter}]
                [:input.btn.pull-right
                 {:on-click login-fn
                  :style {:clear "left" :width "100%"}
                  :type "submit"
                  :value "sign in"}]]]
              ; Logged in
              [:li
               [:p.navbar-text
                (str "[" user "] ")
                [:a
                 {:on-click #(do 
                               (om/transact! app :mode (fn [_] [:show]))
                               (vchain.app/logout!))}
                 "logout"]]])]])))
    om/IDidUpdate
    (did-update [_ _ _]
      ; This cleverness stops the Bootstrap 3 dropdown from 
      ; toggling when one of the text inputs or labels is clicked
      (.click ($ "#username, #password, .dropdown-menu label") 
              (fn [e]
                (.stopPropagation e))))))
