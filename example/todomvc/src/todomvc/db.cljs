(ns todomvc.db
  (:require [malli.core :as m]))

(def registry
  (merge (m/default-schemas)
         {::id int?
          ::title string?
          ::done boolean?

          ::todo [:map {:closed true}
                  [:id ::id]
                  [:title ::title]
                  [:done ::done]]

          ::todos [:map-of ::id ::todo]

          ::showing [:enum :all :active :done]

          ::db [:map [:todos ::todos] [:showing ::showing]]}))

(def initial
  {:todos (sorted-map)
   :showing :all})
