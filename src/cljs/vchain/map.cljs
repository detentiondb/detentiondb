(ns vchain.map
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            vchain.util))

(defn- make-leaflet-map 
  [elem]
  (let [osm-url "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attrib "Map data © OpenStreetMap contributors"]
    (doto (js/L.Map. elem)
      (.setView (js/L.LatLng. 0 0) 1)
      (.addLayer (js/L.TileLayer. osm-url 
                                  #js {:minZoom 1, :maxZoom 19, 
                                       :attribution attrib})))))

(defn- add-geometry [lmap geom]
  (let [layer (.addTo (L.geoJson (clj->js geom)) lmap)]
    (.fitBounds lmap (.pad (.getBounds layer) 0.2))))

(defn map-view 
  [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:lmap nil})
    om/IWillMount
    (will-mount [_]
      (vchain.util/edn-xhr
        {:method :get 
         :url "/locations/PERTH1A?format=edn"
         :on-complete #(om/transact! app :location (fn [_] %))}))
    om/IRender
    (render [_]
      (dom/div #js {:id "leaflet-map"} nil))
    om/IDidMount
    (did-mount [_]
      ; initialise a Leaflet map and stash it in local state
      (om/set-state! owner :lmap (make-leaflet-map "leaflet-map")))
    om/IWillUpdate
    (will-update [_ app-state state]
      (let [lmap (:lmap state)
            geom (get app-state :location)]
        (when (and lmap geom)
          (add-geometry lmap geom))))))

