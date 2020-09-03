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
                 (st/set (assoc-in db [:todos id] {:id id :title text :done false}))))})
