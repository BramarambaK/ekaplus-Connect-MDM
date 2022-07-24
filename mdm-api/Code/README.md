

Build :

Execute the below command by navigating to the source code folder:

>> gradle build -x test

This will generate a build folder after successful build.


Run the Application :

Navigate to sourcefolder\build\libs and execute below command.

>> java -jar mdm-api-0.0.1-SNAPSHOT.jar

By default application will run in http://localhost:8080.

Change server port :

Server port can be changed by adding/editing the server.port configuration in src\main\resources\application.properties file.

server.port = 8080



Postman API Collection URL: https://www.getpostman.com/collections/cebc180d8188284c06bd