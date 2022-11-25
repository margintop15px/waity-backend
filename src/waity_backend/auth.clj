(ns waity-backend.auth)


(def ^{:fx/autowire :fx/entity} user
  [:spec {:table "user"}
   [:id {:identity true} :serial]
   [:name :string]
   [:role {:rel-type :many-to-one} ::role]])


(def ^{:fx/autowire :fx/entity} role
  [:spec {:table "role"}
   [:name {:identity true} :string]])


(def routes
  [["/login" ::login]
   ["/logout" ::logout]
   ["/signup" ::signup]])
