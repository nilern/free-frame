(ns todomvc.core
  (:require [free-frame.core :as fr]
            [free-frame.application :as app]
            [fell.lift :as lift]
            [taksi.core :as t]
            [kissabussi.core :as k]
            [todomvc.db :as db]
            [todomvc.subs :as subs]
            [todomvc.views :as views]
            [todomvc.events :as events]))

(defn handle-effects [app eff]
  (->> (fr/run-app-db eff app)
       (lift/run k/context)
       (t/fork (fn [err] (throw (js/Error. err)))
               (fn [res] (assert (nil? res))))))

(defn render [app]
  (fr/render app [views/todo-app] (js/document.getElementById "app")))

(defn ^:dev/after-load start []
  (let [app (app/create db/initial subs/subscriptions events/handlers handle-effects)]
    (render app)))

(defn ^:export run []
  (start))
