(ns free-frame.malli
  (:require [free-frame.core :refer [app-db-label]]
            [cats.core :refer [extract]]
            [cats.monad.identity :as id]
            [fell.core :refer [weave pure]]
            [fell.eff :refer [Pure Impure]]
            [fell.state :as st]
            [malli.core :as m]
            [malli.error :as merr]))

(defn- state-checker [db-schema opts]
  (let [valid? (m/validator db-schema opts)
        explain (m/explainer db-schema opts)]
    (letfn [(resume [suspension] (run (extract suspension)))
            (run [eff]
              (condp instance? eff
                Pure eff
                Impure (let [^Impure eff eff
                             [request-label request] (.-request eff)]
                         (if (and (= request-label app-db-label)
                                  (instance? st/Set request))
                           (let [state* (.-new_value ^st/Set request)]
                             (if (valid? state*)
                               (weave eff (id/identity nil) resume)
                               (do (js/console.error
                                     (str "invalid db: " (merr/humanize (explain state*))))
                                   (pure nil))))
                           (weave eff (id/identity nil) resume)))))]
      run)))
