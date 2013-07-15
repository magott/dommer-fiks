package geo

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject
import com.mongodb.casbah.query.Imports._


case class LatLong(lat:Double, long:Double) {
  def toMongo = MongoDBObject("lat" -> lat, "long" -> long)
}

object LatLong{
  def fromMongo(dbObject: DBObject) = LatLong(dbObject.as[Double]("lat"), dbObject.as[Double]("long"))
}