(ns vchain.auth
  (:use compojure.core)
  (:require 
    [cemerick.friend :as friend]
    (cemerick.friend [workflows :as workflows]
                     [credentials :as creds])
    [taoensso.timbre :as timbre]
    [vchain.response :as response]))

(timbre/refer-timbre)

(def users (atom {
                  ;"friend" {:username "friend"
                  ;          :password (creds/hash-bcrypt "clojure")
                  ;          :roles #{::user}}
                  "admin" {:username "admin"
                           :password (creds/hash-bcrypt "admin")
                           :roles #{::admin}}}))

(derive ::admin ::user)

(defn auth-fn [arg]
  (@users arg))

; Sample auth response body
#_{:user "friend"
   :message "You do not have privileges to do X"
   :success false}

(defn unauthorised [req]
  (response/html (vchain.templates/message 
                   "Unauthorised" 
                   (str "You do not have privileges to access " (:uri req))
                   "Unauthorised")
                 401))

(defn unauthenticated [req]
  (response/edn {:user nil
             :message "You're not logged in"
             :success false} 
            401))

(defn login-failed [req]
  (response/edn {:user nil
             :message "Login failed"
             :success false} 
            401))

(defn merge-auth-into-response [main-response auth]
  (merge (friend/merge-authentication nil auth) main-response))

(defn auth-workflow
  [& {:keys [credential-fn]}]
  (routes
    (GET "/logout" req
         (friend/logout* (response/edn {:user nil
                                    :message "Logged out"
                                    :success true}
                                   200)))
    (POST "/login" req 
          (let [{{:keys [username password]} :params} req]
            (if-let [user-record (-> username credential-fn)]
              (if
                ; Check user record exists and password matches
                (and
                  [user-record password]
                  (creds/bcrypt-verify password (:password user-record)))
                ; All good: merge session and return success
                (let [user-record (dissoc user-record :password)]
                  (merge-auth-into-response 
                    (response/edn {:user username
                               :message "Logged in" 
                               :success true} 
                              202)
                    (workflows/make-auth 
                      user-record 
                      {:cemerick.friend/workflow :auth-workflow
                       :cemerick.friend/redirect-on-auth? true})))
                (login-failed req))
              (login-failed req))))))

