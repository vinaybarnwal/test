definition(
    name: "My First App",
    namespace: "vinaybarnwal",
    author: "VK",
    description: "Switch on/off based on motion sensor",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Turn on when motion detected:") {
        input "myMotionSensor", "capability.motionSensor", required: true, title: "Where?"
    }
    section("Turn off when there's been no movement for") {
        input "durationInMinutes", "number", required: true, title: "Minutes?"
    }
    section("Turn on/off this light") {
        input "mySwitch", "capability.switch", required: true
    }
}

def installed() {	//Every SmartApp must define this method
	log.debug "<VKB:MyFirstApp> Installed with settings: ${settings}"

	initialize()
}

def updated() {		//Every SmartApp must define this method
	log.debug "<VKB:MyFirstApp> Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	log.debug "<VKB:MyFirstApp> initialize() called..."
	subscribe(myMotionSensor, "motion", motionHandler)
}

// TODO: implement event handlers
def motionHandler(evt) {
    log.debug "<VKB:MyFirstApp> motionHandler() called: $evt"
    if (evt.value == "active") {
        // motion detected
        mySwitch.on()	// To turn the switch on requires only one line of code in event handler
    } else if (evt.value == "inactive") {
        // motion stopped
        runIn(60 * durationInMinutes, checkMotion)
    }
    
}

def checkMotion() {
    log.debug "<VKB:MyFirstApp>In checkMotion() scheduled method"
    // get the current state object for the motion sensor
    def motionState = myMotionSensor.currentState("motion")

    if (motionState.value == "inactive") {
            // get the time elapsed between now and when the motion reported inactive
        def elapsed = now() - motionState.date.time

        // elapsed time is in milliseconds, so the threshold must be converted to milliseconds too
        def threshold = 1000 * 60 * durationInMinutes

        if (elapsed >= threshold) {
            log.debug "<VKB:MyFirstApp> Motion has stayed inactive long enough since last check ($elapsed ms):  turning switch off"
            mySwitch.off()
        } else {
            log.debug "<VKB:MyFirstApp> Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
        // Motion active; just log it and do nothing
        log.debug "<VKB:MyFirstApp> Motion is active, do nothing and wait for inactive"
    }    
}
