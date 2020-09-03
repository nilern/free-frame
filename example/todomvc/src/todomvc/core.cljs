(ns todomvc.core
  (:require [free-frame.core :as fr]
            [free-frame.application :as app]
            [reagent.dom :as rdom]
            [todomvc.db :as db]
            [todomvc.subs :as subs]
            [todomvc.views :as views]
            [todomvc.events :as events]
            [todomvc.effects :as effects]))

(defn render [app]
  (let [app-node (js/document.getElementById "app")]
    (rdom/unmount-component-at-node app-node)
    (fr/render app [views/todo-app] app-node)))

(defn ^:dev/after-load start []
  (let [app (app/create db/initial subs/subscriptions events/handlers effects/handle)]
    (render app)))

(defn ^:export run []
  (start))
