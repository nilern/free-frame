(ns todomvc.events
  (:require [cats.core :refer [mlet]]
            [free-frame.core :refer [get-app-db set-app-db update-app-db]]))

(defn allocate-next-id
  "Returns the next todo id.
  Assumes todos are sorted.
  Returns one more than the current largest id."
  [todos]
  ((fnil inc 0) (last (keys todos))))

(def handlers
  {:set-showing (fn [[_ showing]] (update-app-db assoc :showing showing))
   :add-todo (fn [[_ text]]
               (mlet [db get-app-db
                      :let [id (allocate-next-id (:todos db))]]
                 (set-app-db (assoc-in db [:todos id] {:id id :title text :done false}))))

   :toggle-done (fn [[_ id]] (update-app-db update-in [:todos id :done] not))

   :delete-todo (fn [[_ id]] (update-app-db update :todos dissoc id))

   :save (fn [[_ id title]] (update-app-db assoc-in [:todos id :title] title))

   :complete-all-toggle (fn [_]
                          (mlet [{:keys [todos] :as db} get-app-db
                                 :let [done* (not-every? :done (vals todos))
                                       todos (reduce (fn [todos id] (assoc-in todos [id :done] done*))
                                                     todos
                                                     (keys todos))]]
                            (set-app-db (assoc db :todos todos))))

   :clear-completed (fn [_]
                      (mlet [{:keys [todos] :as db} get-app-db
                             :let [todos (transduce (comp (filter :done)
                                                          (map :id))
                                                    dissoc
                                                    todos
                                                    (vals todos))]]
                        (set-app-db (assoc db :todos todos))))})
