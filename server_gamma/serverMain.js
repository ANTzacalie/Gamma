const sqlite3 = require("sqlite3").verbose();
const nodemailer = require('nodemailer');
const fs = require("fs");
const randomInt = require("crypto");
const https = require("https");
const express = require("express");
const bodyParser = require("body-parser");
const socketIO = require("socket.io");
const { boolean } = require("webidl-conversions");

let db_ok = false;
const dbPath = "DataBaseSQLite.db"
const db = new sqlite3.Database(dbPath, sqlite3.OPEN_READWRITE, (error) => { //creating the instance of the database 

    if (error) {

        console.error('Error connecting to SQLite database:', error.message); db_ok = false;
        return;

    }
    console.log('Connected to SQLite database'); db_ok = true;

});

/*

    --- IMPLEMENT LOGIC TO READ CONFIG.SRV

*/

let PORT = ;
let HOSTNAME = ;
let PASSWORD_SRV = ;/**/
let EMAIL = ;
let EMAIL_KEY = ;
let CHAIN_PATH = ;
let CERTIFICATE_PATH = ;
let PRIVATE_KEY_PATH = ;


if (db_ok) {

    console.error("DATABASE IS OPERATIONAL, STARTING SERVER..........");

} else {

    console.log("DATABASE ONLINE, SERVER IS STARTING TS: " + currentDate.toLocaleString());

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
function getSocketId(email, callback) {

    query = "SELECT SOCKET_ID FROM USERS WHERE EMAIL = ?";

    try {

        db.all(query, [email], function (error, result) {

            if (error) {

                console.log("ERROR RETURNED BY DATABASE ON FUNCTION getSocketID with error:" + error.message);
                callback(false);

            }
            else {

                if (result.length > 0) {

                    console.log("SOCKET_ID FOUND, RETRIVED ID: " + result[0].SOCKET_ID);
                    callback(result[0].SOCKET_ID);

                }
                else {

                    console.log("UNABLE TO GET SOCKET_ID FROM DATABASE, NONE HAS BEEN FOUND, REPORT ON USER: " + email);
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
function registerSocketId(email, socketId) {

    let query = "UPDATE USERS SET SOCKET_ID = ? WHERE EMAIL = ?";

    try {

        db.run(query, [socketId, email], function (error) {

            if (error) {

                console.log("SOCKET_ID NOT BOUND FOR USER: " + email + " WITH ERROR " + error.message);

            }
            else {

                console.log("SOCKET_ID BOUND TO USER");

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
function changeUsername(email, newUsername, callback) {}

//Verify the input code with the one in db
function verifyCode(email, code, callback) {

    const currentTime = currentDate.toLocaleString();

}

//Register code for a specific email
function registerCode(email, callback) {}

//The main method of auth after login 
function authServer(secure_code, email, callback) {}

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

                console.log("ON_CONNECT:: USER " + senderEmail + " HAS CONNECTED TO SERVER AT TIME: " + currentDate.toLocaleString());
                registerSocketId(senderEmail, socket_id);

            }

        });

    });

    ioRoute.on("on_disconnect", (data) => {

        const { senderEmail, serverAccessCode } = data;

        authServer(serverAccessCode, senderEmail, (valid) => {

            if (valid) {

                console.log("ON_DISCONNECT:: USER " + senderEmail + " HAS DISCONNECTED FROM THE SERVER AT TIME: " + currentDate.toLocaleString());

            } else {

                console.log("ON_DISCONNECT:: USER " + senderEmail + " WARNING , UNAUTORIZED USER DETECTED! , TIME: " + currentDate.toLocaleString());

            }

        });

    });

    // SERVER START LOGIC
    const port = parseInt(PORT); //portul care il vom folosi;
    const hostname = HOSTNAME; //numele domeniului(daca avem unul) sau adresa IPV4;

    server.listen(port, hostname, () => {

        console.log("SERVER IS RUNNING on https://" + hostname + ":" + port);

    });

});

}

//
// TODO: 
//
// -REDO ALL FUNCTIONS USING ASYNC
// -ALL USERS HAVE A TEMPORAL SPACE OF UP TO 2GB AT A TIME
// -ALL TRANSACTIONS BETWEEN USERS CANNOT TAKE MORE THAN 3HOURS IN TRANSFER
// -SERVER WILL BE STARTED VERY SIMPLY, ALL PARAMS BE STORED IN CONFIG.SRV
//


// Server Ver: 1.73A
// dev: M.C.A

