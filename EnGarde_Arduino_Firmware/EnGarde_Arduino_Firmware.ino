int readWeapon = A0;  //Reads hit status of weapon from analog pin 'A0'
int led = 5;
uint32_t  counter = 0x00000000; //Holds the current count of the timer
bool justSent = false;
int sinceLastSent = 0;

union fourbyte {
    uint32_t dword;
    uint16_t word[2];
    uint8_t  byte[4]; 
  };

void setup() {
  Serial.begin(9600); //Communicates hit data at Baudrate = 9600 to Bluetooth device
  pinMode(led, OUTPUT);
}

void loop() {
  int wepRead = analogRead(readWeapon);
  float wepVoltage = wepRead * (5.0 / 1023.0);

    uint32_t curCount = counter;
  
  if(wepVoltage <= 1 && !justSent) {
    digitalWrite(led, HIGH);
    if(sendHit(counter, 'n')) {
      justSent = true;
    }
    else {
      do {
        for(int tryAgain; tryAgain <= 200; tryAgain++) {
          counter++;
          delay(1);
        }
      } while(!(sendHit(curCount, 'n')));
      justSent = true;
    }
  }
  else if(wepVoltage >= 4 && !justSent) {
    digitalWrite(led, HIGH);
    if(sendHit(counter, 'y')) {
      justSent = true;
    }
    else {
      do {
        for(int tryAgain; tryAgain <= 200; tryAgain++) {
          counter++;
          delay(1);
        }
      } while(!(sendHit(curCount, 'y')));
      justSent = true;
    }
  }
  else if(justSent) {
    sinceLastSent++;
    if(sinceLastSent >= 2000) {
      sinceLastSent = 0;
      justSent = false;
      digitalWrite(led, LOW);
    }
  }
  
  delay(1);
  counter++;
}

bool sendHit(uint32_t count, uint8_t hitStatus) {
  union fourbyte curCount;
  curCount.dword = count;
  int timeout = 0;
  uint8_t readval = 0x00;

  bool mRecieved = false;

  Serial.write(hitStatus);
  Serial.write(curCount.byte[0]);
  Serial.write(curCount.byte[1]);
  Serial.write(curCount.byte[2]);
  Serial.write(curCount.byte[3]);  
  Serial.write('\n');

  while(mRecieved == false && timeout < 1000) {
    readval = Serial.read();
    counter++;
    timeout++;
    if(readval == 'g') {
      mRecieved = true;
    }
    else if(readval == 'b') {
      while(Serial.available() > 0) {
        char junk = Serial.read();
      }
      return false;
    }
    delay(1);
  } 

  if(timeout <= 1000) {
    return true;
  } else {
    return false;
  }
  
}

