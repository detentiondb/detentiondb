(ns vchain.web
  (:use compojure.core
        [ring.middleware edn json-params file file-info session stacktrace reload]
        ring.middleware.session.cookie)
  (:require [vchain.response :as response]
            vchain.service
            vchain.templates
            vchain.auth
            compojure.handler
            [ring.util.response :as resp]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defroutes vchain-routes
  ; Entities
  (POST "/entity" req
        (let [{params :params edn-params :edn-params} req]
          (friend/authenticated (vchain.service/create-entity! (:current friend/identity req) edn-params))
          (response/edn {:message "Entity creation successful" :success true})))

  ; Closest to the new scheme for different media types so far
  (GET "/entity/:slug" [slug & more]
       (cond 
         (= (:format more) "json")
         (response/json (vchain.service/get-entity slug))
         (= (:format more) "edn")
         (response/edn (vchain.service/get-entity slug))
         :else
         (response/html (vchain.templates/entity))))

  (PUT "/entity/:slug" req
       (let [{params :params edn-params :edn-params} req]
         (friend/authenticated (vchain.service/put-entity! (:current (friend/identity req)) (:slug params) edn-params))
         (response/edn {:message "Entity edit successful" :success true})))

  (DELETE "/entity/:slug" req 
          (let [{params :params} req]
            (friend/authenticated (vchain.service/delete-entity! (:current (friend/identity req)) (:slug params)))
            (response/edn {:message "Delete successful" :success true})))

  ; Relations
  (GET "/entity/:slug/relations" [slug & more]
       (cond
         (= (:format more) "json")
         (response/json (vchain.service/get-relations slug))
         (= (:format more) "edn")
         (response/edn (vchain.service/get-relations slug))
         :else
         (response/edn {:message "Not found" :success false} 404)))

  (GET "/relation-types" [& more]
       (cond
         (= (:format more) "json")
         (response/json (vchain.service/get-relation-types))
         (= (:format more) "edn")
         (response/edn (vchain.service/get-relation-types))
         :else
         (response/edn {:message "Not found" :success false} 404)))

  (GET "/relations" [& more]
       (cond
         (= (:format more) "json")
         (response/json (vchain.service/all-relations))
         (= (:format more) "edn")
         (response/edn (vchain.service/all-relations))
         :else
         (response/edn {:message "Not found" :success false} 404)))

  (POST "/entity/:slug/relations" req
        (let [{params :params edn-params :edn-params} req]
          (friend/authenticated (vchain.service/add-relation! (:current (friend/identity req)) (:slug params) edn-params))
          (response/edn {:message "Relation successful" :success true})))

  (DELETE "/relation/:id" req
          (let [{params :params} req]
            (friend/authenticated (vchain.service/delete-relation! (:current (friend/identity req)) (Integer/parseInt (:id params))))
            (response/edn {:message "Delete successful" :success false})))

  ; Helpers
  (GET "/entity/suggest/:prefix" [prefix & more]
       (response/edn (vchain.service/suggest-entity prefix)))

  (GET "/entity/suggest/" []
       (response/edn [])))


(def vchain-app
  (-> vchain-routes
      ; Need to use "api", not "site" handler to avoid clobbering session by 
      ; double application of wrap-session middleware
      compojure.handler/api
      (friend/authenticate
        {:allow-anon? true
         :credential-fn vchain.auth/auth-fn
         :unauthenticated-handler vchain.auth/unauthenticated
         :workflows [(vchain.auth/auth-workflow 
                       :credential-fn vchain.auth/auth-fn)]})
      (wrap-session {:store (cookie-store)})
      (wrap-json-params)
      (wrap-edn-params)
      (wrap-file "resources/public")
      (wrap-file-info)))

; old code used for debugging
;
;(defn format-request [name r]
;  (with-out-str
;    (println "-------------------------------")
;    (println name)
;    (println "-------------------------------")
;    (clojure.pprint/pprint r)
;    (println "-------------------------------")))
;
;
;(defn wrap-spy [handler spyname]
;  (fn [request]
;    (let [incoming (format-request (str spyname ":\n Incoming Request:") request)]
;      (println incoming)
;      (let [response (handler request)]
;        (let [outgoing (format-request (str spyname ":\n Outgoing Response:") response)]
;          (println outgoing))
;        response))))
