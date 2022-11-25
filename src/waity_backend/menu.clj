(ns waity-backend.menu)


(def ^{:fx/autowire :fx/entity} menu-item
  [:spec {:table "menu_item"}
   [:id {:identity true} :serial]
   [:name :string]
   [:description :string]
   [:image :string]
   [:restaurant {:rel-type :many-to-one} :waity-backend.restaurant/restaurant]
   [:category {:rel-type :many-to-one} ::menu-category]])


(def ^{:fx/autowire :fx/entity} menu-category
  [:spec {:table "menu_category"}
   [:id {:identity true} :serial]
   [:name :string]
   [:image :string]
   [:restaurant {:rel-type :many-to-one} :waity-backend.restaurant/restaurant]])
