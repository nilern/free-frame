(ns todomvc.events
  (:require [cats.core :refer [mlet]]
            [fell.state :as st]))

(defn allocate-next-id
  "Returns the next todo id.
  Assumes todos are sorted.
  Returns one more than the current largest id."
  [todos]
  ((fnil inc 0) (last (keys todos))))

(def handlers
  {:add-todo (fn [[_ text]]
               (mlet [db st/get
                      :let [id (allocate-next-id (:todos db))]]
                 (st/set (assoc-in db [:todos id] {:id id :title text :done false}))))

   :toggle-done (fn [[_ id]]
                  (mlet [db st/get]
                    (st/set (update-in db [:todos id :done] not))))

   :delete-todo (fn [[_ id]]
                  (mlet [db st/get]
                    (st/set (update db :todos dissoc id))))

   :save (fn [[_ id title]]
           (mlet [db st/get]
             (st/set (assoc-in db [:todos id :title] title))))

   :complete-all-toggle (fn [_]
                          (mlet [{:keys [todos] :as db} st/get
                                 :let [done* (not-every? :done (vals todos))
                                       todos (reduce (fn [todos id] (assoc-in todos [id :done] done*))
                                                     todos
                                                     (keys todos))]]
                            (st/set (assoc db :todos todos))))

   :clear-completed (fn [_]
                      (mlet [{:keys [todos] :as db} st/get
                             :let [todos (transduce (comp (filter :done)
                                                          (map :id))
                                                    dissoc
                                                    todos
                                                    (vals todos))]]
                        (st/set (assoc db :todos todos))))})
