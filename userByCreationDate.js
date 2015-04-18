function objectIdWithTimestamp(timestamp) {
    // Convert string date to Date object (otherwise assume timestamp is a date)
    if (typeof(timestamp) == 'string') {
        timestamp = new Date(timestamp);
    }

    // Convert date object to hex seconds since Unix epoch
    var hexSeconds = Math.floor(timestamp/1000).toString(16);

    // Create an ObjectId with that hex timestamp
    var constructedObjectId = ObjectId(hexSeconds + "0000000000000000");

    return constructedObjectId
}

cursor = db.users.find({"_id":{$gt: objectIdWithTimestamp('2015-04-16')}});
 while(cursor.hasNext()){jsonObject = cursor.next(); print(jsonObject.username +" : "+ jsonObject.invoiceData.city + " : " + jsonObject._id.getTimestamp());}