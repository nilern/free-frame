(ns todomvc.effects
  (:require [free-frame.core :as fr]
            [free-frame.malli :refer [state-checker]]
            [fell.lift :as lift]
            [taksi.core :as t]
            [kissabussi.core :as k]
            [todomvc.db :as db]))

(def ^:private check-state (state-checker (::db/db db/registry) {:registry db/registry}))

(defn handle [app eff]
  (let [task (->> (-> eff
                      check-state
                      (fr/run-app-db app))
                  (lift/run k/context))]
    (t/fork (fn [err] (throw (js/Error. err)))
            (fn [res] (assert (nil? res)))
            task)))
