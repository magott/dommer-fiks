package geo

import argonaut.DecodeJson
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject
import com.mongodb.casbah.query.Imports._


case class LatLong(lat:Double, long:Double) {
  def toMongo = MongoDBObject("lat" -> lat, "long" -> long)
}

object LatLong{
  def fromMongo(dbObject: DBObject) = LatLong(dbObject.as[Double]("lat"), dbObject.as[Double]("long"))

  implicit val clickEventDecoder: DecodeJson[LatLong] = DecodeJson(
    c => for {
      lat <- (c --\ "Lat").as[Double]
      long <- (c --\ "Long").as[Double]
    } yield {
      LatLong(lat, long)
    }
  )
}