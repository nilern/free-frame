(ns todomvc.core
  (:require [free-frame.core :as fr]
            [free-frame.application :as app]
            [cats.builtin]                                  ; to make [] a Functor
            [fell.core :refer [pure weave]]
            [fell.eff :refer [Pure Impure]]
            [fell.state :refer [Set]]
            [fell.lift :as lift]
            [taksi.core :as t]
            [kissabussi.core :as k]
            [malli.core :as m]
            [malli.error :as merr]
            [todomvc.db :as db]
            [todomvc.subs :as subs]
            [todomvc.views :as views]
            [todomvc.events :as events]))

(defn- state-checker [db-schema opts]
  (let [valid? (m/validator db-schema opts)
        explain (m/explainer db-schema opts)]
    (letfn [(resume [suspension] (run (first suspension)))
            (run [eff]
              (condp instance? eff
                Pure eff
                Impure (let [^Impure eff eff
                             request (.-request eff)]
                         (if (instance? Set request)
                           (let [state* (.-new_value ^Set request)]
                             (if (valid? state*)
                               (weave eff [nil] resume)
                               (do (js/console.error (str "invalid db: " (merr/humanize (explain state*))))
                                   (pure nil))))
                           (weave eff [nil] resume)))))]
      run)))

(defn effect-handler [check-state]
  (fn [app eff]
    (->> (-> eff
             check-state
             (fr/run-app-db app))
         (lift/run k/context)
         (t/fork (fn [err] (throw (js/Error. err)))
                 (fn [res] (assert (nil? res)))))))

(defn render [app]
  (fr/render app [views/todo-app] (js/document.getElementById "app")))

(defn ^:dev/after-load start []
  (let [check-state (state-checker (::db/db db/registry) {:registry db/registry})
        handle-effects (effect-handler check-state)
        app (app/create db/initial subs/subscriptions events/handlers handle-effects)]
    (render app)))

(defn ^:export run []
  (start))
