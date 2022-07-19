package dev.meshfutures;

import dev.meshfutures.access.MeshFutures;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MeshFuturesMain {

    public static void main(String[] args){
        //Doing this will allow us to access the promise api or adherent future
        List<Integer> count1 = Collections.synchronizedList(new LinkedList<>());
        MeshFutures.createNew().fulfillInAsync(() -> {
            for (int i = 0; i <= 10; i++) {
                count1.add(i);
            }
            //Printing out the numbers once every entry has been added
            for (int numbers : count1) {
                System.out.println(numbers);
            }

            System.out.println("Success for the promise task");

            return true;
        }).fulfillExceptionally(/*Prints if an exception was thrown*/new Exception("Something went wrong with the promise task"));


        List<Integer> count2 = Collections.synchronizedList(new LinkedList<>());
        //Accessing the adherent future:
        MeshFutures.submit(() -> {
            for (int o = 0; o <= 10; o++) {
                count2.add(o);
            }
            return count2;
        }, (list -> {
            //If successful will read back the List
            for (int numbers : list) {
                System.out.println(numbers);
            }

            System.out.println("Successful for the adherent future");
        }), /*Will throw an exception if not successful*/ Throwable::printStackTrace);
    }
}
