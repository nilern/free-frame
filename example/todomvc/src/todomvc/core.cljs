(ns todomvc.core
  (:require [free-frame.core :as fr]
            [free-frame.application :as app]
            [reagent.core :as rg]
            [reagent.dom :as rdom]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.controllers :as rfc]
            [todomvc.db :as db]
            [todomvc.subs :as subs]
            [todomvc.views :as views]
            [todomvc.events :as events]
            [todomvc.effects :as effects]))

(defn create-app [] (app/create db/initial subs/subscriptions events/handlers effects/handle))

(defn render [app] (fr/render app [views/todo-app] (js/document.getElementById "app")))

(defn ^:export run []
  (let [app (create-app)
        routes ["/"
                [""
                 {:name :root
                  :controllers [{:start (fn [_] (fr/dispatch app [:set-showing :all]))}]}]
                [":filter"
                 {:name :filter
                  :controllers [{:parameters {:path [:filter]}
                                 :start (fn [{:keys [path]}]
                                          (fr/dispatch app [:set-showing (keyword (:filter path))]))}]}]]
        router (rf/router routes)
        match (rg/atom nil)]
    (rfe/start! router
                (fn [new-match]
                  (swap! match (fn [old-match]
                                 (when new-match
                                   (let [new-controllers (rfc/apply-controllers (:controllers old-match) new-match)]
                                     (assoc new-match :controllers new-controllers))))))
                {:use-fragment true})
    (render app)))

(defn ^:dev/after-load reset []
  (rdom/unmount-component-at-node (js/document.getElementById "app"))
  (run))
