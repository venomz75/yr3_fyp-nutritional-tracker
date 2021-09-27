//ONLY CONFIGURED TO WORK ON LAN, THIS WEB SERVICE IS HOSTED ON PORT 3000 AND THE DATABASE IS HOSTED ON PORT 27017

var fs = require('fs');
const http = require('http');
const request = require("request")                  //npm install request
var MongoClient = require('mongodb').MongoClient;   //npm install mongodb
const dburl = "mongodb://192.168.0.35/nutrition/";  
const apiurl = "https://world.openfoodfacts.org/api/v0/product/"

function barcodeLookup (barcode) {  //Lookup OpenFoodFacts database using barcode. Returns a JSON file with product metadata.
  foodData = new Array()
  request({
    url: apiurl + barcode,
    method: "GET",
    json: true,
  }, function (error, response, body){
    console.log(body.status)
    if (body.status) {
      if (body.product.product_name != undefined) { //add food name
        foodData.push(body.product.product_name)
      } else {
        foodData.push(body.product.product_name_en)
      }  

      if (body.product.quantity != undefined) {
        foodData.push(body.product.nutriments["energy-kcal"] / (100 / parseInt(body.product.quantity.substring(0,body.product.quantity.indexOf(" ").toInt))))
      } else if (body.product.nutriments["energy-kcal"] != undefined){
        foodData.push(body.product.nutriments["energy-kcal"])
      } else {
        foodData.push(0)
      }
    } else return "0"
  })
  return foodData
}

function saveMealToDB(document) { //After the meal metadata is passed back to the app, it is reviewed by the user before being submitted to the meals collection.
  MongoClient.connect(dburl, function(err, db) {
    if (err) throw err;
    dbo = db.db("nutrition")
    additionDate = new Date()
    delete document.type
    document.date = new Date(additionDate.toISOString())
    dbo.collection("meals").insertOne(document, function(err, res) {
      if (err) throw err;
      console.log("Meal added!")
      db.close()
    })
  })
}

function saveWeightToDB(document) { //Takes the user's new weight, timestamps it and adds it to the weightlog collection.
  MongoClient.connect(dburl, function(err, db) {
    if (err) throw err;
    dbo = db.db("nutrition")
    additionDate = new Date()
    delete document.type
    document.date = new Date(additionDate.toISOString())
    dbo.collection("weightlog").insertOne(document, function(err, res) {
      if (err) throw err;
      console.log("Weight added!")
      db.close()
    })
  })
}

function getMealsFromDB() { //Retrieve meal entries already in database and send to user.
  object = {}
  MongoClient.connect(dburl, function(err, db) {
    if (err) throw err;
    dbo = db.db("nutrition")
    date = new Date()
    isodate = new Date(date.toISOString())
    
    dbo.collection("meals").find({}).toArray(function(err, result) {
      if (err) throw err
      console.log(result)
      for (i in result) {
        object[i] = result[i]
      }
      db.close()
    })
    
  })
  return object
}

function getWeightsFromDB() { //Retrieve weight history to display to user.
  object = {}
  MongoClient.connect(dburl, function(err, db) {
    if (err) throw err;
    dbo = db.db("nutrition")
    date = new Date()
    isodate = new Date(date.toISOString())
    console.log(isodate)
    dbo.collection("weightlog").find({}).toArray(function(err, result) {
      if (err) throw err
      console.log(result)
      for (i in result) {
        object[i] = result[i]
      }
      db.close()
    })
    
  })
  return object
}

function getRecomendation(pref) { //Recommend meal to user, implementation incomplete.
  console.log(pref)
  meal = ""
  MongoClient.connect(dburl, function(err, db) {
    if (err) throw err;
    dbo = db.db("nutrition")
    dbo.collection("recommendations").findOne({}, {type : pref}, function(err, result) {
      if (err) throw err
      console.log(result.meal)
      meal = '{"recommendation":"'+result.meal+'"}'
      db.close()
    })
  
  })
  return 
}


var server = http.createServer(function(request, response) {  //Request handling
  if (request.method == 'POST') {
    var body = ''
    request.on('data', function (data) {
        body += data
    })

    request.on('end', function () {
      try {
        console.log("BODY: "+body)
        var data = JSON.parse(body)
        switch(data.type) {
          case "barcode":
            result = barcodeLookup(data.barcode)
            setTimeout(function(){
              responseText = '{"name":"'+result[0]+'","calories":'+result[1]+'}'
            },2000)
            break;

          case "addmeal":
            saveMealToDB(data)
            responseText = '{"message":"Successfully saved '+data.name+'!"}'
            break;

          case "querymeals":
            results = getMealsFromDB()
            setTimeout(function(){  
              console.log(results)
              responseText = JSON.stringify(results)
              console.log(responseText)
            },2000)
            break;

          case "queryweights":
            results = getWeightsFromDB()
            setTimeout(function(){  
              console.log(results)
              responseText = JSON.stringify(results)
              console.log(responseText)
            },2000) 
            break;
            
          case "addweight":
            saveWeightToDB(data)
            responseText = '{"message":"Successfully saved weight!"}'
            break;

          case "getpref":
            pref = ""
            fs.readFile("pref.txt", "utf8", function(err, data) {
              if (err) console.log(err)
              console.log(data)
              pref = data
            })
            console.log(pref)
            setTimeout(function(){  
              responseText = '{"pref":"'+pref+'"}'
            },2000) 
            break;

          case "setpref":
            fs.writeFile("pref.txt", data.pref, (err) => {
              if (err) console.log(err)
              console.log("Updated user preferences.")
            })
            responseText = '{"message":"Successfully saved preferences."}'
            break;
          case "recommend":
            pref = ""
            fs.readFile("pref.txt", "utf8", function(err, data) {
              if (err) console.log(err)
              pref = data
            })
            meal = getRecomendation(pref)
            setTimeout(function(){ 
              responseText = meal
            },2000) 
          default:
            responseText = "0"
        }

        
        setTimeout(function(){
          console.log("SENDING:"+responseText)
          response.writeHead(200, {"Content-Type": "text/plain"})
          response.end(responseText)
        },2000)
        return
      }catch (err){
        console.log(err)
        response.writeHead(500, {"Content-Type": "text/plain"})
        response.write("Bad data sent.\n")
        response.end("Something went wrong. Check server console.")
        return
      }
    })
  }
})
server.listen(3000)
console.log("Server started.")