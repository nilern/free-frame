(ns todomvc.effects
  (:require [free-frame.core :as fr]
            [free-frame.malli :refer [state-checker]]
            [taksi.core :as t]
            [todomvc.db :as db]))

(def ^:private check-state (state-checker (::db/db db/registry) {:registry db/registry}))

(defn handle [app eff]
  (let [task (-> eff
                 check-state
                 (fr/run-app-db app)
                 fr/run-task)]
    (t/fork (fn [err] (throw (js/Error. err)))
            (fn [res] (assert (nil? res)))
            task)))
