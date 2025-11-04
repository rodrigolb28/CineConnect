# CineConnect
An application for managing movie theaters.
Still in development


## How to run it

### Database
Create a .env file at the root of the project,

Set the database credentials using:
```
DB_USERNAME= //your database username goes here
DB_PASSWORD= //your database password goes here
```


To start the database, run:
```
sudo docker compose up -d 
```

*If upgrading from a previous commit,
delete any previous database tables to avoid migration conflicts
___

### Frontend
To start the React application, run:
```
cd front-end
#navigate to the frontend application folder
npm install
#install the necessary dependencies
npm run dev
#run the project 
```
___
### Backend
To start the spring boot application

Insert the database credentials set previously as environment variables
in your IDE of choice.

```
DB_USERNAME= //your database username goes here
DB_PASSWORD= //your database password goes here
```
This will allow the backend to connect to the database.

### Generating RSA Keys for JWT Encryption

To generate the public and private keys needed for JWT token encryption, run the following commands:

Linux / macOS:
```
# Navigate to the resources folder of the backend
cd back-end/src/main/resources

# Generate a private RSA key
openssl genrsa -out app.key 2048

# Generate the corresponding public key
openssl rsa -in app.key -pubout -out app.pub
```
- app.key:  your private key (keep this secure, do not commit it to Git).

- app.pub : the public key used by the backend to verify JWTs.

Make sure both files are in src/main/resources so Spring Boot can load them via your configuration.

Then run the CineConnectApplication.






