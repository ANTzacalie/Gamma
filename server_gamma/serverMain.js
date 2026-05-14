const sqlite3 = require("sqlite3").verbose();
const nodemailer = require('nodemailer');
const fs = require("fs");
const randomInt = require("crypto");
const https = require("https");
const express = require("express");
const bodyParser = require("body-parser");
const socketIO = require("socket.io")

let readData = [null, null, null, null, null, null, null];
const dbPath = "DataBaseSQLite.db"// DATA_BASE NAME AND LOCATION
const db = new sqlite3.Database(dbPath, sqlite3.OPEN_READWRITE, (error) => { //creating the instance of the database 

    if (error) {

        console.error('Error connecting to SQLite database:', error.message);
        return;

    }
    console.log('Connected to SQLite database');

});

const readQuery = "SELECT PRIVATE_PATH , CERTIFICATE_PATH , CHAIN_PATH, EMAIL , PASSWORD, HOSTNAME, PORT FROM INPUT WHERE Id = 1";
try {

    db.all(readQuery, (error, result) => {

        if (error) {

            console.error("Error reading values from database table INPUT" + error.message);

        } else {

            console.log("All data form table INPUT have been read");

            readData[0] = result[0].PRIVATE_PATH;
            readData[1] = result[0].CERTIFICATE_PATH;
            readData[2] = result[0].CHAIN_PATH;
            readData[3] = result[0].EMAIL;
            readData[4] = result[0].PASSWORD;
            readData[5] = result[0].HOSTNAME;
            readData[6] = result[0].PORT;


            const app = express() //Here we initialize the application with Express for HTTPS Request
            app.use(bodyParser.json()); //JSON int

            const server = https.createServer({

                key: fs.readFileSync(readData[0]),
                cert: fs.readFileSync(readData[1]),
                ca: fs.readFileSync(readData[2]),

            }, app);

            const startSocketIo = socketIO(server , { maxHttpBufferSize: 1e8 } ); //Here we initialize Socketio and specify maxHttpBuffer to 100mb
            const currentDate = new Date(); //Creating an instance of the day/month/year

            function emailSender(email, code) {

                //Create a transporter with your SMTP configuration
                const transporter = nodemailer.createTransport({

                    service: 'gmail',
                    auth: {

                        user: readData[3],
                        pass: readData[4]

                    }

                });

                // Compose the email
                const mailOptions = {

                    from: readData[3],
                    to: email,
                    subject: 'Verification code',
                    text: 'Your code is:' + code

                };

                // Send the email
                transporter.sendMail(mailOptions, function (error, info) {

                    if (error) {

                        console.error('Error sending email:', error);

                    } else {

                        console.log('Email sent:', info.response);

                    }

                });

            }
            
            // Random string generator
            function codeGenerator(lenght) {

                const characters = 'abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ0123456789';

                let password = '';

                for (let i = 0; i < lenght; i++) {

                    const randomIndex = randomInt.randomInt(characters.length);
                    password += characters.charAt(randomIndex);

                }

                console.log("Code generated is: " + password);

                return password;

            }
            
            //Finds the email in db
            function emailFinder(email, callback) {

                
            }

            //Registers account into db
            function registerAccount(email, password, username) {

            

            }
            
            //Login
            function authEmail(email, password, callback) {

                

            }

            //Get socketId from db
            function getSocketId(username, callback) {

                query = "SELECT SOCKET_ID FROM USERS WHERE USERNAME = ?";

                try {

                    db.all(query, [email], function (error, result) {

                        if (error) {

                            console.log("GET SOCKET ID: " + error.message);
                            callback(false);

                        }
                        else {

                            if (result.length > 0) {

                                console.log("GET SOCKET ID , RETRIVED ID: " + result[0].SOCKET_ID);
                                callback(result[0].SOCKET_ID);

                            }
                            else {

                                console.log("GET SOCKET ID: NO ID PRESENT");
                                callback(false);

                            }

                        }

                    });

                } catch (error) {

                    console.log(error);
                    callback(false)

                }

            }

            //Registers the socketId to db
            function registerSocketId(username, socketId) {

                let query = "UPDATE USERS SET SOCKET_ID = ? WHERE USERNAME = ?";

                try {

                    db.run(query, [socketId, email], function (error) {

                        if (error) {

                            console.log("ADD USERS SOCKET_ID FAILED FOR USER: " + email + " WITH ERROR " + error.message);

                        }
                        else {

                            console.log("ADD USER SOCKET_ID WORKING!");

                        }

                    });

                } catch (error) {

                    console.log(error);

                }

            }

            //Modify password field for a specific email
            function changePassword(email, newPassword, callback) {

                let query = "UPDATE USERS SET PASSWORD = ? WHERE EMAIL = ?";




            }

            //Modify Username field for a specific email
            function changeUsername(email, newUsername, callback) {

                

            }

            //Verify the input code with the one in db
            function verifyCode(email, code, callback) {

                const currentTime = currentDate.toLocaleString();


            }

            //Register code for a specific email
            function registerCode(email, callback) {

               

            }

            //The main method of auth after login 
            function authServer(secure_code, email, callback) { // TODO: ADD USER PASSWORD AS A DOUBLE MESURE

                

            }

            //FUN 1 -->> LOGIN
            app.post('/login', async (request, response) => {

                const { email, password } = request.body;

                

            });

            // FUN4 -->> SIGN UP
            app.post('/signup', async (request, response) => {

                const { username, email, password } = request.body;

                

            });

            // FUN5 -->> CODE_VERIFICATION
            app.post('/codeVerify', async (request, response) => {

                const { code, email } = request.body;

                

            });

            // FUN7 -->> CHANGE_USERNAME
            app.post('/changeUsername', async (request, response) => {

                const { email, newUsername, serverAccessCode } = request.body;

               

            });

            // FUN8 -->> CHANGE_PASSWORD 1
            app.post('/changePassword', async (request, response) => {

                const { email } = request.body;

                

            });

            // FUN -->> CHANGE_PASSWORD 2
            app.post('/changePassword2', async (request, response) => {

                const { email, newPassword, code } = request.body;

                

            });

            startSocketIo.on('connection', (ioRoute) => {

                // REGISTERS THE SOCKET ID
                ioRoute.on("on_connect", (data) => {

                    const { senderEmail, serverAccessCode } = data;
                    const socket_id = ioRoute.id

                    authServer(serverAccessCode, senderEmail, (valid) => {

                        if (valid) {

                            console.log("ON_CONNECT:: USER " + senderEmail + " HAS CONNECTED TO SERVER AT TIME: " /* ADD TIME HERE */)
                            registerSocketId(senderEmail, socket_id);

                        }

                    });

                });

                ioRoute.on("on_disconnect", (data) => {

                    const { senderEmail, serverAccessCode } = data;

                    authServer(serverAccessCode, senderEmail, (valid) => {

                        if (valid) {

                            console.log("ON_DISCONNECT:: USER " + senderEmail + " HAS DISCONNECTED FROM THE SERVER AT TIME: " /* ADD TIME HERE */)

                        } else {

                            console.log("ON_DISCONNECT:: USER " + senderEmail + " WARNING , UNAUTORIZED USER DETECTED! , TIME: " /* ADD TIME HERE */)

                        }

                    });

                });

               
                /*
                // Static function, only for testing purposes
                ioRoute.on("file", (data) => {
                    
                    const { someEncodedFile , fileMime } = data;
                    
                    console.log("FILE STREAM USED!");

                    getSocketId("antoniomihalceacatalin43@gmail.com" , (resultId) => {

                        if(resultId) {

                            // There is no need for decoding as there is no use of the file inside the server
                            ioRoute.volatile.to(resultId).emit("FILE_STREAM_RECEIVER", { "someEncodedFile": someEncodedFile , "fileMime": fileMime })
                            console.log("FILE STREAM: A file was sent to user(static)")

                        }

                    })

                });
                */



            });



            // SERVER START LOGIC
            const port = parseInt(readData[6]); //portul care il vom folosi;
            const hostname = readData[5]; //numele domeniului(daca avem unul) sau adresa IPV4;

            server.listen(port, hostname, () => {

                console.log("SERVER IS RUNNING on https://" + hostname + ":" + port);

            });


        }

    });

} catch (error) {

    console.error("Error reading values from database table INPUT" + error.message);

}


// Todo: .Net App finished, binded to the server too.



// Server Ver: 1.72A
// todo: add maximum users, so admin can set a maximum
// todo: add email sender chnage , so we can send emails on other type of EMIAL domains

// dev: M.C.A

let storageActivityA = ["statusMessage", "statusRequest", "request_received", "request_accepted_received", "request_denied_received", "arb_standby", "request_block_received"];
let storageActivityB = ["imOnline", "stb_messages", "stb_request", "stb_arb_standby", "stb_arb"];






























/*DACA UITI JS DB OPERATIONS , IMBECILULE !


pentru SELECT se foloseste db.all(query , etc , function(error , result){
        result este returnat ca array , nu ca vector(un fel de vector) , asa ca citim cu result[0].numeColoana(NUMELE COLOANEI DIN SELECT)
});

pentru orice query de tipul WRITE DB se foloseste db.run(query , etc(informatie si ce din query cu ?), function(error){
        //asta nu inseamna ca nu poate avea callback !!!
});

*/  