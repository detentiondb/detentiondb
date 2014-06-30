(ns vchain.app
  (:use [jayq.core :only [$]])
  (:require vchain.links 
            vchain.util 
            vchain.history))

; APP-STATE structure eg 
#_{:user "friend"
   :error "error on login"
   :entity {:ent_slug "an-entity", 
            :ent_name "An entity"
            :ent_description "An entity description", 
            :another value
            :relations [{:rel_id 2, 
                         :first_ent_name "An entity", 
                         :another value},
                        {:rel_id 3, 
                         :another value}]}
   :mode [:show]
   :relation-types nil
   :all-relations nil} 
; 
; :mode can be :show, :edit, :new, :cancelled or :saved

(def APP-STATE (atom {:user nil :error nil :entity nil :mode [:show]}))

(defn set-error!
  "Set the current application error."
  [err]
  (swap! APP-STATE assoc :error err))

(defn set-new-entity!
  "Start editing a new entity."
  []
  (vchain.history/push-state! "/entity/new")
  (swap! APP-STATE assoc :new-entity true))

(defn set-current-entity! 
  "Set the current entity and update the browser address."
  [ent]
  (let [link (vchain.links/entity-link ent)]
    (println "set-current-entity! ent" ent)
    (vchain.util/edn-xhr
      {:method :get
       ; Be nice to do this with an Accepts: header instead
       :url (str link "?format=edn")
       :on-complete #(do (vchain.history/push-state! link)
                         (swap! APP-STATE assoc :entity (:body %)))
       :on-error #(set-error! (str "Error retrieving entity: " (:error (:body %))))})))

(defn save-entity! [ent]
  (let [link (vchain.links/entity-link ent)]
    (vchain.util/edn-xhr
      {:method :put
       :data ent
       :url (str link "?format=edn")
       :on-complete #(do (println "Edited" (:ent_name ent))
                         (vchain.routes/dispatch! (vchain.links/entity-link ent)))
       :on-error #(set-error! (str "Error updating entity: " (:error (:body %))))})))

(defn save-current-entity!
  "Save the current entity to server."
  []
  (let [ent (get APP-STATE :entity)]
    (save-entity! ent)))

(defn login*
  "Attempt to log in with name and password"
  [username password]
  (vchain.util/edn-xhr
    {:method :post
     :data {:username username :password password}
     :url "/login"
     :on-complete (fn [{:keys [status body]}]
                    (if (:success body) 
                      (swap! APP-STATE assoc :user (:user body))
                      (set-error! (str "Error logging in " (:error body)))))
     :on-error #(set-error! (str "Error logging in: " (:error (:body %))))}))

(defn login!
  "Log in to the server."
  [] 
  (let [username (.val ($ :#username))
        password (.val ($ :#password))]
    (println "Logging in as" username ":" password)
    (login* username password)))

(defn logout! 
  "Log out of the server."
  []
  (vchain.util/edn-xhr
    {:method :get
     :url "/logout"
     :on-complete #(swap! APP-STATE assoc :user nil)
     :on-error #(set-error! (str "Error logging out: " (:error (:body %))))}))

(defn load-relation-types!
  "Retrieve a list of types of relations."
  []
  (println "Loading relationship types ...")
  (vchain.util/edn-xhr
    {:method :get
     :url "/relation-types?format=edn"
     :on-complete #(swap! APP-STATE assoc :relation-types (:body %))
     :on-error #(set-error! (str "Error retrieving relation types: " 
                                 (:error (:body %))))}))

(defn load-all-relations!
  "Retrieve a list of all relations."
  []
  (println "Loading relations ...")
  (vchain.util/edn-xhr
    {:method :get
     :url "/relations?format=edn"
     :on-complete #(swap! APP-STATE assoc :all-relations (:body %))
     :on-error #(set-error! (str "Error retrieving all-relations: " 
                                 (:error (:body %))))}))
(defn get-relations 
  "Retrieve the relations for the current entity."
  [ent]
  (vchain.util/edn-xhr
    {:method :get
     :url (str (vchain.links/entity-link ent) "/relations")
     :on-complete (fn [resp] 
                    (swap! APP-STATE (fn [state]
                                       (assoc (:entity state) :relations 
                                              (:body resp)))))}))


(defn- map-invert
    "Returns the map with the vals mapped to the keys."
    [m] (reduce (fn [m [k v]] (assoc m v k)) {} m))


(defn add-relation!
  "Add a relation to the current entity."
  [ent rel]
  (println rel)
  #_(vchain.util/edn-xhr
    {:method :post
     :data rel
     :url (str (vchain.links/entity-link ent) "/relations")
     :on-complete #(get-relations ent)
     :on-error #(set-error! (str "Error updating relations."
                                 (:error (:body %))))})) 



