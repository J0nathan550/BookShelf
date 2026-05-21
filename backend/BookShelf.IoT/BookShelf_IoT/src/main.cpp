#include <Arduino.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <LiquidCrystal_I2C.h>
#include <Keypad.h>
#include <ArduinoJson.h>

const char* ssid = "Wokwi-GUEST";
const char* password = "";

String baseUrl = "http://8664f6be459a.ngrok-free.app";

const char* iotUserEmail = "kiosk@bookshelf.com"; 
const char* iotUserPassword = "_JEJ5TrbdF";

struct UserAccount {
  String email;
  String password;
  String fullName;
};

UserAccount users[] = {
  {"ihudokormov@gmail.com", "_JEJ5TrbdF", "Jonathan Soderstorm"},
};
const int numUsers = 1;

String iotJwtToken = "";
String currentUserToken = "";
String currentUserId = "";
String currentUserName = "";
bool userLoggedIn = false;

// --- INACTIVITY TIMEOUT ---
const unsigned long INACTIVITY_TIMEOUT = 30000;
unsigned long lastActivityTime = 0;

// --- PERIPHERALS ---
LiquidCrystal_I2C lcd(0x27, 16, 2);

const byte ROWS = 4; 
const byte COLS = 4; 
char hexaKeys[ROWS][COLS] = {
  {'1', '2', '3', 'A'}, 
  {'4', '5', '6', 'B'}, 
  {'7', '8', '9', 'C'}, 
  {'*', '0', '#', 'D'}
};
byte rowPins[ROWS] = {13, 12, 14, 27}; 
byte colPins[COLS] = {26, 25, 33, 32}; 

Keypad customKeypad = Keypad(makeKeymap(hexaKeys), rowPins, colPins, ROWS, COLS);

String inputBuffer = "";
bool isLoginMode = false;

// Forward Declarations
void showMainMenu();
void showError(String msg);
void handleLoginInput(char key);
void handleMainInput(char key);
void sendRequest(String action, String bookId);
bool performIoTLogin();
bool performUserLogin(String email, String password);
void logoutUser();
void showLoginPrompt();
String extractErrorMessage(String response);
void resetActivityTimer();
void checkInactivity();

void setup() {
  Serial.begin(115200);
  
  lcd.init();
  lcd.backlight();
  
  // Connect WiFi
  lcd.setCursor(0, 0);
  lcd.print("Connecting WiFi");
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    lcd.print(".");
  }
  
  lcd.clear();
  lcd.print("WiFi Connected!");
  delay(1000);

  if (performIoTLogin()) {
    lcd.clear();
    lcd.print("IoT Auth OK");
    delay(1000);
    showLoginPrompt();
  } else {
    lcd.clear();
    lcd.print("IoT Auth Failed");
    lcd.setCursor(0,1);
    lcd.print("Check Serial");
    while(true);
  }
  
  resetActivityTimer();
}

void loop() {
  char customKey = customKeypad.getKey();
  if (customKey) {
    resetActivityTimer();
    if (isLoginMode) handleLoginInput(customKey);
    else handleMainInput(customKey);
  }
  checkInactivity();
}

void resetActivityTimer() {
  lastActivityTime = millis();
}

void checkInactivity() {
  if (userLoggedIn && !isLoginMode) {
    unsigned long currentTime = millis();
    if (currentTime - lastActivityTime >= INACTIVITY_TIMEOUT) {
      lcd.clear();
      lcd.print("Timeout!");
      lcd.setCursor(0, 1);
      lcd.print("Auto Logout...");
      delay(1500);
      logoutUser();
    }
  }
}

bool performIoTLogin() {
  if(WiFi.status() != WL_CONNECTED) return false;

  HTTPClient http;
  lcd.clear();
  lcd.print("IoT Logging in..");
  
  String loginUrl = baseUrl + "/api/Auth/login";
  http.begin(loginUrl);
  http.addHeader("Content-Type", "application/json");
  http.addHeader("ngrok-skip-browser-warning", "true");
  http.addHeader("User-Agent", "ESP32-BookshelfKiosk/1.0");

  StaticJsonDocument<200> doc;
  doc["email"] = iotUserEmail;
  doc["password"] = iotUserPassword;
  doc["rememberMe"] = true;
  
  String requestBody;
  serializeJson(doc, requestBody);

  Serial.println("Sending IoT login request...");
  int httpResponseCode = http.POST(requestBody);
  String response = http.getString();

  if (httpResponseCode == 200) {
    DynamicJsonDocument responseDoc(3072); 
    DeserializationError error = deserializeJson(responseDoc, response);

    if (!error) {
      const char* token = responseDoc["token"];
      iotJwtToken = String(token);

      Serial.println("--- IoT LOGIN SUCCESSFUL ---");
      Serial.println("IoT Token: " + iotJwtToken);
      http.end();
      return true;
    } else {
      Serial.print("JSON Parse Failed: ");
      Serial.println(error.c_str());
    }
  } else {
    Serial.print("IoT Login Failed. HTTP Code: ");
    Serial.println(httpResponseCode);
    Serial.println("Response: " + response);
    
    String errorMsg = extractErrorMessage(response);
    lcd.clear();
    lcd.print("Error:");
    lcd.setCursor(0, 1);
    lcd.print(errorMsg.substring(0, 10) + ".");
    delay(3000);
  }

  http.end();
  return false;
}

bool performUserLogin(String email, String password) {
  if(WiFi.status() != WL_CONNECTED) return false;

  HTTPClient http;
  lcd.clear();
  lcd.print("Logging in...");
  
  String loginUrl = baseUrl + "/api/Auth/login";
  http.begin(loginUrl);
  http.addHeader("Content-Type", "application/json");
  http.addHeader("ngrok-skip-browser-warning", "true");
  http.addHeader("User-Agent", "ESP32-BookshelfKiosk/1.0");

  StaticJsonDocument<200> doc;
  doc["email"] = email;
  doc["password"] = password;
  doc["rememberMe"] = false;
  
  String requestBody;
  serializeJson(doc, requestBody);

  Serial.println("Sending user login request...");
  int httpResponseCode = http.POST(requestBody);
  String response = http.getString();

  if (httpResponseCode == 200) {
    DynamicJsonDocument responseDoc(3072); 
    DeserializationError error = deserializeJson(responseDoc, response);

    if (!error) {
      const char* token = responseDoc["token"];
      const char* uid = responseDoc["userId"];
      const char* name = responseDoc["fullName"];
      
      currentUserToken = String(token);
      currentUserId = String(uid);
      currentUserName = String(name);
      userLoggedIn = true;

      Serial.println("--- USER LOGIN SUCCESSFUL ---");
      Serial.println("User Token: " + currentUserToken);
      Serial.println("UserID: " + currentUserId);
      Serial.println("Name: " + currentUserName);
      
      http.end();
      return true;
    }
  } else {
    String errorMsg = extractErrorMessage(response);
    Serial.print("User Login Failed. HTTP Code: ");
    Serial.println(httpResponseCode);
    Serial.println("Error: " + errorMsg);
    
    lcd.clear();
    lcd.print("Login Failed:");
    lcd.setCursor(0, 1);
    lcd.print(errorMsg.substring(0, 10) + ".");
    delay(3000);
  }

  http.end();
  return false;
}

void logoutUser() {
  currentUserToken = "";
  currentUserId = "";
  currentUserName = "";
  userLoggedIn = false;
  
  lcd.clear();
  lcd.print("Logged Out");
  delay(1500);
  showLoginPrompt();
  resetActivityTimer();
}

String extractErrorMessage(String response) {
  DynamicJsonDocument doc(1024);
  DeserializationError error = deserializeJson(doc, response);
  
  if (!error) {
    if (doc.containsKey("errors") && doc["errors"].is<JsonArray>()) {
      JsonArray errors = doc["errors"].as<JsonArray>();
      if (errors.size() > 0 && errors[0].containsKey("message")) {
        return errors[0]["message"].as<String>();
      }
    }
    // Check for simple message format
    if (doc.containsKey("message")) {
      return doc["message"].as<String>();
    }
  }
  return "Unknown Error";
}

void sendRequest(String action, String bookId) {
  if(WiFi.status() == WL_CONNECTED){
    HTTPClient http;
    lcd.clear();
    lcd.print("Sending...");
    
    String scanUrl = baseUrl + "/api/iot/scan";
    http.begin(scanUrl);
    
    http.addHeader("Content-Type", "application/json");
    http.addHeader("ngrok-skip-browser-warning", "true");
    http.addHeader("User-Agent", "ESP32-BookshelfKiosk/1.0");
    http.addHeader("Authorization", "Bearer " + iotJwtToken);
    
    String jsonPayload = "{";
    jsonPayload += "\"bookId\":\"" + bookId + "\",";
    jsonPayload += "\"userId\":\"" + currentUserId + "\",";
    jsonPayload += "\"action\":\"" + action + "\"";
    jsonPayload += "}";
    
    Serial.println("Sending: " + jsonPayload);
    int httpResponseCode = http.POST(jsonPayload);
    String response = http.getString();
    
    lcd.clear();
    
    if(httpResponseCode == 200){
      lcd.print("SUCCESS!");
      lcd.setCursor(0,1);
      if (action == "Lend") lcd.print("Book Borrowed");
      else lcd.print("Book Returned");
    }
    else if (httpResponseCode == 401) {
      lcd.print("Token Expired!");
      delay(1000);
      lcd.clear();
      lcd.print("Relogging IoT...");
      
      http.end();
      
      if (performIoTLogin()) {
        sendRequest(action, bookId);
        return; 
      } else {
        lcd.clear();
        lcd.print("Relogin Failed");
      }
    }
    else {
      String errorMsg = extractErrorMessage(response);
      lcd.print("Error:");
      lcd.setCursor(0,1);
      lcd.print(errorMsg.substring(0, 16));
      
      Serial.print("HTTP Error: ");
      Serial.println(httpResponseCode);
      Serial.println("Error Message: " + errorMsg);
      Serial.println("Full Response: " + response);
    }
    
    http.end();
    delay(3000);
    inputBuffer = "";
    showMainMenu();
  }
  else {
    showError("No WiFi!");
  }
}

void showLoginPrompt() {
  isLoginMode = true;
  inputBuffer = "";
  
  lcd.clear();
  lcd.print("User#(1-1) *:OK");
  lcd.setCursor(0, 1);
  lcd.print("#:Clear");
}

void showMainMenu() {
  isLoginMode = false;
  lcd.clear();
  lcd.setCursor(0, 0);
  if (userLoggedIn) {
    lcd.print(currentUserName.substring(0, 10) + ".");
    lcd.setCursor(0, 1);
    lcd.print("A:Bor B:Ret D:Lo");
  } else {
    lcd.print("Not Logged In");
    lcd.setCursor(0, 1);
    lcd.print("Press A:Login");
  }
}

void handleLoginInput(char key) {
  if (key >= '1' && key <= '1') {
    int userIndex = key - '1';
    if (userIndex < numUsers) {
      lcd.clear();
      lcd.print("Logging in as:");
      lcd.setCursor(0, 1);
      lcd.print(users[userIndex].fullName.substring(0, 10) + ".");
      delay(1500);
      
      if (performUserLogin(users[userIndex].email, users[userIndex].password)) {
        lcd.clear();
        lcd.print("Welcome!");
        lcd.setCursor(0, 1);
        lcd.print(currentUserName.substring(0, 10) + ".");
        delay(2000);
        showMainMenu();
        resetActivityTimer();
      } else {
        delay(2000);
        showLoginPrompt();
      }
    }
  } else if (key == '#') {
    showLoginPrompt();
  }
}

void handleMainInput(char key) {
  if (!userLoggedIn) {
    if (key == 'A') {
      showLoginPrompt();
    }
    return;
  }
  
  if (key == 'D') {
    logoutUser();
  }
  else if (key == 'A') {
    if (inputBuffer.length() > 0) {
      String bookId = inputBuffer;
      inputBuffer = "";
      sendRequest("Lend", bookId);
    } else {
      showError("Enter Book ID");
    }
  } 
  else if (key == 'B') {
    if (inputBuffer.length() > 0) {
      String bookId = inputBuffer;
      inputBuffer = "";
      sendRequest("Return", bookId);
    } else {
      showError("Enter Book ID");
    }
  }
  else if (key == '*') { 
    inputBuffer = "";
    showMainMenu();
  }
  else if (key == '#') {
    if (inputBuffer.length() > 0) {
      inputBuffer = inputBuffer.substring(0, inputBuffer.length() - 1);
    }
    showMainMenu();
    lcd.setCursor(0, 1);
    lcd.print("ID:" + inputBuffer + "    ");
  }
  else if (key >= '0' && key <= '9') {
    inputBuffer += key;
    showMainMenu();
    lcd.setCursor(0, 1);
    lcd.print("ID:" + inputBuffer + "    ");
  }
}

void showError(String msg) {
  lcd.clear();
  lcd.print("Error:");
  lcd.setCursor(0, 1);
  lcd.print(msg);
  delay(2000);
  showMainMenu();
}