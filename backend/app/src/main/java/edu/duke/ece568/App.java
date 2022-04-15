/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package edu.duke.ece568;

import edu.duke.ece568.utils.WorldConnect;

public class App {
    public static void main(String[] args) {

        final String WORLD_HOST = "207.246.90.49";
        final int WORLD_PORT = 23456;

        WorldConnect worldConnect = new WorldConnect(WORLD_HOST, WORLD_PORT);
        worldConnect.setupConnection();
    }
}