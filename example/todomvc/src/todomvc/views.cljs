(ns todomvc.views
  (:require [reagent.core :as rg]
            [free-frame.core :as fr :refer [defnc]]
            [clojure.string :as str]))

(defn todo-input [{:keys [title on-save on-stop]}]
  (let [val (rg/atom title)
        stop #(do (reset! val "")
                  (when on-stop (on-stop)))
        save #(let [v (-> @val str str/trim)]
                (on-save v)
                (stop))]
    (fn [props]
      [:input (merge (dissoc props :on-save :on-stop :title)
                     {:type "text"
                      :value @val
                      :auto-focus true
                      :on-blur save
                      :on-change #(reset! val (-> % .-target .-value))
                      :on-key-down #(case (.-which %)
                                      13 (save)
                                      27 (stop)
                                      nil)})])))

(defnc todo-item [app]
  (let [editing (rg/atom false)]
    (fn [{:keys [id done title]}]
      [:li {:class (str (when done "completed ")
                        (when @editing "editing"))}
       [:div.view
        [:input.toggle
         {:type "checkbox"
          :checked done
          :on-change #(fr/dispatch app [:toggle-done id])}]
        [:label
         {:on-double-click #(reset! editing true)}
         title]
        [:button.destroy
         {:on-click #(fr/dispatch app [:delete-todo id])}]]
       (when @editing
         [todo-input
          {:class "edit"
           :title title
           :on-save #(if (seq %)
                       (fr/dispatch app [:save id %])
                       (fr/dispatch app [:delete-todo id]))
           :on-stop #(reset! editing false)}])])))

(defnc task-list [app]
  (let [visible-todos @(fr/subscribe app [:visible-todos])
        all-complete? @(fr/subscribe app [:all-complete?])]
    [:section#main
     [:input#toggle-all
      {:type "checkbox"
       :checked all-complete?
       :on-change #(fr/dispatch app [:complete-all-toggle])}]
     [:label
      {:for "toggle-all"}
      "Mark all as complete"]
     [:ul#todo-list
      (for [todo visible-todos]
        ^{:key (:id todo)} [todo-item todo])]]))

(defnc task-entry [app]
  [:header#header
   [:h1 "todos"]
   [todo-input
    {:id "new-todo"
     :placeholder "What needs to be done?"
     :on-save #(when (seq %)
                 (fr/dispatch app [:add-todo %]))}]])

(defnc todo-app [app]
  [:div
   [:section#todoapp
    [task-entry]
    (when (seq @(fr/subscribe app [:todos]))
      [task-list])]])
