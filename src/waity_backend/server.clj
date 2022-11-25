(ns waity-backend.server
  (:require
   [waity-backend.auth :as auth]
   [waity-backend.restaurant :as restaurant]
   [ring.adapter.jetty :as jetty]
   [muuntaja.core :as m]
   [reitit.coercion.malli]
   [reitit.dev.pretty :as pretty]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui])
  (:import
   [org.eclipse.jetty.server Server]))


(def routes-data
  {:exception pretty/exception
   :data
   {:coercion   reitit.coercion.malli/coercion
    :muuntaja   m/instance
    :middleware [;; swagger feature
                 swagger/swagger-feature
                 ;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}})


(defn ^:fx/autowire app
  [^:waity-backend.qr/routes qr-routes]
  (ring/ring-handler
   (ring/router
    [["/swagger.json"
      {:get {:no-doc  true
             :swagger {:info {:title "Waity API"}}
             :handler (swagger/create-swagger-handler)}}]

     ["/api"
      auth/routes
      restaurant/routes
      qr-routes]]
    routes-data)
   (ring/routes
    (swagger-ui/create-swagger-ui-handler {:path "/"})
    (ring/create-default-handler))))


(defn ^:fx/autowire jetty
  "Run Jetty server"
  [^::app app {:keys [port join?]}]
  (println "server running on port" port)
  (jetty/run-jetty app {:port port :join? join?}))


(defn stop-jetty
  {:fx/autowire true
   :fx/halt     ::jetty}
  [^Server server]
  (.stop server))

