(ns todomvc.core
  (:require [free-frame.core :as fr]
            [free-frame.application :as app]
            [free-frame.malli :refer [state-checker]]
            [reagent.dom :as rdom]
            [fell.lift :as lift]
            [taksi.core :as t]
            [kissabussi.core :as k]
            [todomvc.db :as db]
            [todomvc.subs :as subs]
            [todomvc.views :as views]
            [todomvc.events :as events]))

(def ^:private check-state (state-checker (::db/db db/registry) {:registry db/registry}))

(defn handle-effects [app eff]
  (->> (-> eff
           check-state
           (fr/run-app-db app))
       (lift/run k/context)
       (t/fork (fn [err] (throw (js/Error. err)))
               (fn [res] (assert (nil? res))))))

(defn render [app]
  (let [app-node (js/document.getElementById "app")]
    (rdom/unmount-component-at-node app-node)
    (fr/render app [views/todo-app] app-node)))

(defn ^:dev/after-load start []
  (let [app (app/create db/initial subs/subscriptions events/handlers handle-effects)]
    (render app)))

(defn ^:export run []
  (start))
