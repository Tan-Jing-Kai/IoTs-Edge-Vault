#include <ESP8266WiFi.h>
#include <ThingSpeak.h>
#include <ESP8266WebServer.h>
#include <Keypad.h> //Include keypad library
#include <Servo.h> //Include Servo library
#include <UniversalTelegramBot.h> // Include the Telegram Bot library
#include <WiFiClientSecure.h>

#include <time.h>
#include "secrets.h"
#include <PubSubClient.h>
#include <ArduinoJson.h>

#include <lcd_i2c.h> //Import lcd_i2c library

#define TIME_ZONE -5


unsigned long lastMessageTime = 0;
int messageCount = 0;
bool keypadEnabled = true;

// WiFi credentials
const char* ssid = "iPhone (6)"; 
const char* password = "felixisgay";
WiFiClient Thinkclient;

// ThingSpeak Credentials
unsigned long channelID = 2427545; // Your ThingSpeak channel ID
const char* apiKey = "458YXQCVS4IZQYH5"; // Your WriteAPIKey for the channel

// Telegram Bot Token
const char* BotToken = "6676496797:AAGxHYba8bS7cM_HRpqE3vtN3Zjk51FK8OY"; // Replace with your Bot token
const char* ChatID = "837961551"; // Replace with your Chat ID

WiFiClientSecure secured_client;

ESP8266WebServer server(80);

//AWS
unsigned long lastMillis = 0;
unsigned long previousMillis = 0;
const long interval = 5000;
 
#define AWS_IOT_PUBLISH_TOPIC   "esp8266/pub"
#define AWS_IOT_SUBSCRIBE_TOPIC "esp8266/sub"

WiFiClientSecure net;
 
BearSSL::X509List cert(cacert);
BearSSL::X509List client_crt(client_cert);
BearSSL::PrivateKey key(privkey);
X509List teleCert(TELEGRAM_CERTIFICATE_ROOT);
 
PubSubClient client(net);
UniversalTelegramBot bot(BotToken, net);

IPAddress ip;
 
time_t now;
time_t nowish = 1510592825;

#include <time.h> // Ensure this is included at the top of your file

#define TIME_ZONE -5 // Make sure this is set to your local time zone's offset from UTC

void NTPConnect(void) {
  Serial.print("Setting time using SNTP");
  configTime(TIME_ZONE * 3600, 0, "time.google.com", "0.pool.ntp.org", "1.pool.ntp.org");
  time_t now = time(nullptr);
  unsigned long startAttemptTime = millis();
  while (now < 24 * 3600) { // Simple 24 hour check to ensure valid time
    delay(500);
    Serial.print(".");
    now = time(nullptr);
    if (millis() - startAttemptTime > 15000) { // Timeout after 15 seconds
      Serial.println("Failed to obtain time.");
      return;
    }
  }
  Serial.println("done!");
  struct tm timeinfo;
  gmtime_r(&now, &timeinfo);
  Serial.print("Current time: ");
  Serial.println(asctime(&timeinfo));
}


void messageReceived(char *topic, byte *payload, unsigned int length)
{
  Serial.print("Received [");
  Serial.print(topic);
  Serial.print("]: ");
  for (int i = 0; i < length; i++)
  {
    Serial.print((char)payload[i]);
  }
  Serial.println();
}

void connectAWS()
{
  delay(3000);
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
 
  Serial.println(String("Attempting to connect to SSID: ") + String(WIFI_SSID));
 
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print(".");
    delay(1000);
  }
  
  Serial.print("WiFi Connected...");
 
  NTPConnect();
 
  net.setTrustAnchors(&cert);
  net.setClientRSACert(&client_crt, &key);
 
  client.setServer(MQTT_HOST, 8883);
  client.setCallback(messageReceived);
 
 
  Serial.println("Connecting to AWS IOT");
 
  while (!client.connect(THINGNAME))
  {
    Serial.print(".");
    delay(1000);
  }
 
  if (!client.connected()) {
    Serial.println("AWS IoT Timeout!");
    return;
  }
  // Subscribe to a topic
  client.subscribe(AWS_IOT_SUBSCRIBE_TOPIC);
 
  Serial.println("AWS IoT Connected!");
}

void publishMessage()
{
  /*
  StaticJsonDocument<200> doc;
  doc["time"] = millis();
  doc["ip address"] = ip;
  char jsonBuffer[512];
  serializeJson(doc, jsonBuffer); // print to client
 
  client.publish(AWS_IOT_PUBLISH_TOPIC, jsonBuffer);
*/
  //client.publish(AWS_IOT_PUBLISH_TOPIC, ip.toString().c_str());

  StaticJsonDocument<200> doc;
  doc["IP Address"] = ip.toString().c_str();
  char jsonBuffer[512];
  serializeJson(doc, jsonBuffer); // print to client
 
  client.publish(AWS_IOT_PUBLISH_TOPIC, jsonBuffer);

}

  unsigned long currentTime = millis();
  unsigned long timeDifference = currentTime - lastMessageTime;

  int checkMessageLimit() {
  // Check if 4 hours have passed since the last message
  if (timeDifference >= 4 * 60 * 60*  1000) {
    // Reset message count if more than 4 hours have passed
    messageCount = 0;
  }

  // Check if the message count has reached the limit
  if (messageCount >= 5) {
    return 1; // Limit reached
  } else {
    return 0; // Limit not reached
  }
}


// Keypad setup
#define ROW_NUM 4
#define COLUMN_NUM 3

// Servo setup
Servo myservo;
int initialPosition = 0; // Initial position of the servo

char keys[ROW_NUM][COLUMN_NUM] = {
  {'1','2','3'},
  {'4','5','6'},
  {'7','8','9'},
  {'*','0','#'}
};

byte pin_rows[ROW_NUM] = {D2, D3, D4, D5}; // Connect to the row pins
byte pin_column[COLUMN_NUM] = {D6, D7, D1}; // Connect to the column pins

Keypad keypad = Keypad(makeKeymap(keys), pin_rows, pin_column, ROW_NUM, COLUMN_NUM);

// Password for the keypad
const String keypadPassword = "6921"; // Change your password here
String input_password;

// Attempt counter
int attemptCount = 0;

lcd_i2c lcd(0x3E,16,2); //The I2C communication for this LCD is 0x3E (HEX) or 62 (DEC), 16 is the number of the columns, 2 is the number of the rows

unsigned long startTime = 0; // Stores the start time
const unsigned long limitinterval = 300000; // Interval at which to print message (60 seconds)

void setup() {
  Serial.begin(9600);
  input_password.reserve(32); // Reserve memory for input password
  myservo.attach(D0); // Attaches the servo on pin D0 to the servo object
  myservo.write(initialPosition); // Set the servo to the initial position
  delay(1000);

  // WiFi setup
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("WiFi connected");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());
  ip = WiFi.localIP();
  // Server setup
  server.on("/data", handleData);
  server.begin();
  Serial.println("Server started");


  ThingSpeak.begin(Thinkclient); // Initialize ThingSpeak

  lcd.begin(); //LCD initialization function  
  lcd.setCursor(0, 0); //cursor setting function start with column followed by row
  lcd.print("Edge Vault"); //Print text
  //AWS Setup
  connectAWS();
  AWSpublishmsg();

  startTime = millis(); // Initialize start time

  // Telegram setup
  secured_client.setInsecure(); // Use insecure connection (not recommended for production)
  configTime(0, 0, "pool.ntp.org");
}

void moveServo() {
  myservo.write(90); // Move the servo to 180 degrees
  delay(500);
  Serial.println("motor open");
}

void handleData() {
  if (server.hasArg("value")) {
    String value = server.arg("value");
    Serial.print("Received value: ");
    Serial.println(value);
/*
    if (checkMessageLimit() == 1) {
      Serial.println("Message limit reached, keypad disabled.");
      keypadEnabled = false; // Disable the keypad
      return; // Exit the function if message limit reached
    }
*/
    if (value == "trigger_motor") {
      moveServo();
      keypadEnabled = true;
    }
    if (value == "trigger_motor" && keypadEnabled == 0){
      timeDifference = 0;
    }
  }
  server.send(200, "text/plain", "Received");
}



void sendTelegramMessage() {
   if (checkMessageLimit() == 0) {
    net.setTrustAnchors(&teleCert);
  Serial.println("Sending message...");
  bool result = bot.sendMessage(ChatID, "Alert: 3 incorrect password attempts!", "");
    if (result) {
    Serial.println("Telegram message sent successfully");
   } else {
    Serial.println("Failed to send Telegram message");
  }
    messageCount++;
    lastMessageTime = millis();
  }
  else {
    Serial.println("Message limit reached, cannot send more messages.");
    keypadEnabled = false;
  }
}



void handleKeypadInput() {
  // Handle keypad input only if keypad is enabled
  char key = keypad.getKey();
  if (key) {
    Serial.println(key);
    
    if (key == '*') {
      //input_password = ""; // Clear input password
      myservo.write(initialPosition); // Move the servo back to the initial position
      ThingSpeak.setField(1, "0");
      int x = ThingSpeak.writeFields(channelID, apiKey);
      delay(500);
      Serial.println("motor closed");
    } else if (key == '#') {
      if (keypadPassword == input_password && keypadEnabled == 1) {
        Serial.println("Password is correct");
        moveServo(); //move servo
        attemptCount = 0; // Reset attempt counter
        
        ThingSpeak.setField(1, "1");
        int x = ThingSpeak.writeFields(channelID, apiKey);

        if (x == 200) {
          Serial.println("Channel update successful.");
        } else {
          Serial.println("Problem updating channel. HTTP error code " + String(x));
        }

        if (checkMessageLimit() == 1) {
        Serial.println("Message limit reached, keypad disabled.");
        keypadEnabled = false; // Disable the keypad
        return; // Exit the function if message limit reached
        }
      } 
      if (keypadEnabled == 0){
        Serial.println("Enter from mobile application");
      }
      if (keypadEnabled == 1 && keypadPassword != input_password){
        Serial.println("Password is incorrect, try again");
        attemptCount++;
        if (attemptCount >= 3) {
          net.setTrustAnchors(&teleCert);
          sendTelegramMessage(); //call tele function
          attemptCount = 0; // Reset attempt count after sending message
        }
      }
      input_password = ""; // Clear input password
    } else {
      input_password += key; // Append new character to input password string
    }
  }
}




void AWSpublishmsg() {
  //ip = Serial.println(WiFi.localIP());
  now = time(nullptr);
    if (!client.connected())
  {
    connectAWS();
  }
  else
  {
    client.loop();
    if (millis() - lastMillis > 5000)
    {
      lastMillis = millis();
      publishMessage();
      Serial.println("Message published to AWS.");
    }
  }
}



void timelimit() {
   if (millis() - startTime >= limitinterval) { // Check if 60 seconds have passed
      Serial.println("Time limit of 30 mins reached, motor closed."); // Print "Hello World" to the Serial Monitor
      myservo.write(initialPosition); // Move the servo back to the initial position
      startTime = millis(); // Reset the start time
  }
}


void loop() {
  server.handleClient();
  handleKeypadInput();
  //AWSpublishmsg();
  timelimit();
}