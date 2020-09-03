(ns todomvc.db)

(def initial
  {:todos (sorted-map)
   :showing :all})
