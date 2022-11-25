(ns waity-backend.qr
  (:require
   [ring.util.http-response :as response]
   [clojure.java.io :as io])
  (:import
   [com.google.zxing.qrcode QRCodeWriter]
   [com.google.zxing BarcodeFormat]
   [com.google.zxing.client.j2se MatrixToImageWriter]
   [java.util.zip ZipOutputStream ZipEntry]
   [javax.imageio ImageIO]
   [java.awt.image BufferedImage]))


(def ^{:fx/autowire :fx/entity} qr-code
  [:spec {:table "qr_code"}
   [:id {:identity true} :serial]
   [:restaurant {:rel-type :many-to-one} :waity-backend.restaurant/restaurant]
   [:tables-count :integer]
   [:zip :string]])


(defn qr-link->image
  "Generate QR code image"
  ^BufferedImage [text & {:keys [w h] :or {w 200 h 200}}]
  (let [qr-writer  (new QRCodeWriter)
        bit-matrix (.encode qr-writer text BarcodeFormat/QR_CODE w h)]
    (MatrixToImageWriter/toBufferedImage bit-matrix)))


(defn generate-codes-zip
  "Produces zip archive with QR codes
   Returns a download link"
  [restaurant-id codes-count]
  (let [zip-file (format "tmp/%s/codes.zip" restaurant-id)]
    (io/make-parents zip-file)

    (with-open [zip (-> zip-file io/file io/output-stream ZipOutputStream.)]
      (dotimes [code-no codes-count]
        (let [qr-code-no (inc code-no)
              qr-link    (format "http://localhost/%s/%d" restaurant-id qr-code-no)
              qr-image   (qr-link->image qr-link)
              qr-file    (format "code-%d.png" qr-code-no)]
          (.putNextEntry zip (ZipEntry. ^String qr-file))
          (ImageIO/write qr-image "PNG" zip)
          (flush)
          (.closeEntry zip))))

    zip-file))


;; =============================================================================
;; Handlers
;; =============================================================================

(defn ^:fx/autowire ^:fx/wrap generate-codes
  [^::qr-code qr-code
   {{{:keys [restaurant-id]} :path
     {:keys [codes-count]}   :body} :parameters}]
  (let [zip-file-link (generate-codes-zip restaurant-id codes-count)
        {qr-id :id} (fx.repo/save! qr-code {:restaurant   restaurant-id
                                            :tables-count codes-count
                                            :zip          zip-file-link})
        download-link (format "/qr-code/%s/%s" restaurant-id qr-id)]
    (response/ok
     {:download-link download-link})))


(defn ^:fx/autowire ^:fx/wrap download-codes
  [^::qr-code qr-code
   {{{:keys [restaurant-id qr-code-id]} :path} :parameters}]
  (let [{:keys [zip]} (fx.repo/find! qr-code {:id qr-code-id :restaurant restaurant-id})]
    (-> (response/file-response zip)
        (response/content-type "application/zip")
        (response/header "Content-Disposition" "attachment; filename=\"codes.zip\""))))


;; =============================================================================
;; Routes
;; =============================================================================

(defn ^:fx/autowire routes
  [^::generate-codes generate-codes
   ^::download-codes download-codes]
  [["/qr-code/:restaurant-id"
    {:swagger {:tags ["QR codes"]}
     :name    :generate-qr-code
     :post    {:summary    "Generate QR code (one or multiple) for restaurant"
               :responses  {200 {:body {:download-link :string}}}
               :parameters {:path {:restaurant-id pos-int?}
                            :body {:codes-count pos-int?}}
               :handler    generate-codes}}]

   ["/qr-code/:restaurant-id/:qr-code-id"
    {:swagger {:tags ["QR codes"]}
     :name    :download-qr-code
     :get     {:summary    "Download QR codes zip file"
               :swagger    {:produces ["application/zip"]}
               :parameters {:path {:restaurant-id pos-int?
                                   :qr-code-id    pos-int?}}
               :handler    download-codes}}]])
