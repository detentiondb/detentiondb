(ns vchain.templates
  (:require [net.cgrand.enlive-html :as html]))

(html/deftemplate entity "templates/entity.html" [])

(html/deftemplate d3 "templates/d3.html" [])

(html/deftemplate message "templates/message.html" 
  [title msg h]
  [:title] (html/content title)
  [:#header] (html/content h)
  [:#message] (html/content msg))

   ; For later: script imports
   ;  [:script.import]  (html/clone-for [script scripts] (set-attr :src script))

   ;(html/deftemplate location "templates/location.html"
   ;  [loc]
   ;  [:title]          (html/content (:loc_name (:properties loc)))
   ;  [:script.map]     (html/content (map-script [(:geometry loc)]))                
   ;  [:li#property]    (html/clone-for [prop-name (keys (:properties loc))] 
   ;                                    (html/content (name prop-name) 
   ;                                                  " : " 
   ;                                                  (prop-name (:properties loc)))))

   ;(html/deftemplate locations "templates/location.html"
   ;  [locations]
   ;  [:title]          (html/content "All locations")
   ;  [:script.map]     (html/content (map-script (map :geometry locations)))) 

   ;(html/deftemplate om-location "templates/om_location.html"
   ;  [loc]
   ;  [:title]                      
   ;  (html/content (:loc_name (:properties loc)))
   ;  [:script.location-permalink]  
   ;  (html/content (location-permalink (:loc_short_name (:properties loc)))))                

