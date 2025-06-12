package tronka.test.linking;

import tronka.justsync.RandomInterface;

public class OtherImplementation implements RandomInterface {

    public OtherImplementation() {}

    @Override
    public void run() {
        System.out.println("----------------");
        System.out.println("-----MATRIX-----");
        System.out.println("----------------");
    }
}
