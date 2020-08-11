(ns free-frame.core)

(defmacro defnc [& args]
  (let [params-index (->> args
                          (map-indexed (fn [i v] (if (vector? v) i nil)))
                          (some identity))]
    (assert (and params-index (< 0 params-index 3)))
    (let [[app-param & params] (nth args params-index)]
      `(defn ~@(take params-index args) [~@params]
         (let [~app-param (free-frame.core/useApplication)]
           ~@(drop (inc params-index) args))))))
