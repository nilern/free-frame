(ns todomvc.subs)

(def subscriptions
  {:showing (fn [db _] (:showing db))

   :sorted-todos (fn [db _] (:todos db))

   :todos (fn [db _] (vals (:todos db)))

   :visible-todos (fn [db _]
                    (let [filter-fn (case (:showing db)
                                      :active (complement :done)
                                      :done :done
                                      :all identity)]
                      (-> db :todos vals (filter filter-fn))))

   :all-complete? (fn [db _] (->> db :todos vals (every? :done)))})
