# Final year project: Monitoring food intake using a smartphone camera
### My final year project at university is a nutrition tracking app utilising the smartphone's camera. The system is comprised of 3 components:
* Front end (Android, Kotlin)
* Back end (NodeJS, PM2)
* Database (MongoDB)
* Food API (JSON)

![Architecture](/readme_img/architecture.png)

## Front end
Android application written in Kotlin which handles all of the user interaction as well as data inputs/outputs. The application communicates with the back end webservice to make database and API calls. A barcode scanner is used to scan the barcode which is sent to the API using to get the nutritional data.

![Barcode Scanner](/readme_img/barcode.jpeg)

## Back end
NodeJS web service that was hosted on a Raspberry Pi 3B+ (LAN only), which serves as the connection between the app, database and nutritional database API. PM2 was used to keep the process running indefinitely.

![NodeJS Console](/readme_img/node.png)

## Database
MongoDB was used due to it's BSON formatting which is similar to the JSON format the system uses to send information throughout. In addition, a document database is easier to set up and more flexible than a relational database.

![MongoDB Database in Robo3T](/readme_img/mongo.png)


## API
The API that was used to get nutritional data from a barcode is [OpenFoodFacts](https://world.openfoodfacts.org/). It provides this data in a JSON format, making it very easy to parse and send data between the various components of the system.

![OpenFoodFacts](/readme_img/open.png)
