package net.iowntheinter.cornerstone.util

import groovy.json.JsonOutput

/**
 * Created by g on 6/21/16.
 */
class displayOutput {
    String outputType;
    displayOutput(String type = "json"){
        outputType = type
    }

    void display(Map d){
        switch (outputType){
            case "json":
                displayJson(d);
                break;

        }

    }

    void displayJson(Map input) {
        println(JsonOutput.prettyPrint(JsonOutput.toJson(input)))
    }

}


