(ns waity-backend.restaurant)


(def ^{:fx/autowire :fx/entity} restaurant
  [:spec {:table "restaurant"}
   [:id {:identity true} :serial]
   [:name :string]])


(def ^{:fx/autowire :fx/entity} restaurant-admin
  [:spec {:table    "restaurant_admin"
          :identity [:user :restaurant]}
   [:user {:wrap     true
           :rel-type :one-to-one} :waity-backend.auth/user]
   [:restaurant {:rel-type :one-to-one} ::restaurant]])


(def routes
  ["/restaurant" ::restaurant])
