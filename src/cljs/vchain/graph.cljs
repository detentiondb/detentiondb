(ns vchain.graph
  (:require [om.core :as om :include-macros true]
            [cljs.core.async :as async :refer [put! chan mult]]
            [clojure.string :as string :refer [join]]
            [sablono.core :as html :refer-macros [html]]
            [strokes :refer [d3]]
            vchain.links
            vchain.routes
            vchain.slug
            vchain.wrap))

(enable-console-print!)

(strokes/bootstrap)

; Some slightly fancy code reduced from
; http://stackoverflow.com/questions/4830900/how-do-i-find-the-index-of-an-item-in-a-vector

(defn- positions
  "Returns the positions at which pred is true for items in coll."
  [pred coll]
  (for [[idx elt] (map vector (iterate inc 0) coll) :when (pred elt)] idx))

(defn- make-d3-graph-data 
  "Make graph data suitable for use with a D3 forcelayout from a collection of 
  vchain entity relationships."
  [rels]
  (let [nodes* (distinct (concat 
                                 (map (fn [rel] {:name (:first_ent_name rel)
                                                 :type (:first_ety_short_name rel)}) rels)
                                 (map (fn [rel] {:name (:second_ent_name rel)
                                                 :type (:second_ety_short_name rel)}) rels)))
        
        make-node (fn [nod] (assoc nod :group 1))
        make-link (fn [rel]
                    {:source (first (positions #{(:first_ent_name rel)} (map :name nodes*)))
                     :target (first (positions #{(:second_ent_name rel)} (map :name nodes*)))
                     :value 5})
        nodes (map make-node nodes*)
        links (map make-link rels)]
    [(clj->js nodes)
     (clj->js links)]))

; Do these need to be fixed?
(def width 960)
(def height 500)

(defn graph-view [rels owner {:keys [override-id] :as opts}]
  (let [id (if override-id 
             override-id
             "graph")]
    (reify
      om/IRender
      (render [_]
        (html [:div.span6
               [:div {:id id}]]))
      om/IDidUpdate
      (did-update [_ _ _]
        (-> d3 (.select "svg") (.remove))
        (when rels
          (let [color (-> d3 (.-scale) (.-category20))

                force (-> (.d3adaptor js/cola)
                          (.linkDistance 100)
                          (.size (array width height)))

                svg (-> d3 (.select (str "#" id)) (.append "svg"))

                vis svg

                ;vis (.append svg "svg:g")

                [nodes links] (make-d3-graph-data rels)]
            (-> svg 
                ; this code means some degree of resizeability - graph still sometimes off-canvas
                (.attr {:width "100%"
                        :height "100%"
                        ;:viewBox (join " " (map str [0 0 width height])) 
                        :preserveAspectRatio "xMidYMin meet"
                        :pointer-events "all"})
                (.call (-> d3 (.-behavior) (.zoom)
                           (.on "zoom" (fn []
                                         (.attr vis {:transform 
                                                     (concat "translate("
                                                             (.. d3 -event -translate)
                                                             ") scale("
                                                             (.. d3 -event -scale)
                                                             ")")}))))))
            (-> force
                (.nodes nodes)
                (.links links))

            (let [link (-> vis
                           (.selectAll ".link")
                           (.data links)
                           (.enter)
                           (.append "line")
                           (.attr {:class "link"})
                           (.style "stroke-width" 
                                   #(js/Math.sqrt %)))
                  node (-> vis 
                           (.selectAll ".node")
                           (.data nodes)
                           (.enter)
                           (.append "g"))
                  circle (-> node
                             (.append "circle")
                             (.on "click" #(vchain.routes/dispatch!
                                             (vchain.links/entity-link 
                                               {:ent_slug (vchain.slug/make-slug (.-name %))})))
                             (.attr {:class #(case (.-type %) 
                                               "ORG" "node-org"
                                               "PERSON" "node-person"
                                               "ASSET" "node-asset")
                                     :r #(case (.-type %) 
                                           "ORG" 30
                                           "PERSON" 15
                                           "ASSET" 15)}))
                  labeller (fn [d]
                             (this-as item
                                      (let [el (-> d3 (.select item)
                                                   (.append "text"))]
                                        (map (fn [line] (-> el
                                                            (.append "tspan")
                                                            (.text line)))
                                             (vchain.wrap/split-lines 10 (.-name d))))))
                  label (-> node
                            (.each labeller)
                            (.attr {:class "node-label"}))]

              (-> circle
                  (.append "title")    
                  (.text #(.-name %)))

              #_(.on force "tick"
                   (fn []
                     (-> link 
                         (.attr {:x1 #(.-x (.-source %))})
                         (.attr {:y1 #(.-y (.-source %))})
                         (.attr {:x2 #(.-x (.-target %))})
                         (.attr {:y2 #(.-y (.-target %))}))
                     (-> circle
                         (.attr {:cx #(.-x %)})
                         (.attr {:cy #(.-y %)}))
                     (-> label
                         (.attr {:x #(.-x %)})
                         (.attr {:y #(.-y %)}))))

            ; Run 100 times then stop
            (js/setTimeout 
              (fn []
                (.start force)
                (doall (repeatedly 100 #(.tick force)))
                (.stop force)
                (-> link 
                    (.attr {:x1 #(.-x (.-source %))})
                    (.attr {:y1 #(.-y (.-source %))})
                    (.attr {:x2 #(.-x (.-target %))})
                    (.attr {:y2 #(.-y (.-target %))}))
                (-> circle
                    (.attr {:cx #(.-x %)})
                    (.attr {:cy #(.-y %)}))
                (-> label
                    (.attr {:x #(.-x %)})
                    (.attr {:y #(.-y %)}))) 1))))))))


(defn full-view [app owner opts]
  (reify
    om/IRender
    (render [_]
      (om/build graph-view (get app :all-relations) {:opts opts}))))

